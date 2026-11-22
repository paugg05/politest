-- V2: los 8 ejes y sus traducciones (es, ca, en)

INSERT INTO axis (code, sort_order) VALUES
    ('ECON', 1),
    ('WELFARE', 2),
    ('LIBERTY', 3),
    ('CULTURE', 4),
    ('NATION', 5),
    ('ECOLOGY', 6),
    ('DEMOCRACY', 7),
    ('CHANGE', 8);

INSERT INTO axis_translation (axis_code, locale, name, left_label, right_label) VALUES
    ('ECON', 'es', 'Economía', 'Planificación y propiedad colectiva', 'Mercado y propiedad privada'),
    ('ECON', 'ca', 'Economia', 'Planificació i propietat col·lectiva', 'Mercat i propietat privada'),
    ('ECON', 'en', 'Economy', 'Planning and collective ownership', 'Market and private ownership'),
    ('WELFARE', 'es', 'Bienestar', 'Servicios públicos universales', 'Estado mínimo'),
    ('WELFARE', 'ca', 'Benestar', 'Serveis públics universals', 'Estat mínim'),
    ('WELFARE', 'en', 'Welfare', 'Universal public services', 'Minimal state'),
    ('LIBERTY', 'es', 'Libertades y orden', 'Libertades individuales', 'Autoridad y orden'),
    ('LIBERTY', 'ca', 'Llibertats i ordre', 'Llibertats individuals', 'Autoritat i ordre'),
    ('LIBERTY', 'en', 'Liberty and order', 'Individual liberties', 'Authority and order'),
    ('CULTURE', 'es', 'Cultura', 'Progresismo', 'Tradición'),
    ('CULTURE', 'ca', 'Cultura', 'Progressisme', 'Tradició'),
    ('CULTURE', 'en', 'Culture', 'Progressivism', 'Tradition'),
    ('NATION', 'es', 'Nación', 'Cosmopolitismo', 'Soberanía nacional'),
    ('NATION', 'ca', 'Nació', 'Cosmopolitisme', 'Sobirania nacional'),
    ('NATION', 'en', 'Nation', 'Cosmopolitanism', 'National sovereignty'),
    ('ECOLOGY', 'es', 'Ecología', 'Prioridad al medio ambiente', 'Prioridad a la producción'),
    ('ECOLOGY', 'ca', 'Ecologia', 'Prioritat al medi ambient', 'Prioritat a la producció'),
    ('ECOLOGY', 'en', 'Ecology', 'Environment first', 'Production first'),
    ('DEMOCRACY', 'es', 'Democracia', 'Participación directa', 'Representación y expertos'),
    ('DEMOCRACY', 'ca', 'Democràcia', 'Participació directa', 'Representació i experts'),
    ('DEMOCRACY', 'en', 'Democracy', 'Direct participation', 'Representation and experts'),
    ('CHANGE', 'es', 'Cambio', 'Reforma gradual', 'Transformación radical'),
    ('CHANGE', 'ca', 'Canvi', 'Reforma gradual', 'Transformació radical'),
    ('CHANGE', 'en', 'Change', 'Gradual reform', 'Radical transformation');
