package com.aeroforge.intelligence.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.aeroforge.intelligence.dto.MaterialRequest;
import com.aeroforge.intelligence.dto.MechanicalTestRequest;
import com.aeroforge.intelligence.dto.PredictionRequest;
import com.aeroforge.intelligence.dto.WeldRequest;
import com.aeroforge.intelligence.service.EngineeringService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class EngineeringController {
    private final EngineeringService engineering;
    public EngineeringController(EngineeringService engineering) { this.engineering = engineering; }

    @GetMapping("/dashboard") public Map<String,Object> dashboard(){ return engineering.dashboard(); }
    @GetMapping("/materials") public List<Map<String,Object>> materials(){ return engineering.materials(); }
    @GetMapping("/reference-materials") public List<Map<String,Object>> referenceMaterials(){ return engineering.referenceMaterials(); }
    @PostMapping("/materials") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> createMaterial(@Valid @RequestBody MaterialRequest r, Principal p){ return engineering.createMaterial(r,p.getName()); }
    @GetMapping("/welds") public List<Map<String,Object>> welds(){ return engineering.welds(); }
    @PostMapping("/welds") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> createWeld(@Valid @RequestBody WeldRequest r){ return engineering.createWeld(r); }
    @GetMapping("/predictions") public List<Map<String,Object>> predictions(){ return engineering.predictions(); }
    @PostMapping("/predictions") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> predict(@Valid @RequestBody PredictionRequest r){ return engineering.predict(r); }
    @GetMapping("/mechanical-tests") public List<Map<String,Object>> tests(){ return engineering.mechanicalTests(); }
    @PostMapping("/mechanical-tests") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> createTest(@Valid @RequestBody MechanicalTestRequest r){ return engineering.createMechanicalTest(r); }
    @GetMapping("/alerts") public List<Map<String,Object>> alerts(){ return engineering.alerts(); }
    @PatchMapping("/alerts/{id}/acknowledge")
    public Map<String,Object> acknowledge(@PathVariable Long id, Principal p){ return engineering.acknowledgeAlert(id,p.getName()); }
}
