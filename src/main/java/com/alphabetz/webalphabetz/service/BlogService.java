package com.alphabetz.webalphabetz.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alphabetz.webalphabetz.model.Blog;
import com.alphabetz.webalphabetz.repository.BlogRepository;
import com.alphabetz.webalphabetz.utils.ValidationUtils;

@Service
public class BlogService {

    private static final Safelist BLOG_CONTENT_SAFELIST = Safelist.basic()
            .addTags("h2", "h3")
            .addAttributes("li", "data-list")
            .removeProtocols("a", "href", "ftp")
            .addEnforcedAttribute("a", "rel", "nofollow noopener noreferrer");

    private final BlogRepository blogRepository;
    private final FileUploadService fileUploadService;

    public BlogService(BlogRepository blogRepository, FileUploadService fileUploadService) {
        this.blogRepository = blogRepository;
        this.fileUploadService = fileUploadService;
    }

    @Transactional
    public Blog createPost(String titulo, String categoria, String conteudo, MultipartFile imageFile) {
        String sanitizedContent = validateAndSanitize(titulo, categoria, conteudo);
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("A imagem de capa é obrigatória.");
        }

        String imageUrl = fileUploadService.salvarImagem(imageFile);
        try {
            Blog blog = new Blog();
            blog.setId(UUID.randomUUID());
            blog.setTitulo(titulo.trim());
            blog.setCategoria(categoria.trim());
            blog.setConteudo(sanitizedContent);
            blog.setImagemUrl(imageUrl);
            blog.setNew(true);
            return enrich(blogRepository.save(blog));
        } catch (RuntimeException exception) {
            fileUploadService.removerImagem(imageUrl);
            throw exception;
        }
    }

    @Transactional
    public Blog updatePost(UUID id, String titulo, String categoria, String conteudo,
            MultipartFile imageFile) {
        UUID blogId = Objects.requireNonNull(id, "id");
        String sanitizedContent = validateAndSanitize(titulo, categoria, conteudo);
        Blog existingPost = blogRepository.findById(blogId)
                .orElseThrow(() -> new IllegalArgumentException("Postagem não encontrada."));

        String oldImageUrl = existingPost.getImagemUrl();
        String newImageUrl = imageFile == null || imageFile.isEmpty()
                ? null
                : fileUploadService.salvarImagem(imageFile);
        try {
            existingPost.setTitulo(titulo.trim());
            existingPost.setCategoria(categoria.trim());
            existingPost.setConteudo(sanitizedContent);
            if (newImageUrl != null) {
                existingPost.setImagemUrl(newImageUrl);
            }
            existingPost.setNew(false);
            Blog updatedPost = blogRepository.save(existingPost);

            if (newImageUrl != null && oldImageUrl != null && !oldImageUrl.isBlank()) {
                fileUploadService.removerImagem(oldImageUrl);
            }
            return enrich(updatedPost);
        } catch (RuntimeException exception) {
            if (newImageUrl != null) {
                fileUploadService.removerImagem(newImageUrl);
            }
            throw exception;
        }
    }

    @Transactional
    public void deletePost(UUID id) {
        UUID blogId = Objects.requireNonNull(id, "id");
        Blog existingPost = blogRepository.findById(blogId)
                .orElseThrow(() -> new IllegalArgumentException("Postagem não encontrada."));

        blogRepository.deleteById(blogId);
        if (existingPost.getImagemUrl() != null && !existingPost.getImagemUrl().isBlank()) {
            fileUploadService.removerImagem(existingPost.getImagemUrl());
        }
    }

    public List<Blog> getAllPosts() {
        List<Blog> posts = blogRepository.findAllByOrderByTituloAsc();
        posts.forEach(this::enrich);
        return posts;
    }

    private String validateAndSanitize(String titulo, String categoria, String conteudo) {
        ValidationUtils.validarCampoStringObrigatorio(titulo, "titulo");
        ValidationUtils.validarCampoStringObrigatorio(categoria, "categoria");
        ValidationUtils.validarCampoStringObrigatorio(conteudo, "conteúdo");

        String sanitizedContent = Jsoup.clean(conteudo, BLOG_CONTENT_SAFELIST);
        if (Jsoup.parseBodyFragment(sanitizedContent).text().isBlank()) {
            throw new IllegalArgumentException("O conteúdo da postagem é obrigatório.");
        }
        return sanitizedContent;
    }

    private Blog enrich(Blog blog) {
        String safeContent = Jsoup.clean(
                blog.getConteudo() == null ? "" : blog.getConteudo(),
                BLOG_CONTENT_SAFELIST);
        blog.setConteudo(safeContent);
        String plainText = Jsoup.parseBodyFragment(safeContent).text().trim();
        blog.setResumo(summarize(plainText));
        int words = plainText.isBlank() ? 0 : plainText.split("\\s+").length;
        blog.setTempoLeitura(Math.max(1, (int) Math.ceil(words / 200.0)));
        return blog;
    }

    private String summarize(String text) {
        if (text.length() <= 180) {
            return text;
        }
        String shortened = text.substring(0, 177);
        int lastSpace = shortened.lastIndexOf(' ');
        return (lastSpace > 120 ? shortened.substring(0, lastSpace) : shortened) + "...";
    }
}
