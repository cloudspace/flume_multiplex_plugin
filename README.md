Multiplex
=========

This is a collection of plugins for [Cloudera's Flume](http://github.com/cloudera/flume) that are 
used internally at [Cloudspace](http://www.cloudspace.com/). They make it slightly easier to include Flume
in a custom architecture that might not include Hadoop or HDFS for storage. Included are:

* TCPSink
* JsonMultiplexDecorator

Installation
------------

This package is designed to be a git submodule of the Flume source.

	$ cd /usr/local/src
	$ git clone git://github.com/cloudera/flume.git
	$ cd flume
	$ ant
	$ git submodule add git://github.com/cloudspace/flume_multiplex_plugin.git plugins/multiplex
	$ cd plugins/multiplex
	$ ant install
	
After building and installing with `ant install`, the jar file will be copied to `/usr/local/flume/plugins/` 
so that will need to be added to your `FLUME_CLASSPATH` shell variable:

	$ export FLUME_CLASSPATH="$FLUME_CLASSPATH:/usr/local/flume/plugins/cloudspace_multiplex_plugin.jar"

Usage
-----

The plugin is fairly simple to use. It exposes two new elements to 
[Flume's dataflow specification language](http://archive.cloudera.com/cdh/3/flume/UserGuide.html#_flume_8217_s_dataflow_specification_language): 
the `tcpSink()` sink and the `multiplexDecorator()` decorator.

###TcpSink###

The `tcpSink()` sink takes two arguments: 

* `serverAddress` which is a quoted String specifying the DNS name or IP address of the TCP server
you will be sending to
* `serverPort` which is an Integer specifying the port the TCP server is listening on

This sink simply opens a TCP Socket connection and writes all incoming events to it.

###JsonMultiplexDecorator###

The `jsonMultiplexDecorator()` decorator takes two arguments:

* `serverName` which is a quoted String specifying the name of the server these events belong to
* `logType` which is a quoted String specifying what kind of log data (eg. apache_access, syslog, etc) these events belong to

This decorator transforms incoming events into JSON objects, like so:

	{ "server": "example.com_production", "log_type": "apache_access", "body": "apache_access_event" }

###Dataflow Specification Examples###

Simple example with a single agent that reads from a log and sends it to a TCP listener:

	agent: tail("/var/log/syslog") | tcpSink("127.0.0.1", 12345)
	
Slightly more complex, with the decorator:

	agent: tail("/var/log/syslog") | { jsonMultiplexDecorator("localhost", "syslog") => tcpSink("127.0.0.1", 12345) }
	
Using several nodes, 2 agents and a collector:

	agent1: tail("/var/log/syslog") | { jsonMultiplexDecorator("example.com", "syslog") => agentSink("collector", 35872) }
	agent2: tail("/var/log/syslog") | { jsonMultiplexDecorator("test.com", "syslog") => agentSink("collector", 35872) }
	collector: collectorSource(35872) | tcpSink("127.0.0.1", 12345)
	
Links
-----

* <http://www.cloudspace.com/>
* <http://www.cloudera.com/>
* [Flume User Guide](http://archive.cloudera.com/cdh/3/flume/UserGuide.html)
* [Flume Source](http://github.com/cloudera/flume)

Copyright
---------

Copyright (c) Josh Lindsey at Cloudspace. See LICENSE for details.