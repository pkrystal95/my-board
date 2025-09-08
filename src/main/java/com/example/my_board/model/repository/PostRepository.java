package com.example.my_board.model.repository;

import com.example.my_board.model.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    // 검색과 페이징을 위한 메서드...(?) JPA Query Method...
}
