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
    // @formatter:on
    public abstract void patch(OpenttdServer external, @MappingTarget OpenttdServer internal);


}
