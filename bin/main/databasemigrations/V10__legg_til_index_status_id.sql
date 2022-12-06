CREATE INDEX IX_STATUS_ID
    ON STATUS (ID);


-- Run this update manually
-- UPDATE status
-- SET ferdig = false
-- WHERE id IN (
--     SELECT max(s_inner.id) id
--     FROM STATUS s_inner
--     GROUP BY s_inner.AKTOR_ID
-- )