package com.hse.leihsy.mapper;

import com.hse.leihsy.model.dto.BookingDTO;
import com.hse.leihsy.model.entity.Booking;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import java.time.LocalDateTime;
import com.hse.leihsy.model.entity.BookingStatus;
import com.hse.leihsy.model.entity.Item; 
import com.hse.leihsy.model.entity.BookingStatus;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "roomNr", expression = "java(resolveRoomNr(booking))")
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
    @Mapping(target = "overdue", ignore = true) // Ignorieren während main mapping
    @Mapping(target = "status", expression = "java(booking.calculateStatus().name())")
    @Mapping(source = "studentGroup.id", target = "groupId")
    @Mapping(source = "studentGroup.name", target = "groupName")
    @Mapping(target = "groupMemberNames", expression = "java(mapGroupMemberNames(booking.getStudentGroup()))")
    BookingDTO toDTO(Booking booking);

    List<BookingDTO> toDTOList(List<Booking> bookings);

    //Raum suche 
    default String resolveRoomNr(Booking booking) {
        if (booking == null || booking.getItem() == null) {
            return "-";
        }
        
        Item item = booking.getItem();
        
        if (item.getLocation() != null && item.getLocation().getRoomNr() != null) {
            return item.getLocation().getRoomNr();
        }
        
        if (item.getProduct() != null && item.getProduct().getLocation() != null) {
            return item.getProduct().getLocation().getRoomNr();
        }
        
        return "Unbekannt";
    }
    // Logik zur Berechnung der Dringlichkeit und Überfälligkeit
    @AfterMapping
    default void calculateComputedFields(Booking booking, @MappingTarget BookingDTO dto) {
        LocalDateTime now = LocalDateTime.now();

        // Dringend, wenn vor mehr als 20 Stunden erstellt und noch PENDING
        if (booking.getCreatedAt() != null) {
            LocalDateTime limit20h = booking.getCreatedAt().plusHours(20);
            if (now.isAfter(limit20h) && "PENDING".equals(dto.getStatus())) {
                dto.setUrgent(true);
            }
            else {
                dto.setUrgent(false);
            }
        }

            // Überfällig, wenn: Element den Status PICKED_UP (aktiv) hat UND das Enddatum in der Vergangenheit liegt
        boolean isActive = booking.getDistributionDate() != null && booking.getReturnDate() == null;

        if (isActive && booking.getEndDate().isBefore(now)) {
            dto.setOverdue(true);
        }
        else {
            dto.setOverdue(false);
        }
    }

    default List<String> mapGroupMemberNames(com.hse.leihsy.model.entity.StudentGroup group) {
        if (group == null || group.getMembers() == null) {
            return null;
        }
        return group.getMembers().stream()
                .map(com.hse.leihsy.model.entity.User::getName)
                .collect(java.util.stream.Collectors.toList());
    }
}