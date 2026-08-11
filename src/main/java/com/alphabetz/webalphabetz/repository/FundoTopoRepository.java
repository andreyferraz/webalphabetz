package com.alphabetz.webalphabetz.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.alphabetz.webalphabetz.model.FundoTopo;

@Repository
public interface FundoTopoRepository extends CrudRepository<FundoTopo, UUID> {

    List<FundoTopo> findAllByOrderByNomeAsc();

}
