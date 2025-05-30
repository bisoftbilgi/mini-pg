FROM rockylinux:8

# 0. Disable GPG checks
RUN printf "\n[main]\ngpgcheck=0\nrepo_gpgcheck=0\nskip_if_unavailable=True\n" \
    > /etc/dnf/dnf.conf

# 1. Add Bisoft & EPEL repos
RUN dnf install -y \
    https://nexus.bisoft.com.tr/repository/bfm-yum/repo/bisoft-repo-1.0-1.noarch.rpm \
    https://dl.fedoraproject.org/pub/epel/epel-release-latest-8.noarch.rpm \
    && dnf clean all \
    && rm -rf /var/cache/dnf/*

# 2. Install PGDG repo
RUN dnf install -y wget \
    && rpm -Uvh https://download.postgresql.org/pub/repos/yum/reporpms/EL-8-x86_64/pgdg-redhat-repo-latest.noarch.rpm \
    && sed -i 's/gpgcheck=1/gpgcheck=0/g; s/repo_gpgcheck=1/repo_gpgcheck=0/g' /etc/yum.repos.d/pgdg*.repo \
    && dnf clean all \
    && rm -rf /var/cache/dnf/*

# 3. Install PostgreSQL 16, Mini-PG and Java
RUN dnf -y module reset postgresql \
    && dnf -y module enable postgresql:16 \
    && dnf install -y \
        postgresql-server \
        postgresql-contrib \
        minipg-rpm-package \
        java-21-openjdk-headless \
    && dnf clean all \
    && rm -rf /var/cache/dnf/*

# 4. Create symlinks for pg_ctl and psql (+ eski yolu yönlendir)
RUN mkdir -p /usr/pgsql-16/bin \
 && ln -s /usr/bin/pg_ctl    /usr/pgsql-16/bin/pg_ctl \
 && ln -s /usr/bin/psql      /usr/pgsql-16/bin/psql  \
 && ln -s /usr/pgsql-16      /usr/pgsql-12
# loglarda hâlâ /usr/pgsql-12/bin/pg_ctl bulunamadı hatası alınca ekledim(incelediğimde MiniPGHelper içinden sanırsam)


# 5. Add PG binaries to PATH
ENV PATH=/usr/pgsql-16/bin:$PATH

# 6. Prepare PGDATA directory
ENV PGDATA=/var/lib/pgsql/16/data
RUN mkdir -p "${PGDATA}" \
    && chown -R postgres:postgres "${PGDATA}" \
    && chmod 700 "${PGDATA}"

# 7. Copy Mini-PG application
WORKDIR /app
COPY app/target/minipg-app-*.jar ./minipg-app.jar

# 8. Initialize database at build time
USER postgres
RUN initdb -D "${PGDATA}" \
    && { echo "host all all 0.0.0.0/0 scram-sha-256"; echo "local all all trust"; } \
       >> "${PGDATA}/pg_hba.conf" \
    && echo "listen_addresses='*'" >> "${PGDATA}/postgresql.conf"
USER root

# 9. Entrypoint script
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

EXPOSE 5432 7779
VOLUME ["${PGDATA}"]
HEALTHCHECK --interval=30s --timeout=30s --start-period=5s --retries=3 \
    CMD pg_isready -U postgres

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]