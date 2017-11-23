# Simple spring-boot service for managing users

## Concepts 
This is a basic spring-boot project demonstrating several concepts:

* Spring Web/REST controllers
* Spring Data/JPA repositories
* Managing database schema using Liquibase
* Transaction support
* Request validation support
* Dealing with response statuses using response entities and error handlers
* E2E integration testing using `TestRestTemplate` and in-memory H2 database with pre-populated data

## Build & Run

* Bulding
```bash
./gradlew clean build
```

* Running
```bash
./gradlew bootRun
```
Or simply run the main [UserApplication](https://github.com/skomarica/user/blob/master/src/main/java/io/github/skomarica/practice/user/UserApplication.java) class.