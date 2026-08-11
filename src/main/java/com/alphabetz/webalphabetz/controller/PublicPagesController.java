package com.alphabetz.webalphabetz.controller;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import com.alphabetz.webalphabetz.model.Blog;
import com.alphabetz.webalphabetz.model.Slides;
import com.alphabetz.webalphabetz.model.TurmasImagens;
import com.alphabetz.webalphabetz.service.BlogService;
import com.alphabetz.webalphabetz.service.DepoimentosService;
import com.alphabetz.webalphabetz.service.SlidesService;
import com.alphabetz.webalphabetz.service.TurmasImagensService;

@Controller
public class PublicPagesController {

    private static final String[] APPROACH_PILLARS = { "cuida", "brinca", "interage", "desenvolve" };

    private final SlidesService slidesService;
    private final BlogService blogService;
    private final DepoimentosService depoimentosService;
    private final TurmasImagensService turmasImagensService;

    public PublicPagesController(SlidesService slidesService, BlogService blogService,
            DepoimentosService depoimentosService, TurmasImagensService turmasImagensService) {
        this.slidesService = slidesService;
        this.blogService = blogService;
        this.depoimentosService = depoimentosService;
        this.turmasImagensService = turmasImagensService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("testimonials", depoimentosService.getAllDepoimentos());
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
        Map<String, TurmasImagens> classesByName = new LinkedHashMap<>();
        turmasImagensService.getAll().forEach(classItem ->
                classesByName.putIfAbsent(normalizeClassName(classItem.getNome()), classItem));

        Slides turmasSlide = slidesService.getAllSlides().stream()
                .filter(slide -> slide.getTitulo().trim().toLowerCase(Locale.ROOT).contains("turmas"))
                .findFirst()
                .orElse(null);
        model.addAttribute("classesByName", classesByName);
        model.addAttribute("turmasSlide", turmasSlide);
        return "turmas";
    }

    private String normalizeClassName(String name) {
        return Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }

    @GetMapping("/blog")
    public String blog(Model model) {
        var posts = blogService.getPublicPostsNewestFirst();
        model.addAttribute("posts", posts);
        model.addAttribute("latestPost", posts.isEmpty() ? null : posts.get(0));
        return "blog";
    }

    @GetMapping("/blog/{slug}")
    public String blogPost(@PathVariable String slug, Model model) {
        Blog post = blogService.findPublicPostBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("post", post);
        model.addAttribute("recentPosts", blogService.getLatestPostsExcluding(post.getId(), 4));
        return "blog-post";
    }

    @GetMapping("/trabalhe-conosco")
    public String trabalheConosco() {
        return "trabalhe-conosco";
    }

    @GetMapping("/ouvidoria")
    public String ouvidoria() {
        return "ouvidoria";
    }

    @GetMapping("/contato")
    public String contatoRedirect() {
        return "redirect:/ouvidoria";
    }

}
