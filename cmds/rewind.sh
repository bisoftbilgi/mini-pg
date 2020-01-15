SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
PG_PATH="/usr/pgsql-10"
PG_DATA_PATH="/var/lib/pgsql/10/data"

echo "script : "  $0 
echo "param1 : " $1 
echo "script : "  $SCRIPTPATH 

$PG_PATH/bin/pg_rewind --target-pgdata=$PG_DATA_PATH/. --source-server="host=$1  port=5432 user=postgres dbname=postgres password=vds0665"
sed  "s/HOSTIP/$1/g" $SCRIPTPATH/recovery.template > $PG_DATA_PATH/recovery.conf  
 
$SCRIPTPATH/start.sh
  