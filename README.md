Title: Twitter Civil Unrest Analysis with Apache Spark
Date: 2015-04-29
Category: Data Science
tags: python, spark
Author: Will Farmer
Summary: Using the Streaming Twitter API to analyze tweets as an indicator of civil unrest.

#Introduction

On Monday I attended the CU Boulder Computer Science Senior Projects Expo, and there was one project
in particular that I thought was pretty neat: determining areas of civil unrest through Twitter
post analysis. They had some pretty cool visuals and used Apache Spark, so I figured I'd try to
recreate it on my own. What follows is an initial prototype attempt as a proof of concept. I'll go
through each step included here, [but I've also included the code on my github
profile](https://github.com/willzfarmer/TwitterPanic), so feel free to clone and run it. (Please
also checkout this article on my [website](www.will-farmer.com) where the formatting will actually
work.)

The final result is pretty neat.

<script>
<video width="960" height="540" controls>
<source src="demosped.webm" type="video/webm">
</video> 
</script>

#PySpark Streaming

One initial design choice that I made was to create a streaming program, that constantly ran and
produced results. This meant that my best bet was to use [Spark's streaming
API](https://spark.apache.org/docs/latest/api/python/pyspark.streaming.html). As with any other
Spark program we first create our `SparkContext` object, and since I'm set up on my laptop (with 4
workers) we designate our program to use the local Spark instance. After we instantiate our
`SparkContext` we create our `StreamingContext` and establish a batch interval, which is simply the
interval at which the streaming API will update.

    :::python
    sc  = SparkContext('local[4]', 'Social Panic Analysis')
    ssc = StreamingContext(sc, BATCH_INTERVAL)

We can now create our stream, which is done by setting a default "blank" RDD, and creating a new
`queueStream` whose default contents is the blank RDD from before.

    :::python
    rdd = ssc.sparkContext.parallelize([0])
    stream = ssc.queueStream([], default=rdd)

At this point our stream consists of a single entry, the RDD containing `[0]`. We need to convert that to
usable output, which we do by applying a flat map over every element in the stream. This mapping
process (in my case) converts each element currently in the stream to some new RDD that has some set
data. (Note, you could absolutely have a ton of elements in your blank RDD to start, which would
mean that you're pulling in data over each one of those elements. I did not do that due to [Twitter's
Rate Limiting](https://dev.twitter.com/rest/public/rate-limiting), which I'll go over more in depth
later.)

    :::python
    stream = stream.transform(tfunc)

Now we perform our analysis on the streaming object. (I'll cover this more in depth later)

    :::python
    coord_stream = stream.map(lambda line: ast.literal_eval(line)) \
                        .filter(filter_posts) \
                        .map(get_coord)

After the analysis is done we output the data. (Again, I'll go over this later)

    :::python
    coord_stream.foreachRDD(lambda t, rdd: q.put(rdd.collect()))

Since Spark is lazily evaluated, nothing has been done yet. All we've done is establish the fact
that we have some data stream and that we wish to perform some series of steps on it in our
analysis. The final step is to start our stream and basically just wait for something to stop it.

    :::python
    ssc.start()
    ssc.awaitTermination()

And that's it! We have now a constantly streaming object that we can pull from.

#Transforming our Data Stream

In order to convert our initial blank RDD to twitter data, we apply a flat map over it. This
converts each element (currently just one) to my stream output.

    :::python
    return rdd.flatMap(lambda x: stream_twitter_data())

The stream output is determined by the contents of the following streaming endpoint.

    https://stream.twitter.com/1.1/statuses/filter.json?language=en&locations=-130,20,-60,50

This restricts my results to English tweets over a bounding box that is mostly just the United
States. (You could remove the bounding box option, but since the only language I know is English I
wanted to be able to accurately parse the results.)

The response that we get when we use the `stream=True` option with `requests` has an iterable
object. We iterate over those lines, yield them, and after receiving a set number of tweets we break
and terminate our request. If we run into any hiccups along the way, for the purpose of this
prototype, we just print those out for visual debugging.

    :::python
    response  = requests.get(query_url, auth=config.auth, stream=True)
    print(query_url, response) # 200 <OK>
    count = 0
    for line in response.iter_lines():  # Iterate over streaming tweets
        try:
            if count > BLOCKSIZE:
                break
            post     = json.loads(line.decode('utf-8'))
            contents = [post['text'], post['coordinates'], post['place']]
            count   += 1
            yield str(contents)
        except:
            print(line)

# Analysis

Since each element in our resulting RDD is a string representation of a Python object, we first map
each line in our RDD to a literal Python object.

    :::python
    coord_stream = stream.map(lambda line: ast.literal_eval(line))

We now wish to filter by posts whose text content contains language indication social unrest. I
don't really have the background for the best sentiment analysis, so all I have is a list of
keywords as my filtering criteria. (I've also included the word "http" in there so that in my pool
of limited results I actually have some content. Of course for real analysis you would remove that.)

    :::python
    filtered_stream = coord_stream.filter(filter_posts)

The next step would be to apply a more sophisticated process, including word association and natural
language processing, however I didn't implement something along those lines because of the nature of
this project as well as the somewhat lack of background.

    :::python
    final_stream = filtered_stream.map(get_coord)

At this point we only have posts that indicate violence, so we need to reduce each tweet to just its
geographical coordinates. Once this is complete we can plot each point.

    :::python
    final_stream.foreachRDD(lambda t, rdd: q.put(rdd.collect()))

#Plotting

As you probably noticed, our "plotting" that we reference above actually puts the contents of the
RDD into a queue. This queue is also accessed by the other thread of the program which checks the
queue every 5 seconds and pulls data out.

We use [Cartopy](http://scitools.org.uk/cartopy/docs/latest/) for the plotting process, and create a
new `matplotlib` figure set to interactive mode. If there is a new entry in the queue, we pull it
out and add the points to our figure instance.

    :::python
    plt.ion() # Interactive mode
    fig = plt.figure(figsize=(30, 30))
    ax = plt.axes(projection=ccrs.PlateCarree())
    ax.set_extent([-130, -60, 20, 50])
    ax.coastlines()
    while True:
        if q.empty():
            time.sleep(5)
        else:
            data = np.array(q.get())
            try:
                ax.scatter(data[:, 0], data[:, 1], transform=ccrs.PlateCarree())
                plt.draw()
            except IndexError: # Empty array
                pass

#Rate Limiting

Because Twitter will only let me query their site 15 times in 15 minutes, I'm restricted in how much
actual analysis I can do. Not only do I have to use their sample stream, but I can only update once
every 60 seconds unless I want to be rate limited very quickly. Hence why this process is so slow.
The next step to make this update at a more reasonable speed is to maintain the `response` object
that we get from the request so that we don't have to run into query limits, but all attempts to
make that work failed horribly (I'm hoping to get it working at one point, but right now it will
just hang and never evaluate anything).

#Final Thoughts

All in all it was a really neat project. The streaming API still has some kinks to iron out, mostly
in the documentation. No real tutorials exist yet for this tool and I had to refer to the unit tests
for some of modules I was using as it was the only instance of working code I could find. I'm hoping
that this article may help someone who decides to work on Spark in the future.

There is a ton I could do with this project. Something that I'm planning on doing in the next few
weeks (sometime after finals finish up) is to implement a static version that acts on a collected
data set and shows behavior over the last few weeks.

My visualization is not very pretty. Yes, it gets the job done but the black coastlines leave a lot
to be desired...

Ideally this project just passes around the response object. Again, I was never quite able to get
that part implemented, but I'm hoping after a few more hours of working with it I can figure out
what the problem is.

Please let my know what you think! Leave comments below or on its github page and I'll get back to
you as quickly as I can.
