package com.example.gradleannotateuse15.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.emilnasyrov.lib.response.modules.AbstractResponse;
import ru.emilnasyrov.lib.response.modules.Locals;

public class MyResponse extends AbstractResponse {
    @Override
    public ResponseEntity<?> generateError(HttpStatus httpStatus, int i, String s, Locals locals) {
        return new ResponseEntity<>(new MyResponse(), httpStatus);
    }
}
