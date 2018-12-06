# royal #

## Install MySQL ##
```sh
$ sudo apt-get install mysql-server
```

## Create database and user
```mysql
mysql> CREATE DATABASE royal CHARACTER SET utf8 COLLATE utf8_general_ci;
mysql> CREATE USER 'royal'@'localhost' IDENTIFIED BY 'mysql';
mysql> GRANT ALL ON royal.* TO 'royal'@'localhost';
```
Database config `/src/main/resources/c3p0.properties`

## Install OpenJDK ##
```sh
$ sudo apt-get install openjdk-8-jdk
$ sudo apt-get install openjdk-8-jre
```
## Build & Run ##
Download SBT [here](https://www.scala-sbt.org/download.html)
```sh
$ cd royal_backend
$ sbt
> jetty:start
> browse
```

If `browse` doesn't launch your browser, manually open [http://localhost:8080/](http://localhost:8080/) in your browser.
