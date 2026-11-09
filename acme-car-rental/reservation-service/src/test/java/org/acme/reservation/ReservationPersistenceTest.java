package org.acme.reservation;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import org.acme.reservation.entity.Reservation;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class ReservationPersistenceTest {

    @Test
    @Transactional
    void testCreateReservation() {
        Reservation reservation = new Reservation();
        reservation.startDay = LocalDate.now().plusDays(5);
        reservation.endDay = LocalDate.now().plusDays(12);
        reservation.carId = 384L;
        reservation.persist();

        assertNotNull(reservation.id);
        assertEquals(1, Reservation.count());
        Reservation persistedEntity = Reservation.findById(reservation.id);
        assertNotNull(persistedEntity);
        assertEquals(reservation.id, persistedEntity.id);
    }
}