#!/usr/bin/env bash
set -euo pipefail

# 0) PGDATA dizinini oluştur ve izinleri ayarla
mkdir -p "${PGDATA}" && chown -R postgres:postgres "${PGDATA}"

# 0.5) .pgpass dosyasını otomatik oluştur (yoksa)
if [ ! -f "/config/.pgpass" ]; then
  echo "*:5432:*:postgres:${POSTGRES_PASSWORD:-postgres}" > /config/.pgpass
  chmod 600 /config/.pgpass
fi
cp /config/.pgpass /var/lib/pgsql/.pgpass
chown postgres:postgres /var/lib/pgsql/.pgpass
chmod 600  /var/lib/pgsql/.pgpass

# 1) İlk kurulum (PGDATA boşsa)
if [ ! -s "${PGDATA}/PG_VERSION" ]; then
  echo "Initializing PostgreSQL cluster…"
  echo "${POSTGRES_PASSWORD:-postgres}" > /tmp/pwfile
  chown postgres:postgres /tmp/pwfile && chmod 600 /tmp/pwfile

  runuser -u postgres -- initdb -D "${PGDATA}" -U postgres -A md5 --pwfile=/tmp/pwfile
  rm /tmp/pwfile

  # Master için repl. parametreleri
  if [ -z "${MINIPG_REPLICATION_NODES:-}" ]; then
    echo "Configuring master parameters…"
    runuser -u postgres -- psql -c "ALTER SYSTEM SET wal_level = replica;"
    runuser -u postgres -- psql -c "ALTER SYSTEM SET max_wal_senders = 10;"
    runuser -u postgres -- psql -c "ALTER SYSTEM SET hot_standby = on;"
    runuser -u postgres -- psql -c "ALTER USER postgres WITH REPLICATION;"
    runuser -u postgres -- psql -c "SELECT pg_reload_conf();"
  fi
fi

# 2) pg_hba.conf güncelle
grep -q "^host replication postgres" "${PGDATA}/pg_hba.conf" || \
  echo "host replication postgres 0.0.0.0/0 md5" >> "${PGDATA}/pg_hba.conf"
grep -q "^host all all" "${PGDATA}/pg_hba.conf" || \
  echo "host all all 0.0.0.0/0 md5" >> "${PGDATA}/pg_hba.conf"

export PGPASSWORD="${POSTGRES_PASSWORD:-postgres}"

# 3) Stand-by ise basebackup al 
if [ -n "${MINIPG_REPLICATION_NODES:-}" ] && [ ! -f "${PGDATA}/PG_VERSION.bak" ]; then
  MASTER_HOST="${MINIPG_REPLICATION_NODES%%:*}"
  MASTER_PORT="${MINIPG_REPLICATION_NODES##*:}"

  echo "Waiting master ${MASTER_HOST}:${MASTER_PORT}…"
  until pg_isready -h "$MASTER_HOST" -p "$MASTER_PORT" -U postgres; do sleep 2; done

  echo "Taking basebackup…"
  rm -rf "${PGDATA:?}/"* && chown -R postgres:postgres "${PGDATA}"

  # PGAPPNAME artık Docker Compose'tan geliyor
  runuser -u postgres -- env PGPASSWORD="${PGPASSWORD}" PGAPPNAME="${PGAPPNAME}" \
    pg_basebackup -h "$MASTER_HOST" -p "$MASTER_PORT" \
    -U postgres -D "${PGDATA}" -Fp -Xs -P -R

  touch "${PGDATA}/standby.signal"
  cp "${PGDATA}/PG_VERSION" "${PGDATA}/PG_VERSION.bak"
fi

# 4) listen_addresses = '*'
sed -i "s/^#*listen_addresses.*/listen_addresses = '*'/" "${PGDATA}/postgresql.conf"

# 5) PostgreSQL’i başlat
echo "Starting PostgreSQL…"
runuser -u postgres -- pg_ctl -D "${PGDATA}" -w start

# 5.1) primary_conninfo ayarla 
if [ -n "${MINIPG_REPLICATION_NODES:-}" ]; then
  MASTER_HOST="${MINIPG_REPLICATION_NODES%%:*}"
  MASTER_PORT="${MINIPG_REPLICATION_NODES##*:}"
  runuser -u postgres -- psql -h 127.0.0.1 -U postgres -v ON_ERROR_STOP=1 -c "\
    ALTER SYSTEM SET primary_conninfo = \
    'host=${MASTER_HOST} port=${MASTER_PORT} user=postgres password=${POSTGRES_PASSWORD} application_name=${PGAPPNAME}';"
  runuser -u postgres -- psql -h 127.0.0.1 -U postgres -c "SELECT pg_reload_conf();"
fi

# 6) Mini-PG’yi başlat
echo "Launching Mini-PG…"
exec runuser -u postgres -- java -jar /app/minipg-app.jar
