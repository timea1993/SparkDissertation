# SparkDissertation - steps to install the Arules app
# Prerequisites
In order to install the app be sure that SPARK(2.0.2 preferred) is installed on your workstation.
# Configuration
* As SPARK works with log4j be sure that that you configure the **log4j.properties** file
from the $SPARK_HOME/conf directory as follows ![here](config/log4j.properties)
* As I want to be able to configure the minConfidence, minSupport, inputFile, outputFile parameters of the app
wihout redeploying the app, the parameters are configured in the **spark-defaults.conf** file from the $SPARK_HOME/conf
directory as follows ![here](config/spark-defaults.conf)

# Building the app
In order to build the app you should use the maven clean and the maven install commands, after that there will be generated two jars, one with the core functionalites as follows: **Arules-0.0.1-SNAPSHOT.jar** and **Arules-0.0.1-SNAPSHOT-jar-with-dependencies.jar**. The jar with the dependencies will be deployed.
# Launching Application with spark-submit
Once a user application is bundled, it can be launched using the $SPARK_HOME/bin/spark-submit script:

./bin/spark-submit \
  --class <main-class> \
  --master <master-url> \
  --deploy-mode <deploy-mode> \
  --conf <key>=<value> \
  ... # other options
  <application-jar> \
  [application-arguments]

In this particular case you should run: spark-submit --class com.dissertation.arules.Main $jar_path\Arules\target\Arules-0.0.1-SNAPSHOT-jar-with-dependencies.jar,
with an optional **test** argument in order to run the unit tests. The $jar_path should be replaced with the location of the jar.

## Problem
In order to have a better performance: we should compute the chisquare of each rule as an RDD without transforming the RDD into a Java collection. Spark does not allow to iterate over an RDD while making some calculations based on another RDD at the same time as shown in the commented lines between 119-133 in **AssociationRuleMiner.java**

## FYI 
Spark Applications could be monitored at http://<driver-node>:4040 in a web browser, see more at http://spark.apache.org/docs/latest/monitoring.html