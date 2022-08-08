# Twitter Stream Processing for Sentiment Analysis
Analysing the sentiment of live tweet streams.

This repository contains a software which leverages stream processing techniques to analyze a the sentiment of a live stream of tweets related to hashtags in real-time. The project consists of a Web-App, an API Gateway and a sentiment analyzer job. In the Web-App, the user enters hashtags and submits them to start receiving tweets with those hashtags. A visualization of the real-time sentiment results is shown in the UI, along with the tweets which were analyzed.

The 3 components of the software work together in the following way: The Web-App sends the hashtags to the API Gateway. The Gateway has an active connection to the Twitter API and updates the rules of the Twitter API to receive a stream of tweets based on the requested hashtags. Those tweets are sent to a Kafka topic. The sentiment analyzer job reads these tweets, analyzes their sentiment and writes the results to another Kafka topic. The sentiment analyzer job runs on Spark. The sentiment results are read from Kafka by the API Gateway and delivered back to the Web-App via SSE. All of this is happening in parallel and in real-time.

The Architecture is based on Kotlin Flow, Reactor, SSE and Spark Structured Streaming:
- Kotlin Flow, Reactor and Spring are used in the API Gateway.
- SSE is used for sending the sentiment results from the API Gateway to the Web-App.
- Spark Structured Streaming, PySpark and TextBlob are used for the sentiment analyzer job.
- The Web-App is implemented using React.

The `Backend` folder contains the API Gateway and the sentiment analyzer job. The `Deployment/dev` folder contains a `docker-compose.yml` file for spinning up most of the necessary infrastructure for a development environment. Note that Spark is not part of the docker-compose setup. The Web-App can be found in the `Frontend` folder.

The `application.properties` file of the API Gateway contains the property `twitter.token` which you need set to your Twitter token (a bearer token). This is used to access the Twitter API (receiving a tweet stream, managing the rules for filtering the stream).

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
