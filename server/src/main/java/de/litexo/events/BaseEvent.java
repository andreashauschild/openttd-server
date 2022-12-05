package de.litexo.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.ToString;

/**
 * @author Andreas Hauschild
 */
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "_type")
public abstract class BaseEvent{

    private long created = System.currentTimeMillis();

    private final Class<? extends Object> clazz;

    public BaseEvent(Object eventSource) {
        this.clazz = eventSource.getClass();
    }

    public String getSource() {
        return this.clazz.getCanonicalName();
    }

    public long getCreated() {
        return created;
    }

    public String toJson() {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
