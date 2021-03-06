#Getting the appropriate aws_availability zone
data "aws_availability_zones" "available" {}

#Creating a VPC resource with a vpc name
resource "aws_vpc" "main" {
  cidr_block           = "${var.vpcCidrBlock}"
  enable_dns_hostnames =  true
  tags = {
    Name = "${var.vpcName}"
  }
}

#Creating 3 subnets with appropraite subnet names and subnet-cidr-block
resource "aws_subnet" "main" {
  count = 3

  availability_zone = "${data.aws_availability_zones.available.names[count.index]}"
  cidr_block        = "${var.subnetCidrBlock[count.index]}"
  vpc_id            = "${aws_vpc.main.id}"
  map_public_ip_on_launch="true"
  tags = {
    Name ="${var.vpcName}.subnet.${count.index}"  
  }
}

#Creating an internet-gateway
resource "aws_internet_gateway" "main" {
  vpc_id = "${aws_vpc.main.id}"

  tags = {
    Name = "${var.vpcName}.gateway"
  }
}

#Creating a route-table resource
resource "aws_route_table" "main" {
  vpc_id = "${aws_vpc.main.id}"
  
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = "${aws_internet_gateway.main.id}"
  }

  tags = {
    Name = "${var.vpcName}.RouteTable"
  }
}

#Mapping the subnets to appropriate route table
resource "aws_route_table_association" "main" {
  count = 3

  subnet_id      = "${aws_subnet.main.*.id[count.index]}"
  route_table_id = "${aws_route_table.main.id}"
}

#subnet group
resource "aws_db_subnet_group" "main" {
  name       = "main-${var.vpcName}"
  subnet_ids = "${aws_subnet.main.*.id}"

  tags = {
    Name = "My DB subnet group"
  }
  depends_on = [aws_subnet.main]
}