# Github crawler

## Description

This app is using github V3 api to find contributors names and their contributions within specified company.

## Note

Large organizations may contain a lot of information, so it may take some time to process all data. This app uses in
memory cache, so if you are getting request timed out errors or something similar, you could retry http call to this app
until it will succeed.

## How to run

You could either use:

* `sbt run` command
* run in your favorite IDE (Intellij IDEA or Eclipse for example)

afterwards trigger endpoint:
GET http://localhost:8080/org/{org_name}/contributors
until it succeeds

## How to run tests

You could either use:

* `sbt test` command
* run tests in your favorite IDE (Intellij IDEA or Eclipse for example)

## Configuration options

application.conf file contains configuration, here are options meaning:

```
{
    port = 8080 // port on which app is running
    timeout = 30 seconds // idle http timeout for app
    github: {
        token = "dummy" // github app token
        token = ${?GH_TOKEN}
        max-retry-count = 2 // retry count
        base-path = "https://api.github.com" // github api base path
        page-size = 30 // page size
        cache-expiration = 60 seconds // app cache expiration
    }
}```