package org.acme.reservation.reservation;

import jakarta.inject.Singleton;
import org.acme.reservation.entity.Reservation;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class InMemoryReservationRepository implements ReservationRepository {

    private final AtomicLong ids = new AtomicLong(0);
    private final List<Reservation> store = new CopyOnWriteArrayList<>();

    @Override
    public List<Reservation> findAll() {
        return Collections.unmodifiableList(store);
    }

    @Override
    public Reservation save(Reservation reservation) {
        reservation.id = ids.incrementAndGet();
        store.add(reservation);
        return reservation;
    }

    @Override
    public void delete(String userId, Long id) {
        store.removeIf(reservation -> reservation.userId.equals(userId) && reservation.id.equals(id));
    }
}
