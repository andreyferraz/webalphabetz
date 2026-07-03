package com.alphabetz.webalphabetz.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PublicPagesController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/escola")
    public String escola() {
        return "escola";
    }

}
