package com.taskmanagement.task_management_api.service;

import java.util.List;

import com.taskmanagement.task_management_api.dto.request.TagRequest;
import com.taskmanagement.task_management_api.dto.response.TagResponse;

public interface TagService {
    TagResponse createTag(TagRequest request);

    List<TagResponse> getAllTags();

    TagResponse getTagById(Long id);

    TagResponse updateTag(Long id, TagRequest request);

    void deleteTag(Long id);
}
