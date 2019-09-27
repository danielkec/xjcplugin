#!/bin/bash

wget -q http://repo2.maven.org/maven2/com/sun/xml/bind/jaxb-xjc/maven-metadata.xml 
#sed -i 's/^<version>([^<]*)<\/version>$/\1/' ./maven-metadata.xml
#grep '<version>' maven-metadata.xml | sed -r 's/^\s*<version>([^<]*)<\/version>\s*$/\1/'

mvn -q org.apache.maven.plugins:maven-dependency-plugin:2.8:get -Dartifact=javax.xml.bind:jaxb-api:2.3.1
mvn -q org.apache.maven.plugins:maven-dependency-plugin:2.8:get -Dartifact=com.sun.xml.bind:jaxb-core:2.3.0.1;
mvn -q org.apache.maven.plugins:maven-dependency-plugin:2.8:get -Dartifact=jakarta.activation:jakarta.activation-api:1.2.1;

echo $(java -version)

for jaxbVersion in `grep '<version>' maven-metadata.xml | sed -r 's/^\s*<version>([^<]*)<\/version>\s*$/\1/'`; 
do 
  mvn -q org.apache.maven.plugins:maven-dependency-plugin:2.8:get -Dartifact=com.sun.xml.bind:jaxb-xjc:${jaxbVersion} &>/dev/null
  mvn -q org.apache.maven.plugins:maven-dependency-plugin:2.8:get -Dartifact=com.sun.xml.bind:jaxb-impl:${jaxbVersion} &>/dev/null
  echo "JAXB $jaxbVersion"

  ant clean run -Dtask.class=com.sun.tools.xjc.XJCTask -Djaxb.version=${jaxbVersion} 2>&1 \
  | grep 'build.xml:\|HELLO WORLD FROM XJC PLUGIN' \
  | sed 's/^.*HELLO WORLD.*$/XJCTask .... OK/g' \
  | sed -r 's/^.*build.xml:[0-9]*(.*)$/XJCTask ... NOK\1/g'

  ant clean run -Dtask.class=com.sun.tools.xjc.XJC2Task -Djaxb.version=${jaxbVersion} 2>&1 \
  | grep 'build.xml:\|HELLO WORLD FROM XJC PLUGIN' \
  | sed 's/^.*HELLO WORLD.*$/XJC2Task ... OK/g' \
  | sed -r 's/^.*build.xml:[0-9]*(.*)$/XJC2Task ... NOK\1/g'
done

rm maven-metadata.xml*