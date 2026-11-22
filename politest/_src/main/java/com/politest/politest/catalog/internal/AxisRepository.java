package com.politest.politest.catalog.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AxisRepository extends JpaRepository<Axis, String> {

    List<Axis> findAllByOrderBySortOrderAsc();
}
