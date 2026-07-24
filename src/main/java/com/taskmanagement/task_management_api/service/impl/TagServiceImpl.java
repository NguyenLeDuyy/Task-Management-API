package com.taskmanagement.task_management_api.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.taskmanagement.task_management_api.dto.request.TagRequest;
import com.taskmanagement.task_management_api.dto.response.TagResponse;
import com.taskmanagement.task_management_api.entity.Tag;
import com.taskmanagement.task_management_api.entity.User;
import com.taskmanagement.task_management_api.exception.ResourceNotFoundException;
import com.taskmanagement.task_management_api.exception.UnauthorizedException;
import com.taskmanagement.task_management_api.mapper.TagMapper;
import com.taskmanagement.task_management_api.repository.TagRepository;
import com.taskmanagement.task_management_api.repository.UserRepository;
import com.taskmanagement.task_management_api.service.TagService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final TagMapper tagMapper;

    @Override
    public TagResponse createTag(TagRequest request) {
        User user = getCurrentUser();

        // Kiểm tra xem tag name đã tồn tại cho user này chưa
        if (tagRepository.existsByTagNameAndUser_Id(request.getTagName(), user.getId())) {
            throw new IllegalArgumentException("Tag đã tồn tại!");
        }

        Tag tag = new Tag();
        tag.setTagName(request.getTagName());
        tag.setUser(user);
        tag.setCreatedAt(LocalDateTime.now());
        tag.setUpdatedAt(LocalDateTime.now());

        Tag savedTag = tagRepository.save(tag);
        return tagMapper.toResponse(savedTag);
    }

    @Override
    public List<TagResponse> getAllTags() {
        User user = getCurrentUser();
        List<Tag> tags = tagRepository.findByUser_Id(user.getId());
        return tags.stream()
                .map(tagMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TagResponse getTagById(Long id) {
        User user = getCurrentUser();
        Tag tag = getTagAndCheckOwnership(id, user);
        return tagMapper.toResponse(tag);
    }

    @Override
    public TagResponse updateTag(Long id, TagRequest request) {
        User user = getCurrentUser();
        Tag currentTag = getTagAndCheckOwnership(id, user);

        // Kiểm tra xem tag name mới có trùng với tag khác không
        if (!currentTag.getTagName().equals(request.getTagName()) &&
                tagRepository.existsByTagNameAndUser_Id(request.getTagName(), user.getId())) {
            throw new IllegalArgumentException("Tag đã tồn tại!");
        }

        currentTag.setTagName(request.getTagName());
        currentTag.setUpdatedAt(LocalDateTime.now());

        Tag updatedTag = tagRepository.save(currentTag);
        return tagMapper.toResponse(updatedTag);
    }

    @Override
    public void deleteTag(Long id) {
        User user = getCurrentUser();
        Tag currentTag = getTagAndCheckOwnership(id, user);
        tagRepository.delete(currentTag);
    }

    private User getCurrentUser() {
        String loggedInUserEmail = SecurityContextHolder
                .getContext().getAuthentication().getName();
        return userRepository.findByEmail(loggedInUserEmail)
                .orElseThrow(() -> new UnauthorizedException("User không tồn tại hoặc phiên bản đăng nhập hết hạn!"));
    }

    private Tag getTagAndCheckOwnership(Long id, User user) {
        return tagRepository.findByIdAndUser_Id(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tag không tồn tại!"));
    }
}
