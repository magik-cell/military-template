package ua.edu.viti.military.mapper;

import org.mapstruct.*;
import ua.edu.viti.military.dto.request.PersonnelCreateDTO;
import ua.edu.viti.military.dto.request.PersonnelUpdateDTO;
import ua.edu.viti.military.dto.response.PersonnelResponseDTO;
import ua.edu.viti.military.entity.Personnel;

import java.util.List;

@Mapper(componentModel = "spring", uses = {MilitaryUnitMapper.class})
public interface PersonnelMapper {
    
    // Entity → ResponseDTO (автоматичний маппінг)
    PersonnelResponseDTO toResponseDTO(Personnel entity);
    
    // List<Entity> → List<ResponseDTO>
    List<PersonnelResponseDTO> toResponseDTOList(List<Personnel> entities);
    
    // CreateDTO → Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "unit", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Personnel toEntity(PersonnelCreateDTO dto);
    
    // UpdateDTO → Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "militaryId", ignore = true)
    @Mapping(target = "unit", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(PersonnelUpdateDTO dto, @MappingTarget Personnel entity);
}
