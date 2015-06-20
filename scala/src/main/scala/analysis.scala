// https://spark.apache.org/docs/latest/streaming-programming-guide.html
// https://github.com/apache/spark/blob/master/examples/src/main/scala/org/apache/spark/examples/streaming/TwitterPopularTags.scala
import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.twitter._
import org.apache.spark.streaming.twitter.TwitterUtils

object Analysis {
  def main(args: Array[String]) {
    val keys = parseArgs(args)
    System.setProperty("twitter4j.oauth.consumerKey", keys.consumerKey)
    System.setProperty("twitter4j.oauth.consumerSecret", keys.consumerKeySecret)
    System.setProperty("twitter4j.oauth.accessToken", keys.accessToken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", keys.accessTokenSecret)
    //System.setProperty("twitter4j.streamBaseURL", "https://stream.twitter.com/1.1/statuses/filter.json?locations=-130,20,-60,50")

    val filters = Array(Array("language", "en"), Array("locations", "-130,20,-60,50"))

    val sparkConf = new SparkConf().setAppName("TwitterPanic")
    val ssc = new StreamingContext(sparkConf, Seconds(2))
    val stream = TwitterUtils.createStream(ssc, None)

    stream.foreachRDD(rdd => rdd.collect() foreach println)

    ssc.start()
    ssc.awaitTermination()
  }

  def parseArgs(args: Array[String]): Config = {
    /* Using immutable option parsing */

    var parser = new scopt.OptionParser[Config]("TwitterPanic") {
      head("scopt", "3.x")
      opt[String]('c', "consumerKey") action { (x, c) =>
        c.copy(consumerKey = x) } text("Consumer Key")
      opt[String]('s', "consumerKeySecret") action { (x, c) =>
        c.copy(consumerKeySecret = x) } text("Consumer Secret Key")
      opt[String]('a', "accessToken") action { (x, c) =>
        c.copy(accessToken = x) } text("Access Token")
      opt[String]('t', "accessTokenSecret") action { (x, c) =>
        c.copy(accessTokenSecret = x) } text("Access Secret Token")
      help("help") text("Arguments are keys")
    }

    parser.parse(args, Config()) match {
      case Some(config) => return config
      case None => {
        println("Error, malformed arguments")
        System.exit(1)
        return Config()
      }
    }
  }

  case class Config(
    consumerKey: String = "",
    consumerKeySecret: String = "",
    accessToken: String = "",
    accessTokenSecret: String = ""
  )
}
