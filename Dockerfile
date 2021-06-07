FROM navikt/java:11
COPY scripts/init_secrets.sh /init-scripts/init_secrets.sh
COPY build/libs/pam-brukernotifikasjon-0.1.jar ./app.jar
