package com.example.batch.processors;

import com.example.batch.models.Persona;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.item.ItemProcessor;


public class PersonaItemProcessor implements ItemProcessor<Persona, Persona> {

    private static final Logger LOG = LoggerFactory.getLogger(PersonaItemProcessor.class);

    @Override
    public Persona process(Persona item) throws Exception {
        Persona persona = new Persona(
                item.getNombre().toUpperCase(),
                item.getApellido().toUpperCase(),
                item.getTelefono().toUpperCase()
        );
        LOG.info("Transformando {} en {}", item, persona);
        return persona;
    }
}
