#!/bin/bash
java -Djavax.net.ssl.trustStore=../mytruststore.jks -Djavax.net.ssl.trustStorePassword=Password123 -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -classpath ../out/production/PAI2 client.ClientSocket
