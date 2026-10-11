ALTER TABLE materials ADD COLUMN evidence_class VARCHAR(40) NOT NULL DEFAULT 'USER_MEASURED';
ALTER TABLE materials ADD COLUMN description TEXT NULL;
ALTER TABLE materials ADD COLUMN image_url VARCHAR(900) NULL;
ALTER TABLE materials ADD COLUMN image_source_url VARCHAR(900) NULL;
ALTER TABLE weld_records ADD COLUMN evidence_class VARCHAR(40) NOT NULL DEFAULT 'USER_MEASURED';
ALTER TABLE weld_records ADD COLUMN source_url VARCHAR(900) NULL;
ALTER TABLE mechanical_tests ADD COLUMN evidence_class VARCHAR(40) NOT NULL DEFAULT 'USER_MEASURED';
ALTER TABLE mechanical_tests ADD COLUMN source_url VARCHAR(900) NULL;

INSERT IGNORE INTO materials
(material_code,material_name,alloy_grade,supplier_name,batch_number,thickness_mm,density_g_cm3,status,evidence_class,description,image_url,image_source_url) VALUES
('REF-2219-01','AA2219-T87 thin-shell reference','AA2219-T87','Published aerospace reference','NOT-A-PHYSICAL-LOT',3.200,2.8400,'REFERENCE','PUBLISHED_REFERENCE','Weldable aluminium-copper alloy used for cryogenic tanks and launch-vehicle shell structures. This record is a learning/reference scenario, not received stock.','https://www.smithsadvanced.com/it/assets/img/products/2219-barra-di-alluminio.jpg','https://www.smithsadvanced.com/it/2219-barra-di-alluminio.htm'),
('REF-7075-01','AA7075-T6 structural reference','AA7075-T6','Published aerospace reference','NOT-A-PHYSICAL-LOT',6.000,2.8100,'REFERENCE','PUBLISHED_REFERENCE','High-strength aluminium-zinc alloy for highly loaded frames and fittings; fusion welding is generally unsuitable for critical structures.','https://www.aerospace-aluminum.com/upload/image/20250326163858_35243.jpeg','https://www.aerospace-aluminum.com/blog/7075-aluminum-block/'),
('REF-TI64-01','Ti-6Al-4V aerospace reference','Ti-6Al-4V Grade 5','Published aerospace reference','NOT-A-PHYSICAL-LOT',5.000,4.4300,'REFERENCE','PUBLISHED_REFERENCE','Alpha-beta titanium alloy with high specific strength, corrosion resistance and broad aerospace structural use.','https://www.onlinetitanium.com/assets/images/resources/Titanium%206Al-4V%20Grade%205.png','https://www.onlinetitanium.com/titanium-6al-4v-grade-5-block'),
('REF-IN718-01','Inconel 718 hot-section reference','Inconel 718','Published aerospace reference','NOT-A-PHYSICAL-LOT',4.000,8.1900,'REFERENCE','PUBLISHED_REFERENCE','Precipitation-hardened nickel superalloy used where strength, oxidation resistance and fatigue performance are needed at elevated temperature.','https://www.nickel-cobaltalloy.com/photo/ps20724264-uns_n07718_inconel_718_nickel_alloy_vim_esr_smelting_process_readily_fabricated.jpg','https://www.nickel-cobaltalloy.com/sale-14176128-uns-n07718-inconel-718-nickel-alloy-vim-esr-smelting-process-readily-fabricated.html'),
('REF-304L-01','AISI 304L cryogenic reference','AISI 304L','Published NIST/NASA reference','NOT-A-PHYSICAL-LOT',2.000,8.0000,'REFERENCE','PUBLISHED_REFERENCE','Low-carbon austenitic stainless steel with good weldability and useful cryogenic toughness for tanks, tubing and plumbing.','https://www.antonmetal.com/uploads/304L-stainless-steel-plate.jpg','https://www.antonmetal.com/304304l-uns-s30400s30403-is-the-most-widely-utilized-18-8-chromium-nickel-austenitic-stainless-steel-it-is-an-economical-and-versatile-corrosion-resistant-alloy-suitable-for-a-w-product/'),
('REF-2195-01','AA2195-T8 tank reference','AA2195-T8','Published NASA literature','NOT-A-PHYSICAL-LOT',3.000,2.7100,'REFERENCE','PUBLISHED_REFERENCE','Low-density aluminium-lithium alloy developed for lightweight welded cryogenic launch-vehicle tanks.','https://scx2.b-cdn.net/gfx/news/hires/nasaspinsman.jpg','https://phys.org/news/2010-03-nasa-technology-forge-tank-hardware.html');

INSERT IGNORE INTO weld_records
(material_id,weld_code,welding_process,current_amp,voltage_v,travel_speed_mm_s,heat_input_kj_mm,shielding_gas,operator_name,evidence_class,source_url)
SELECT id,'REF-WELD-2219-A','TIG',145,13.5,4.2,0.3729,'Argon 99.999%','Published parameter scenario','PUBLISHED_REFERENCE','https://www.isro.gov.in/CapacityBuildingProgramme.html' FROM materials WHERE material_code='REF-2219-01';
INSERT IGNORE INTO weld_records
(material_id,weld_code,welding_process,current_amp,voltage_v,travel_speed_mm_s,heat_input_kj_mm,shielding_gas,operator_name,evidence_class,source_url)
SELECT id,'REF-WELD-304L-A','TIG',120,12.0,3.5,0.3291,'Argon 99.999%','Published parameter scenario','PUBLISHED_REFERENCE','https://trc.nist.gov/cryogenics/materials/materialproperties.htm' FROM materials WHERE material_code='REF-304L-01';

INSERT INTO mechanical_tests
(material_id,weld_record_id,test_type,tensile_strength_mpa,yield_strength_mpa,elongation_percent,test_temperature_c,evidence_class,source_url)
SELECT m.id,NULL,'PUBLISHED_NOMINAL_BASELINE',c.nominal_tensile_mpa,c.nominal_yield_mpa,c.nominal_elongation_percent,23,'PUBLISHED_REFERENCE',c.source_url
FROM materials m JOIN material_reference_catalogue c ON c.alloy_grade=m.alloy_grade
WHERE m.material_code IN ('REF-2219-01','REF-7075-01','REF-TI64-01','REF-IN718-01','REF-304L-01','REF-2195-01')
AND NOT EXISTS (SELECT 1 FROM mechanical_tests t WHERE t.material_id=m.id AND t.test_type='PUBLISHED_NOMINAL_BASELINE');

INSERT INTO alerts(prediction_id,weld_record_id,severity,title,message,status)
SELECT NULL,id,'WARNING','Reference scenario requires HAZ measurement','The AA2219 TIG scenario has no measured HAZ morphology record. Capture macrograph width before using it for engineering comparison.','OPEN'
FROM weld_records w WHERE w.weld_code='REF-WELD-2219-A' AND NOT EXISTS (SELECT 1 FROM alerts a WHERE a.title='Reference scenario requires HAZ measurement');
INSERT INTO alerts(prediction_id,weld_record_id,severity,title,message,status)
SELECT NULL,id,'WARNING','Published baseline is not lot acceptance data','The 304L values are published nominal references, not a mill certificate or laboratory result for a received batch.','OPEN'
FROM weld_records w WHERE w.weld_code='REF-WELD-304L-A' AND NOT EXISTS (SELECT 1 FROM alerts a WHERE a.title='Published baseline is not lot acceptance data');
