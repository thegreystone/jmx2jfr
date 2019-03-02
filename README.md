# jmx2jfr
Configurable agent which records JMX data into the Java Flight Recorder.

NOTE: Only works with Oracle JDK 7 and 8. I'll make one working with all JDKs 7+ as soon as I find some time.

## Usage
Run the agent on the command line and provide a jmxprobes.xml file to define what to record. Don't forget to enable flight recorder. 

Here is an example:
>java -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -javaagent:jmx2jfr.jar=jmxprobes.xml -cp jmx2jfr.jar se.hirt.jmx2jfr.test.HelloWorldTest

For more information, see the following blog:
>http://hirt.se/blog/?p=689
