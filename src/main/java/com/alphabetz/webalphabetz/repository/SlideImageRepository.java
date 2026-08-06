package com.alphabetz.webalphabetz.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.alphabetz.webalphabetz.model.SlideImage;

@Repository
public interface SlideImageRepository extends CrudRepository<SlideImage, UUID> {

    List<SlideImage> findAllBySlideIdOrderByOrdemAsc(UUID slideId);

    void deleteAllBySlideId(UUID slideId);
}
