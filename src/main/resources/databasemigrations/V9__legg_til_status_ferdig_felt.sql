ALTER TABLE status
ADD COLUMN ferdig BOOLEAN NOT NULL DEFAULT true;


UPDATE status
SET ferdig = false
WHERE id IN (
    SELECT id
    FROM STATUS s INNER JOIN
         (
             SELECT max(s_inner.TIDSPUNKT) tidspunkt, s_inner.AKTOR_ID
             FROM STATUS s_inner
             GROUP BY s_inner.AKTOR_ID
         ) as s2 on s2.TIDSPUNKT = s.TIDSPUNKT AND s2.AKTOR_ID = s.AKTOR_ID
)
