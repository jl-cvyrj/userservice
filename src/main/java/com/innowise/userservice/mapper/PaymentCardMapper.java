package com.innowise.userservice.mapper;

import com.innowise.userservice.dto.PaymentCardDto;
import com.innowise.userservice.entity.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {

    PaymentCardMapper INSTANCE = Mappers.getMapper(PaymentCardMapper.class);

    @Mapping(source = "user.id", target = "userId")
    PaymentCardDto toDto(PaymentCard card);

    @Mapping(source = "userId", target = "user.id")
    PaymentCard toEntity(PaymentCardDto cardDto);
}