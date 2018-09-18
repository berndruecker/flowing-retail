@echo off
FOR /F "tokens=1,2 delims==" %%G IN (environment.properties) DO (set %%G=%%H)
TITLE Checkout
mvn exec:java -f %flowing%\kafka\java\checkout\