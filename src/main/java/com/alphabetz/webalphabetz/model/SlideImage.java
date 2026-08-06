package com.alphabetz.webalphabetz.model;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("slide_images")
public class SlideImage implements Persistable<UUID> {

    @Id
    @Column("id")
    private UUID id;

    @Column("slide_id")
    private UUID slideId;

    @Column("imagem_url")
    private String imagemUrl;

    @Column("ordem")
    private Integer ordem;

    @Transient
    private boolean isNew;

    @Override
    public boolean isNew() {
        return isNew;
    }
}
