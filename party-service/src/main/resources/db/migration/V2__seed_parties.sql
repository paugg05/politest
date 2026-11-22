-- Datos PROVISIONALES: posiciones estimadas por eje, con fuente y nivel de confianza.
-- Los programas oficiales de las elecciones del 29-N-2026 aún no están publicados.
-- right_pct: 0 = polo izquierdo del eje, 100 = polo derecho. confidence: 1 baja, 2 media, 3 alta.

INSERT INTO party (code, name, color, sort_order) VALUES
    ('PP', 'Partido Popular', '#0b5cad', 1),
    ('PSOE', 'PSOE', '#c8102e', 2),
    ('VOX', 'Vox', '#3f7d20', 3),
    ('FRENTE_AMPLIO', 'Frente Amplio', '#c2255c', 4),
    ('PODEMOS', 'Podemos', '#6f42c1', 5);

INSERT INTO party_position (party_code, axis_code, right_pct, confidence, note, source_url) VALUES
    ('PP', 'ECON', 80, 2, 'Propone revisar más de 100 subidas de impuestos y bajar impuestos para incentivar el ahorro, el trabajo y la actividad empresarial.', 'https://www.moncloa.com/2026/10/19/feijoo-espana-3451414/'),
    ('PP', 'WELFARE', 62, 2, 'Mantiene las pensiones indexadas al IPC y propone reforzar la plantilla sanitaria, junto con menos presión fiscal y hacer el trabajo más rentable que los subsidios.', 'https://www.moncloa.com/2026/10/19/feijoo-espana-3451414/'),
    ('PP', 'LIBERTY', 68, 2, 'Propone más medios y autoridad para las fuerzas de seguridad, desalojo de ocupaciones en 48 horas y endurecimiento de la política migratoria.', 'https://www.moncloa.com/2026/10/19/feijoo-espana-3451414/'),
    ('PP', 'CULTURE', 66, 1, 'Propone reforzar la autoridad del profesorado y combatir el adoctrinamiento en las aulas. El resumen consultado no cubre otros asuntos de valores: estimación.', 'https://www.moncloa.com/2026/10/19/feijoo-espana-3451414/'),
    ('PP', 'NATION', 66, 2, 'Defiende la soberanía y la integridad territorial, endurece los requisitos de nacionalidad y rechaza las regularizaciones colectivas, manteniendo las alianzas con la UE y Latinoamérica.', 'https://www.moncloa.com/2026/10/19/feijoo-espana-3451414/'),
    ('PP', 'ECOLOGY', 68, 1, 'El resumen consultado de su contrato con España no incluye propuestas climáticas; estimación a partir de su énfasis en vivienda, suelo e industria.', 'https://www.moncloa.com/2026/10/19/feijoo-espana-3451414/'),
    ('PP', 'DEMOCRACY', 78, 2, 'Centra su propuesta en reforzar instituciones: independencia de la Fiscalía y de organismos reguladores y presupuestos anuales. No plantea mecanismos de democracia directa.', 'https://www.moncloa.com/2026/10/19/feijoo-espana-3451414/'),
    ('PP', 'CHANGE', 35, 1, 'Propone reformas dentro del marco institucional vigente (justicia, gasto público). Estimación.', 'https://www.moncloa.com/2026/10/19/feijoo-espana-3451414/'),
    ('PSOE', 'ECON', 38, 2, 'Interviene de forma moderada en vivienda: límites a las subidas del alquiler y a los fondos de inversión, vivienda pública y avales, dentro de una economía de mercado regulada.', 'https://es.euronews.com/2026/10/10/sanchez-abre-la-campana-electoral-del-29n-en-su-ciudad-talisman-con-la-vivienda-como-eje-p'),
    ('PSOE', 'WELFARE', 28, 2, 'Línea de crédito ICO de 10.000 millones para vivienda, deducción por alquiler y financiación de vivienda social: refuerzo del papel del Estado en vivienda.', 'https://es.euronews.com/2026/10/10/sanchez-abre-la-campana-electoral-del-29n-en-su-ciudad-talisman-con-la-vivienda-como-eje-p'),
    ('PSOE', 'LIBERTY', 40, 1, 'Estimación a partir de la trayectoria del partido; pendiente de contrastar con su programa de 2026.', NULL),
    ('PSOE', 'CULTURE', 28, 1, 'Estimación a partir de la trayectoria del Gobierno (por ejemplo, ley de eutanasia y ley trans); pendiente de contrastar con su programa de 2026.', NULL),
    ('PSOE', 'NATION', 35, 1, 'Estimación: posición proeuropea y multilateral; pendiente de contrastar con su programa de 2026.', NULL),
    ('PSOE', 'ECOLOGY', 38, 1, 'Estimación: transición ecológica compatible con la industria; pendiente de contrastar con su programa de 2026.', NULL),
    ('PSOE', 'DEMOCRACY', 66, 1, 'Estimación: defiende el marco representativo e institucional vigente.', NULL),
    ('PSOE', 'CHANGE', 25, 1, 'Estimación: reformismo gradual desde las instituciones.', NULL),
    ('VOX', 'ECON', 85, 2, 'Su programa de 2023 proponía una amplia reforma fiscal con bajadas de impuestos e IVA cero en bienes de primera necesidad.', 'https://www.elindependiente.com/espana/2023/07/07/vox-propone-una-amplia-reforma-fiscal-tras-el-23-j-e-iva-cero-en-bienes-de-primera-necesidad/amp/'),
    ('VOX', 'WELFARE', 70, 1, 'Defiende menos impuestos y menos gasto, pero promete mantener el pago de las pensiones. Posición mixta: estimación.', 'https://www.democrata.es/elecciones-generales-espana/abascal-elecciones-trampas-psoe-pensiones/'),
    ('VOX', 'LIBERTY', 88, 2, 'La inmigración y la seguridad son ejes centrales de su campaña.', 'https://www.politicaelectoral.com/es/espana/generales-2026/nacional'),
    ('VOX', 'CULTURE', 92, 1, 'Estimación a partir de su trayectoria en valores tradicionales; pendiente de contrastar con su programa de 2026.', NULL),
    ('VOX', 'NATION', 93, 2, 'Advierte contra las regularizaciones de inmigrantes y sitúa la inmigración como eje central.', 'https://www.ultimahora.es/noticias/comunidades/2026/01/29/2559193/abascal-vox-alerta-seran-muchos-mas-inmigrantes-regularizados-despues-vendran-millones.html'),
    ('VOX', 'ECOLOGY', 90, 1, 'Estimación a partir de su oposición a las políticas climáticas vigentes; pendiente de contrastar con su programa de 2026.', NULL),
    ('VOX', 'DEMOCRACY', 60, 1, 'Posición ambigua: discurso de liderazgo fuerte, pero actúa dentro del sistema representativo. Estimación.', NULL),
    ('VOX', 'CHANGE', 72, 1, 'Estimación: plantea reformas profundas del modelo de Estado; pendiente de contrastar con su programa de 2026.', NULL),
    ('FRENTE_AMPLIO', 'ECON', 18, 2, 'Defiende intervenir el mercado del alquiler y reforzar los servicios públicos.', 'https://www.politicaelectoral.com/es/espana/generales-2026/nacional'),
    ('FRENTE_AMPLIO', 'WELFARE', 12, 2, 'Declara como principio la defensa de los servicios públicos y el derecho a la vivienda.', 'https://www.publico.es/politica/acuerdo-frente-amplio-elecciones-29n-directo-ultima-hora-negociaciones-reacciones.html'),
    ('FRENTE_AMPLIO', 'LIBERTY', 22, 1, 'Estimación a partir de la trayectoria del partido; pendiente de contrastar con su programa de 2026.', NULL),
    ('FRENTE_AMPLIO', 'CULTURE', 12, 2, 'Declara el feminismo como uno de sus principios.', 'https://www.publico.es/politica/acuerdo-frente-amplio-elecciones-29n-directo-ultima-hora-negociaciones-reacciones.html'),
    ('FRENTE_AMPLIO', 'NATION', 25, 1, 'Declara el pacifismo como principio; el resto es una estimación.', 'https://www.publico.es/politica/acuerdo-frente-amplio-elecciones-29n-directo-ultima-hora-negociaciones-reacciones.html'),
    ('FRENTE_AMPLIO', 'ECOLOGY', 15, 2, 'Declara el ecologismo como uno de sus principios.', 'https://www.publico.es/politica/acuerdo-frente-amplio-elecciones-29n-directo-ultima-hora-negociaciones-reacciones.html'),
    ('FRENTE_AMPLIO', 'DEMOCRACY', 35, 1, 'Estimación: coalición de partidos y movimientos cuyas bases votaron la alianza en consultas internas (Más Madrid: 86,69 %).', 'https://www.publico.es/politica/acuerdo-frente-amplio-elecciones-29n-directo-ultima-hora-negociaciones-reacciones.html'),
    ('FRENTE_AMPLIO', 'CHANGE', 55, 1, 'Estimación: plantea transformaciones en vivienda y servicios dentro del marco institucional.', 'https://www.publico.es/politica/acuerdo-frente-amplio-elecciones-29n-directo-ultima-hora-negociaciones-reacciones.html'),
    ('PODEMOS', 'ECON', 12, 2, 'Defiende la expropiación y el control de precios en vivienda, limitar los precios de bienes básicos y un salario mínimo de 1.800 euros.', 'https://es.ara.cat/politica/irene-montero-presenta-candidatura-generales-gente-buena_1_5848007.html'),
    ('PODEMOS', 'WELFARE', 8, 2, 'Propone una jornada laboral de 30 horas semanales y bajo el lema «Paz, casa y trabajo» defiende un fuerte papel público.', 'https://es.ara.cat/politica/irene-montero-presenta-candidatura-generales-gente-buena_1_5848007.html'),
    ('PODEMOS', 'LIBERTY', 25, 1, 'Estimación a partir de la trayectoria del partido; pendiente de contrastar con su programa de 2026.', NULL),
    ('PODEMOS', 'CULTURE', 10, 2, 'Propone armonizar la enseñanza en toda España con contenidos sobre sexualidad, género y derechos LGBTI (propuesta presentada en su acto de candidatura).', 'https://es.ara.cat/politica/irene-montero-presenta-candidatura-generales-gente-buena_1_5848007.html'),
    ('PODEMOS', 'NATION', 28, 2, 'Rechaza culpar a las personas migrantes de los problemas sociales; lema «Paz».', 'https://es.ara.cat/politica/irene-montero-presenta-candidatura-generales-gente-buena_1_5848007.html'),
    ('PODEMOS', 'ECOLOGY', 17, 1, 'Estimación a partir de la trayectoria del partido; pendiente de contrastar con su programa de 2026.', NULL),
    ('PODEMOS', 'DEMOCRACY', 32, 1, 'Estimación: elige candidaturas mediante primarias abiertas.', 'https://es.ara.cat/politica/irene-montero-presenta-candidatura-generales-gente-buena_1_5848007.html'),
    ('PODEMOS', 'CHANGE', 78, 2, 'Plantea medidas de ruptura con el modelo actual, como la expropiación y el control de precios.', 'https://es.ara.cat/politica/irene-montero-presenta-candidatura-generales-gente-buena_1_5848007.html');

INSERT INTO dataset_info (id, version, status, note, updated_on) VALUES
    (1, '2026-10-21', 'provisional', 'Posiciones estimadas a partir de propuestas publicadas en prensa y de la trayectoria de cada partido. A revisar cuando se publiquen los programas oficiales y tras la proclamación de candidaturas (2-11-2026).', '2026-10-21');
