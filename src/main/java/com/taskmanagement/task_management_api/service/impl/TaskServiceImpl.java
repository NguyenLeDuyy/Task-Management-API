package com.taskmanagement.task_management_api.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import com.taskmanagement.task_management_api.exception.ResourceNotFoundException;
import com.taskmanagement.task_management_api.exception.UnauthorizedException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.taskmanagement.task_management_api.dto.request.TaskRequest;
import com.taskmanagement.task_management_api.dto.response.TaskResponse;
import com.taskmanagement.task_management_api.entity.Task;
import com.taskmanagement.task_management_api.entity.User;
import com.taskmanagement.task_management_api.entity.enums.TaskPriority;
import com.taskmanagement.task_management_api.entity.enums.TaskStatus;
import com.taskmanagement.task_management_api.repository.TaskRepository;
import com.taskmanagement.task_management_api.repository.UserRepository;
import com.taskmanagement.task_management_api.service.TaskService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;

    @Override
    public TaskResponse createTask(TaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());

        task.setStatus(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO);
        task.setPriority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM);

        task.setDueDate(request.getDueDate());
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        User user = getCurrentUser();

        task.setUser(user);

        Task saveTask = taskRepository.save(task);
        return mapToResponse(saveTask);
    }

    @Override
    public List<TaskResponse> findAllTasks() {
        User user = getCurrentUser();

        List<Task> allTask = taskRepository.findByUserId(user.getId());

        return allTask.stream()
                .map((task) -> mapToResponse(task))
                .toList();

    }

    @Override
    public TaskResponse updateTask(Long id, TaskRequest request) {

        User user = getCurrentUser();

        Task currentTask = getTaskAndCheckOwnership(id, user);

        if (request.getTitle() != null) {
            currentTask.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            currentTask.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            currentTask.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            currentTask.setPriority(request.getPriority());
        }
        if (request.getDueDate() != null) {
            currentTask.setDueDate(request.getDueDate());
        }
        currentTask.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(taskRepository.save(currentTask));
    }

    @Override
    public void deleteTask(Long id) {
        User user = getCurrentUser();

        Task currentTask = getTaskAndCheckOwnership(id, user);

        taskRepository.delete(currentTask);
    }

    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .userId(task.getUser().getId())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    private User getCurrentUser() {
        String loggedInUserEmail = SecurityContextHolder
                .getContext().getAuthentication().getName();
        return userRepository.findByEmail(loggedInUserEmail)
                .orElseThrow(() -> new UnauthorizedException("User không tồn tại hoặc phiên bản đăng nhập hết hạn!"));
    }

    private Task getTaskAndCheckOwnership(Long id, User user) {
        Task currentTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task không tồn tại!"));

        if (!user.getId().equals(currentTask.getUser().getId())) {
            throw new ResourceNotFoundException("Task không tồn tại!");
        }
        return currentTask;
    }

    @Override
    public TaskResponse getTaskById(Long id) {

        User user = getCurrentUser();

        Task currentTask = getTaskAndCheckOwnership(id, user);

        return mapToResponse(currentTask);
    }

}
