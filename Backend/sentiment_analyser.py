import sys

# Not used in the MVP 
# Contains the basic setup for a Spark Structured Streaming job which will be integrated into the application architecture

if __name__ == "__main__":
    # Kafka connection details, configured via command-line arguments
    host = sys.argv[1]
    port = sys.argv[2]
    input_topic = sys.argv[3]
    output_topic = sys.argv[4]
    
    session = SparkSession\
        .builder\
        .appName("SentimentAnalyser")\
        .getOrCreate()

    df = session.readStream\
        .format("kafka")\
        .option("kafka.bootstrap.servers", host + ":" + port)\
        .option("subscribe", input_topic)\
        .load()\
        .selectExpr("CAST(value AS STRING) as text")

    df.writeStream\
        .format("kafka")\
        .option("kafka.bootstrap.servers", host + ":" + port)\
        .option("topic", output_topic)\
        .start()