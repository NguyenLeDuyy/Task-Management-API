package com.taskmanagement.task_management_api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.taskmanagement.task_management_api.dto.response.TaskResponse;
import com.taskmanagement.task_management_api.entity.Task;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    TaskMapper INSTANCE = Mappers.getMapper(TaskMapper.class);

    @Mapping(source = "user.id", target = "userId")
    TaskResponse toResponse(Task task);
}
