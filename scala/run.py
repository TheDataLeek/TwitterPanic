#!/usr/bin/env python3

import os

with open('config', 'r') as conf_file:
    conf = [c[:-1] for c in conf_file.readlines()]

    os.system('sbt assembly')

    execute = ('$SPARK_HOME/bin/spark-submit '
               '--class "Analysis" '
               '--master local[4] '
               'target/scala-2.10/TwitterPanic-assembly-1.0.jar '
               '-c {} '
               '-s {} '
               '-a {} '
               '-t {}').format(conf[0], conf[1], conf[2], conf[3])

    os.system(execute)
