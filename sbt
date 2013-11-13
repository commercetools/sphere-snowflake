#!/bin/sh

### downloads and starts SBT http://www.scala-sbt.org/

SBT_VERSION=${SBT_VERSION:=0.13.0}
LAUNCHER_FILE=$HOME/.sbt/sbt-launch-$SBT_VERSION.jar
URL="http://repo.typesafe.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/$SBT_VERSION/sbt-launch.jar"

#### install

if [ ! -r $LAUNCHER_FILE ]; then
  mkdir -p $(dirname $LAUNCHER_FILE)
  wget -c -O "$LAUNCHER_FILE" $URL
  if [ ! -r $LAUNCHER_FILE ]; then
    curl $URL > "$LAUNCHER_FILE"
    if [ ! -r $LAUNCHER_FILE ]; then
      echo "Download of SBT failed. You need to install wget or cURL."
    fi
  fi
fi

#### launch 
if [ -r $LAUNCHER_FILE ]; then
  java -Xms512M -Xmx1536M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=768M -jar $LAUNCHER_FILE "$@"
fi 
