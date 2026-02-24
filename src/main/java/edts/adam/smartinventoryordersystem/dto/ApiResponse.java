package edts.adam.smartinventoryordersystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.Instant;

/**
 * Generic API response wrapper providing a consistent structure for all
 * endpoints.
 * Includes success/error status, HTTP code, message, data payload, and
 * timestamp.
 *
 * @param <T> the type of the response data payload
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "success", "code", "message", "data", "timestamp" })
public class ApiResponse<T> {

    private boolean success;
    private String code;
    private String message;
    private T data;
    private Instant timestamp;

    public ApiResponse() {
        this.timestamp = Instant.now();
    }

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now();
    }

    public ApiResponse(boolean success, String code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now();
    }

    // === Static Factory Methods ===

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static ApiResponse<Object> error(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }

    // === Getters & Setters ===
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
