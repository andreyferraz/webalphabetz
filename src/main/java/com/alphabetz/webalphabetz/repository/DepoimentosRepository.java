package com.alphabetz.webalphabetz.repository;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.alphabetz.webalphabetz.model.Depoimentos;

@Repository
public interface DepoimentosRepository extends CrudRepository<Depoimentos, UUID> {

}
