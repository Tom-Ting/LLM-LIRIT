# MAPIT 后端

## 简介

该项目是MAPIT项目的后端部分，其中分为三个子项目，分别是AlgorithmServer，Device和LIT，分别对应于算法服务端，设备管理端和业务处理端。其中算法端使用Python语言编写，具体配置方式参考"LIT-Backend/AlgorithmServer/README.md"。

以下介绍业务处理端和设备管理端的配置方式。

## 安装

### Java

业务处理端和设备管理端均使用Java语言编写，jdk版本为8，使用maven管理依赖。

### 数据库

使用Mysql8作为数据库，需要创建名为'lit'的数据库，并导入'LIT-Backend/LIT-Database.sql'文件。

导入LIT-Database.sql文件的方式
`source $PATH$\\lit-database.sql`

注意，此处地址分隔符不能使用单斜线'\'，必须使用双斜线'\\'

否则，会报错
`mysql: Character set 'odeMapLIRATLIT-Backendlit-database.sql;' is not a compiled character set and is not specified in the 'C:\Program Files\MySQL\...\Index.xml`

修改'LIT-Backend/LIT/src/main/resources/application.yaml'文件中的'spring.datasource.username'和'spring.datasource.password'为本地数据库的用户名和密码。

### IOS端

需要在MacOS系统上安装Xcode，以及在iPhone上安装WebDriverAgent和ios-minicao。

## 启动顺序

启动时，先启动业务处理端，再启动设备管理端。

## 实验

实验代码位于"LIT-Backend/LIT/src/test/java/cn/iselab/mooctest/lit/experimentMultipleTest.java",仅作参考。