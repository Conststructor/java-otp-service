package otpservice.model;

import java.time.LocalDateTime;

public class OtpCode {
    private Long id;
    private String code;
    private OtpStatus status;
    private String operationId;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public OtpCode() {}
    public OtpCode(Long id, Long userId, String operationId, String code,
                   OtpStatus status, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.id = id;
        this.code = code;
        this.status = status;
        this.operationId = operationId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public OtpStatus getStatus() { return status; }
    public void setStatus(OtpStatus status) { this.status = status; }
    public String getOperationId() { return operationId; }
    public void setOperationId(String operationId) { this.operationId = operationId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}