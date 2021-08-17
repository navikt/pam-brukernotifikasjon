ALTER TABLE status
    ADD COLUMN ferdig BOOLEAN NOT NULL DEFAULT true;

-- Run this update manually
-- UPDATE status
-- SET ferdig = false
-- WHERE id IN (
--     SELECT max(s_inner.id) id
--     FROM STATUS s_inner
--     GROUP BY s_inner.AKTOR_ID
-- )

