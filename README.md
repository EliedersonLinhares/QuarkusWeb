# Web Development with Quarkus
<img src="https://icongr.am/devicon/java-original.svg?size=128&color=currentColor" alt="java" width="40" height="40"/> 
<img src="https://icongr.am/devicon/postgresql-original.svg?size=128&color=currentColor" alt="java" width="40" height="40"/>
<img src="https://icon.icepanel.io/Technology/png-shadow-512/Quarkus.png" alt="java" width="40" height="40"/>

Advanced project to demonstrate how to make a web application
using Quarkus and java

### Technologies

- `Java 17` -> Program Language
- `Quarkus` -> Ecosystem
- `PostgreeSql` -> Database
- `Panache` -> Hibernate SQL library
- `Swagger` -> Customized docummentation
- `SmallRye JWT` -> JWT token generation 
- `MapStruct` ->  Mapping entities


### Features

<b>`Crud operations`⌨</b> -> Basic Crud operations like ADD,UPDATE and REMOVE

<b>`Pagination`📚</b> -> Pagination system with sorting and filtering

<b>`Security`🍪 </b> -> Security by Jwt save on cookies 

<b>`Refresh Token`💦</b> -> Token refresh implemented

<b>`Authorization`🔐</b> -> All endpoints secure by roles

<b>`Tests`🧪</b> -> Include tests for controller and service layers with Junit 5
and RestAssured


### Running the application in development mode

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
I create a file "StartupData" with contains a method to insert a
admin level user, if you don't have any data in user table. The data of
this user can be changed in application.properties


> **_NOTE:_** To access the UI of quarkus : http://localhost:8080/q/dev/.
> 
>To access the Swager: http://localhost:8080/q/swagger-ui/

### Future Development

1. [ ] Email verification for register
2. [ ] Email user for password change