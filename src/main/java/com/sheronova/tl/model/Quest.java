package com.sheronova.tl.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "tl_quests")
public class Quest {

    @Id
    private Integer id;

    @Column(name = "quest")
    private String quest;

    @Column(name = "answers")
    @Type(type = "com.sheronova.tl.model.types.JsonType")
    private JsonNode answers;

    @Column(name = "correct")
    private Integer correct;

    @Column(name = "help")
    private String help;

    @Column(name = "image_file")
    private String imageFile;
}
