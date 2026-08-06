package com.alphabetz.webalphabetz.controller;

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
import com.alphabetz.webalphabetz.service.BlogService;
import com.alphabetz.webalphabetz.service.SlidesService;

@Controller
public class PublicPagesController {

    private static final String[] APPROACH_PILLARS = { "cuida", "brinca", "interage", "desenvolve" };

    private final SlidesService slidesService;
    private final BlogService blogService;

    public PublicPagesController(SlidesService slidesService, BlogService blogService) {
        this.slidesService = slidesService;
        this.blogService = blogService;
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

    @GetMapping("/contato")
    public String contato() {
        return "contato";
    }

}
