# Temperature Monitor

Let's say you have a house in a cold climate, but you're not there all the
time in the winter. If there's a problem with the heat, you want to know
with enough time to get it fixed before pipes freeze.

So what you do is:
* Buy a cheap weather station that:
   * monitors the indoor temperature, not just outdoor
   * can report to [weathercloud](https://weathercloud.net)
* Run this app to watch the indoor temperature and alert you if it's too low
* Enjoy not paying anyone a monthly fee

## Installation

Copy [src/main/resources/application.properties.example](src/main/resources/application.properties.example)
to `application.properties` and customize for your setup.

Build the app with `gradlew build` (creates `build/libs/tempmon-{version}.jar`)

Then you're ready to run with `java -jar build/libs/tempmon-{version}.jar`.

## Why is this Java?

Yes, it's overkill. I write a lot of Java for work at the moment,
so this was most familiar to me.