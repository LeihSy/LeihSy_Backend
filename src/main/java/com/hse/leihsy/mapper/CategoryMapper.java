package com.hse.leihsy.mapper;

import com.hse.leihsy.model.dto.CategoryDTO;
import com.hse.leihsy.model.entity.Category;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDTO toDTO(Category category);

    List<CategoryDTO> toDTOList(List<Category> categories);
}

