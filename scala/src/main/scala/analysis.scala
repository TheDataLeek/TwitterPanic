// https://spark.apache.org/docs/latest/streaming-programming-guide.html
// https://github.com/apache/spark/blob/master/examples/src/main/scala/org/apache/spark/examples/streaming/TwitterPopularTags.scala
import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.twitter._
import org.apache.spark.streaming.twitter.TwitterUtils
//import com.github.scopt._

object Analysis {
  def main(args: Array[String]) {
    val Array(consumerKey, consumerSecret, accessToken, accessTokenSecret) = args.take(4);
    val filters = args.takeRight(args.length - 4)

    System.setProperty("twitter4j.oauth.consumerKey", consumerKey)
    System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret)
    System.setProperty("twitter4j.oauth.accessToken", accessToken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", accessTokenSecret)

    val sparkConf = new SparkConf().setAppName("Social Panic Analysis")
    val ssc = new StreamingContext(sparkConf, Seconds(2))
    val stream = TwitterUtils.createStream(ssc, None, filters)

    ssc.start()
    ssc.awaitTermination()
  }

  def parseArgs() {
    var parser = new scopt.OptionParser[Config]("scopt") {
      head("scopt", "3.x")
      opt[String]('c', "consumerKey") action { (x, c) => 
        c.copy(consumerKey = x) } text("Consumer Key")
      opt[String]('s', "consumerKeySecret") action { (x, c) => 
        c.copy(consumerKeySecret = x) } text("Consumer Secret Key")
      opt[String]('a', "accessToken") action { (x, c) => 
        c.copy(accessToken = x) } text("Access Token")
      opt[String]('t', "accessTokenSecret") action { (x, c) => 
        c.copy(accessTokenSecret = x) } text("Access Secret Token")
      note("Twitter Analysis.\n")
      help("help") text("Arguments are keys")
    }

    parser.parse(args, Config()) match {
      case Some(config) => println(config)
      case None => println("Error, malformed arguments")
    }
  }
}

case class Config(
  consumerKey: String = "",
  consumerKeySecret: String = "",
  accessToken: String = "",
  accessTokenSecret: String = ""
)
