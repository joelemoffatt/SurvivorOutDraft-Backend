package com.vivida;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Castaway {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String full_name;

    private String date_of_birth;
    private String date_of_death;   // many missing
    private String city;            // from castaway details
    private String state;           // from castaway details
    private String gender;

    private String occupation;
    private String hobbies;         // many missing
    private String pet_peeves;      // many missing
    private String three_words;
}
