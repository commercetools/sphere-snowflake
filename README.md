SPHERE.IO - Snowflake
=====================

[![Build Status](https://travis-ci.org/commercetools/sphere-snowflake.png)](https://travis-ci.org/commercetools/sphere-snowflake)

:no_entry_sign: _**This shop template is deprecated!**_

_This shop template is no longer maintained nor up to date, built on the deprecated  [SPHERE Play SDK](https://github.com/commercetools/sphere-play-sdk). Please use [Sunrise](https://github.com/sphereio/sphere-sunrise) instead, the shop template for the new [SPHERE JVM SDK](https://github.com/sphereio/sphere-jvm-sdk).


This is a fully functional example web store for the [SPHERE.IO](http://sphere.io) PaaS.

## Live demo
Visit a live demo of SPHERE.io snowflake store at [snowflake.sphere.io](http://snowflake.sphere.io/).

## Getting started

### Set it up
- Install at least JDK 6 on your machine. We recommend using [JDK 7](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html).
- [Clone](http://git-scm.com/book/en/Git-Basics-Getting-a-Git-Repository#Cloning-an-Existing-Repository) sphere-snowflake project from GitHub. or download it as [zip file](https://github.com/commercetools/sphere-snowflake/archive/master.zip).
- Run `./sbt run` or `sbt.bat run` (Windows) command in root project directory.
- Open your browser and point it to [http://localhost:9000](http://localhost:9000).

### Configure it

#### SPHERE.IO data - mandatory
- Point to [SPHERE Login](https://admin.sphere.io/login) or register a new account with [SPHERE Signup](https://admin.sphere.io/signup).
- Create a new project, preferably with sample data.
- Go to `Developers -> API Clients` to retrieve your project data.
![API Backend](https://raw.github.com/commercetools/sphere-snowflake/master/public/images/mc_api.png)
- To use your SPHERE.IO project, modify [sphere.project](https://github.com/commercetools/sphere-snowflake/blob/master/conf/application.conf#L24), [sphere.clientId](https://github.com/commercetools/sphere-snowflake/blob/master/conf/application.conf#L26) and [sphere.clientSecret](https://github.com/commercetools/sphere-snowflake/blob/master/conf/application.conf#L28) in [conf/application.conf](https://github.com/commercetools/sphere-snowflake/blob/master/conf/application.conf).

[More about the ecommerce PaaS SPHERE.IO.](http://dev.sphere.io)

#### PAYMILL keys - optional
- [Login or register at PAYMILL](https://app.paymill.com/user/login) to get the (test) API keys.
- Go to [your PAYMILL dashboard](https://app.paymill.com/dashboard), then `Development -> API keys` to retrieve your keys.
- To use your PAYMILL account, modify [sphere.key.private](https://github.com/commercetools/sphere-snowflake/blob/master/conf/application.conf#L52) and [sphere.clientSecret](https://github.com/commercetools/sphere-snowflake/blob/master/conf/application.conf#L24) in [conf/application.conf](https://github.com/commercetools/sphere-snowflake/blob/master/conf/application.conf).

[More about doing payments with PAYMILL.](http://www.paymill.com)

## Deployment

### heroku

To run this SPHERE.IO example web shop on [heroku](https://www.heroku.com) just click the button:

<a href="https://heroku.com/deploy?template=https://github.com/commercetools/sphere-snowflake"><img src="https://www.herokucdn.com/deploy/button.png" alt="Deploy"></a>

## Development

### Getting IDE settings from Play! framework

- Install your favourite IDE (preferably IntelliJ, Eclipse or Netbeans).
- Generate configuration files for your chosen IDE, following [these instructions](http://www.playframework.com/documentation/2.2.x/IDE).
- Run `./sbt` command in root project directory.
- Inside the SBT shell, type `clean test` for compiling and testing it.
- Start SBT with `./sbt -jvm-debug 5005` to enable debugging with port 5005

### Use Typesafe Activator

- Typesafe Activator allows you to run and compile your code using a nice UI
- Install your favourite IDE (preferably IntelliJ, Eclipse or Netbeans).
- Download Typesafe Activator mini-package setup, following [this link](https://typesafe.com/platform/getstarted).
- Run Typesafe Activator in root project directory using `$ ~/path/to/activator/folder/activator ui`
- Import project to your IDE using Activator GUI: `Code -> Settings -> Open Project in ...`
- Currently supports [IntelliJ IDEA](http://www.jetbrains.com/idea/) and [Eclipse](https://www.eclipse.org/)

### Special info: Eclipse

- [Eclipse](https://www.eclipse.org/) has no native support of Scala/Play applications
- Please use this prepackaged version of Eclipse: [Scala-IDE](http://scala-ide.org)
