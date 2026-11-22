package com.politest.figure.domain.model;

/** Identity of a historical figure, with texts in a given language. */
public record Figure(String code, String name, String description, int birthYear, int deathYear, String color) {
}
