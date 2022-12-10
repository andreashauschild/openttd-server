package de.litexo.model.mapper;

import de.litexo.api.ServiceRuntimeException;
import de.litexo.model.external.OpenttdServerConfigUpdate;
import de.litexo.model.internal.InternalOpenttdServerConfig;
import de.litexo.model.external.OpenttdServerConfigGet;
import de.litexo.security.SecurityUtils;
import org.mapstruct.AfterMapping;
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
public abstract class OpenttdServerConfigMapper {
    public abstract OpenttdServerConfigGet toExternal(InternalOpenttdServerConfig internal);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "path", ignore = true)
    @Mapping(target = "passwordSha256Hash", ignore = true)
    @Mapping(target = "servers", ignore = true)
    // @formatter:on
    public abstract void patch(OpenttdServerConfigUpdate external, @MappingTarget InternalOpenttdServerConfig internal);

    @AfterMapping
    protected void patchAfterMapping(OpenttdServerConfigUpdate external, @MappingTarget InternalOpenttdServerConfig internal) {
        if (external.getPassword() != null && external.getOldPassword() != null) {
            if (SecurityUtils.isEquals(external.getOldPassword(), internal.getPasswordSha256Hash())) {
                internal.setPasswordSha256Hash(SecurityUtils.toSHA256(external.getPassword()));
            } else {
                throw new ServiceRuntimeException("Could not update password. Old password is not correct!");
            }

        }
    }


}
