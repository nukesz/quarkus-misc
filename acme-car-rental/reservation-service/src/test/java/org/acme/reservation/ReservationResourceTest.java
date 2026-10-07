package org.acme.reservation;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.DisabledOnIntegrationTest;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.acme.reservation.inventory.Car;
import org.acme.reservation.inventory.GraphQLInventoryClient;
import org.acme.reservation.reservation.Reservation;
import org.acme.reservation.reservation.ReservationResource;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

@QuarkusTest
public class ReservationResourceTest {

    @TestHTTPEndpoint(ReservationResource.class)
    @TestHTTPResource
    private URI reservationResource;

    @TestHTTPEndpoint(ReservationResource.class)
    @TestHTTPResource("availability")
    private URL availability;

    @Test
    void testReservationIds() {
        Reservation reservation = new Reservation();
        reservation.carId = 12345L;
        reservation.startDay = LocalDate.parse("2025-03-20");
        reservation.endDay = LocalDate.parse("2025-03-29");
        RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .body(reservation)
                .when()
                    .post(reservationResource)
                .then()
                    .statusCode(HttpStatus.SC_OK)
                    .body("id", notNullValue());
    }

    /**
     * Test is replaced by {@link #testMakingAReservationAndCheckAvailability }
     * {@link org.acme.reservation.inventory.MockInventoryClient is also disabled }
     */
    // @Test
    void testAvailableCars() {
        RestAssured
            .given()
            .when()
                .get(availability)
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(1),
                        "[0].id", is(1),
                        "[0].licensePlateNumber", is("ABC123"),
                        "[0].manufacturer", is("Peugeot"),
                        "[0].model", is("406"));
    }

    @DisabledOnIntegrationTest(forArtifactTypes = DisabledOnIntegrationTest.ArtifactType.NATIVE_BINARY)
    @Test
    void testMakingAReservationAndCheckAvailability() {
        GraphQLInventoryClient mock = Mockito.mock(GraphQLInventoryClient.class);
        when(mock.allCars()).thenReturn(List.of(new Car(1L, "ABC123", "Peugeot", "406")));
        QuarkusMock.installMockForType(mock, GraphQLInventoryClient.class);

        String startDate = "2022-01-01";
        String endDate = "2022-01-10";
        // List available cars for our requested timeslot and choose one
        Car[] cars = RestAssured
                .given()
                    .queryParam("startDate", startDate)
                    .queryParam("endDate", endDate)
                .when()
                    .get(availability)
                .then()
                    .statusCode(200)
                    .extract().as(Car[].class);
        Car car = cars[0];

        // Prepare a Reservation object
        Reservation reservation = new Reservation();
        reservation.carId = car.id;
        reservation.startDay = LocalDate.parse(startDate);
        reservation.endDay = LocalDate.parse(endDate);

        // Submit the reservation
        RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .body(reservation)
                .when()
                    .post(reservationResource)
                .then()
                    .statusCode(200)
                    .body("carId", is(car.id.intValue()));

        // Verify that this car doesn't show as available anymore
        RestAssured
                .given()
                    .queryParam("startDate", startDate)
                    .queryParam("endDate", endDate)
                .when()
                    .get(availability)
                .then()
                    .statusCode(200)
                .body("findAll { car -> car.id == " + car.id + "}", hasSize(0));
    }
}
