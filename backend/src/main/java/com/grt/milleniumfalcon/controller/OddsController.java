package com.grt.milleniumfalcon.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OddsController {
    @GetMapping("")
    public String example() {
        return "Hello world";
    }
}
