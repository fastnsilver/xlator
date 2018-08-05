# xlator
[![Build Status](https://travis-ci.org/fastnsilver/xlator.svg)](https://travis-ci.org/fastnsilver/xlator)
[![codecov.io](https://codecov.io/github/fastnsilver/xlator/coverage.svg?branch=master)](https://codecov.io/github/fastnsilver/xlator?branch=master)
[![Apache License 2](https://img.shields.io/badge/license-ASF2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)

![codecov.io](https://codecov.io/github/fastnsilver/xlator/branch.svg?branch=master)


Translation service prototype that is backed by either an in-memory or a hosted Elasticache (Redis) instance

Current service implementation makes requests of [frengly](http://frengly.com) or Google [Translate API](https://cloud.google.com/translate/v2/using_rest#Translate).

## to use Frengly 
You will need to register for an account and add `app.frengly.email` and `app.frengly.password` to [application.yml](https://raw.githubusercontent.com/fastnsilver/xlator/master/src/main/resources/application.yml).

Note that frengly.com throttles requests, so intermittent HTTP 5xx responses are to be expected.

## to use Google Translate
You will need to sign-up for a Google [Cloud Platform](https://cloud.google.com/) account, then follow instructions to setup a project, billing, and enable Translate API calls.
Then you'll need to configure [application.yml](https://raw.githubusercontent.com/fastnsilver/xlator/master/src/main/resources/application.yml) by adding a `app.google.key` and changing `app.defaults.service` to be `google`.

Please review the [disclaimer](https://cloud.google.com/translate/v2/attribution#disclaimer) if you configure `xlator` to use Google Translate API.


The recommended way to update the application properties mentioned above is to create a `config` directory underneath `src/main/resources` and just add the key-value pairs mentioned above to it. This will serve as your application overrides.  Consult the [Application property files](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html#boot-features-external-config-application-property-files) section of the Spring Boot documentation for further details.

## Service Endpoints

`GET /translation/source/{src}/target/{target}/text/{text}`

* Inputs: source [locale](https://docs.oracle.com/javase/8/docs/api/java/util/Locale.html), target locale and text to translate
* Output: [translation](https://github.com/fastnsilver/xlator/blob/master/src/main/java/com/fns/xlator/model/Translation.java)

`GET /translation/target/{target}/text/{text}` 

* Inputs: target locale and text to translate (default locale must be configured in application.yml)
* Output: translation

`GET /translation/source/{src}/targets/{targets}/text/{text}`

* Inputs: source locale, comma-separated target locales, and text to translate
* Output: translations (one per target)

`GET /translation/targets/{targets}/text/{text}`

* Inputs: comma-separated target locales and text to translate
* Output: translations (one per target)

`POST /translation/`

* Inputs: A [TranslationRequest](https://github.com/fastnsilver/xlator/blob/master/src/main/java/com/fns/xlator/TranslationRequest.java) array, this form is preferred for multi-word text translations and/or when you want to perform translations on different combinations of source, target and text input parameters
* Output: translations

`DELETE /translation/source/{src}/target/{target}/text/{text}`

* Inputs: cache key comprised of source locale, target locale and text that was previously translated
* Output: entry is removed from cache provider


## Developer Notes

This is a [Spring Boot](http://projects.spring.io/spring-boot/) application.  

It is initialized with: [Application.java](https://github.com/fastnsilver/xlator/blob/master/src/main/java/com/fns/xlator/Application.java)


### Prerequisites

* Java [JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) 1.8.0_181 or better
* [Maven](https://maven.apache.org/download.cgi) 3.5.4 or better


### How to build

```
$ mvn clean install
```


### How to run

#### with Spring Boot


First, change directories

```
cd xlator
```

Then

```
$ mvn spring-boot:run
```

Or 



```
$ java -jar xlator-x.x.x.jar
```

where `x.x.x` is a version like `0.0.1-SNAPSHOT`


#### with Docker

Go get Docker for [Windows](https://www.docker.com/docker-windows), [Mac](https://www.docker.com/docker-mac), or [Ubuntu](https://www.docker.com/docker-ubuntu)


##### Build images

```
touch .dockerize
mvn clean install
```


##### Publish images

Consult the [Authentication](http://dmp.fabric8.io/#pull-vs-push-authentication) section of the [docker-maven-plugin](https://github.com/fabric8io/docker-maven-plugin) documentation for alternative options.

```
mvn docker:push -Ddocker.username={registry_username} -Ddocker.password={registry_password}
```

> Replace `{registry_username}` and `{registry_password }` above with credentials to a registry like [Docker hub](https://hub.docker.com).  If you intend to use an alternate registry you'll expend a bit more effort to configure.


##### Pull images

Visit [Dockerhub](https://hub.docker.com/u/fastnsilver/)

Pull the xlator image


##### Run images

You will have to set some environment variables in [xlator.env](https://github.com/fastnsilver/xlator/blob/master/deploy/docker/xlator.env) first!

```
cd deploy/docker
./startup.sh
```


##### Work with images

Services are accessible via the Docker host (or IP address) and port 

Service           |  Port
------------------|-------
Xlator            | 80
Prometheus        | 9090
Grafana           | 3000
CAdvisor          | 9080
Redis             | 6379

> Consider importing a couple of dashboards into Grafana, like [jvm](http://micrometer.io/docs/registry/prometheus#_grafana_dashboard) and [throughput](https://grafana.com/dashboards/5373)


##### Stop images (and remove them)

```
./shutdown.sh
docker-compose rm -f
```


### Working with Maven Site 

> Note: as of 2018-08-05, this Maven Site is only known to generate successfully employing JDK 8u181. 

#### Stage

```
mvn site site:stage -Pdocumentation
```

#### Publish

Assumes a `gh-pages` (orphan) branch has been set up in advance.  In addition, appropriate authentication credentials have been declared in `$HOME/.m2/settings.xml`. See:

* [Creating Project Pages manually](https://help.github.com/articles/creating-project-pages-manually/)
* [Security and Deployment Settings](http://maven.apache.org/guides/mini/guide-deployment-security-settings.html)

```
mvn scm-publish:publish-scm -Pdocumentation
```

#### Review

* [Maven Site](http://fastnsilver.github.io/xlator/)


## Roadmap

// TODO

1) Improve test coverage

2) Deploy service to AWS


## Credits

Major props go out to

* [Jon Schneider](https://www.youtube.com/watch?reload=9&v=LkWVFz9WGeU)
* [Byteville](http://www.bytesville.com/springboot-micrometer-prometheus-grafana/)