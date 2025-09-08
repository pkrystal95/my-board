package com.example.my_board.service;

import com.example.my_board.model.dto.PostDTO;
import com.example.my_board.model.entity.Post;
import com.example.my_board.model.entity.UserAccount;
import com.example.my_board.model.repository.PostRepository;
import com.example.my_board.model.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserAccountRepository userAccountRepository;

    // 1. create
    @Transactional
    public Post createPost(PostDTO.Request dto) {
        UserAccount userAccount = userAccountRepository
                .findByUsername(dto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        Post post = new Post();
        post.setAuthor(userAccount);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        return postRepository.save(post);
    }
    // 2-1. findAll
    @Transactional(readOnly = true)
    public List<Post> findAll() {
        return postRepository.findAll();
    }
    // 2-2. findOne (byId...)
    @Transactional(readOnly = true)
    public Post findById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시물 없음"));
    }

    // -------------------------
    // 3. update
    // 4. delete
}