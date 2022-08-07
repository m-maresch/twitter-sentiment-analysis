from pyspark.sql import SparkSession
from pyspark.sql.types import *
from pyspark.sql.functions import *
from textblob import TextBlob
import sys

def normalize(tweets):
    tweets = tweets.withColumn("text_normalized", col("text"))
    tweets = tweets.withColumn('text_normalized', regexp_replace('text_normalized', 'http\S+', '')) # Remove links
    tweets = tweets.withColumn('text_normalized', regexp_replace('text_normalized', '(@\w+|RT)', '')) # Remove mentions and retweet indicator
    tweets = tweets.withColumn('text_normalized', regexp_replace('text_normalized', '(:|;|#)', '')) # Various symbols
    return tweets

if __name__ == "__main__":
    # Kafka connection details, configured via command-line arguments
    host = sys.argv[1]
    port = sys.argv[2]
    input_topic = sys.argv[3]
    output_topic = sys.argv[4]

    session = SparkSession\
        .builder\
        .config("spark.driver.host", "localhost")\
        .appName("SentimentAnalyser")\
        .getOrCreate()

    session.sparkContext.setLogLevel('ERROR')

    df = session.readStream\
        .format("kafka")\
        .option("kafka.bootstrap.servers", host + ":" + port)\
        .option("subscribe", input_topic)\
        .option("failOnDataLoss", "false")\
        .load()

    jsonSchema = StructType().add("text", StringType())

    df = df.select(from_json(col("value").cast("string"), jsonSchema).alias("parsed_value"))\
        .select(col("parsed_value.*"))

    polarity = udf(lambda text: TextBlob(text).sentiment.polarity * 100, DoubleType())
    subjectivity = udf(lambda text: TextBlob(text).sentiment.subjectivity * 100, DoubleType())

    df = normalize(df)\
        .withColumn("polarity", polarity("text_normalized"))\
        .withColumn("subjectivity", subjectivity("text_normalized"))\
        .selectExpr("cast(text as string) as tweet", "polarity", "subjectivity")\
        .withColumn("json", to_json(struct('tweet', 'polarity', 'subjectivity')))

    consoleQuery = df.writeStream\
        .outputMode('append')\
        .format('console')\
        .start()

    kafkaSinkQuery = df.selectExpr("cast(null as string) as key", "json as value")\
        .writeStream\
        .format("kafka")\
        .option("kafka.bootstrap.servers", host + ":" + port)\
        .option("topic", output_topic)\
        .option("checkpointLocation", "checkpoint")\
        .start()

    consoleQuery.awaitTermination()
    kafkaSinkQuery.awaitTermination()