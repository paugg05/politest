package com.politest.politest.result.internal;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import java.util.LinkedHashMap;
import java.util.Map;

/** Resultado anónimo compartido por enlace: solo el porcentaje de cada eje. */
@Entity
@Table(name = "shared_result")
public class SharedResult {

    @Id
    private String id;

    @ElementCollection
    @CollectionTable(name = "shared_result_score", joinColumns = @JoinColumn(name = "result_id"))
    @MapKeyColumn(name = "axis_code")
    @Column(name = "right_pct", nullable = false)
    private Map<String, Integer> rightPct = new LinkedHashMap<>();

    protected SharedResult() {
    }

    public SharedResult(String id, Map<String, Integer> rightPct) {
        this.id = id;
        this.rightPct = new LinkedHashMap<>(rightPct);
    }

    public String getId() {
        return id;
    }

    public Map<String, Integer> getRightPct() {
        return rightPct;
    }
}
