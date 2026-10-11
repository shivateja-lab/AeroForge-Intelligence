CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(40) NOT NULL DEFAULT 'ENGINEER',
    department VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE materials (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    material_code VARCHAR(60) NOT NULL UNIQUE,
    material_name VARCHAR(150) NOT NULL,
    alloy_grade VARCHAR(80) NOT NULL,
    supplier_name VARCHAR(150),
    batch_number VARCHAR(100) NOT NULL,
    thickness_mm DECIMAL(10,3),
    density_g_cm3 DECIMAL(10,4),
    status VARCHAR(40) NOT NULL DEFAULT 'RECEIVED',
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_material_created_by
        FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE weld_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    material_id BIGINT NOT NULL,
    weld_code VARCHAR(80) NOT NULL UNIQUE,
    welding_process VARCHAR(60) NOT NULL DEFAULT 'TIG',
    current_amp DECIMAL(10,3),
    voltage_v DECIMAL(10,3),
    travel_speed_mm_s DECIMAL(10,3),
    heat_input_kj_mm DECIMAL(10,4),
    shielding_gas VARCHAR(80),
    operator_name VARCHAR(120),
    weld_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_weld_material
        FOREIGN KEY (material_id) REFERENCES materials(id)
);

CREATE TABLE repair_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    weld_record_id BIGINT NOT NULL,
    repair_number INT NOT NULL,
    defect_type VARCHAR(120),
    repair_method VARCHAR(150),
    remarks TEXT,
    repaired_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_repair_weld
        FOREIGN KEY (weld_record_id) REFERENCES weld_records(id)
);

CREATE TABLE mechanical_tests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    material_id BIGINT NOT NULL,
    weld_record_id BIGINT,
    test_type VARCHAR(80) NOT NULL,
    tensile_strength_mpa DECIMAL(12,4),
    yield_strength_mpa DECIMAL(12,4),
    elongation_percent DECIMAL(8,4),
    hardness_hv DECIMAL(10,4),
    fracture_toughness DECIMAL(12,4),
    test_temperature_c DECIMAL(10,3),
    tested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_test_material
        FOREIGN KEY (material_id) REFERENCES materials(id),
    CONSTRAINT fk_test_weld
        FOREIGN KEY (weld_record_id) REFERENCES weld_records(id)
);

CREATE TABLE sensor_readings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    weld_record_id BIGINT NOT NULL,
    temperature_c DECIMAL(10,3),
    current_amp DECIMAL(10,3),
    voltage_v DECIMAL(10,3),
    vibration_mm_s DECIMAL(10,4),
    gas_flow_l_min DECIMAL(10,3),
    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sensor_weld
        FOREIGN KEY (weld_record_id) REFERENCES weld_records(id),
    INDEX idx_sensor_recorded_at (recorded_at)
);

CREATE TABLE ml_models (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    model_name VARCHAR(150) NOT NULL,
    model_version VARCHAR(40) NOT NULL,
    algorithm VARCHAR(100) NOT NULL,
    accuracy_score DECIMAL(8,5),
    artifact_path VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT FALSE,
    trained_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_model_version (model_name, model_version)
);

CREATE TABLE predictions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    material_id BIGINT NOT NULL,
    weld_record_id BIGINT,
    model_id BIGINT NOT NULL,
    predicted_tensile_strength_mpa DECIMAL(12,4),
    predicted_yield_strength_mpa DECIMAL(12,4),
    predicted_elongation_percent DECIMAL(8,4),
    defect_probability DECIMAL(8,5),
    risk_level VARCHAR(30) NOT NULL,
    confidence_score DECIMAL(8,5),
    explanation TEXT,
    predicted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prediction_material
        FOREIGN KEY (material_id) REFERENCES materials(id),
    CONSTRAINT fk_prediction_weld
        FOREIGN KEY (weld_record_id) REFERENCES weld_records(id),
    CONSTRAINT fk_prediction_model
        FOREIGN KEY (model_id) REFERENCES ml_models(id)
);

CREATE TABLE alerts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    prediction_id BIGINT,
    weld_record_id BIGINT,
    severity VARCHAR(30) NOT NULL,
    title VARCHAR(180) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    acknowledged_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    CONSTRAINT fk_alert_prediction
        FOREIGN KEY (prediction_id) REFERENCES predictions(id),
    CONSTRAINT fk_alert_weld
        FOREIGN KEY (weld_record_id) REFERENCES weld_records(id),
    CONSTRAINT fk_alert_user
        FOREIGN KEY (acknowledged_by) REFERENCES users(id)
);

CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id BIGINT,
    details TEXT,
    ip_address VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_audit_created_at (created_at)
);