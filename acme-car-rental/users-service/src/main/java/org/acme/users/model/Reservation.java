package org.acme.users.model;

import java.time.LocalDate;

public record Reservation(Long id,
                          String userId,
                          Long carId,
                          LocalDate startDay,
                          LocalDate endDay) {
}
