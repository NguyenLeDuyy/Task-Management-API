package com.taskmanagement.task_management_api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.taskmanagement.task_management_api.dto.response.TagResponse;
import com.taskmanagement.task_management_api.entity.Tag;

@Mapper(componentModel = "spring")
public interface TagMapper {
    TagMapper INSTANCE = Mappers.getMapper(TagMapper.class);

    TagResponse toResponse(Tag tag);
}
