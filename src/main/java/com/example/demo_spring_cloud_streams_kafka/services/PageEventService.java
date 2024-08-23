package com.example.demo_spring_cloud_streams_kafka.services;

import com.example.demo_spring_cloud_streams_kafka.entities.PageEvent;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * PageEventService.java
 * <p>
 * Auteur: Administrateur
 */
@Service
public class PageEventService {

    @Bean
    public Consumer<PageEvent> pageEventConsumer(){
        return (input)->{
            System.out.println("**********************");
            System.out.println(input.toString());
            System.out.println("**********************");
        };
    }
    @Bean
    public Supplier<PageEvent> pageEventSupplier() {
        return () ->
                new PageEvent(Math.random() > 0.5 ? "P1" : "P2",
                        Math.random() > 0.5 ? "U1" : "U2",
                        new Date(),
                        new Random().nextInt(9000));

    }

    /**
     * On prend un enregistrement en input on le traite et on produit un autre enregistrement en sorti
     * Ce cas est particulier car on prend en entree un pageEvent et un retourne un autre PageEvent
     *
     * @return
     */
    @Bean
    public Function<PageEvent, PageEvent> pageEventFunction() {
        return (input) -> {
            input.setName("L:" + input.getName().length());
            input.setUser("UUUU");
            return input;
        };
    }

    /**
     * On filtre les donnee danc la duree de visite>100
     * par defaut la cle est null pour l'enregistrement en entree
     * .map((k,v)-> new KeyValue<>(v.getName(),0L)) ici on lui donne la cle la valeur du nom de la page et la valeur 0L
     * ensuite en faisant le groupBy on va serializer et deserializer la cle en String
     * <p>
     * Avec GroupBy on doit utiliser un count
     * count(Materialized.as("page-count")) on demande a kafka stream une fois que tu calcul produit une table qui
     * s'appelle page-count et met le dans un store qui peut etre dans une base de donnée ou en memoire dans un store
     * On exploitera cela dans le RestController
     * @return
     */
    @Bean
    public Function<KStream<String, PageEvent>, KStream<String, Long>> kStreamFunction() {
        return (input) -> {
            return input
                    .filter((k, v) -> v.getDuration() > 100)
                    .map((k, v) -> new KeyValue<>(v.getName(), 0L))
                    //.groupByKey(Grouped.keySerde(Serdes.String()))
                    .groupBy((k, v) -> k, Grouped.with(Serdes.String(),Serdes.Long()))
                    .windowedBy(TimeWindows.of(Duration.ofSeconds(5)))//produit les resultats du count observe uniquement les 5 dernieres secondes
                    .count(Materialized.as("page-count"))
                    .toStream()
                    .map((k,v)->new KeyValue<>("=>"+k.window().startTime()+k.window().endTime()+":"+k.key(),v));        };
    }


}
