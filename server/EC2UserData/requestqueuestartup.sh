#!/bin/bash
cd /home/ec2-user
rm server-0.0.1-SNAPSHOT.jar
aws s3 cp s3://dfs-jar-bucket/server-0.0.1-SNAPSHOT.jar .
sudo java -cp server-0.0.1-SNAPSHOT.jar -Dloader.main=RequestQueue.Server.RequestQueueServerMain org.springframework.boot.loader.PropertiesLauncher
