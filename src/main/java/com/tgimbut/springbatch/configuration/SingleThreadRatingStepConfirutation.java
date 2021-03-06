package com.tgimbut.springbatch.configuration;

import com.tgimbut.springbatch.common.BeerProcessorListener;
import com.tgimbut.springbatch.common.RatingProcessor;
import com.tgimbut.springbatch.domain.Beer;
import com.tgimbut.springbatch.service.NoAlcoholException;
import org.hibernate.SessionFactory;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.HibernateCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.repeat.CompletionPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.persistence.EntityManagerFactory;

@Configuration
@Profile("singleThreadStep")
public class SingleThreadRatingStepConfirutation {

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Step ratingStep(ItemStreamReader<Beer> ratingReader,
                           RatingProcessor ratingProcessor,
                           ItemWriter<Beer> ratingWriter,
                           BeerProcessorListener beerProcessorListener) {
        return stepBuilderFactory.get("ratingStep")
                .<Beer, Beer>chunk(10)
                .reader(ratingReader)
                .processor(ratingProcessor)
                .writer(ratingWriter)
                .faultTolerant()
                .skipLimit(10000)
                .skip(NoAlcoholException.class)
                .listener(beerProcessorListener)
                .build();
    }

    @Bean
    public ItemStreamReader<Beer> ratingReader(SessionFactory sessionFactory) {
        HibernateCursorItemReader<Beer> itemReader = new HibernateCursorItemReader<>();
        itemReader.setSessionFactory(sessionFactory);
        itemReader.setQueryString("FROM Beer WHERE rating IS NULL");
        itemReader.setUseStatelessSession(true);
        itemReader.setFetchSize(10);
        return itemReader;
    }

    @Bean
    public ItemWriter<Beer> ratingWriter(EntityManagerFactory entityManagerFactory) {
        JpaItemWriter<Beer> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

}
