package ru.emilnasyrov.lib.unitpay.modules;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.emilnasyrov.lib.unitpay.annotates.HttpException;

public class ExceptionDateResponse extends AbstractResponse {
    private Integer status;
    private String error;
    private String message;
    private Integer code;
    private String local;

    public ExceptionDateResponse(){}

    public ExceptionDateResponse (Integer status, String error, String message, Integer code, String local){
        this.status = status;
        this.code = code;
        this.local = local;
        this.error = error;
        this.message = message;
    }

    public Integer getCode() { return code; }

    public Integer getStatus() { return status; }

    public String getError() { return error; }

    public String getLocal() { return local; }

    public String getMessage() { return message; }

    public void setCode(Integer code) { this.code = code; }

    public void setError(String error) { this.error = error; }

    public void setLocal(String local) { this.local = local; }

    public void setMessage(String message) { this.message = message; }

    public void setStatus(Integer status) { this.status = status; }

    private Integer getCode(HttpStatus httpStatus){
        return Integer.decode(httpStatus.toString().substring(0, 3));
    }

    @Override
    public ResponseEntity<?> generateError(HttpStatus status, int code, String message, Locals locals) {
        return new ResponseEntity<>(new ExceptionDateResponse(
                getCode(status),
                status.getReasonPhrase(),
                message,
                code,
                locals.toString()
        ), status);
    }
}
