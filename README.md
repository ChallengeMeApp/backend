# Welcome

This is the backend for the Questophant app.

# Getting Started

## Which software is required to build the project?

To build the backend components you need the following software:

- Java 10+
- Maven 3.6+

## How do I clone the Git repositories?
	 
	$ git clone https://github.com/Questophant/backend.git
	 
## How to build the backend?

Open a command shell aka terminal and run:
    
    $ cd backend
    $ mvn clean install

## How to run a local development server?

To start the server run in directory `backend:
    
    $ mvn spring-boot:run

Then open the browser for URL [http://localhost:8080].

## How to setup a local database?

To run the local development server you need a [MySQL database](https://www.mysql.com/) installed. Either you install MySQL on your machine or you can use local build infrastructure with docker/docker-compose using the official MySQL docker image.
Install [Docker](https://docs.docker.com/get-docker/) for your operating system and then run:

    $ docker run --name mysqldb -e MYSQL_ROOT_PASSWORD=secret -e MYSQL_USER=test -e MYSQL_PASSWORD=password -e MYSQL_DATABASE=questophant -p 3306:3306 -d mysql

This command will start a MySQL server with a default database named `questophant` and a user `test` (password: `password`). The  user `root` has the password `secret`.

### Import Challenges

Optional: Download the Google Doc file to an Excel (.xlsx) file and replace the file in the `documents` folder.

To import the challenges from the file run the backend with:
    
    $ mvn spring-boot:run "-Dspring-boot.run.arguments=import='documents/Challenges - Zusammenfassung gemeinsames Projekt.xlsx'"

### MariaDB
The backend is built to be used with MySQL. If you want to use MariaDB, run the script /scripts/mariadb.sql after you changed to your database "USE questophant;".

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.2.6.RELEASE/maven-plugin/)

