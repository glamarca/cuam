# --- !Ups

create table USER(ID INT PRIMARY KEY AUTO_INCREMENT ,USER_NAME varchar(30) not null unique ,PASSWORD CHAR(60) not null,LASTNAME varchar(30),FIRSTNAME varchar(30),EMAIL varchar(30) not null unique ,CREATION_DATE DATE,MODIFICATION_DATE DATE,UPDATING_USER varchar(30) not null);
create table GROUPS(ID INT PRIMARY KEY AUTO_INCREMENT,NAME varchar(30) not null,REF_NAME varchar(30) not null,CREATION_DATE DATE,MODIFICATION_DATE DATE,DESCRIPTION varchar(50),UPDATING_USER varchar(30) not null);
create table USER_GROUP(ID INT PRIMARY KEY AUTO_INCREMENT,USER_ID INT not null,GROUP_ID INT not null ,ADDED DATE);
create table PERMISSION(ID INT PRIMARY KEY AUTO_INCREMENT,NAME varchar(30) not null,REF_NAME varchar(30) not null,CREATION_DATE DATE,DESCRIPTION varchar(50),MODIFICATION_DATE DATE,UPDATING_USER varchar(30) not null);
create table PERMISSION_GROUP(ID INT PRIMARY KEY AUTO_INCREMENT,PERMISSION_ID INT not null,GROUP_ID INT not null,ADDED DATE);

create index UNAME_INDEX on USER(USER_NAME);
create index GROUPREF_INDEX on GROUPS(REF_NAME);
create index PERMISSIONREF_INDEX on PERMISSION(REF_NAME);

alter table USER_GROUP add constraint FK_GROUP_MEMBER_USER FOREIGN KEY (USER_ID) references USER(ID);
alter table USER_GROUP add constraint FK_GROUP_MEMBER_GROUP FOREIGN KEY (GROUP_ID) references GROUPS(ID);
alter table PERMISSION_GROUP add constraint FK_PERMISSION_GROUP_GROUP FOREIGN KEY (GROUP_ID) references GROUPS(ID);
alter table PERMISSION_GROUP add constraint FK_PERMISSION_GROUP_PERMISSION FOREIGN KEY (PERMISSION_ID) references PERMISSION(ID);



# --- !Downs

alter table USER_GROUP drop constraint FK_GROUP_MEMBER_USER;
alter table USER_GROUP drop constraint FK_GROUP_MEMBER_GROUP;
alter table PERMISSION_GROUP drop constraint FK_PERMISSION_GROUP_GROUP;
alter table PERMISSION_GROUP drop constraint FK_PERMISSION_GROUP_PERMISSION;

drop table PERMISSION_GROUP;
drop table USER_GROUP;
drop table USER;
drop table GROUPS;
drop table PERMISSION;