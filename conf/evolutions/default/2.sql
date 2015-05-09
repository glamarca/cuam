#---Up
create table APPLICATION(ID Int not null primary key auto_increment,NAME VARCHAR(30) not null,REF_NAME VARCHAR(30) not null,DESCRIPTION VARCHAR(50),DATE_CREATION Date not null,DATE_MODIFICATION Date not null,UPDATING_USER VARCHAR(50));

create INDEX APP_REF_NAME_IDEX on APPLICATION(REF_NAME);

alter table PERMISSION add APPLICATION_ID Int not null;
alter table GROUPS add APPLICATION_ID Int not null;
alter table GROUPS add constraint FK_GROUPS_APPLICATION FOREIGN KEY (APPLICATION_ID) references APPLICATION(ID);
alter table PERMISSION add constraint FK_PERMISSION_APPLICATION FOREIGN KEY (APPLICATION_ID) references APPLICATION(ID);

#--Down
alter table PERMISSION drop constraint FK_GROUPS_APPLICATION;
alter table GROUPS drop constraint FK_GROUPS_APPLICATION;
alter table GROUPS drop column APPLICATION_ID;
alter table PERMISSION drop column APPLICATION_ID;

drop index APP_REF_NAME_IDEX;

drop table APPLICATION;