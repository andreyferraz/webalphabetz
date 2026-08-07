package com.alphabetz.webalphabetz.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alphabetz.webalphabetz.service.OuvidoriaService;

@RestController
public class OuvidoriaController {

    private final OuvidoriaService ouvidoriaService;

    public OuvidoriaController(OuvidoriaService ouvidoriaService) {
        this.ouvidoriaService = ouvidoriaService;
    }

    @PostMapping(value = "/ouvidoria/manifestacoes",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> submit(
            @RequestParam String nome,
            @RequestParam String sobrenome,
            @RequestParam String email,
            @RequestParam String celular,
            @RequestParam String perfil,
            @RequestParam String areaAtuacao,
            @RequestParam String assunto,
            @RequestParam String detalhes,
            @RequestParam(name = "receberRetorno", defaultValue = "false") boolean receberRetorno,
            @RequestParam(name = "arquivo", required = false) MultipartFile arquivo,
            @RequestParam(name = "documentoImagem", required = false) MultipartFile documentoImagem) {
        try {
            ouvidoriaService.register(nome, sobrenome, email, celular, perfil, areaAtuacao,
                    assunto, detalhes, receberRetorno, arquivo, documentoImagem);
            return ResponseEntity.ok(Map.of("message", "Manifestação registrada com sucesso."));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "message", "Não foi possível registrar a manifestação agora. Tente novamente mais tarde."));
        }
    }
}
