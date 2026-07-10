package ca.gc.aafc.dina.user.api.mapper;

import java.util.Set;

import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import ca.gc.aafc.dina.mapper.DinaMapperV2;
import ca.gc.aafc.dina.user.api.dto.NotificationDto;
import ca.gc.aafc.dina.user.api.entity.Notification;

@Mapper
public interface NotificationMapper extends DinaMapperV2<NotificationDto, Notification> {

  NotificationMapper INSTANCE = Mappers.getMapper(NotificationMapper.class);

  NotificationDto toDto(Notification entity, @Context Set<String> provided, @Context String scope);

  @Mapping(target = "id", ignore = true)
  Notification toEntity(NotificationDto dto, @Context Set<String> provided, @Context String scope);

  @Mapping(target = "id", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void patchEntity(@MappingTarget Notification entity, NotificationDto dto, @Context Set<String> provided, @Context String scope);
}
