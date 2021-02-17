package com.example.gradleannotateuse15.controllers;

import com.example.gradleannotateuse15.exceptions.DefaultException;
import com.example.gradleannotateuse15.exceptions.MyExceptions;
import com.example.gradleannotateuse15.exceptions.UnitpayException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
//import com.example.gradleannotateuse15.service.CodeGlobalErrorService;

@RestController
@AllArgsConstructor
public class MyController {
    //private final CodeGlobalErrorService codeGlobalErrorService;

    @GetMapping("/getUnitpayException")
    public HttpStatus getUnitpayException(){

        throw new UnitpayException();
    }

    @GetMapping("/check")
    public HttpStatus check(){

        return HttpStatus.OK;
    }

    @GetMapping("/getDefaultException")
    public HttpStatus getDefaultException() throws DefaultException {

        throw new DefaultException();
    }

    @GetMapping("/getMyExceptions")
    public HttpStatus getMyExceptions(){

        throw new MyExceptions();
    }

    @GetMapping("/setNewGlobalError")
    public HttpStatus setNewGlobalError(){
//        codeGlobalErrorService.addNewGlobalError(
//                1000,
//                "",
//                "",
//                1
//        );
        return HttpStatus.OK;
    }
}
