package com.hse.leihsy.mapper;

import com.hse.leihsy.model.dto.InsyImportItemDTO;
import com.hse.leihsy.model.entity.InsyImportItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct Mapper fuer InsyImportItem Entity <-> DTO Konvertierung.
 *
 * hasMatchingProduct, matchingProductId und matchingProductName
 * werden im Service gesetzt, nicht hier gemappt.
 */
@Mapper(componentModel = "spring")
public interface InsyImportMapper {

    @Mapping(target = "importedProductId", source = "importedProduct.id")
    @Mapping(target = "importedProductName", source = "importedProduct.name")
    @Mapping(target = "importedItemId", source = "importedItem.id")
    @Mapping(target = "importedItemInvNumber", source = "importedItem.invNumber")
    @Mapping(target = "hasMatchingProduct", ignore = true)
    @Mapping(target = "matchingProductId", ignore = true)
    @Mapping(target = "matchingProductName", ignore = true)
    InsyImportItemDTO toDTO(InsyImportItem entity);

    List<InsyImportItemDTO> toDTOList(List<InsyImportItem> entities);
}