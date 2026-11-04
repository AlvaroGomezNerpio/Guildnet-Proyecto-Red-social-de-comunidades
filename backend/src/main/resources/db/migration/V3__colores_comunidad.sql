-- Paleta propia de cada comunidad: color principal y color de acento del degradado.
-- Las comunidades existentes reciben los colores de GuildNet.
ALTER TABLE communities ADD COLUMN primary_color VARCHAR(7) NOT NULL DEFAULT '#7c3aed';
ALTER TABLE communities ADD COLUMN accent_color VARCHAR(7) NOT NULL DEFAULT '#06b6d4';
