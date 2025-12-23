package com.hse.leihsy.mapper;

import com.hse.leihsy.model.dto.LocationDTO;
import com.hse.leihsy.model.entity.Location;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationDTO toDTO(Location location);

    List<LocationDTO> toDTOList(List<Location> locations);
}

