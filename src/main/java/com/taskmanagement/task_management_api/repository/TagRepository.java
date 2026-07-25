package com.taskmanagement.task_management_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taskmanagement.task_management_api.entity.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByUser_Id(Long userId);

    Optional<Tag> findByIdAndUser_Id(Long id, Long userId);

    List<Tag> findByIdInAndUser_Id(List<Long> ids, Long userId);

    boolean existsByTagNameAndUser_Id(String tagName, Long userId);
}
