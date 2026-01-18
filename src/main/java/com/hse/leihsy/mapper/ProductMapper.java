package com.hse.leihsy.mapper;

import com.hse.leihsy.model.dto.ProductDTO;
import com.hse.leihsy.model.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import com.hse.leihsy.model.dto.ProductRelationDTO;

import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "locationId", source = "location.id")
    @Mapping(target = "locationRoomNr", source = "location.roomNr")
    @Mapping(target = "availableItems", expression = "java(product.getAvailableItemCount())")
    @Mapping(target = "totalItems", expression = "java(product.getTotalItemCount())")
    @Mapping(target = "relatedItems", expression = "java(mapRelatedItems(product))")
    ProductDTO toDTO(Product product);

    List<ProductDTO> toDTOList(List<Product> products);
    default List<ProductRelationDTO> mapRelatedItems(Product product) {
        if (product.getRecommendedSets() == null) {
            return Collections.emptyList();
        }
        
        return product.getRecommendedSets().stream()
            .map(set -> new ProductRelationDTO(
                set.getChildProduct().getId(),
                set.getChildProduct().getName(),
                set.getType() != null ? set.getType().name().toLowerCase() : "recommended"
            ))
            .collect(Collectors.toList());
    }

}