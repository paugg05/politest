package com.politest.politest.catalog.internal;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "axis")
public class Axis {

    @Id
    private String code;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    /** Traducciones por idioma: "es", "ca", "en". */
    @ElementCollection
    @CollectionTable(name = "axis_translation", joinColumns = @JoinColumn(name = "axis_code"))
    @MapKeyColumn(name = "locale")
    private Map<String, AxisText> texts = new HashMap<>();

    protected Axis() {
    }

    public String getCode() {
        return code;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public Map<String, AxisText> getTexts() {
        return texts;
    }
}
