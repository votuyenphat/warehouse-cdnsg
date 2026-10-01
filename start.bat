@echo off
cd /d D:\pmql\cms-cdnsg-main

echo Starting Java server...
start cmd /k java -jar target\warehouse-cdnsg-0.0.1-SNAPSHOT.jar

timeout /t 5

echo Starting ngrok...
start cmd /k ngrok http 8080