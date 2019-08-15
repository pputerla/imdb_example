# Build

`mvn clean verify`

* jacoco report: `target/site/jacoco/intex.html`
* pittest report: `target/pit-reports/<date>/index.html`

# Run

Required: Java 12 (may be run other versions; requires change in "java.version" property in pom.xml)

`java -jar target/imdb-1.1-SNAPSHOT.jar --db.filename=./path_to_db_file --log.filename=./path_to_request_log_file --loader.autostart=true`

options:
* `loader.autostart` - default is false - not to automatically start loader on app startup
* `db.filename` - set the path to H2 db filename (without .mv.db suffix); when using different url (`spring.datasource.url`) the parameter will be ignored
* `log.filename` - set the path to http request logs (logbook)

If loader is not automatically started it must to be started via JMX operation (startLoader)

other database can be used by overriing standard spring datasource parameters + adding required driver

When using external db one may want to disable ddl generation (`--spring.jpa.hibernate.ddl-auto=none`) after the first execution of the app.

# UI

* `http://localhost:8080/swagger-ui.html`
* `http://localhost:8080/userStats.html`

# JMX

* `imdb:category=Statistics,name=userStatistics` (`UserHitCount`)
* `imdb:category=Loader,name=control` (`startLoader`, `stopLoader`)
