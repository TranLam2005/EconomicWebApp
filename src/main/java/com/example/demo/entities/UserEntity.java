package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.*;

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
}
