## request_handler

These are a few pre-requisites to install:

1. setup :

        $> git@github.com:jogunism/request_handler.git 

1. delete .git folder in project : 

        $> rm -rf .git

1. import maven project in eclipse
1. run

        # **make runner folder**
        $> mkdir ../request_handler_runner
        # **maven install for service**
        $> mvn clean install assembly:single -Preal
        # **copy target to runner folder**
        $> cp ./target/request_handler-real-stand-alone.zip
        # **move to runner folder**
        $> cd ../request_handler_runner
        # **unzip target**
        $> unzip request_handler-real-stand-alone.zip
        # **run**
        $> ./bin/runner [start/stop]

