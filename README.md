# Twitter Sentiment Analysis
Analysing the sentiment of tweets using a Streaming Architecture.

The Deployment folder contains a docker-compose.yml file for spinning up most of the necessary infrastructure for a development environment. Note that Spark is not part of the docker-compose setup. 

The `spark-submit` tool can be used to launch the sentiment analyzer via the following command: 

`./spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.3.0 .../Backend/sentiment_analyser.py 127.0.0.1 9092 tweets analyzed-tweets` 

(make sure that TextBlob is available in the environment)

If you have any questions about the applications or you'd like to know how to run them then feel free to contact me via [mmaresch.com](http://mmaresch.com).

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
