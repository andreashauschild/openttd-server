package de.litexo.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.ToString;
import org.jboss.logging.Logger;

/**
 * @author Andreas Hauschild
 */
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "_type")
public abstract class BaseEvent {

    private static final Logger LOG = Logger.getLogger(BaseEvent.class);

    /**
     * One mapper for every event. {@link #toJson()} runs on the pump thread of the process, so building a mapper per
     * batch would delay the console output. No pretty printer either, it inflates the websocket payload by roughly a
     * third without a reader that benefits from it.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private long created = System.currentTimeMillis();

    private final Class<? extends Object> clazz;

    protected BaseEvent(Object eventSource) {
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
            return MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to serialize event " + this.getClass().getSimpleName(), e);
        }
        return null;
    }

}
