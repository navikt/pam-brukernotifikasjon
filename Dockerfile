FROM navikt/java:11
COPY scripts/init-secrets.sh /init-scripts/init-secrets.sh
COPY build/libs/pam-brukernotifikasjon-*-all.jar ./app.jar