CREATE INDEX IX_STATUS_AKTOR_ID_TIDSPUNKT
    ON STATUS (AKTOR_ID,TIDSPUNKT);

CREATE INDEX IX_STATUS_STATUS
    ON STATUS (STATUS);