package com.brendhacasaro.remi_central.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

@Table(name = "users")
@Entity
@Getter
@Setter
@RequiredArgsConstructor
@DynamicUpdate
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    @NonNull
    private String username;

    @Column(nullable = false)
    @NonNull
    private String password;
}
