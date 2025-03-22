#!/bin/bash
java -Djavax.net.ssl.keyStore=../mykeystore.jks -Djavax.net.ssl.keyStorePassword=Password123 -cp "../sqlite-jdbc-3.41.2.2.jar:." -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 server.Server
