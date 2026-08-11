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
import com.alphabetz.webalphabetz.model.FundoTopo;
import com.alphabetz.webalphabetz.model.Slides;
import com.alphabetz.webalphabetz.model.TurmasImagens;
import com.alphabetz.webalphabetz.service.BlogService;
import com.alphabetz.webalphabetz.service.DepoimentosService;
import com.alphabetz.webalphabetz.service.FundoTopoService;
import com.alphabetz.webalphabetz.service.SlidesService;
import com.alphabetz.webalphabetz.service.TurmasImagensService;

@Controller
public class PublicPagesController {

    private static final String[] APPROACH_PILLARS = { "cuida", "brinca", "interage", "desenvolve" };

    private final SlidesService slidesService;
    private final BlogService blogService;
    private final DepoimentosService depoimentosService;
    private final TurmasImagensService turmasImagensService;
    private final FundoTopoService fundoTopoService;

    public PublicPagesController(SlidesService slidesService, BlogService blogService,
            DepoimentosService depoimentosService, TurmasImagensService turmasImagensService,
            FundoTopoService fundoTopoService) {
        this.slidesService = slidesService;
        this.blogService = blogService;
        this.depoimentosService = depoimentosService;
        this.turmasImagensService = turmasImagensService;
        this.fundoTopoService = fundoTopoService;
    }

    @GetMapping("/")
    public String home(Model model) {
        addHeroBackground(model, "inicio", "Página inicial");
        model.addAttribute("classesByName", getClassesByName());
        model.addAttribute("testimonials", depoimentosService.getAllDepoimentos());
        return "index";
    }

    @GetMapping("/escola")
    public String escola(Model model) {
        addHeroBackground(model, "A Escola", "Escola");
        return "escola";
    }

    @GetMapping("/abordagem")
    public String abordagem(Model model) {
        addHeroBackground(model, "Abordagem");
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
        addHeroBackground(model, "Turmas");
        Slides turmasSlide = slidesService.getAllSlides().stream()
                .filter(slide -> slide.getTitulo().trim().toLowerCase(Locale.ROOT).contains("turmas"))
                .findFirst()
                .orElse(null);
        model.addAttribute("classesByName", getClassesByName());
        model.addAttribute("turmasSlide", turmasSlide);
        return "turmas";
    }

    private Map<String, TurmasImagens> getClassesByName() {
        Map<String, TurmasImagens> classesByName = new LinkedHashMap<>();
        turmasImagensService.getAll().forEach(classItem ->
                classesByName.putIfAbsent(normalizeClassName(classItem.getNome()), classItem));
        return classesByName;
    }

    private String normalizeClassName(String name) {
        return normalizeName(name);
    }

    private void addHeroBackground(Model model, String... pageNames) {
        Map<String, FundoTopo> backgroundsByPage = new LinkedHashMap<>();
        fundoTopoService.getAll().forEach(background ->
                backgroundsByPage.putIfAbsent(normalizeName(background.getNomePagina()), background));

        for (String pageName : pageNames) {
            FundoTopo background = backgroundsByPage.get(normalizeName(pageName));
            if (background != null) {
                model.addAttribute("heroBackground", background);
                return;
            }
        }
    }

    private String normalizeName(String name) {
        return Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }

    @GetMapping("/blog")
    public String blog(Model model) {
        addHeroBackground(model, "Blog");
        var posts = blogService.getPublicPostsNewestFirst();
        model.addAttribute("posts", posts);
        model.addAttribute("latestPost", posts.isEmpty() ? null : posts.get(0));
        return "blog";
    }

    @GetMapping("/blog/{slug}")
    public String blogPost(@PathVariable String slug, Model model) {
        addHeroBackground(model, "Blog");
        Blog post = blogService.findPublicPostBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("post", post);
        model.addAttribute("recentPosts", blogService.getLatestPostsExcluding(post.getId(), 4));
        return "blog-post";
    }

    @GetMapping("/trabalhe-conosco")
    public String trabalheConosco(Model model) {
        addHeroBackground(model, "Trabalhe Conosco");
        return "trabalhe-conosco";
    }

    @GetMapping("/ouvidoria")
    public String ouvidoria(Model model) {
        addHeroBackground(model, "Ouvidoria");
        return "ouvidoria";
    }

    @GetMapping("/contato")
    public String contatoRedirect() {
        return "redirect:/ouvidoria";
    }

}
