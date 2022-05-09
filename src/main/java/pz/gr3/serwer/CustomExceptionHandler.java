package pz.gr3.serwer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.EntityNotFoundException;
import java.util.NoSuchElementException;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {EntityNotFoundException.class,
            NullPointerException.class,
            IllegalArgumentException.class,
            NoSuchElementException.class})
    protected ResponseEntity<Object> handleCriticalErrorExceptions() {
        return new ResponseEntity<>(new CustomResponse("Critical error"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
