package de.litexo.model.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.MappingInheritanceStrategy;

@MapperConfig(componentModel = "cdi",
        mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_FROM_CONFIG)
public class ServiceMapperConfiguration {
}
