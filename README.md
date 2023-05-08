# Project Overview
This simple spring boot project performs CRUD operations of images to Imgur app. It includes the following features:

## Features
* User registration and sign-in with JWT
* Role-based authorization leveraging spring security
* Logout feature
* Image CRUD management connecting to the image hosting service, Imgur
* Inputs user and image metadata to Kafka topics
* Leverages in-memory H2 data store

## Tools & Technologies
* Spring Boot 3.0
* Spring Security
* JSON Web Tokens (JWT)
* BCrypt
* Maven
* Apache Kafka
* H2 Database
* Imgur image hosting service
 
## Getting Started
Environment required to run this project:

* Preferably Linux based OS
* JDK 17+
* Maven 3+
* Apache Kakfa 3.4.0

Follow below steps to compile & run the application:

* Firstly clone the git repository: `git clone https://github.com/swathichittajallu/synchrony-image-handler-service.git`
* Navigate to the project directory: cd synchrony-image-handler-service
* Add the required environment variables
* Build the project: mvn clean install
* Run the project: mvn spring-boot:run
* Start by registering a user to the /api/v1/auth/register API with the admin bearer JWT token
* Sign-in with this user to the /api/v1/auth/sign-in controller API and perform the image CRUD operations
* For ease of use, both the Kafka Producer and Consumer capabilities are provided in the same application, in production decouple these services

## TO-DO
* Implement metrics & traces on the API controllers
* Prepare 100% test coverage, leverage Cucumber BDD & perform tests
* Integrate the application with Gitlab CI
* Dockerize the application
* Prepare helm charts for the application deployment on Kubernetes
* Leverage Kubernetes operators for Apache Kafka management