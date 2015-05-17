# --- !Ups
Create Table DOCUMENT(ID Int not null primary key auto_increment,NAME VARCHAR(30) not null,FILE_NAME varchar(60) not null,MIME_TYPE varchar(30) not null,DESCRIPTION VARCHAR(50),CREATION_DATE Date not null,MODIFICATION_DATE Date not null,UPDATING_USER VARCHAR(50));
Create table DOCUMENT_USER(ID Int not null primary key auto_increment,USER_ID Int not null,DOCUMENT_ID Int not null);

Alter table DOCUMENT_USER add constraint FK_DOC_USER_USER foreign key (USER_ID) references USER(ID);
Alter table DOCUMENT_USER add constraint FK_DOC_USER_DOC foreign key (DOCUMENT_ID) references DOCUMENT(ID);

# --- !Downs
ALTER TABLE DOCUMENT_USER drop constraint FK_DOC_USER_DOC;
ALTER TABLE DOCUMENT_USER drop constraint FK_DOC_USER_DOC;

Drop table DOCUMENT_USER;
Drop table DOCUMENTS;
