package com.politest.figure.domain.model;

import java.time.LocalDate;

/** Version and status of the dataset the positions come from. */
public record DatasetInfo(String version, String status, String note, LocalDate updatedOn) {
}
