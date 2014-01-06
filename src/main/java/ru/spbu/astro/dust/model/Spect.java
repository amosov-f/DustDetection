package ru.spbu.astro.dust.model;

public class Spect {
    double type;
    int luminСlass;

    public Spect(String s) {
        if (s.contains("I") && !s.contains("II")) {
            luminСlass = 1;
        }
        if (s.contains("II") && !s.contains("III")) {
            luminСlass = 2;
        }
        if (s.contains("III")) {
            luminСlass = 3;
        }
        if (s.contains("IV")) {
            luminСlass = 4;
        }
        if (s.contains("V") && !s.contains("I")) {
            luminСlass = 5;
        }




    }
}
