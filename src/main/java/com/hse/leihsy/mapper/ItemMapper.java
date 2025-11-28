package com.hse.leihsy.mapper;

import com.hse.leihsy.model.dto.ItemDTO;
import com.hse.leihsy.model.entity.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    ItemDTO toDTO(Item item);

    List<ItemDTO> toDTOs(List<Item> items);
}