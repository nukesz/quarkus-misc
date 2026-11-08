package org.acme.reservation.reservation;

import io.quarkus.logging.Log;
import io.smallrye.graphql.client.GraphQLClient;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import org.acme.reservation.entity.Reservation;
import org.acme.reservation.inventory.Car;
import org.acme.reservation.inventory.GraphQLInventoryClient;
import org.acme.reservation.inventory.InventoryClient;
import org.acme.reservation.rental.Rental;
import org.acme.reservation.rental.RentalClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestQuery;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("reservation")
@Produces(MediaType.APPLICATION_JSON)
public class ReservationResource {

    private static final Logger LOG = Logger.getLogger(ReservationResource.class);
    private final InventoryClient inventoryClient;
    private final RentalClient rentalClient;
    @Inject
    SecurityContext sc;

    public ReservationResource(@GraphQLClient("inventory") GraphQLInventoryClient inventoryClient,
                               // InventoryClient inventoryClient,
                               @RestClient RentalClient rentalClient
    ) {
        this.inventoryClient = inventoryClient;
        this.rentalClient = rentalClient;
    }

    @GET
    @Path("availability")
    public Collection<Car> availability(@RestQuery LocalDate startDate, @RestQuery LocalDate endDate) {
        Log.infov("Stardate #1: {0}", startDate);
        LOG.info("Stardate #2: " + startDate);

        // obtain all cars from inventory
        List<Car> availableCars = inventoryClient.allCars();

        // create a map from id to car
        Map<Long, Car> carsById = new HashMap<>();
        for (Car car : availableCars) {
            carsById.put(car.id, car);
        }

        // get all current reservations
        List<Reservation> reservations = Reservation.listAll();

        for (Reservation reservation : reservations) {
            if (reservation.isReserved(startDate, endDate)) {
                carsById.remove(reservation.carId);
            }
        }

        return carsById.values();
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    @Transactional
    public Reservation make(CreateReservationRequest request) {
        Log.info("Making reservation " + request);
        Reservation reservation = new Reservation();
        reservation.userId = sc.getUserPrincipal() != null ? sc.getUserPrincipal().getName() : "anonymous";
        reservation.carId = request.carId();
        reservation.startDay = request.startDay();
        reservation.endDay = request.endDay();
        reservation.persist();
        Log.info("Successfully reserved reservation: " + reservation);
        if (reservation.startDay.equals(LocalDate.now())) {
            Rental rental = rentalClient.start(reservation.userId, reservation.id);
            Log.info("Successfully started rental " + rental);
        }
        return reservation;
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Long id) {
        String userId = sc.getUserPrincipal() != null ? sc.getUserPrincipal().getName() : null;
        Reservation.delete(userId, id);
    }

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Path("/all")
    public List<Reservation> allReservations() {
        String userId = sc.getUserPrincipal() != null ? sc.getUserPrincipal().getName() : null;
        Log.info("User id : " + userId);
        return Reservation.<Reservation>streamAll()
                .filter(reservation -> userId == null || userId.equals(reservation.userId))
                .toList();
    }
}
