package com.megrez.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
public class TestController {
    @GetMapping
    public String test() {
        return "Here is analytics Service!";
    }
}
