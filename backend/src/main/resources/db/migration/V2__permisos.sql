-- Catálogo de permisos que se pueden asignar a los roles de una comunidad.
INSERT INTO permissions (name, description) VALUES
    ('EDIT_POST',                 'Editar publicaciones de otros miembros'),
    ('DELETE_POST',               'Borrar cualquier publicación'),
    ('EDIT_COMMENTS',             'Editar comentarios de otros miembros'),
    ('DELETE_COMMENTS',           'Borrar cualquier comentario'),
    ('ASSIGN_ROLES',              'Crear roles y asignarlos a los miembros'),
    ('ASSIGN_TITLES',             'Crear títulos y asignarlos a los miembros'),
    ('BAN_USER',                  'Expulsar miembros de la comunidad'),
    ('MANAGE_COMMUNITY_SETTINGS', 'Editar la información de la comunidad');
