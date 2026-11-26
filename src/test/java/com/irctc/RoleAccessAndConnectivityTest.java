package com.irctc;

import com.irctc.model.PassengerDetail;
import com.irctc.model.Station;
import com.irctc.model.Train;
import com.irctc.model.TrainRouteStop;
import com.irctc.model.enums.UserRole;
import com.irctc.service.StationService;
import com.irctc.service.TrainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RoleAccessAndConnectivityTest {

    @Autowired
    private TrainService trainService;

    @Autowired
    private StationService stationService;

    @Test
    @DisplayName("Station Halt Logic: Major junctions get 10 mins, small stations get 2 mins")
    void testStationHaltLogic() {
        Station junction = new Station("KLBG", "Kalaburagi Junction", "Gulbarga", "Karnataka");
        junction.setJunction(true);
        junction.setPlatformCount(5);
        assertTrue(junction.isMajorStation());
        assertEquals(10, junction.getDefaultHaltMinutes(), "Junction should default to 10 minutes halt");

        Station waystation = new Station("SEM", "Sedam", "Sedam", "Karnataka");
        waystation.setJunction(false);
        waystation.setPlatformCount(2);
        assertFalse(waystation.isMajorStation());
        assertEquals(2, waystation.getDefaultHaltMinutes(), "Waystation should default to 2 minutes halt");
    }

    @Test
    @DisplayName("Connectivity: Kalaburagi (KLBG) to Hyderabad/Secunderabad trains must exist and be searchable")
    void testKalaburagiToHyderabadConnectivity() {
        List<Train> trainsToHyb = trainService.searchTrains("KLBG", "HYB");
        assertFalse(trainsToHyb.isEmpty(), "Expected trains between KLBG and HYB");

        List<Train> trainsToSc = trainService.searchTrains("KLBG", "SC");
        assertFalse(trainsToSc.isEmpty(), "Expected trains between KLBG and SC");

        // Verify stops for Hussain Sagar Express (12701)
        List<TrainRouteStop> stops = trainService.getTrainStops("12701");
        assertFalse(stops.isEmpty(), "Stops for 12701 should not be empty");

        // Verify stops contain KLBG and HYB in sequence
        int klbgIdx = -1;
        int hybIdx = -1;
        for (int i = 0; i < stops.size(); i++) {
            if ("KLBG".equals(stops.get(i).getStation().getStationCode())) klbgIdx = i;
            if ("HYB".equals(stops.get(i).getStation().getStationCode())) hybIdx = i;
        }

        assertTrue(klbgIdx != -1, "KLBG must be a stop on train 12701");
        assertTrue(hybIdx != -1, "HYB must be a stop on train 12701");
        assertTrue(klbgIdx < hybIdx, "KLBG must precede HYB on train 12701");
    }

    @Test
    @DisplayName("Station Master: Update platform number and delay minutes")
    void testStationMasterUpdates() {
        List<TrainRouteStop> stops = trainService.getStationStops("KLBG");
        assertFalse(stops.isEmpty(), "KLBG station should have train stops");

        TrainRouteStop stop = stops.get(0);
        Long stopId = stop.getId();

        // Update platform
        TrainRouteStop updatedPlatform = trainService.updateStopPlatform(stopId, "4");
        assertEquals("4", updatedPlatform.getPlatformNumber());

        // Update delay
        TrainRouteStop updatedDelay = trainService.updateStopDelay(stopId, 25);
        assertEquals(25, updatedDelay.getDelayMinutes());
    }

    @Autowired
    private com.irctc.service.BookingService bookingService;

    @Autowired
    private com.irctc.service.DistanceFareService distanceFareService;

    @Test
    @DisplayName("User Roles: Verify new roles exist and have proper display names")
    void testUserRoles() {
        assertEquals("Ticket Collector (TTE)", UserRole.TICKET_COLLECTOR.getDisplayName());
        assertEquals("Station Master", UserRole.STATION_MASTER.getDisplayName());
        assertEquals("Passenger", UserRole.PASSENGER.getDisplayName());
        assertEquals("Administrator", UserRole.ADMIN.getDisplayName());
    }

    @Test
    @DisplayName("Distance-Based Fare: Intermediate station booking (KLBG to SBC) calculates fare by distance, not full train origin")
    void testDistanceBasedFareCalculation() {
        java.time.LocalDate journeyDate = java.time.LocalDate.now().plusDays(2);
        List<com.irctc.dto.TrainAvailabilityDTO> dtos = bookingService.searchTrains("KLBG", "SBC", journeyDate);
        assertFalse(dtos.isEmpty(), "Should find trains from KLBG to SBC");

        com.irctc.dto.TrainAvailabilityDTO dto = dtos.get(0);
        Train train = dto.getTrain();

        assertNotNull(dto.getDistanceKm(), "Segment distance must be calculated");
        assertTrue(dto.getDistanceKm() > 0, "Distance between KLBG and SBC should be > 0");

        // Distance between KLBG and SBC should be around 500-600 km, less than full train route
        List<TrainRouteStop> stops = trainService.getTrainStops(train.getTrainNo());
        int totalDistance = distanceFareService.calculateTotalRouteDistance(stops);
        assertTrue(dto.getDistanceKm() < totalDistance, "KLBG to SBC distance must be less than full train run distance");

        for (com.irctc.model.enums.ClassType ct : train.getClasses()) {
            Double segmentFare = dto.getClassFares().get(ct);
            assertNotNull(segmentFare, "Segment fare must be calculated for class: " + ct);

            Double fullFare = train.getFareStructure() != null ? train.getFareStructure().get(ct.name()) : null;
            if (fullFare != null) {
                assertTrue(segmentFare < fullFare, 
                        "Segment fare (" + segmentFare + ") for " + ct + " must be lower than full route fare (" + fullFare + ")");
            }
        }
    }
}
