package com.example.batch;

import com.example.batch.listeners.JobListener;
import com.example.batch.models.Persona;
import com.example.batch.processors.PersonaItemProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    public final JobBuilderFactory jobBuilderFactory;

    public final StepBuilderFactory stepBuilderFactory;

    @Autowired
    public BatchConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public FlatFileItemReader<Persona> reader() {
        BeanWrapperFieldSetMapper<Persona> wrapperFieldSetMapper = new BeanWrapperFieldSetMapper<>();
        wrapperFieldSetMapper.setTargetType(Persona.class);
        return new FlatFileItemReaderBuilder<Persona>()
                .name("personaItemReader")
                .resource(new ClassPathResource("sample-data.csv"))
                .delimited()
                .names("nombre", "apellido", "telefono")
                .fieldSetMapper(wrapperFieldSetMapper)
                .build();
    }

    @Bean
    public PersonaItemProcessor processor() {
        return new PersonaItemProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Persona> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Persona>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO persona (nombre, apellido, telefono) VALUES (:nombre, :apellido, :telefono)")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public Job importPersonaJob(JobListener listener, Step step) {
        return jobBuilderFactory
                .get("importPersonaJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step)
                .end()
                .build();
    }

    @Bean
    public Step step(JdbcBatchItemWriter<Persona> writer) {
        return stepBuilderFactory
                .get("step")
                .<Persona, Persona> chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer)
                .build();
    }
}
