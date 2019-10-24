# Role for EC2 Instance
resource "aws_iam_role" "EC2_Role" {
  name = "EC2_Role"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "ec2.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF

  tags = {
      Name = "EC2 Role"
  }
}

#Profile for the EC2 Role
resource "aws_iam_instance_profile" "ec2_profile" {
  name = "ec2_profile"
  role = "${aws_iam_role.EC2_Role.name}"
}


resource "aws_iam_role_policy" "ec2_role_policy" {
  name = "ec2_policy"
  role = "${aws_iam_role.EC2_Role.id}"

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": [
        "s3:*"
      ],
      "Effect": "Allow",
      "Resource": "*"
    }
  ]
}
EOF
}


#### SECURITY GROUP #####

#Application security group
resource "aws_security_group" "application" {
  name          = "application_security_group"
  vpc_id        = "${var.vpc_id}"
  ingress{
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks  = ["0.0.0.0/0"]
  }
  ingress{
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks  = ["0.0.0.0/0"]
  }
  ingress{
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks  = ["0.0.0.0/0"]
  }
  ingress{
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks  = ["0.0.0.0/0"]
  }
  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags          = {
    Name        = "Application Security Group"
    Environment = "${var.env}"
  }
}

# Database security group
resource "aws_security_group" "database"{
  name          = "database_security_group"
  vpc_id        = "${var.vpc_id}"
  tags          = {
    Name        = "Database Security Group"
    Environment = "${var.env}"
  }
}

# Database security group rule
resource "aws_security_group_rule" "database"{

  type        = "ingress"
  from_port   = 5432
  to_port     = 5432
  protocol    = "tcp"
  // cidr_blocks  = "${var.subnet_id_list}"
  
  source_security_group_id  = "${aws_security_group.application.id}"
  security_group_id         = "${aws_security_group.database.id}"
}

# S3 Bucket
resource "aws_s3_bucket" "bucket" {
  bucket = "webapp.${var.env}.${var.domainName}"
  acl = "private"
  force_destroy = "true"

  server_side_encryption_configuration {
    rule {
      apply_server_side_encryption_by_default {
        sse_algorithm     = "aws:kms"
      }
    }
  }

  lifecycle_rule {
    enabled = true

    transition {
      days = 30
      storage_class = "STANDARD_IA"
    }
  }
}


#RDS DB instance
resource "aws_db_instance" "myRDS" {
  allocated_storage    = 20
  storage_type         = "gp2"
  name                 = "${var.rdsDBName}"
  username             = "${var.rdsUsername}"
  password             = "${var.rdsPassword}"
  identifier = "${var.rdsInstanceIdentifier}"
  engine            = "postgres"
  engine_version    = "10.10"
  instance_class    = "db.t2.medium"
  # storage_encrypted = false
  port     = "5432"
  vpc_security_group_ids = [ "${aws_security_group.database.id}" ]
  final_snapshot_identifier = "${var.rdsInstanceIdentifier}-SNAPSHOT"
  skip_final_snapshot = true
  
  publicly_accessible = true
  multi_az = false

  tags = {
    Name        = "myRDS"
    Owner       = "${var.rdsOwner}"
  }

  # DB subnet group
  db_subnet_group_name = "${var.aws_db_subnet_group_name}"

}

# EC2 Instance
resource "aws_instance" "ec2_instance" {
  ami = "${var.ami}"
  instance_type = "t2.micro"
  security_groups = [ "${aws_security_group.application.id}" ]
  subnet_id = "${var.subnet_id}"
  disable_api_termination = false
  key_name = "${var.aws_ssh_key}"
  iam_instance_profile = "${aws_iam_instance_profile.ec2_profile.name}"
  user_data = "${templatefile("${path.module}/prepare_aws_instance.sh",
                                    {
                                      s3_bucket_name = "${aws_s3_bucket.bucket.id}",
                                      aws_db_endpoint = "${aws_db_instance.myRDS.endpoint}",
                                      aws_db_name = "${aws_db_instance.myRDS.name}",
                                      aws_db_username = "${aws_db_instance.myRDS.username}",
                                      aws_db_password = "${aws_db_instance.myRDS.password}",
                                      aws_region = "${var.region}",
                                      aws_profile = "${var.env}"
                                    })}"

  root_block_device {
    volume_type = "gp2"
    volume_size = "20"
    delete_on_termination = true
  }

  tags = {
    Name        = "myEC2Instance"
  }

  // TODO: depends_on, user_data
  depends_on = [aws_s3_bucket.bucket,aws_db_instance.myRDS]
}


#Dynamo db
resource "aws_dynamodb_table" "basic-dynamodb-table" {
  name           = "${var.dynamoName}"
  read_capacity  = 20
  write_capacity = 20
  hash_key       = "id"

  attribute {
    name = "id"
    type = "S"
  }

  tags = {
    Name        = "${var.dynamoName}"
  }
}
