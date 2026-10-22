# DVSA TestHelper - Active Support
This library consists of helper methods that are not specific to any one area and is to be used as a toolkit of support libraries in the [VOL Functional test suite.](https://github.com/dvsa/vol-functional-tests)

## Requirements
- Java 11
- Maven 3.6.3 or greater



## Installation
Add the following Maven dependency to your project's `pom.xml` file:
```xml
<dependency>
    <groupId>org.dvsa.testing.lib</groupId>
    <artifactId>active-support</artifactId>
    <version>LATEST</version>
</dependency>
```

This repository will create a release PR on merge to main and deploy a package to GH on merge of that.


## Running against nonprod through the jumpbox
If the [jumpbox-proxy](https://github.com/dvsa/aws-helper/tree/main/jumpbox-proxy) container is running on your machine, runs against `dev`, `qa`/`int` and `reg` go through it with no extra flags:

```
mvn clean verify -Denv=dev
```

At the start of a JUnit run, active-support checks for the proxy on `127.0.0.1:3128` and, when it finds it, sends Java, RestAssured and local browser traffic through it. Database steps connect to that environment's own database on the container's local port (dev `13306`, qa/int `23306`, reg `33306`), logging in as `olcsapi` with the password from the environment's `DEVAPP<ENV>-BASE-SM-APPLICATION-API` secret, or with `-Ddb.username` / `-Ddb.password` if you pass them. The log says `Using jumpbox-proxy on 127.0.0.1:3128`.

Nothing changes when the container is not running, for `-Denv=local`, when the run already has a proxy set (`-Dhttps.proxyHost` and friends, as in CI), or with `-Djumpbox.proxy=off`. Browsers on a Selenium grid keep their own proxy settings. Code that is not run by JUnit (e.g. a Gatling simulation) can call `JumpboxProxy.enableIfRunning()` itself.
