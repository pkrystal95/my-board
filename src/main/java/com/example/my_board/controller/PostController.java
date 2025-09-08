package com.example.my_board.controller;

import com.example.my_board.model.dto.PostDTO;
import com.example.my_board.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller // 스캔
@RequiredArgsConstructor // 의존성
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;

    // 게시물 목록
    @GetMapping
    public String list(Model model) {
        model.addAttribute("posts", postService.findAll());
        return "post/list"; // templates/post/list.html
    }
    // 개별 게시물
    @GetMapping("/{id}")
    public String list(
            @PathVariable Long id,
            Model model) {
        // 각각 개별이니까... 1개.
        model.addAttribute("post", postService.findById(id));
        return "post/detail"; // templates/post/list.html
    }
    // 게시물 작성
    @GetMapping("/new")
    public String createForm(
            Model model, Authentication authentication) {
        PostDTO.Request dto = new PostDTO.Request();
        dto.setUsername(authentication.getName());
        model.addAttribute("post", dto);
        return "post/form"; // templates/post/list.html
    }
    // POST 처리...

    // 수정
    // 삭제
}