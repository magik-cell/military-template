package ua.edu.viti.military.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ua.edu.viti.military.dto.request.MilitaryUnitCreateDTO;
import ua.edu.viti.military.dto.request.MilitaryUnitUpdateDTO;
import ua.edu.viti.military.dto.response.MilitaryUnitResponseDTO;
import ua.edu.viti.military.entity.MilitaryUnit;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UnitTypeMapper.class})
public interface MilitaryUnitMapper {
    
    // Entity → ResponseDTO (автоматичний маппінг)
    MilitaryUnitResponseDTO toResponseDTO(MilitaryUnit entity);
    
    // List<Entity> → List<ResponseDTO>
    List<MilitaryUnitResponseDTO> toResponseDTOList(List<MilitaryUnit> entities);
    
    // CreateDTO → Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "unitType", ignore = true)
    @Mapping(target = "parentUnit", ignore = true)
    @Mapping(target = "commander", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MilitaryUnit toEntity(MilitaryUnitCreateDTO dto);
    
    // UpdateDTO → Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "unitType", ignore = true)
    @Mapping(target = "parentUnit", ignore = true)
    @Mapping(target = "commander", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(MilitaryUnitUpdateDTO dto, @MappingTarget MilitaryUnit entity);
}
