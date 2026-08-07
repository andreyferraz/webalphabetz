package com.alphabetz.webalphabetz.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alphabetz.webalphabetz.service.CareerApplicationService;

@RestController
public class CareerApplicationController {

    private final CareerApplicationService careerApplicationService;

    public CareerApplicationController(CareerApplicationService careerApplicationService) {
        this.careerApplicationService = careerApplicationService;
    }

    @PostMapping(value = "/trabalhe-conosco/candidatura",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> submitApplication(
            @RequestParam("nome") String nome,
            @RequestParam("email") String email,
            @RequestParam("telefone") String telefone,
            @RequestParam("area") String area,
            @RequestParam(name = "formacao", defaultValue = "") String formacao,
            @RequestParam(name = "disponibilidade", defaultValue = "") String disponibilidade,
            @RequestParam(name = "experiencia", defaultValue = "") String experiencia,
            @RequestParam(name = "linkedin", defaultValue = "") String linkedin,
            @RequestParam(name = "consentimento", defaultValue = "false") boolean consentimento,
            @RequestParam("curriculo") MultipartFile curriculo) {
        try {
            careerApplicationService.registerApplication(nome, email, telefone, area,
                    formacao, disponibilidade, experiencia, linkedin, consentimento, curriculo);
            return ResponseEntity.ok(Map.of(
                    "message", "Candidatura registrada com sucesso."));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "message", "Não foi possível registrar a candidatura agora. Tente novamente mais tarde."));
        }
    }
}
