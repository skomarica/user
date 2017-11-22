package io.github.skomarica.practice.user.domain;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author Sinisa Komarica
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "username")
@ToString
@Entity
@Table(name = "sample_user")
public class User {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    @Column
    private String username;

    @NotNull
    @Column
    private String password;
}
