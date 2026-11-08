package org.acme.reservation.reservation;

import java.time.LocalDate;

public record CreateReservationRequest(
        Long carId,
        LocalDate startDay,
        LocalDate endDay
) {
}
