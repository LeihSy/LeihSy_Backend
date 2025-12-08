package com.hse.leihsy.mapper;

import com.hse.leihsy.model.dto.ProductDTO;
import com.hse.leihsy.model.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring") // Makes this a Spring Bean (@Component)
public interface ProductMapper {

    // Map Entity to DTO
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "location.id", target = "locationId")
    @Mapping(source = "location.roomNr", target = "locationRoomNr")
    @Mapping(source = "lender.id", target = "lenderId")
    @Mapping(source = "lender.name", target = "lenderName")
    // These methods calculate counts in the Entity, MapStruct calls them automatically
    @Mapping(source = "availableItemCount", target = "availableItems")
    @Mapping(source = "totalItemCount", target = "totalItems")
    ProductDTO toDTO(Product product);

    // Map List of Entities to List of DTOs
    List<ProductDTO> toDTOs(List<Product> products);

    // Map DTO to Entity (for creation/updates)
    @Mapping(target = "category", ignore = true) // Handled manually in Service
    @Mapping(target = "location", ignore = true) // Handled manually in Service
    @Mapping(target = "lender", ignore = true)   // Handled manually in Service
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "recommendedSets", ignore = true)
    Product toEntity(ProductDTO productDTO);
}