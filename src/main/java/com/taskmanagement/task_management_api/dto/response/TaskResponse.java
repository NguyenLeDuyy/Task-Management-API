package com.taskmanagement.task_management_api.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.taskmanagement.task_management_api.entity.enums.TaskPriority;
import com.taskmanagement.task_management_api.entity.enums.TaskStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate dueDate;
    private Long userId;
    private List<TagResponse> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
