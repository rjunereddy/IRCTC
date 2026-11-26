package com.irctc.service;

import com.irctc.model.Train;
import com.irctc.model.TrainRouteStop;
import com.irctc.model.enums.ClassType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DistanceFareService {

    /**
     * Computes the segment distance in kilometers between two stations on a train route.
     */
    public int calculateSegmentDistance(List<TrainRouteStop> stops, String fromStationCode, String toStationCode) {
        if (stops == null || stops.isEmpty() || fromStationCode == null || toStationCode == null) {
            return 0;
        }

        TrainRouteStop fromStop = null;
        TrainRouteStop toStop = null;

        for (TrainRouteStop stop : stops) {
            if (stop.getStation() != null) {
                String code = stop.getStation().getStationCode();
                if (fromStop == null && code.equalsIgnoreCase(fromStationCode.trim())) {
                    fromStop = stop;
                } else if (fromStop != null && code.equalsIgnoreCase(toStationCode.trim())) {
                    toStop = stop;
                    break;
                }
            }
        }

        if (fromStop != null && toStop != null) {
            int d1 = fromStop.getDistanceInKm() != null ? fromStop.getDistanceInKm() : 0;
            int d2 = toStop.getDistanceInKm() != null ? toStop.getDistanceInKm() : 0;
            int diff = Math.abs(d2 - d1);
            if (diff > 0) return diff;
            int orderDiff = Math.abs(toStop.getStopOrder() - fromStop.getStopOrder());
            return Math.max(50, orderDiff * 65);
        }

        return 0;
    }

    /**
     * Computes the total distance of a train route from its origin to destination stop.
     */
    public int calculateTotalRouteDistance(List<TrainRouteStop> stops) {
        if (stops == null || stops.size() < 2) {
            return 800;
        }
        TrainRouteStop first = stops.get(0);
        TrainRouteStop last = stops.get(stops.size() - 1);
        int dFirst = first.getDistanceInKm() != null ? first.getDistanceInKm() : 0;
        int dLast = last.getDistanceInKm() != null ? last.getDistanceInKm() : 0;
        int total = Math.abs(dLast - dFirst);
        return total > 50 ? total : Math.max(120, (stops.size() - 1) * 65);
    }

    /**
     * Calculates the dynamic base fare for a passenger traveling a specific distance on a train.
     */
    public double calculateSegmentFare(Train train, ClassType classType, int segmentDistance, int totalRouteDistance) {
        if (classType == null) return 100.0;

        // If segment distance is equal to or exceeds total route, or either is not set, use train full fare
        if (segmentDistance <= 0 || totalRouteDistance <= 0 || segmentDistance >= totalRouteDistance) {
            if (train != null && train.getFareStructure() != null && train.getFareStructure().containsKey(classType.name())) {
                return train.getFareStructure().get(classType.name());
            }
            return classType.getBaseFare();
        }

        double fullFare = 0.0;
        if (train != null && train.getFareStructure() != null && train.getFareStructure().containsKey(classType.name())) {
            fullFare = train.getFareStructure().get(classType.name());
        } else {
            fullFare = classType.getBaseFare();
        }

        // Distance ratio
        double ratio = (double) segmentDistance / (double) totalRouteDistance;
        ratio = Math.min(1.0, Math.max(0.08, ratio));

        double rawFare = fullFare * ratio;

        // Apply Indian Railways minimum floor fare per class
        double minFare = getMinFareForClass(classType);
        double calculated = Math.max(minFare, rawFare);

        // Round to nearest multiple of 5 (standard Indian Railways practice)
        return Math.round(calculated / 5.0) * 5.0;
    }

    /**
     * Minimum floor fare per class as per Indian Railways tariff rules.
     */
    public double getMinFareForClass(ClassType classType) {
        if (classType == null) return 50.0;
        return switch (classType) {
            case SECOND_SITTING -> 40.0;
            case SLEEPER -> 120.0;
            case AC_CHAIR_CAR -> 200.0;
            case THIRD_AC -> 350.0;
            case SECOND_AC -> 500.0;
            case FIRST_AC -> 750.0;
        };
    }

    /**
     * Builds a map of class type -> distance-calculated fare for all classes on a train.
     */
    public Map<String, Double> buildSegmentFareMap(Train train, int segmentDistance, int totalRouteDistance) {
        Map<String, Double> map = new HashMap<>();
        if (train == null || train.getClasses() == null) return map;
        for (ClassType ct : train.getClasses()) {
            map.put(ct.name(), calculateSegmentFare(train, ct, segmentDistance, totalRouteDistance));
        }
        return map;
    }
}
