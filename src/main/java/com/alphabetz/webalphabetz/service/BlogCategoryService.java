package com.alphabetz.webalphabetz.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alphabetz.webalphabetz.model.BlogCategory;
import com.alphabetz.webalphabetz.repository.BlogCategoryRepository;
import com.alphabetz.webalphabetz.utils.ValidationUtils;

@Service
public class BlogCategoryService {

    private static final int MAX_CATEGORY_LENGTH = 80;

    private final BlogCategoryRepository blogCategoryRepository;
    private final JdbcTemplate jdbcTemplate;

    public BlogCategoryService(BlogCategoryRepository blogCategoryRepository,
            JdbcTemplate jdbcTemplate) {
        this.blogCategoryRepository = blogCategoryRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<BlogCategory> getAllCategories() {
        return blogCategoryRepository.findAllByOrderByNomeAsc();
    }

    @Transactional
    public BlogCategory createCategory(String nome) {
        String normalizedName = normalizeName(nome);
        if (findByName(normalizedName) != null) {
            throw new IllegalArgumentException("Essa categoria já está cadastrada.");
        }

        BlogCategory category = new BlogCategory();
        category.setId(UUID.randomUUID());
        category.setNome(normalizedName);
        category.setNew(true);

        try {
            return blogCategoryRepository.save(category);
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException("Essa categoria já está cadastrada.", exception);
        }
    }

    @Transactional
    public BlogCategory updateCategory(UUID id, String nome) {
        BlogCategory category = findById(id);
        String normalizedName = normalizeName(nome);
        BlogCategory categoryWithSameName = findByName(normalizedName);
        if (categoryWithSameName != null && !categoryWithSameName.getId().equals(category.getId())) {
            throw new IllegalArgumentException("Essa categoria já está cadastrada.");
        }

        String oldName = category.getNome();
        category.setNome(normalizedName);
        category.setNew(false);

        try {
            BlogCategory updatedCategory = blogCategoryRepository.save(category);
            jdbcTemplate.update(
                    "UPDATE blog SET categoria = ? WHERE categoria = ? COLLATE NOCASE",
                    normalizedName, oldName);
            return updatedCategory;
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException("Essa categoria já está cadastrada.", exception);
        }
    }

    @Transactional
    public void deleteCategory(UUID id) {
        BlogCategory category = findById(id);
        Long postsUsingCategory = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM blog WHERE categoria = ? COLLATE NOCASE",
                Long.class, category.getNome());
        if (postsUsingCategory != null && postsUsingCategory > 0) {
            throw new IllegalArgumentException(
                    "Essa categoria está vinculada a postagens e não pode ser removida.");
        }
        blogCategoryRepository.deleteById(category.getId());
    }

    public String requireExistingCategory(String nome) {
        String normalizedName = normalizeName(nome);
        BlogCategory category = findByName(normalizedName);
        if (category == null) {
            throw new IllegalArgumentException("Selecione uma categoria cadastrada.");
        }
        return category.getNome();
    }

    private BlogCategory findByName(String nome) {
        return getAllCategories().stream()
                .filter(category -> category.getNome().equalsIgnoreCase(nome))
                .findFirst()
                .orElse(null);
    }

    private BlogCategory findById(UUID id) {
        return blogCategoryRepository.findById(Objects.requireNonNull(id, "id"))
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada."));
    }

    private String normalizeName(String nome) {
        ValidationUtils.validarCampoStringObrigatorio(nome, "categoria");
        String normalizedName = nome.trim().replaceAll("\\s+", " ");
        if (normalizedName.length() > MAX_CATEGORY_LENGTH) {
            throw new IllegalArgumentException("A categoria deve ter no máximo 80 caracteres.");
        }
        return normalizedName;
    }
}
