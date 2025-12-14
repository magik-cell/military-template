package ua.edu.viti.military.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ua.edu.viti.military.dto.request.UnitTypeCreateDTO;
import ua.edu.viti.military.dto.request.UnitTypeUpdateDTO;
import ua.edu.viti.military.dto.response.UnitTypeResponseDTO;
import ua.edu.viti.military.entity.UnitType;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UnitTypeMapper {
    
    // Entity → ResponseDTO
    UnitTypeResponseDTO toResponseDTO(UnitType entity);
    
    // List<Entity> → List<ResponseDTO>
    List<UnitTypeResponseDTO> toResponseDTOList(List<UnitType> entities);
    
    // CreateDTO → Entity
    UnitType toEntity(UnitTypeCreateDTO dto);
    
    // Оновлення існуючого Entity з CreateDTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(UnitTypeCreateDTO dto, @MappingTarget UnitType entity);
    
    // Оновлення існуючого Entity з UpdateDTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromUpdateDTO(UnitTypeUpdateDTO dto, @MappingTarget UnitType entity);
}
