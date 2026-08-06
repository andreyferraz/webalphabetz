package com.alphabetz.webalphabetz.controller;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.alphabetz.webalphabetz.model.Slides;
import com.alphabetz.webalphabetz.service.SlidesService;

@Controller
public class PublicPagesController {

    private static final String[] APPROACH_PILLARS = { "cuida", "brinca", "interage", "desenvolve" };

    private final SlidesService slidesService;

    public PublicPagesController(SlidesService slidesService) {
        this.slidesService = slidesService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/escola")
    public String escola() {
        return "escola";
    }

    @GetMapping("/abordagem")
    public String abordagem(Model model) {
        Map<String, Slides> slidesByTitle = new LinkedHashMap<>();
        slidesService.getAllSlides().forEach(slide -> {
            String normalizedTitle = slide.getTitulo().trim().toLowerCase(Locale.ROOT);
            for (String pillar : APPROACH_PILLARS) {
                if (normalizedTitle.equals(pillar) || normalizedTitle.startsWith(pillar + " ")) {
                    slidesByTitle.putIfAbsent(pillar, slide);
                    break;
                }
            }
        });
        model.addAttribute("slidesByTitle", slidesByTitle);
        return "abordagem";
    }

    @GetMapping("/turmas")
    public String turmas(Model model) {
        Slides turmasSlide = slidesService.getAllSlides().stream()
                .filter(slide -> slide.getTitulo().trim().toLowerCase(Locale.ROOT).contains("turmas"))
                .findFirst()
                .orElse(null);
        model.addAttribute("turmasSlide", turmasSlide);
        return "turmas";
    }

    @GetMapping("/blog")
    public String blog() {
        return "blog";
    }

    @GetMapping("/trabalhe-conosco")
    public String trabalheConosco() {
        return "trabalhe-conosco";
    }

    @GetMapping("/contato")
    public String contato() {
        return "contato";
    }

}
