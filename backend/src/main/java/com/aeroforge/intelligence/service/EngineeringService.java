package com.aeroforge.intelligence.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.aeroforge.intelligence.dto.MaterialRequest;
import com.aeroforge.intelligence.dto.MechanicalTestRequest;
import com.aeroforge.intelligence.dto.PredictionRequest;
import com.aeroforge.intelligence.dto.WeldRequest;
import com.aeroforge.intelligence.repository.UserRepository;

@Service
public class EngineeringService {
    private final JdbcTemplate jdbc;
    private final UserRepository users;

    public EngineeringService(JdbcTemplate jdbc, UserRepository users) {
        this.jdbc = jdbc;
        this.users = users;
    }

    public List<Map<String, Object>> materials() {
        return jdbc.queryForList("SELECT * FROM materials ORDER BY created_at DESC");
    }

    public List<Map<String, Object>> referenceMaterials() {
        return jdbc.queryForList("SELECT * FROM material_reference_catalogue ORDER BY material_family, alloy_grade");
    }

    @Transactional
    public Map<String, Object> createMaterial(MaterialRequest r, String email) {
        Long userId = users.findByEmailIgnoreCase(email).orElseThrow().getId();
        KeyHolder key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                INSERT INTO materials(material_code, material_name, alloy_grade, supplier_name,
                  batch_number, thickness_mm, density_g_cm3, status, created_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, r.materialCode()); ps.setString(2, r.materialName());
            ps.setString(3, r.alloyGrade()); ps.setString(4, r.supplierName());
            ps.setString(5, r.batchNumber()); ps.setBigDecimal(6, r.thicknessMm());
            ps.setBigDecimal(7, r.densityGCm3());
            ps.setString(8, r.status() == null ? "RECEIVED" : r.status()); ps.setLong(9, userId);
            return ps;
        }, key);
        return one("SELECT * FROM materials WHERE id = ?", key.getKey().longValue());
    }

    public List<Map<String, Object>> welds() {
        return jdbc.queryForList("""
            SELECT w.*, m.material_code, m.alloy_grade FROM weld_records w
            JOIN materials m ON m.id=w.material_id ORDER BY w.weld_timestamp DESC
            """);
    }

    @Transactional
    public Map<String, Object> createWeld(WeldRequest r) {
        require("materials", r.materialId());
        BigDecimal heat = r.heatInputKjMm();
        if (heat == null) {
            heat = r.currentAmp().multiply(r.voltageV()).multiply(new BigDecimal("0.8"))
                .divide(r.travelSpeedMmS().multiply(new BigDecimal("1000")), 4, RoundingMode.HALF_UP);
        }
        final BigDecimal calculatedHeat = heat;
        KeyHolder key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                INSERT INTO weld_records(material_id,weld_code,welding_process,current_amp,voltage_v,
                  travel_speed_mm_s,heat_input_kj_mm,shielding_gas,operator_name)
                VALUES (?,?,?,?,?,?,?,?,?)
                """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, r.materialId()); ps.setString(2, r.weldCode());
            ps.setString(3, r.weldingProcess() == null ? "TIG" : r.weldingProcess());
            ps.setBigDecimal(4, r.currentAmp()); ps.setBigDecimal(5, r.voltageV());
            ps.setBigDecimal(6, r.travelSpeedMmS()); ps.setBigDecimal(7, calculatedHeat);
            ps.setString(8, r.shieldingGas()); ps.setString(9, r.operatorName());
            return ps;
        }, key);
        return one("SELECT * FROM weld_records WHERE id = ?", key.getKey().longValue());
    }

    public List<Map<String, Object>> predictions() {
        return jdbc.queryForList("""
            SELECT p.*, m.material_code, w.weld_code FROM predictions p
            JOIN materials m ON m.id=p.material_id
            LEFT JOIN weld_records w ON w.id=p.weld_record_id ORDER BY p.predicted_at DESC
            """);
    }

    public List<Map<String, Object>> mechanicalTests() {
        return jdbc.queryForList("""
            SELECT t.*, m.material_code, m.alloy_grade, w.weld_code FROM mechanical_tests t
            JOIN materials m ON m.id=t.material_id
            LEFT JOIN weld_records w ON w.id=t.weld_record_id ORDER BY t.tested_at DESC
            """);
    }

    @Transactional
    public Map<String, Object> createMechanicalTest(MechanicalTestRequest r) {
        require("materials", r.materialId());
        if (r.weldRecordId() != null) require("weld_records", r.weldRecordId());
        KeyHolder key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                INSERT INTO mechanical_tests(material_id,weld_record_id,test_type,tensile_strength_mpa,
                  yield_strength_mpa,elongation_percent,hardness_hv,fracture_toughness,test_temperature_c)
                VALUES (?,?,?,?,?,?,?,?,?)
                """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, r.materialId());
            if (r.weldRecordId() == null) ps.setNull(2, java.sql.Types.BIGINT); else ps.setLong(2, r.weldRecordId());
            ps.setString(3, r.testType()); ps.setBigDecimal(4, r.tensileStrengthMpa());
            ps.setBigDecimal(5, r.yieldStrengthMpa()); ps.setBigDecimal(6, r.elongationPercent());
            if (r.hardnessHv() == null) ps.setNull(7, java.sql.Types.DECIMAL); else ps.setBigDecimal(7, r.hardnessHv());
            if (r.fractureToughness() == null) ps.setNull(8, java.sql.Types.DECIMAL); else ps.setBigDecimal(8, r.fractureToughness());
            if (r.testTemperatureC() == null) ps.setNull(9, java.sql.Types.DECIMAL); else ps.setBigDecimal(9, r.testTemperatureC());
            return ps;
        }, key);
        return one("SELECT * FROM mechanical_tests WHERE id = ?", key.getKey().longValue());
    }

    public List<Map<String, Object>> alerts() {
        return jdbc.queryForList("""
            SELECT a.*, p.risk_level, p.defect_probability, m.material_code, w.weld_code
            FROM alerts a LEFT JOIN predictions p ON p.id=a.prediction_id
            LEFT JOIN materials m ON m.id=p.material_id
            LEFT JOIN weld_records w ON w.id=a.weld_record_id ORDER BY a.created_at DESC
            """);
    }

    @Transactional
    public Map<String, Object> acknowledgeAlert(Long id, String email) {
        Long userId = users.findByEmailIgnoreCase(email).orElseThrow().getId();
        int changed = jdbc.update("UPDATE alerts SET status='ACKNOWLEDGED', acknowledged_by=? WHERE id=? AND status='OPEN'", userId, id);
        if (changed == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Open alert not found");
        return one("SELECT * FROM alerts WHERE id=?", id);
    }

    @Transactional
    public Map<String, Object> predict(PredictionRequest r) {
        Map<String, Object> material = one("SELECT * FROM materials WHERE id = ?", r.materialId());
        Map<String, Object> weld = r.weldRecordId() == null ? Map.of() :
            one("SELECT * FROM weld_records WHERE id = ? AND material_id = ?", r.weldRecordId(), r.materialId());
        jdbc.update("""
            INSERT IGNORE INTO ml_models(model_name,model_version,algorithm,accuracy_score,active,trained_at)
            VALUES ('AeroForge Property Predictor','2.0.0','Traceable rule-based screening model',NULL,TRUE,CURRENT_TIMESTAMP)
            """);
        Long modelId = jdbc.queryForObject("SELECT id FROM ml_models WHERE model_name=? AND model_version=?",
            Long.class, "AeroForge Property Predictor", "2.0.0");
        String alloy = String.valueOf(material.get("alloy_grade"));
        Map<String,Object> reference = one("SELECT nominal_tensile_mpa, nominal_yield_mpa, nominal_elongation_percent FROM material_reference_catalogue WHERE alloy_grade=?", alloy);
        double base = number(reference.get("nominal_tensile_mpa"), 455.0);
        double baseYield = number(reference.get("nominal_yield_mpa"), base * .78);
        double baseElongation = number(reference.get("nominal_elongation_percent"), 10.0);
        double heat = number(weld.get("heat_input_kj_mm"), 0.60);
        double haz = r.hazWidthMm() == null ? 2.4 : r.hazWidthMm();
        double repairs = r.repairCount();
        double tensile = clamp(base - Math.abs(heat - .55) * 42 - repairs * 13 - Math.max(0, haz - 3) * 5, 100, 1200);
        double retained = tensile / base;
        double yield = clamp(baseYield * retained, 80, 2000);
        double elongation = clamp(baseElongation * retained - repairs * .45 - Math.max(0, haz - 3) * .2, 1, 50);
        double defect = clamp(.045 + Math.abs(heat - .55) * .24 + repairs * .11 + Math.max(0, haz - 3) * .035, .01, .98);
        String risk = defect >= .55 ? "HIGH" : defect >= .25 ? "MEDIUM" : "LOW";
        double confidence = clamp(.82 - repairs * .035 - Math.abs(heat - .55) * .06, .55, .84);
        String explanation = "Screening estimate starts from the source-linked " + alloy + " nominal baseline (" + base
            + " MPa tensile), then applies explicit TIG heat-input, HAZ-width and " + repairs
            + " repair penalty terms. Confidence represents rule coverage, not validated ML accuracy; laboratory validation is mandatory.";
        KeyHolder key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                INSERT INTO predictions(material_id,weld_record_id,model_id,predicted_tensile_strength_mpa,
                  predicted_yield_strength_mpa,predicted_elongation_percent,defect_probability,risk_level,
                  confidence_score,explanation) VALUES (?,?,?,?,?,?,?,?,?,?)
                """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, r.materialId());
            if (r.weldRecordId() == null) ps.setNull(2, java.sql.Types.BIGINT); else ps.setLong(2, r.weldRecordId());
            ps.setLong(3, modelId); ps.setDouble(4, round(tensile)); ps.setDouble(5, round(yield));
            ps.setDouble(6, round(elongation)); ps.setDouble(7, round(defect)); ps.setString(8, risk);
            ps.setDouble(9, round(confidence)); ps.setString(10, explanation); return ps;
        }, key);
        if ("HIGH".equals(risk)) {
            jdbc.update("INSERT INTO alerts(prediction_id,weld_record_id,severity,title,message) VALUES (?,?,?,?,?)",
                key.getKey().longValue(), r.weldRecordId(), "CRITICAL", "High material risk detected", explanation);
        }
        return one("SELECT * FROM predictions WHERE id = ?", key.getKey().longValue());
    }

    public Map<String, Object> dashboard() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("materials", count("materials")); data.put("welds", count("weld_records"));
        data.put("predictions", count("predictions")); data.put("openAlerts",
            jdbc.queryForObject("SELECT COUNT(*) FROM alerts WHERE status='OPEN'", Long.class));
        data.put("highRisk", jdbc.queryForObject("SELECT COUNT(*) FROM predictions WHERE risk_level='HIGH'", Long.class));
        data.put("tests", count("mechanical_tests"));
        data.put("referenceMaterials", count("material_reference_catalogue"));
        data.put("averageConfidence", jdbc.queryForObject("SELECT COALESCE(AVG(confidence_score),0) FROM predictions", Double.class));
        return data;
    }

    private long count(String table) { return jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Long.class); }
    private void require(String table, Long id) { if (countFor("SELECT COUNT(*) FROM " + table + " WHERE id=?", id) == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found"); }
    private int countFor(String sql, Object... args) { return jdbc.queryForObject(sql, Integer.class, args); }
    private Map<String,Object> one(String sql, Object... args) { List<Map<String,Object>> rows=jdbc.queryForList(sql,args); if(rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Record not found"); return rows.getFirst(); }
    private double number(Object value, double fallback) { return value instanceof Number n ? n.doubleValue() : fallback; }
    private double clamp(double v,double min,double max){ return Math.max(min,Math.min(max,v)); }
    private double round(double v){ return Math.round(v*10000.0)/10000.0; }
}
