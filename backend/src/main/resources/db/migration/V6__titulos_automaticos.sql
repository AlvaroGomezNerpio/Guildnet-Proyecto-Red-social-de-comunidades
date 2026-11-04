-- Títulos automáticos: un título puede tener una regla ("al llegar a 10 publicaciones...")
-- y se asigna solo a los miembros que la cumplen. Sin regla, el título es manual.
ALTER TABLE titles ADD COLUMN auto_metric VARCHAR(20) NULL;
ALTER TABLE titles ADD COLUMN auto_threshold INT NULL;
