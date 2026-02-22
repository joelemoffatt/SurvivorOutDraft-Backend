package com.vivida.game.castaway;


import jakarta.persistence.Column;
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
    
    @Column(nullable = false, unique = true)
    private String json_id;  // Original JSON ID like "US0001"
    
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String full_name;

    @Column(nullable = false)
    private String date_of_birth;
    private String date_of_death;   // many missing
    private String city;            // from castaway details
    private String state;           // from castaway details
    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private String occupation;
    @Column(length = 1000)
    private String hobbies;         // many missing, can be long
    @Column(length = 1000)
    private String pet_peeves;      // many missing, can be long
    private String three_words;
}
