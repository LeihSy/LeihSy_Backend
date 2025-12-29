package com.hse.leihsy.mapper;

import com.hse.leihsy.model.dto.StudentGroupDTO;
import com.hse.leihsy.model.entity.StudentGroup;
import com.hse.leihsy.model.entity.User;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MapStruct Mapper für StudentGroup Entity <-> DTO Konvertierung
 */
@Mapper(componentModel = "spring")
public interface StudentGroupMapper {

    /**
     * Konvertiert StudentGroup Entity zu DTO
     */
    @Mapping(source = "createdBy.id", target = "createdById")
    @Mapping(source = "createdBy.name", target = "createdByName")
    @Mapping(target = "members", expression = "java(mapMembers(entity.getMembers(), entity.getCreatedBy()))")
    @Mapping(target = "memberCount", expression = "java(entity.getMemberCount())")
    @Mapping(target = "activeBookingsCount", ignore = true) // Wird im Service gesetzt
    StudentGroupDTO toDTO(StudentGroup entity);

    /**
     * Konvertiert Liste von Entities zu DTOs
     */
    List<StudentGroupDTO> toDTOList(List<StudentGroup> entities);

    /**
     * Mapped die Mitglieder zu GroupMemberDTOs
     */
    default List<StudentGroupDTO.GroupMemberDTO> mapMembers(Set<User> members, User owner) {
        if (members == null) {
            return List.of();
        }
        return members.stream()
                .map(user -> StudentGroupDTO.GroupMemberDTO.builder()
                        .userId(user.getId())
                        .userName(user.getName())
                        .userEmail(user.getUniqueId()) // uniqueId ist die Email/Keycloak-ID
                        .isOwner(owner != null && owner.getId().equals(user.getId()))
                        .build())
                .collect(Collectors.toList());
    }
}