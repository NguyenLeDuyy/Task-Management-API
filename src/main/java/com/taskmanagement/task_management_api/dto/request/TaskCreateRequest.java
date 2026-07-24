package com.taskmanagement.task_management_api.dto.request;

import java.time.LocalDate;
import java.util.List;
import com.taskmanagement.task_management_api.entity.enums.TaskPriority;
import com.taskmanagement.task_management_api.entity.enums.TaskStatus;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TaskCreateRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private List<Long> tagIds;

    private LocalDate dueDate;
}
