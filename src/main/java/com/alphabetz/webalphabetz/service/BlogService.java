package com.alphabetz.webalphabetz.service;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alphabetz.webalphabetz.model.Blog;
import com.alphabetz.webalphabetz.repository.BlogRepository;
import com.alphabetz.webalphabetz.utils.ValidationUtils;

@Service
public class BlogService {

    private final BlogRepository blogRepository;
    private final FileUploadService fileUploadService;

    public BlogService(BlogRepository blogRepository, FileUploadService fileUploadService) {
        this.blogRepository = blogRepository;
        this.fileUploadService = fileUploadService;
    }

    @Transactional
    public Blog createPost(Blog blog, MultipartFile imageFile){

        ValidationUtils.validarCampoObrigatorio(blog, "blog");
        ValidationUtils.validarCampoStringObrigatorio(blog.getTitulo(), "titulo");
        ValidationUtils.validarCampoStringObrigatorio(blog.getCategoria(), "categoria");
        ValidationUtils.validarCampoStringObrigatorio(blog.getConteudo(), "conteudo");
        if(imageFile != null && !imageFile.isEmpty()){
            String imageUrl = fileUploadService.salvarImagem(imageFile);
            blog.setImagemUrl(imageUrl);
        } else {
            ValidationUtils.validarCampoStringObrigatorio(blog.getImagemUrl(), "imagemUrl");
        }

        if(blog.getId() == null){
            blog.setId(UUID.randomUUID());
        }
        blog.setNew(true);

        return blogRepository.save(blog);
    }

    @Transactional
    public Blog updatePost(Blog blog, MultipartFile imageFile){
        ValidationUtils.validarCampoObrigatorio(blog, "blog");
        ValidationUtils.validarCampoStringObrigatorio(blog.getTitulo(), "titulo");
        ValidationUtils.validarCampoStringObrigatorio(blog.getCategoria(), "categoria");
        ValidationUtils.validarCampoStringObrigatorio(blog.getConteudo(), "conteudo");
        UUID id = Objects.requireNonNull(blog.getId(), "id");

        Blog existingPost = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Blog não encontrado."));

        existingPost.setTitulo(blog.getTitulo());
        existingPost.setCategoria(blog.getCategoria());
        existingPost.setConteudo(blog.getConteudo());

        if(imageFile != null && !imageFile.isEmpty()){
            if(existingPost.getImagemUrl() != null && !existingPost.getImagemUrl().isEmpty()){
                fileUploadService.removerImagem(existingPost.getImagemUrl());
            }
            String imageUrl = fileUploadService.salvarImagem(imageFile);
            existingPost.setImagemUrl(imageUrl);
        } else {
            existingPost.setImagemUrl(existingPost.getImagemUrl());
        }
        existingPost.setNew(false);

        return blogRepository.save(existingPost);
    }

    @Transactional
    public void deletePost(Blog blog){
        ValidationUtils.validarCampoObrigatorio(blog, "blog");
        UUID id = Objects.requireNonNull(blog.getId(), "id");

        Blog existingPost = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Blog não encontrado."));

        if(existingPost.getImagemUrl() != null && !existingPost.getImagemUrl().isEmpty()){
            fileUploadService.removerImagem(existingPost.getImagemUrl());
        }
        blogRepository.deleteById(id);
    }

    public Iterable<Blog> getAllPosts(){
        return blogRepository.findAll();
    }

    public Blog getPostById(UUID id){
        return blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Blog não encontrado."));
    }

}