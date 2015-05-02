# --- !Ups

create table USER(ID INT PRIMARY KEY AUTO_INCREMENT ,USER_NAME varchar(30) not null,PASSWORD varchar(30) not null,LASTNAME varchar(30),FIRSTNAME varchar(30),EMAIL varchar(30) not null,CREATION_DATE DATE,UPDATE_DATE DATE,UPDATING_USER varchar(30) not null);
create table GROUP(ID INT PRIMARY KEY AUTO_INCREMENT,NAME varchar(30) not null,REF_NAME varchar(30) not null,CREATION_DATE DATE,UPDATE_DATE DATE,DESCRIPTION varchar(50),UPDATING_USER varchar(30) not null);
create table USER_GROUP(ID INT PRIMARY KEY AUTO_INCREMENT,USER_ID INT not null,GROUP_ID INT not null ,ADDED DATE);
create table ROLE(ID INT PRIMARY KEY AUTO_INCREMENT,NAME varchar(30) not null,REF_NAME varchar(30) not null,CREATION_DATE DATE,DESCRIPTION varchar(50),MODIFICATION_DATE DATE,UPDATING_USER varchar(30) not null);
create table ROLE_GROUP(ID INT PRIMARY KEY AUTO_INCREMENT,ROLE_ID INT not null,GROUP_ID INT not null,ADDED DATE);

alter table USER_GROUP add constraint FK_GROUP_MEMBER_USER FOREIGN KEY (USER_ID) references USER(ID);
alter table USER_GROUP add constraint FK_GROUP_MEMBER_GROUP FOREIGN KEY (GROUP_ID) references GROUP(ID);
alter table ROLE_GROUP add constraint FK_ROLE_GROUP_GROUP FOREIGN KEY (GROUP_ID) references GROUP(ID);
alter table ROLE_GROUP add constraint FK_ROLE_GROUP_ROLE FOREIGN KEY (ROLE_ID) references ROLE(ID);


# --- !Downs

alter table USER_GROUP drop constraint FK_GROUP_MEMBER_USER;
alter table USER_GROUP drop constraint FK_GROUP_MEMBER_GROUP;
alter table ROLE_GROUP drop constraint FK_ROLE_GROUP_GROUP;
alter table ROLE_GROUP drop constraint FK_ROLE_GROUP_ROLE;

drop table ROLE_GROUP;
drop table USER_GROUP;
drop table USER;
drop table GROUP;
drop table ROLE;