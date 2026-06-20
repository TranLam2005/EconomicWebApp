package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    //  primary key of table users auto increment
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "varchar(255)", nullable = false, name = "first_name")
    private String firstName;

    @Column(columnDefinition = "varchar(255)", nullable = false, name = "last_name")
    private String lastName;

    // password
    @Column(columnDefinition = "varchar(255)", nullable = false, name = "password_hash")
    private String password;

    // email
    @Column(columnDefinition = "varchar(255)", nullable = false)
    private String email;

    // role of user
    @Column(columnDefinition = "varchar(100)", nullable = false)
    private String role = "USER";

    @Column(columnDefinition = "datetime", updatable = false)
    private LocalDateTime createdAt;

    @Column(columnDefinition = "datetime", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
