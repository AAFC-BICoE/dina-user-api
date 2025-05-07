package ca.gc.aafc.dinauser.api.mapper;

import java.util.Set;

import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import ca.gc.aafc.dina.mapper.DinaMapperV2;
import ca.gc.aafc.dinauser.api.dto.UserPreferenceDto;
import ca.gc.aafc.dinauser.api.entity.UserPreference;

@Mapper
public interface UserPreferenceMapper extends DinaMapperV2<UserPreferenceDto, UserPreference> {

  UserPreferenceMapper INSTANCE = Mappers.getMapper(UserPreferenceMapper.class);

  UserPreferenceDto toDto(UserPreference entity, @Context Set<String> provided, @Context String scope);

  UserPreference toEntity(UserPreferenceDto dto, @Context Set<String> provided, @Context String scope);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void patchEntity(@MappingTarget UserPreference entity, UserPreferenceDto dto, @Context Set<String> provided, @Context String scope);
}
