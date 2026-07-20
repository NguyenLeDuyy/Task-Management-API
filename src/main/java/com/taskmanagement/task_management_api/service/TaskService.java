package com.taskmanagement.task_management_api.service;

import java.util.List;

import com.taskmanagement.task_management_api.dto.request.TaskCreateRequest;
import com.taskmanagement.task_management_api.dto.request.TaskUpdateRequest;
import com.taskmanagement.task_management_api.dto.response.TaskResponse;

public interface TaskService {
    public TaskResponse createTask(TaskCreateRequest request);

    public List<TaskResponse> findAllTasks();

    public TaskResponse updateTask(Long id, TaskUpdateRequest request);

    public void deleteTask(Long id);

    public TaskResponse getTaskById(Long id);
}
