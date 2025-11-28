package com.hse.leihsy.mapper;

import com.hse.leihsy.model.dto.BookingDTO;
import com.hse.leihsy.model.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "receiver.id", target = "receiverId")
    @Mapping(source = "receiver.name", target = "receiverName")
    @Mapping(source = "item.id", target = "itemId")
    @Mapping(source = "item.invNumber", target = "itemInvNumber")
    @Mapping(source = "item.product.id", target = "productId")
    @Mapping(source = "item.product.name", target = "productName")
    @Mapping(source = "proposalBy.id", target = "proposalById")
    @Mapping(source = "proposalBy.name", target = "proposalByName")
    BookingDTO toDTO(Booking booking);

    List<BookingDTO> toDTOs(List<Booking> bookings);
}