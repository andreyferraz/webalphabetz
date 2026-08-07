package com.alphabetz.webalphabetz.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.alphabetz.webalphabetz.model.Blog;
import com.alphabetz.webalphabetz.model.CareerApplication;
import com.alphabetz.webalphabetz.model.Slides;
import com.alphabetz.webalphabetz.service.AdminService;
import com.alphabetz.webalphabetz.service.BlogCategoryService;
import com.alphabetz.webalphabetz.service.BlogService;
import com.alphabetz.webalphabetz.service.CareerApplicationService;
import com.alphabetz.webalphabetz.service.DashboardService;
import com.alphabetz.webalphabetz.service.SlidesService;

@Controller
public class AdminPagesController {

    private final SlidesService slidesService;
    private final BlogService blogService;
    private final BlogCategoryService blogCategoryService;
    private final AdminService adminService;
    private final DashboardService dashboardService;
    private final CareerApplicationService careerApplicationService;

    public AdminPagesController(SlidesService slidesService, BlogService blogService,
            BlogCategoryService blogCategoryService, AdminService adminService,
            DashboardService dashboardService, CareerApplicationService careerApplicationService) {
        this.slidesService = slidesService;
        this.blogService = blogService;
        this.blogCategoryService = blogCategoryService;
        this.adminService = adminService;
        this.dashboardService = dashboardService;
        this.careerApplicationService = careerApplicationService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/admin")
    public String overview(Model model) {
        model.addAttribute("dashboard", dashboardService.getSummary());
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
    public String blog(Model model) {
        List<Blog> posts = blogService.getAllPosts();
        model.addAttribute("posts", posts);
        model.addAttribute("categories", blogCategoryService.getAllCategories());
        return "admin/blog";
    }

    @PostMapping("/admin/blog/categorias")
    public String createBlogCategory(@RequestParam String nome,
            RedirectAttributes redirectAttributes) {
        try {
            blogCategoryService.createCategory(nome);
            redirectAttributes.addFlashAttribute("successMessage", "Categoria criada com sucesso.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage(exception));
        }
        return "redirect:/admin/blog";
    }

    @PostMapping("/admin/blog/categorias/{id}")
    public String updateBlogCategory(@PathVariable UUID id, @RequestParam String nome,
            RedirectAttributes redirectAttributes) {
        try {
            blogCategoryService.updateCategory(id, nome);
            redirectAttributes.addFlashAttribute("successMessage", "Categoria atualizada com sucesso.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage(exception));
        }
        return "redirect:/admin/blog";
    }

    @PostMapping("/admin/blog/categorias/{id}/excluir")
    public String deleteBlogCategory(@PathVariable UUID id,
            RedirectAttributes redirectAttributes) {
        try {
            blogCategoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Categoria removida com sucesso.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage(exception));
        }
        return "redirect:/admin/blog";
    }

    @PostMapping("/admin/blog")
    public String createBlogPost(@RequestParam String titulo,
            @RequestParam String categoria,
            @RequestParam String conteudo,
            @RequestParam(name = "imagem", required = false) MultipartFile image,
            RedirectAttributes redirectAttributes) {
        try {
            blogService.createPost(titulo, categoria, conteudo, image);
            redirectAttributes.addFlashAttribute("successMessage", "Postagem criada com sucesso.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage(exception));
        }
        return "redirect:/admin/blog";
    }

    @PostMapping("/admin/blog/{id}")
    public String updateBlogPost(@PathVariable UUID id,
            @RequestParam String titulo,
            @RequestParam String categoria,
            @RequestParam String conteudo,
            @RequestParam(name = "imagem", required = false) MultipartFile image,
            RedirectAttributes redirectAttributes) {
        try {
            blogService.updatePost(id, titulo, categoria, conteudo, image);
            redirectAttributes.addFlashAttribute("successMessage", "Postagem atualizada com sucesso.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage(exception));
        }
        return "redirect:/admin/blog";
    }

    @PostMapping("/admin/blog/{id}/excluir")
    public String deleteBlogPost(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            blogService.deletePost(id);
            redirectAttributes.addFlashAttribute("successMessage", "Postagem excluída com sucesso.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage(exception));
        }
        return "redirect:/admin/blog";
    }

    @GetMapping("/admin/seguranca")
    public String security() {
        return "admin/seguranca";
    }

    @GetMapping("/admin/candidaturas")
    public String applications(Model model) {
        model.addAttribute("applications", careerApplicationService.getAllApplications());
        return "admin/candidaturas";
    }

    @GetMapping("/admin/candidaturas/{id}/curriculo")
    public ResponseEntity<ByteArrayResource> downloadResume(@PathVariable UUID id) {
        try {
            CareerApplication application = careerApplicationService.getApplication(id);
            MediaType contentType = resumeContentType(application.getCurriculoTipo());
            ContentDisposition disposition = ContentDisposition.attachment()
                    .filename(application.getCurriculoNome(), StandardCharsets.UTF_8)
                    .build();
            return ResponseEntity.ok()
                    .contentType(contentType)
                    .contentLength(application.getCurriculoTamanho())
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                    .body(new ByteArrayResource(application.getCurriculoConteudo()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/admin/candidaturas/{id}/excluir")
    public String deleteApplication(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            careerApplicationService.deleteApplication(id);
            redirectAttributes.addFlashAttribute("successMessage", "Candidatura excluída com sucesso.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage(exception));
        }
        return "redirect:/admin/candidaturas";
    }

    @PostMapping("/admin/seguranca/senha")
    public String changePassword(Authentication authentication,
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {
        try {
            adminService.changePasswordByUsername(
                    authentication.getName(), currentPassword, newPassword, confirmPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Senha atualizada com sucesso.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage(exception));
        }
        return "redirect:/admin/seguranca";
    }

    private String errorMessage(RuntimeException exception) {
        return exception.getMessage() == null || exception.getMessage().isBlank()
                ? "Não foi possível concluir a operação."
                : exception.getMessage();
    }

    private MediaType resumeContentType(String contentType) {
        try {
            return MediaType.parseMediaType(contentType);
        } catch (RuntimeException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
