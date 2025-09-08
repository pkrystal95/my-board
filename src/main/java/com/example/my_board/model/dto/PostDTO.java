package com.example.my_board.model.dto;

import lombok.Getter;
import lombok.Setter;

public class PostDTO {
    @Getter
    @Setter
    public static class Request {
        private String title;
        private String content;
        private String username;
    }
}
