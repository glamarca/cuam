# Common Users and Authorizations Management

This application uses [Scala](http://www.scala-lang.org/), [Play Framework](https://www.playframework.com/), [Slick](http://slick.typesafe.com/) and [Bootstrap](http://getbootstrap.com/)

## [Play Framework](https://www.playframework.com/) Documentation
Once the application is deployed, you can always access [Play Framework](https://www.playframework.com/) Documentation at [http://localhost:9000/@documentation](http://localhost:9000/@documentation) .

Online documentation : https://www.playframework.com/documentation

## Goal

The goal of this application is to centralize the management of the users and the authorisations of multiple applications.
It will provide common operations :

1. Users creation and update
2. Permissions creation and update
3. Groups creation and update
4. Applications tags creation and update
5. Assign users to groups
6. Assign permissions to groups
7. Assigns groups and permissions to applications
8. Connect from applications to DB or Api and retrieve users informations and authorisations

## Current Status : Developement

## Installing

### Download and Install [Activator](https://www.typesafe.com/community/core-tools/activator-and-sbt)

[Activator](https://www.typesafe.com/community/core-tools/activator-and-sbt) is a tool created by [Typesafe](https://www.typesafe.com) to create and develop [Scala](http://www.scala-lang.org/) applications. You can download [Activator](https://www.typesafe.com/community/core-tools/activator-and-sbt) from the following address: https://www.typesafe.com/community/core-tools/activator-and-sbt.

[Activator](https://www.typesafe.com/community/core-tools/activator-and-sbt) use [SBT](http://www.scala-sbt.org/) as build tool and dependencies manager. All [SBT](http://www.scala-sbt.org/) dependencies configuration links can be found on the [MVN Repository](http://mvnrepository.com/) , in the [SBT](http://www.scala-sbt.org/)tab

To install activator, simply unzip the downloaded file and add the absolute path to the new directory in the environment variable `$PATH`. You will be able to run the command `activator` from anywhere in your file system.

### Clone the repository

Clone the repository locally using [Git](http://git-scm.com/downloads):

```
    $ mkdir projects
    $ cd projects
    $ git clone https://github.com/glamarca/cuam
```

It will create the directory `cuam` where all cloned files will be located.

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

### Run the Application

To run the application:
```
  $ cd cuam
  $ activator

  [cuam] $ run
```

Once the server is started, you can open the application in your browser accessing the address: [http://localhost:9000/](http://localhost:9000/)

## Contributing

To contribute to the project you have to follow the exactly same steps of the previous section, but cloning from your own fork:

1. Login on [GitHub](https://github.com)
2. Visit the repository at: https://github.com/glamarca/cuam
3. Click on **Fork** to have your own copy of the project

The new clone url is the one from your own fork:

     $ git clone https://github.com/[your-username]/cuam.git

A few additional commands are necessary to keep your repository in sync with the original. Use `git remote` to link your local repository with the original:

      $ git remote add upstream https://github.com/glamarca/cuam.git

Everytime you want to update your fork with the changes made in the original, run the following commands:

      $ git fetch origin upstream
      $ git merge upstream/master

The merge was done locally and are not yet on your Github fork. For that, you have to push local modifications to the server:

      $ git push origin master

Contributions to the original repository are done through pull requests. So, make sure you have updated your fork and solved all conflicts before sending your pull requests.
