package com.alphabetz.webalphabetz.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.alphabetz.webalphabetz.model.Blog;

@Repository
public interface BlogRepository extends CrudRepository<Blog, UUID> {

    List<Blog> findAllByOrderByTituloAsc();

    @Query("SELECT id, titulo, categoria, conteudo, imagem_url FROM blog ORDER BY rowid DESC")
    List<Blog> findAllNewestFirst();
}
