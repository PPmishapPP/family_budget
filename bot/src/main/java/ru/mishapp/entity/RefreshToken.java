package ru.mishapp.entity;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("refresh_token")
@Getter
public class RefreshToken {

    @Id
    private final Long id;
    private final Long userId;
    private final String tokenHash;
    private final LocalDateTime expiresAt;
    private final boolean revoked;
    private final LocalDateTime createdAt;

    public RefreshToken(Long userId, String tokenHash, LocalDateTime expiresAt,
                        boolean revoked, LocalDateTime createdAt) {
        this(null, userId, tokenHash, expiresAt, revoked, createdAt);
    }

    @PersistenceCreator
    public RefreshToken(Long id, Long userId, String tokenHash, LocalDateTime expiresAt,
                        boolean revoked, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
        this.createdAt = createdAt;
    }

    public RefreshToken revoke() {
        return new RefreshToken(id, userId, tokenHash, expiresAt, true, createdAt);
    }
}
