package com.hse.leihsy.mapper;

import com.hse.leihsy.model.dto.BookingDTO;
import com.hse.leihsy.model.entity.Booking;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import java.time.LocalDateTime;

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
    @Mapping(target = "urgent", ignore = true) // Wird im AfterMapping berechnet
    BookingDTO toDTO(Booking booking);

    List<BookingDTO> toDTOList(List<Booking> bookings);

    // Logik zur Berechnung der Dringlichkeit
    @AfterMapping
    default void calculateUrgency(Booking booking, @MappingTarget BookingDTO dto) {
        if (booking.getCreatedAt() != null) {
            // Berechne Zeit seit Erstellung
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime limit20h = booking.getCreatedAt().plusHours(20);

            // Wenn 20 Stunden vergangen sind (also < 4h übrig von den 24h), ist es dringend
            // Und nur wenn der Status noch PENDING ist (keine Vorschläge gemacht)
            if (now.isAfter(limit20h) && dto.getStatus().equals("PENDING")) dto.setUrgent(true);
            else {
                dto.setUrgent(false);
            }
        }
    }

}