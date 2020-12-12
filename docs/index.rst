*****
MiniPG
*****

Overview
########

MiniPG is dummy PostgreSQL service which acts like PostgreSQL. It is installed on PostgreSQL servers and executes administrative commands on PostgreSQL Servers. It uses the PostgreSQL protocol and does not need any SSH connection for command execution.

Defaults
########

MiniPG uses 9998 port as default for communication.

Config File
###########

MiniPG uses application.properties config file located under /etc/bfm/minipg

Here is a example config file

This is a simple example:
::

    logging.pattern.console= %-18.18logger{39} : %m%n%wEx
    java.util.logging.ConsoleHandler.level = debug
    logging.level.com.bisoft=debug
    logging.ConsoleHandler.level = debug
    #minipg.postgres_bin_path=C:/Program Files/PostgreSQL/9.6/bin/
    #minipg.postgres_data_path=D:/winPG_yedek/
    #minipg.os=windows

