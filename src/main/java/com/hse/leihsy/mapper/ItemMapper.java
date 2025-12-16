package com.hse.leihsy.mapper;

import com.hse.leihsy.model.dto.ItemDTO;
import com.hse.leihsy.model.entity.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct Mapper für Item Entity <-> DTO Konvertierung
 */
@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(target = "lenderId", source = "lender.id")
    @Mapping(target = "lenderName", source = "lender.name")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "isAvailable", expression = "java(item.isAvailable())")
    ItemDTO toDTO(Item item);

    List<ItemDTO> toDTOList(List<Item> items);
}