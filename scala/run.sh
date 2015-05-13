#!/bin/bash

#sbt package

CONFIG=$(cat config)

echo "${CONFIG[2]}"

#$SPARK_HOME/bin/spark-submit \
#    --class "Analysis" \
#    --master local[4] \
#    target/scala-2.11/twitter-panic_2.11-1.0.jar \

