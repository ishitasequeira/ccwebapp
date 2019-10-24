# CSYE 6225 - Fall 2019

## Team Information

| Name | NEU ID | Email Address |
| --- | --- | --- |
| Ishita Sequeira| 001403357 | sequeira.i@husky.neu.edu |
| Tejas Shah | 001449694 | shah.te@husky.neu.edu |
| Sumit Anglekar | 001475969 | anglekar.s@husky.neu.edu |

## Technology Stack
1. SpringBoot
2. Spring Security
3. Hibernate
4. Postgres Database
5. Version Control : Git

## Build Instructions
1. Clone the git repository: git@github.com:<username>/ccwebapp.git
   Usernames that can be used are SumitAnglekar94, shah-tejas, ishitasequeira
2. Traverse to the folder ./ccwebapp/webapp/recipe.
3. Run the command to build the module: mvn clean install.

## Deploy Instructions
1. Build a custom AMI instance using packer by building the template from https://github.com/csye6225-cloud-computing/fa19-team-002-ami.
    (For detailed steps on this follow the steps in the README at https://github.com/csye6225-cloud-computing/fa19-team-002-ami)
2. Navigate to `<REPO_DIR>/ccwebapp/infrastructure/aws/terraform` directory.
3. Build the EC2 instance using the terraform command: `terraform apply`.
4. Build the WAR file for the recipe application by running the command `mvn clean install` from  `<REPO_DIR>/ccwebapp/webapp/recipe` directory.
5. The WAR file from the above step would be created in `<REPO_DIR>/ccwebapp/webapp/recipe/target` directory.
6. SCP the WAR file to the EC2 instance created above. Example:
    `scp <path-to-source-file> centos@<ec2_instance_ip>:<destination_path>`
7. In the EC2 instance copy the WAR file to tomcat's webapp directory.
8. Restart tomcat to deploy the application.

## Application Endpoints
1. Register a User (localhost:8080/v1/user)
2. Get User records (localhost:8080/v1/user/self)
3. Update User recordds (localhost:8080/v1/user/self)
4. Register a Recipe (localhost:8080/v1/recipe/)
5. Get recipe Information (localhost:8080/v1/recipe/{id})
6. Delete a particular recipe (localhost:8080/v1/recipe/{id})

## Running Tests
1. Run the "run all tests" configuration for JUnit.

## CLI Installation and Configuration
1. Kindly follow the steps in the given link for CLI setup and installation:
https://docs.aws.amazon.com/cli/latest/userguide/install-linux-al2017.html

2. Kindly follow the steps in the given link for CLI configuration:
https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-configure.html

## CI/CD
