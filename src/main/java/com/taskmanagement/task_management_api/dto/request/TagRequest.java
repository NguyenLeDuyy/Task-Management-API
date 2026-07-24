package com.taskmanagement.task_management_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TagRequest {
    @NotBlank(message = "Tag name is mandatory")
    private String tagName;
}
