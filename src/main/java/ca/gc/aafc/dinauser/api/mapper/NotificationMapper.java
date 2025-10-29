package ca.gc.aafc.dinauser.api.mapper;

import java.util.Set;

import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import ca.gc.aafc.dina.mapper.DinaMapperV2;
import ca.gc.aafc.dinauser.api.dto.NotificationDto;
import ca.gc.aafc.dinauser.api.entity.Notification;

@Mapper
public interface NotificationMapper extends DinaMapperV2<NotificationDto, Notification> {

  NotificationMapper INSTANCE = Mappers.getMapper(NotificationMapper.class);

  NotificationDto toDto(Notification entity, @Context Set<String> provided, @Context String scope);

  Notification toEntity(NotificationDto dto, @Context Set<String> provided, @Context String scope);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void patchEntity(@MappingTarget Notification entity, NotificationDto dto, @Context Set<String> provided, @Context String scope);
}
