package com.hse.leihsy.mapper;

import com.hse.leihsy.model.dto.ProductDTO;
import com.hse.leihsy.model.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "locationId", source = "location.id")
    @Mapping(target = "locationRoomNr", source = "location.roomNr")
    @Mapping(target = "availableItems", expression = "java(product.getAvailableItemCount())")
    @Mapping(target = "totalItems", expression = "java(product.getTotalItemCount())")
    ProductDTO toDTO(Product product);

    List<ProductDTO> toDTOList(List<Product> products);
}