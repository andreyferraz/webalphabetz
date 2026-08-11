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
import com.alphabetz.webalphabetz.model.Depoimentos;
import com.alphabetz.webalphabetz.model.OuvidoriaManifestacao;
import com.alphabetz.webalphabetz.model.Slides;
import com.alphabetz.webalphabetz.service.AdminService;
import com.alphabetz.webalphabetz.service.BlogCategoryService;
import com.alphabetz.webalphabetz.service.BlogService;
import com.alphabetz.webalphabetz.service.CareerApplicationService;
import com.alphabetz.webalphabetz.service.DashboardService;
import com.alphabetz.webalphabetz.service.DepoimentosService;
import com.alphabetz.webalphabetz.service.OuvidoriaService;
import com.alphabetz.webalphabetz.service.SlidesService;
import com.alphabetz.webalphabetz.service.TurmasImagensService;

@Controller
public class AdminPagesController {

    private final SlidesService slidesService;
    private final BlogService blogService;
    private final BlogCategoryService blogCategoryService;
    private final AdminService adminService;
    private final DashboardService dashboardService;
    private final CareerApplicationService careerApplicationService;
    private final OuvidoriaService ouvidoriaService;
    private final DepoimentosService depoimentosService;
    private final TurmasImagensService turmasImagensService;

    public AdminPagesController(SlidesService slidesService, BlogService blogService,
            BlogCategoryService blogCategoryService, AdminService adminService,
            DashboardService dashboardService, CareerApplicationService careerApplicationService,
            OuvidoriaService ouvidoriaService, DepoimentosService depoimentosService,
            TurmasImagensService turmasImagensService) {
        this.slidesService = slidesService;
        this.blogService = blogService;
        this.blogCategoryService = blogCategoryService;
        this.adminService = adminService;
        this.dashboardService = dashboardService;
        this.careerApplicationService = careerApplicationService;
        this.ouvidoriaService = ouvidoriaService;
        this.depoimentosService = depoimentosService;
        this.turmasImagensService = turmasImagensService;
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

    @GetMapping("/admin/depoimentos")
    public String testimonials(Model model) {
        model.addAttribute("testimonials", depoimentosService.getAllDepoimentos());
        return "admin/depoimentos";
    }

    @PostMapping("/admin/depoimentos")
    public String createTestimonial(@RequestParam String nome,
            @RequestParam String depoimento,
            @RequestParam(name = "imagem", required = false) MultipartFile image,
            RedirectAttributes redirectAttributes) {
        try {
            depoimentosService.createDepoimento(testimonial(nome, depoimento), image);
            redirectAttributes.addFlashAttribute("successMessage", "Depoimento criado com sucesso.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage(exception));
        }
        return "redirect:/admin/depoimentos";
    }

    @PostMapping("/admin/depoimentos/{id}")
    public String updateTestimonial(@PathVariable UUID id,
            @RequestParam String nome,
            @RequestParam String depoimento,
            @RequestParam(name = "imagem", required = false) MultipartFile image,
            RedirectAttributes redirectAttributes) {
        try {
            depoimentosService.updateDepoimento(id, testimonial(nome, depoimento), image);
            redirectAttributes.addFlashAttribute("successMessage", "Depoimento atualizado com sucesso.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage(exception));
        }
        return "redirect:/admin/depoimentos";
    }

    @PostMapping("/admin/depoimentos/{id}/excluir")
    public String deleteTestimonial(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            depoimentosService.deleteDepoimento(id);
            redirectAttributes.addFlashAttribute("successMessage", "Depoimento excluído com sucesso.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage(exception));
        }
        return "redirect:/admin/depoimentos";
    }

    @GetMapping("/admin/turmas")
    public String classes(Model model) {
        model.addAttribute("classes", turmasImagensService.getAll());
        return "admin/turmas";
    }

    @PostMapping("/admin/turmas")
    public String createClass(@RequestParam String nome,
            @RequestParam(name = "imagem", required = false) MultipartFile image,
            RedirectAttributes redirectAttributes) {
        try {
            turmasImagensService.create(nome, image);
            redirectAttributes.addFlashAttribute("successMessage", "Turma criada com sucesso.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage(exception));
        }
        return "redirect:/admin/turmas";
    }

    @PostMapping("/admin/turmas/{id}")
    public String updateClass(@PathVariable UUID id,
            @RequestParam String nome,
            @RequestParam(name = "imagem", required = false) MultipartFile image,
            RedirectAttributes redirectAttributes) {
        try {
            turmasImagensService.update(id, nome, image);
            redirectAttributes.addFlashAttribute("successMessage", "Turma atualizada com sucesso.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage(exception));
        }
        return "redirect:/admin/turmas";
    }

    @PostMapping("/admin/turmas/{id}/excluir")
    public String deleteClass(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            turmasImagensService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Turma excluída com sucesso.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage(exception));
        }
        return "redirect:/admin/turmas";
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

    @GetMapping("/admin/ouvidoria")
    public String ombudsman(Model model) {
        model.addAttribute("manifestations", ouvidoriaService.getAll());
        return "admin/ouvidoria";
    }

    @GetMapping("/admin/ouvidoria/{id}/arquivo")
    public ResponseEntity<ByteArrayResource> downloadManifestationFile(@PathVariable UUID id) {
        try {
            OuvidoriaManifestacao manifestation = ouvidoriaService.get(id);
            return downloadAttachment(manifestation.getArquivoNome(), manifestation.getArquivoTipo(),
                    manifestation.getArquivoTamanho(), manifestation.getArquivoConteudo());
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/admin/ouvidoria/{id}/documento")
    public ResponseEntity<ByteArrayResource> downloadManifestationDocument(@PathVariable UUID id) {
        try {
            OuvidoriaManifestacao manifestation = ouvidoriaService.get(id);
            return downloadAttachment(manifestation.getDocumentoImagemNome(),
                    manifestation.getDocumentoImagemTipo(), manifestation.getDocumentoImagemTamanho(),
                    manifestation.getDocumentoImagemConteudo());
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/admin/ouvidoria/{id}/excluir")
    public String deleteManifestation(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            ouvidoriaService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Manifestação excluída com sucesso.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage(exception));
        }
        return "redirect:/admin/ouvidoria";
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

    private Depoimentos testimonial(String name, String content) {
        Depoimentos testimonial = new Depoimentos();
        testimonial.setNome(name);
        testimonial.setDepoimento(content);
        return testimonial;
    }

    private MediaType resumeContentType(String contentType) {
        try {
            return MediaType.parseMediaType(contentType);
        } catch (RuntimeException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private ResponseEntity<ByteArrayResource> downloadAttachment(
            String name, String type, long size, byte[] content) {
        if (name == null || name.isBlank() || content == null) {
            return ResponseEntity.notFound().build();
        }
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(name, StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(resumeContentType(type))
                .contentLength(size)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(new ByteArrayResource(content));
    }
}
