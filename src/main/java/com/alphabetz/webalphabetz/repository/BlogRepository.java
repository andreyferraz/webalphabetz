package com.alphabetz.webalphabetz.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.alphabetz.webalphabetz.model.Blog;

@Repository
public interface BlogRepository extends CrudRepository<Blog, UUID> {

    List<Blog> findAllByOrderByTituloAsc();
}
