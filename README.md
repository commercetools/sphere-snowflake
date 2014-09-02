SPHERE.IO - Snowflake
=====================

This is a fully functional example web store for the [SPHERE.IO](http://sphere.io) PaaS.

## Live demo
Visit a live demo of SPHERE-DONUT store at [snowflake.sphere.io](http://snowflake.sphere.io/).

## Getting started [![Build Status](https://travis-ci.org/commercetools/sphere-snowflake.png)](https://travis-ci.org/commercetools/sphere-snowflake)

### Set it up
- Install [Play 2.2.1](http://www.playframework.com/documentation/2.2.x/Installing).
- [Clone](http://git-scm.com/book/en/Git-Basics-Getting-a-Git-Repository#Cloning-an-Existing-Repository) sphere-donut project from GitHub. or download it as [zip file](https://github.com/commercetools/sphere-snowflake/archive/master.zip).
- Run `play run` command in root project directory.
- Open your browser and point it to [http://localhost:9000](http://localhost:9000).

### Configure it

#### SPHERE.IO data
- Point to [SPHERE Login](https://admin.sphere.io/login) or register a new account with [SPHERE Signup](https://admin.sphere.io/signup).
- Go to `Developers -> API Clients` to retrieve your project data.
![API Backend](https://raw.github.com/commercetools/sphere-donut/master/public/images/mc_api.png)
- To use your SPHERE.IO project, modify `sphere.project`, `sphere.clientId` and `sphere.clientSecret` in `conf/application.conf`.

[More about the ecommerce PaaS SPHERE.IO.](http://sphere.io)

#### PAYMILL keys
- [Register at PAYMILL](https://app.paymill.com/en-gb/auth/register) to get the (test) API keys.
- Go to `PAYMILL Cockpit -> My account -> Settings -> API keys` to retrieve your keys.
- To use your PAYMILL account, modify `sphere.project`, `sphere.clientId` and `sphere.clientSecret` in `conf/application.conf`.

[More about doing payments with PAYMILL.](http://www.paymill.com)

## Deployment

### CloudBees

To run this SPHERE.IO example web shop on [CloudBees](http://cloudbees.com) just click the button:

<a href="https://grandcentral.cloudbees.com/?CB_clickstart=https://raw.github.com/commercetools/sphere-snowflake/master/deploy/cloudbees/clickstart.json"><img src="https://d3ko533tu1ozfq.cloudfront.net/clickstart/deployInstantly.png"/></a>

### heroku

To run this SPHERE.IO example web shop on [heroku](https://www.heroku.com) just click the button:

<a href="https://heroku.com/deploy?template=https://github.com/commercetools/sphere-snowflake"><img src="https://www.herokucdn.com/deploy/button.png" alt="Deploy"></a>

## Development

### Getting IDE settings from Play! framework

- Install your favourite IDE (preferably IntelliJ, Eclipse or Netbeans).
- Generate configuration files for your chosen IDE, following [these instructions](http://www.playframework.com/documentation/2.2.x/IDE).
- Run `play` command in root project directory.
- Inside Play Shell, type `clean test` for compiling and testing it.

### Use Typesafe Activator

- Install your favourite IDE (preferably IntelliJ, Eclipse or Netbeans).
- Download Typesafe Activator mini-package setup, following [this link](https://typesafe.com/platform/getstarted).
- Run Typesafe Activator in root project directory using `~/path/to/activator/folder/activator ui`
- Import project to your IDE using Activator GUI: `Code -> Settings -> Open Project in ...`