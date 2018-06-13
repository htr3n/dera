# DERA : Proof-Of-Concept

This project is an implementation of [DERA](DERA-theoretical-part.md) system using Java (the core) and Groovy (the examples). More theoretical foundation can be found [here](DERA-theoretical-part.md).

![](DevelopmentToolchain.png)

#### Quick Start

For a quick glance on how a DERA system works, you can jump to the folder `src/main/groovy/examples` for some examples. Executing any of these Groovy classes will start corresponding DERA domains and trigger DERA actors and events.

#### Technical Details

* DERA core has been implemented using pure Java and some [Google Guava](https://github.com/google/guava) collection helpers. 
* DERA internal event exchanging is based on [LMAX-Exchange Disruptor](https://github.com/LMAX-Exchange/disruptor) - a lock-free high performance messenging library.
* DERA interfaces (Web, REST, Web socket) have been developed using a lightweight embedded [Eclipse Jetty](https://www.eclipse.org/jetty/) server, [Jersey JAX-RS](https://jersey.github.io), Apache [HTTPClient](https://hc.apache.org/httpcomponents-client-4.5.x/index.html) and [HTTPAsyncClient](https://hc.apache.org/httpcomponents-asyncclient-ga/index.html) for handling HTTP/REST, [FasterXML's Jackson](https://github.com/FasterXML/jackson) for binding Java objects and JSON, and [Joda-Time](http://www.joda.org/joda-time) for date/time processing.

