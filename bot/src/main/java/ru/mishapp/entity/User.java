package ru.mishapp.entity;

import jakarta.annotation.Nonnull;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Table("app_user")
@Getter
public class User {

    @Id
    private final Long id;
    @Nonnull
    private final String login;
    @Nonnull
    private final String password;
    @Nonnull
    private final String userZone;
    @MappedCollection(idColumn = "user_id")
    private final Set<UserAccountRef> accounts;

    public User(@Nonnull String login, @Nonnull String password, @Nonnull String userZone) {
        this(null, login, password, userZone, new HashSet<>());
    }

    @PersistenceCreator
    public User(Long id, @Nonnull String login, @Nonnull String password, @Nonnull String userZone, Set<UserAccountRef> accounts) {
        this.id = id;
        this.login = Objects.requireNonNull(login, "login");
        this.password = Objects.requireNonNull(password, "password");
        this.userZone = Objects.requireNonNull(userZone, "userZone");
        this.accounts = accounts;
    }
}
