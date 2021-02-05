package com.example.annotateusespring15.controllers;

import com.example.annotateusespring15.exceptions.DefaultException;
import com.example.annotateusespring15.exceptions.MyExceptions;
import com.example.annotateusespring15.exceptions.UnitpayException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MyController {

    @GetMapping("/getUnitpayException")
    public HttpStatus getUnitpayException(){

        throw new UnitpayException();
    }

    @GetMapping("/getDefaultException")
    public HttpStatus getDefaultException() {

        throw new DefaultException();
    }

    @GetMapping("/getMyExceptions")
    public HttpStatus getMyExceptions(){

        throw new MyExceptions();
    }
}
