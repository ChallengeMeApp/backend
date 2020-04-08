# Welcome

This is the backend for the ChallengeMe app.

# Getting Started

- Install Maven
- Run: mvn spring-boot:run

### Import Challenges

- Optional: Download the Google Doc file to an Excel (.xlsx) file and replace the file in the documents folder.
- Run: mvn spring-boot:run "-Dspring-boot.run.arguments=import='documents/Challenges - Zusammenfassung gemeinsames Projekt.xlsx'"


### MariaDB
The backend is built to be used with MySQL. If you want to use MariaDB, run the script /scripts/mariadb.sql after you changed to your database "USE ChallengeMe;".

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.2.6.RELEASE/maven-plugin/)

