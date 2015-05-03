# Common Users and Authorizations Management

This application provide an externalization of the users and authorization management :

1. User creation and update
2. Permission creation and update
3. Group creation and update
4. Assign users to groups

### External Database Configuration

By default, the template use a [H2](http://www.h2database.com) embedded database, usefull for dev or demo.
A new [H2](http://www.h2database.com) database is created if the "h2" folder is empty.

To recreate the database , stop the application , delete all the files in the "h2" folder and restart the applictation. The evolution scripts will reconstruct the database structure.

If you want to use another database , you have to edit the **application.conf** file to provide the correct connexion informations.
You also have to change the dependencies in the **Build.scala** file in the project subfolder.

#### [MySql](http://www.mysql.com) example

* Create DB :
```
   $ mysql -u root -p
   $ mysql> CREATE DATABASE $DB_NAME;
```
* Create User :
```
$ mysql> CREATE USER '$USER_NAME'@'localhost' IDENTIFIED BY '$USER_PASSWORD';
```
* Grant Privileges :
```
  $ mysql> GRANT ALL PRIVILEGES ON DB_NAME.* TO '$USER_NAME'@'localhost';
  $ mysql> FLUSH PRIVILEGES;
```
* Edit **application.conf** file :
```
  db.default.driver=com.mysql.jdbc.Driver
  db.default.url="jdbc:mysql://localhost/$DB_NAME"
  db.default.user="$USER_NAME"
  db.default.password="$USER_PASSWORD"
```
* Edit **Build.scala** file :  
  Replace `"com.h2database" % "h2" % "1.4.187"` by `mysql" % "mysql-connector-java" % "5.1.30`
