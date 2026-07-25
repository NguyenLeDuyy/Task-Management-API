package com.taskmanagement.task_management_api.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.taskmanagement.task_management_api.dto.request.TaskCreateRequest;
import com.taskmanagement.task_management_api.dto.request.TaskUpdateRequest;
import com.taskmanagement.task_management_api.dto.response.TaskResponse;
import com.taskmanagement.task_management_api.entity.Tag;
import com.taskmanagement.task_management_api.entity.Task;
import com.taskmanagement.task_management_api.entity.User;
import com.taskmanagement.task_management_api.exception.ResourceNotFoundException;
import com.taskmanagement.task_management_api.mapper.TaskMapper;
import com.taskmanagement.task_management_api.repository.TagRepository;
import com.taskmanagement.task_management_api.repository.TaskRepository;
import com.taskmanagement.task_management_api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Test
    void createTask_ShouldReturnTaskResponse_WhenValidRequest() {
        // 1. ARRANGE
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(
                new UsernamePasswordAuthenticationToken("test@gmail.com", "password"));
        SecurityContextHolder.setContext(securityContext);

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@gmail.com");

        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("Học Spring Boot");
        request.setTagIds(List.of());

        Task mockSaveTask = new Task();
        mockSaveTask.setId(100L);
        mockSaveTask.setTitle("Học Spring Boot");

        TaskResponse mockResponse = new TaskResponse();
        mockResponse.setId(100L);
        mockResponse.setTitle("Học Spring Boot");

        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));
        when(taskRepository.save(any(Task.class))).thenReturn(mockSaveTask);
        when(taskMapper.toResponse(any(Task.class))).thenReturn(mockResponse);
        // 2. ACT
        TaskResponse actualResponse = taskService.createTask(request);

        // 3. ASSERT
        assertNotNull(actualResponse);
        assertEquals(100L, actualResponse.getId());
        assertEquals("Học Spring Boot", actualResponse.getTitle());
    }

    @Test
    void createTask_ShouldThrowResourseNotFound_WhenTagIdFake() {
        // 1. ARRANGE (Dựng bối cảnh, mớm kịch bản cho Mock)
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTagIds(List.of(1L, 999L));

        Tag mockTag = new Tag();
        mockTag.setId(1L);

        when(tagRepository.findAllById(request.getTagIds())).thenReturn(List.of(mockTag));
        // 2. ACT

        // 3. ASSERT
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.createTask(request);
        });

        Mockito.verify(taskRepository, Mockito.never()).save(Mockito.any(Task.class));
    }

    @Test
    void updateTask_ShouldThrowResourceNotFound_WhenUserIsNotOwner() {
        // 1. ARRANGE
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(
                new UsernamePasswordAuthenticationToken("test@gmail.com", "password"));
        SecurityContextHolder.setContext(securityContext);

        User intruder = new User();
        intruder.setId(1L);
        intruder.setEmail("test@gmail.com");

        User victim = new User();
        victim.setId(2L);

        TaskUpdateRequest request = new TaskUpdateRequest();

        Task mockUpdateTask = new Task();
        mockUpdateTask.setId(100L);
        mockUpdateTask.setUser(victim);

        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(intruder));
        when(taskRepository.findById(100L)).thenReturn(Optional.of(mockUpdateTask));
        // 2. ACT

        // 3. ASSERT
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.updateTask(100L, request);
        });

        Mockito.verify(taskRepository, Mockito.never()).save(Mockito.any(Task.class));
    }

    @Test
    void deleteTask_ShouldThrowResourceNotFound_WhenUserIsNotOwner() {
        // 1. ARRANGE
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(
                new UsernamePasswordAuthenticationToken("test@gmail.com", "password"));
        SecurityContextHolder.setContext(securityContext);

        User intruder = new User();
        intruder.setId(1L);
        intruder.setEmail("test@gmail.com");

        User victim = new User();
        victim.setId(2L);

        Task mockTask = new Task();
        mockTask.setId(100L);
        mockTask.setUser(victim);

        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(intruder));
        when(taskRepository.findById(100L)).thenReturn(Optional.of(mockTask));
        // 2. ACT

        // 3. ASSERT
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.deleteTask(100L);
        });

        Mockito.verify(taskRepository, Mockito.never()).delete(Mockito.any(Task.class));
    }

    @Test
    void deleteTask_WhenValidId() {
        // 1. ARRANGE
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(
                new UsernamePasswordAuthenticationToken("test@gmail.com", "password"));
        SecurityContextHolder.setContext(securityContext);

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@gmail.com");

        Task mockTask = new Task();
        mockTask.setId(100L);
        mockTask.setUser(mockUser);

        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));
        when(taskRepository.findById(100L)).thenReturn(Optional.of(mockTask));

        // 2. ACT
        taskService.deleteTask(mockTask.getId());

        // 3. ASSERT
        Mockito.verify(taskRepository, Mockito.times(1)).delete(mockTask);
    }
}
