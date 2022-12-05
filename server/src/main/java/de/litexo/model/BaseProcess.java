package de.litexo.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BaseProcess {
    private String processId;
    private String processData;
}
