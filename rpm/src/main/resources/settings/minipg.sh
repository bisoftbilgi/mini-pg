#!/bin/bash
SERVICE_NAME=minipg
BASE_PATH=/etc/bfm/minipg
PATH_TO_JAR=/etc/bfm/minipg/*.jar
PATH_TO_APP_PROP=/etc/bfm/minipg/application.properties
PATH_TO_LOG_ROTATE=/etc/logrotate.conf
PATH_TO_LOGD=/etc/logrotate.d/minipglog
case $1 in
start)
       echo "Starting $SERVICE_NAME ..."
        PS_8=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $8}')
                if [ $PS_8 == "java" ]
                then
                PS_10=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $10}')
                if [ $PS_10 == $PATH_TO_JAR ]
                then
                        PID=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $2}')
                        echo "$SERVICE_NAME is already running on $PID pid number"
                fi
                else
                        nohup java -jar $PATH_TO_JAR -Dspring.config.location=$PATH_TO_APP_PROP >> /etc/bfm/minipg/minipg.log 2>&1 &
                                logrotate -s /etc/bfm/minipg/logrotate.state $PATH_TO_LOG_ROTATE
                                logrotate -s /etc/bfm/minipg/logrotate.state -f $PATH_TO_LOGD

                        PS_8=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $8}')
                                if [ $PS_8 == "java" ]
                                then
                                        PS_10=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $10}')
                                if [ $PS_10 == $PATH_TO_JAR ]
                                then
                                        PID2=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $2}')
                                        echo "$SERVICE_NAME is started on $PID2 pid number"
                                fi
                                else
                                echo "$SERVICE_NAME could not start ..."
                                fi

                fi


;;
stop)
         PS_8=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $8}')
                if [ $PS_8 == "java" ]
                then
                PS_10=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $10}')
                if [ $PS_10 == $PATH_TO_JAR ]
                then
                        PID=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $2}')
                        echo "$SERVICE_NAME is running on $PID pid number"
                        kill $PID;
                        echo "$SERVICE_NAME stopped..."
                fi
                else
                        echo "$SERVICE_NAME is not running ..."
                fi
;;
restart)
                PS_8=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $8}')
                if [ $PS_8 == "java" ]
                then
                PS_10=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $10}')
                if [ $PS_10 == $PATH_TO_JAR ]
                then
                        PID=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $2}')
                        echo "$SERVICE_NAME is running on $PID pid number"
                        kill $PID;
                        echo "$SERVICE_NAME stopped..."
                        nohup java -jar $PATH_TO_JAR -Dspring.config.location=$PATH_TO_APP_PROP >> /etc/bfm/minipg/minipg.log 2>&1 &
                                logrotate -s /etc/bfm/minipg/logrotate.state $PATH_TO_LOG_ROTATE
                                logrotate -s /etc/bfm/minipg/logrotate.state -f $PATH_TO_LOGD

                        PS_8=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $8}')
                                if [ $PS_8 == "java" ]
                                then
                                        PS_10=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $10}')
                                if [ $PS_10 == $PATH_TO_JAR ]
                                then
                                PID2=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $2}')
                                 echo "$SERVICE_NAME is restarted on $PID2 pid number"
                                fi
                                else
                                echo "$SERVICE_NAME could not start ..."
                                fi

                fi
                else
                        nohup java -jar $PATH_TO_JAR -Dspring.config.location=$PATH_TO_APP_PROP > /etc/bfm/minipg/minipg.log 2>&1 &
                                logrotate -s /etc/bfm/minipg/logrotate.state $PATH_TO_LOG_ROTATE
                                logrotate -s /etc/bfm/minipg/logrotate.state -f $PATH_TO_LOGD

                        PS_8=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $8}')
                                if [ $PS_8 == "java" ]
                                then
                                        PS_10=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $10}')
                                if [ $PS_10 == $PATH_TO_JAR ]
                                then
                                PID=$(ps -ef|grep $BASE_PATH|awk 'NR==1{print $2}')
                                 echo "$SERVICE_NAME is started on $PID pid number"
                                fi
                                else
                                echo "$SERVICE_NAME could not start ..."
                                fi

                fi

;;
 esac


