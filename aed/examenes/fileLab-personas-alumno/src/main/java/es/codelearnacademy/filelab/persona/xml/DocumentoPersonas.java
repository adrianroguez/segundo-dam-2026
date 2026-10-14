package es.codelearnacademy.filelab.persona.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import es.codelearnacademy.filelab.model.Persona;

import java.util.*;

@JacksonXmlRootElement(localName = "personas")
public class DocumentoPersonas {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "persona")
    private List<Persona> personas = new ArrayList<>();

    public DocumentoPersonas() {
    }

    public DocumentoPersonas(List<Persona> personas) {
        this.personas = personas;
    }

    public List<Persona> getPersonas() {
        return personas;
    }

    public void setPersonas(List<Persona> personas) {
        this.personas = personas;
    }
    // TODO: anade las anotaciones Jackson XML para <personas><persona>...</persona></personas>
}
