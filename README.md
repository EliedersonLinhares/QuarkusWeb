# Web Development with Quarkus
<img src="https://icongr.am/devicon/java-original.svg?size=128&color=currentColor" alt="java" width="40" height="40"/> 
<img src="https://icongr.am/devicon/postgresql-original.svg?size=128&color=currentColor" alt="java" width="40" height="40"/>
<img src="https://icon.icepanel.io/Technology/png-shadow-512/Quarkus.png" alt="java" width="40" height="40"/>

It's a basic project to demonstrate how to make a web application
using Quarkus and java

- Java 17
- Quarkus
- PostgreSql
- Swagger


## Features

<b>`Crud operations`</b> -> Basic Crud operations like ADD,UPDATE and REMOVE

<b>`Pagination`</b> -> Pagination system with sorting and filtering

## Running the application in development mode

Create one Database schema in PostGreSql with name defined in apllication.Properties

For run the application use the comand in terminal:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTA:_** To access the UI of quarkus : http://localhost:8080/q/dev/.
> 
>To access the Swager: http://localhost:8080/q/swagger/

