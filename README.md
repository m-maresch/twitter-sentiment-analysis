# Twitter Stream Processing for Sentiment Analysis
Analysing the sentiment of live tweet streams.

The `application.properties` file of the API Gateway contains the property `twitter.token` which you need set to your Twitter token (a bearer token).

These are used to access the Twitter API (receiving a tweet stream, managing the rules for filtering the stream).

The `Deployment/dev` folder contains a `docker-compose.yml` file for spinning up most of the necessary infrastructure for a development environment. Note that Spark is not part of the docker-compose setup. 

The `spark-submit` tool can be used to launch the sentiment analyzer via the following command: 

`./spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.3.0 .../Backend/sentiment_analyser.py 127.0.0.1 9092 tweets analyzed-tweets`

If you have any questions about the applications or you need help with running them then feel free to contact me via [mmaresch.com](http://mmaresch.com).

# Dependencies
Thanks to everyone contributing to any of the following projects:
- Any Spring project
- Kafka
- Reactor
- React
- Material-UI
- Chart.js
- Twitter API Client Library for Java
- Spark, PySpark
- TextBlob
