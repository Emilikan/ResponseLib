package com.example.annotateusespring15.controllers;

import com.example.annotateusespring15.exceptions.DefaultException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MyController {
    @GetMapping("/get")
    public HttpStatus get(){

        throw new DefaultException();
        //return HttpStatus.OK;
    }
}
