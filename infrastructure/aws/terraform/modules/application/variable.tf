# AWS S3 Bucket Variables

variable "env" {
  type        = "string"
}

variable "region" {
  type         = "string"
}


# #Domain name to be used for S3 Bucket name
variable "domainName" {
    type    = "string"
}

# #RDS owner
variable "rdsOwner"{
  type      = "string"
  default = "csye6225"
}

# #RDS Instance Identifier
variable "rdsInstanceIdentifier"{
  type      = "string"
  default = "csye6225-fall2019"
}

# #RDS username
variable "rdsUsername"{
  type      = "string"
  default = "dbuser"
}

# #RDS Password
variable "rdsPassword"{
  type      = "string"  
}

# #RDS DB Variable
variable "rdsDBName"{
  type      = "string"
  default = "csye6225"
}

# #Dynamo Table Variable
variable "dynamoName"{
  type      = "string"
  default = "csye6225"
}

#VPC ID
variable "vpc_id" {
  type = "string"
}

#subnet cidr block
variable "subnetCidrBlock" {
  type        = "list"
}

#aws_db_subnet_group_name
variable "aws_db_subnet_group_name" {
  type        = "string"
}

# subnet id
variable "subnet_id" {
  type = "string"
  description = "The Subnet to use for the instance"
}

variable "subnet_id_list" {
  type = "list"
}

# SSH key
variable "aws_ssh_key" {
  type = "string"
  description = "The ssh key pair name configured in AWS"
}

variable "timeToLive" {
  type = number
  description = "TimeToLive for DynamoDB"
}