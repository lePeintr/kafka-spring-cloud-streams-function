package com.example.demo_spring_cloud_streams_kafka.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.util.Date;

/**
 * PageEvent.java
 * Auteur: Administrateur
 * Description: l'objet à envoyer par message avec kafka
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PageEvent {
    private String name;
    private String user;
    private Date date;
    private long duration;

}
