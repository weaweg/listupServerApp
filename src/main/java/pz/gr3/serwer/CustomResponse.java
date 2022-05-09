package pz.gr3.serwer;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import java.time.LocalDateTime;

@JsonAppend
public class CustomResponse {
    private LocalDateTime timestamp;
    private String message;

    public CustomResponse(String message) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
