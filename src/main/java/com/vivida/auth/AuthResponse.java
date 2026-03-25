package com.vivida.auth;

import com.vivida.game.castaway.Castaway;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private Integer id;
    private String token;
    private String username;
    private String email;
    private Role role;
    private String avatarImage;
    private List<Castaway> favoriteCastaways;
    private String bio;
}
