package de.litexo.model.mapper;

import de.litexo.model.external.OpenttdServer;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(config = ServiceMapperConfiguration.class,
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.WARN)
@SuppressWarnings("java:S1610")
public abstract class OpenttdServerMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "process", ignore = true)
    // Primitive, so NullValuePropertyMappingStrategy.IGNORE does not apply: without this a PUT from the ui would
    // always reset the flag to false and the server would not come back after a restart.
    @Mapping(target = "lastKnownRunning", ignore = true)
    // @formatter:on
    public abstract void patch(OpenttdServer external, @MappingTarget OpenttdServer internal);


}
