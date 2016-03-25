# xlator

Translation service prototype that is backed by either an in-memory or a hosted Elasticache (Redis) instance

Current service implementation makes requests of [frengly](http://frengly.com).

You will need to register for an account and add `app.frengly.email` and `app.frengly.password` to [application.yml](https://raw.githubusercontent.com/fastnsilver/xlator/master/src/main/resources/application.yml).

Note that frengly.com throttles requests, so intermittent HTTP 5xx responses are to be expected.

## Service Endpoints

`GET /translation/source/{src}/target/{target}/text/{text}`

* Inputs: source [locale](https://docs.oracle.com/javase/8/docs/api/java/util/Locale.html), target locale and text to translate
* Output: translation

`GET /translation/target/{target}/text/{text}` 

* Inputs: target locale and text to translate (default locale must be configured in application.yml)
* Output: translation

`GET /translation/source/{src}/targets/{targets}/text/{text}`

* Inputs: source locale, comma-separated target locales, and text to translate
* Output: translations (one per target)

`GET /translation/targets/{targets}/text/{text}`

* Inputs: comma-separated target locales and text to translate
* Output: translations (one per target)

`POST /translations/`

* Inputs: A [TranslationRequest](https://github.com/fastnsilver/xlator/blob/master/src/main/java/com/fns/xlator/TranslationRequest.java) array, this form is preferred for multi-word text translations and/or when you want to perform translations on different combinations of source, target and text input parameters
* Output: translations

`DELETE /translations/source/{src}/target/{target}/text/{text}`

* Inputs: cache key comprised of source locale, target locale and text that was previously translated
* Output: entry is removed from cache provider

## Developer Notes

This is a [Spring Boot](http://projects.spring.io/spring-boot/) application.  

It is initialized with: [App.java](https://github.com/fastnsilver/xlator/blob/master/src/main/java/com/fns/xlator/App.java)


### Prerequisites

* Java [JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) 1.8.0_45 or better
* [Maven](https://maven.apache.org/download.cgi) 3.3.3 or better


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


## Roadmap

Still left to explore...

a) The ability to warm the cache from an external file

b) Completing integration w/ Elasticache  (@see aws Maven profile in POM)

c) Listening to an event from Redis, Kafka or Kinesis instead of exposing an invalidate HTTP end-point

d) Authoring an alternate service implementation backed by Google Translate [API](https://cloud.google.com/translate/v2/using_rest)

