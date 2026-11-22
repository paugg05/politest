package com.politest.politest.catalog.internal;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "question")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "axis_code", nullable = false)
    private String axisCode;

    private int direction;

    private double weight;

    private boolean active;

    @ElementCollection
    @CollectionTable(name = "question_translation", joinColumns = @JoinColumn(name = "question_id"))
    @MapKeyColumn(name = "locale")
    @Column(name = "statement", nullable = false)
    private Map<String, String> statements = new HashMap<>();

    protected Question() {
    }

    public Long getId() {
        return id;
    }

    public String getAxisCode() {
        return axisCode;
    }

    public int getDirection() {
        return direction;
    }

    public double getWeight() {
        return weight;
    }

    public boolean isActive() {
        return active;
    }

    public Map<String, String> getStatements() {
        return statements;
    }
}
