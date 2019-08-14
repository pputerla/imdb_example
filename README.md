# Build

`mvn clean verify`

* jacoco report: `target/site/jacoco/intex.html`
* pittest report: `target/pit-reports/<date>/index.html`

# Run

`java -jar target/imdb-1.0-SNAPSHOT.jar --db.filename=./path_to_db_file --log.filename=./path_to_request_log_file`

# UI

`http://localhost:8080/swagger-ui.html`
`http://localhost:8080/userStats.html`

# JMX
`imdb:category=Statistics,name=userStatistics` (`UserHitCount`)
