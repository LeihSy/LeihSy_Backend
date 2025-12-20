package com.hse.leihsy.mapper;

import com.hse.leihsy.model.dto.BookingDTO;
import com.hse.leihsy.model.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "lenderId", source = "lender.id")
    @Mapping(target = "lenderName", source = "lender.name")
    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemInvNumber", source = "item.invNumber")
    @Mapping(target = "productId", source = "item.product.id")
    @Mapping(target = "productName", source = "item.product.name")
    @Mapping(target = "proposalById", source = "proposalBy.id")
    @Mapping(target = "proposalByName", source = "proposalBy.name")
    @Mapping(target = "status", expression = "java(booking.calculateStatus().name())")
    BookingDTO toDTO(Booking booking);

    List<BookingDTO> toDTOList(List<Booking> bookings);
}