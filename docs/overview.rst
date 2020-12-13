********
Overview
********

Overview
########

MiniPG is dummy PostgreSQL service which acts like PostgreSQL. It is installed on PostgreSQL servers and executes administrative commands on PostgreSQL Servers. It uses the PostgreSQL protocol and does not need any SSH connection for command execution.

Defaults
########

MiniPG uses 9998 port as default for communication.

Config File
###########

MiniPG uses application.properties config file located under /etc/bfm/minipg

Here is a example config file:
::

    logging.pattern.console= %-18.18logger{39} : %m%n%wEx
    java.util.logging.ConsoleHandler.level = debug
    logging.level.com.bisoft=debug
    logging.ConsoleHandler.level = debug
    #minipg.postgres_bin_path=C:/Program Files/PostgreSQL/9.6/bin/
    #minipg.postgres_data_path=D:/winPG_yedek/
    #minipg.os=windows
    #fill the following parameters in accordance with the cluster environment
    minipg.postgres_bin_path=/usr/pgsql-10/bin/
    minipg.pgctl_bin_path=/usr/pgsql-10/bin/
    minipg.postgres_data_path=/var/lib/pgsql/10/data/
    minipg.os=linux
    minipg.version=1.8.0-SNAPSHOT
    application.minipg.UserName =  username
    application.minipg.pwd      =  password