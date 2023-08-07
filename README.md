# Web Development with Quarkus
<img src="https://icongr.am/devicon/java-original.svg?size=128&color=currentColor" alt="java" width="40" height="40"/> 
<img src="https://icongr.am/devicon/postgresql-original.svg?size=128&color=currentColor" alt="java" width="40" height="40"/>
<img src="https://icon.icepanel.io/Technology/png-shadow-512/Quarkus.png" alt="java" width="40" height="40"/>

It's a basic project to demonstrate how to make a web application
using Quarkus and java

- Java 17
- Quarkus
- PostgreeSql
- Panache 
- Swagger
- SmallRye JWT


## Features

<b>`Crud operations`</b> -> Basic Crud operations like ADD,UPDATE and REMOVE

<b>`Pagination`</b> -> Pagination system with sorting and filtering

<b>`Security`</b> -> Security by Jwt save on cookies 

<b>`Authorization`</b> -> All endpoints secure by roles


## Running the application in development mode

Create one Database schema in PostGreSql with name defined in application.Properties

For run the application use the command in terminal:
```shell script
./mvnw compile quarkus:dev
```
To use the application you need generate public and private key with
openSSl with commands in terminal:
```shell script
openssl genrsa -out rsaPrivateKey.pem 2048
openssl rsa -pubout -in rsaPrivateKey.pem -out publicKey.pem
```
and
```shell script
openssl pkcs8 -topk8 -nocrypt -inform pem -in rsaPrivateKey.pem -outform pem -out privateKey.pem
```
**_IMPORTANT:_** Now You can move the keys to places defined int application properties.
I put the public on the /resources/META_INF/resources, and the
private(Take care don't let this key public) in /resources folder
.The rsaPrivateKey.pem you can delete.
```
mp.jwt.verify.publickey.location=META-INF/resources/publicKey.pem
smallrye.jwt.sign.key.location=privateKey.pem
```


> **_NOTE:_** To access the UI of quarkus : http://localhost:8080/q/dev/.
> 
>To access the Swager: http://localhost:8080/q/swagger/

