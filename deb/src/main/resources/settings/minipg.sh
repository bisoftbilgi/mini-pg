#!/bin/bash
SERVICE_NAME=minipg
BASE_PATH=/etc/bfm/minipg
PATH_TO_JAR=/etc/bfm/minipg/*.jar
PATH_TO_APP_PROP=/etc/bfm/minipg/application.properties
case $1 in
start)
       echo "Starting $SERVICE_NAME ..."
        PS_10=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $10}')
        if [ $PS_10 == $PATH_TO_JAR ]
        then
                PID=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $2}')
                echo "$SERVICE_NAME is already running on $PID pid number"
        else
                nohup java -jar $PATH_TO_JAR -Dspring.config.location=$PATH_TO_APP_PROP >> /etc/bfm/minipg/minipg.log 2>&1 &

                PS_10=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $10}')
                if [ $PS_10 == $PATH_TO_JAR ]
                then
                        PID2=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $2}')
                        echo "$SERVICE_NAME is started on $PID2 pid number"
                else
                        echo "$SERVICE_NAME could not start ..."
                fi
        fi
;;
stop)
        PS_10=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $10}')
        if [ $PS_10 == $PATH_TO_JAR ]
        then
                PID=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $2}')
                echo "$SERVICE_NAME is running on $PID pid number"
                kill $PID;
                echo "$SERVICE_NAME stopped..."
        else
                echo "$SERVICE_NAME is not running ..."
        fi
;;
restart)
        PS_10=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $10}')
        if [ $PS_10 == $PATH_TO_JAR ]
        then
                PID=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $2}')
                echo "$SERVICE_NAME is running on $PID pid number"
                kill $PID;
                echo "$SERVICE_NAME stopped..."
                nohup java -jar $PATH_TO_JAR -Dspring.config.location=$PATH_TO_APP_PROP >> /etc/bfm/minipg/minipg.log 2>&1 &

                PS_10=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $10}')
                if [ $PS_10 == $PATH_TO_JAR ]
                then
                        PID2=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $2}')
                        echo "$SERVICE_NAME is restarted on $PID2 pid number"
                else
                        echo "$SERVICE_NAME could not start ..."
                fi
        else
                nohup java -jar $PATH_TO_JAR -Dspring.config.location=$PATH_TO_APP_PROP > /etc/bfm/minipg/minipg.log 2>&1 &

                PS_10=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $10}')
                if [ $PS_10 == $PATH_TO_JAR ]
                then
                        PID=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $2}')
                        echo "$SERVICE_NAME is started on $PID pid number"
                else
                        echo "$SERVICE_NAME could not start ..."
                fi
        fi
;;
 esac


