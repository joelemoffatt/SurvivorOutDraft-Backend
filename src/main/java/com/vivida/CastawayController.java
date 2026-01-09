package com.vivida;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/castaways")
public class CastawayController {

    @GetMapping
    public List<Castaway> getCastaways() {
        return List.of(
                new Castaway(
                        1,
                        "Savannah Louie",
                        49,
                        1,
                        31,
                        32,
                        "1993-11-16",
                        "Former Reporter",
                        "Atlanta, Georgia",
                        "Female",
                        null,
                        false,
                        "Adventurous, No-Nonsense, Curious",
                        null,
                        1,
                        1,
                        82,
                        1,
                        87,
                        33,
                        83,
                        54,
                        100,
                        62
                ),
                new Castaway(
                        2,
                        "Sophi Balerdi",
                        49,
                        2,
                        27,
                        27,
                        "1998-02-08",
                        "Entrepreneur",
                        "Miami, Florida",
                        "Female",
                        null,
                        false,
                        "Spicy, Thoughtful, Relentless",
                        null,
                        1,
                        0,
                        64,
                        2,
                        26,
                        32,
                        65,
                        79,
                        100,
                        25
                )
        );
    }
}
