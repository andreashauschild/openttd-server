package de.litexo.model.external;

import lombok.Data;

/**
 * Body of 'PATCH /api/openttd-server/config'. Every field is optional, an absent one keeps its current value, so the
 * numbers are boxed: MapStruct's 'NullValuePropertyMappingStrategy.IGNORE' cannot skip a primitive, and a partial
 * body used to reset the other numbers to 0.
 */
@Data
public class OpenttdServerConfigUpdate {
    private Integer autoSaveMinutes;
    private Integer numberOfAutoSaveFilesToKeep;
    private Integer numberOfManuallySaveFilesToKeep;

    private String password;

    private String oldPassword;


}
