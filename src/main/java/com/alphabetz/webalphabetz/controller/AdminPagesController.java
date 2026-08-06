package com.alphabetz.webalphabetz.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.alphabetz.webalphabetz.model.Slides;
import com.alphabetz.webalphabetz.service.SlidesService;

@Controller
public class AdminPagesController {

    private final SlidesService slidesService;

    public AdminPagesController(SlidesService slidesService) {
        this.slidesService = slidesService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/admin")
    public String overview() {
        return "admin/index";
    }

    @GetMapping("/admin/slides")
    public String slides(Model model) {
        List<Slides> slides = slidesService.getAllSlides();
        model.addAttribute("slides", slides);
        model.addAttribute("totalFotos", slidesService.getTotalImages(slides));
        return "admin/slides";
    }

    @PostMapping("/admin/slides")
    public String createSlide(@RequestParam String titulo,
            @RequestParam(name = "images", required = false) List<MultipartFile> images,
            RedirectAttributes redirectAttributes) {
        try {
            slidesService.createSlide(titulo, images);
            redirectAttributes.addFlashAttribute("successMessage", "Slide criado com sucesso.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage(exception));
        }
        return "redirect:/admin/slides";
    }

    @PostMapping("/admin/slides/{id}")
    public String updateSlide(@PathVariable UUID id,
            @RequestParam String titulo,
            @RequestParam(name = "images", required = false) List<MultipartFile> images,
            @RequestParam(name = "removedImageIds", required = false) List<UUID> removedImageIds,
            RedirectAttributes redirectAttributes) {
        try {
            slidesService.updateSlide(id, titulo, images, removedImageIds);
            redirectAttributes.addFlashAttribute("successMessage", "Slide atualizado com sucesso.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage(exception));
        }
        return "redirect:/admin/slides";
    }

    @PostMapping("/admin/slides/{id}/excluir")
    public String deleteSlide(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            slidesService.deleteSlide(id);
            redirectAttributes.addFlashAttribute("successMessage", "Slide excluído com sucesso.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage(exception));
        }
        return "redirect:/admin/slides";
    }

    @GetMapping("/admin/blog")
    public String blog() {
        return "admin/blog";
    }

    @GetMapping("/admin/seguranca")
    public String security() {
        return "admin/seguranca";
    }

    private String errorMessage(RuntimeException exception) {
        return exception.getMessage() == null || exception.getMessage().isBlank()
                ? "Não foi possível concluir a operação."
                : exception.getMessage();
    }
}
