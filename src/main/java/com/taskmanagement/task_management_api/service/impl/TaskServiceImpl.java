package com.taskmanagement.task_management_api.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.taskmanagement.task_management_api.exception.ResourceNotFoundException;
import com.taskmanagement.task_management_api.exception.UnauthorizedException;
import com.taskmanagement.task_management_api.mapper.TaskMapper;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.taskmanagement.task_management_api.dto.request.TaskCreateRequest;
import com.taskmanagement.task_management_api.dto.request.TaskUpdateRequest;
import com.taskmanagement.task_management_api.dto.response.TaskResponse;
import com.taskmanagement.task_management_api.entity.Tag;
import com.taskmanagement.task_management_api.entity.Task;
import com.taskmanagement.task_management_api.entity.User;
import com.taskmanagement.task_management_api.entity.enums.TaskPriority;
import com.taskmanagement.task_management_api.entity.enums.TaskStatus;
import com.taskmanagement.task_management_api.repository.TagRepository;
import com.taskmanagement.task_management_api.repository.TaskRepository;
import com.taskmanagement.task_management_api.repository.UserRepository;
import com.taskmanagement.task_management_api.service.TaskService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;

    private final TagRepository tagRepository;

    private final TaskMapper taskMapper;

    @Override
    public TaskResponse createTask(TaskCreateRequest request) {

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());

        task.setStatus(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO);
        task.setPriority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM);

        task.setDueDate(request.getDueDate());
        task.setTags(validateAndGetTags(request.getTagIds()));

        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        User user = getCurrentUser();

        task.setUser(user);

        Task saveTask = taskRepository.save(task);
        return taskMapper.toResponse(saveTask);
    }

    @Override
    public List<TaskResponse> findAllTasks() {
        User user = getCurrentUser();

        List<Task> allTask = taskRepository.findByUserId(user.getId());

        return allTask.stream()
                .map(taskMapper::toResponse)
                .toList();

    }

    @Override
    public TaskResponse updateTask(Long id, TaskUpdateRequest request) {

        User user = getCurrentUser();

        Task currentTask = getTaskAndCheckOwnership(id, user);

        currentTask.setTitle(request.getTitle());
        currentTask.setDescription(request.getDescription());
        currentTask.setStatus(request.getStatus());
        currentTask.setPriority(request.getPriority());
        currentTask.setTags(validateAndGetTags(request.getTagIds()));
        currentTask.setDueDate(request.getDueDate());
        currentTask.setUpdatedAt(LocalDateTime.now());

        return taskMapper.toResponse(taskRepository.save(currentTask));
    }

    @Override
    public void deleteTask(Long id) {
        User user = getCurrentUser();

        Task currentTask = getTaskAndCheckOwnership(id, user);

        taskRepository.delete(currentTask);
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

    private List<Tag> validateAndGetTags(List<Long> tagIds) {
        List<Tag> tagToSave = new ArrayList<>();
        if (tagIds != null && !tagIds.isEmpty()) {
            tagToSave = tagRepository.findAllById(tagIds);
            Set<Tag> dbTags = new HashSet<>(tagToSave);

            Set<Long> idDbTags = dbTags.stream().map(e -> e.getId()).collect(Collectors.toSet());

            Set<Long> outsideTags = tagIds.stream().filter(e -> !idDbTags.contains(e))
                    .collect(Collectors.toSet());
            if (!outsideTags.isEmpty()) {
                throw new ResourceNotFoundException("Không tồn tại các tags có id: " + outsideTags);
            }
        }
        return tagToSave;
    }

    @Override
    public TaskResponse getTaskById(Long id) {

        User user = getCurrentUser();

        Task currentTask = getTaskAndCheckOwnership(id, user);

        return taskMapper.toResponse(currentTask);
    }

}
