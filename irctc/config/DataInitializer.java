package com.irctc.config;

import com.irctc.model.*;
import com.irctc.model.enums.ClassType;
import com.irctc.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TrainRepository trainRepository;
    private final StationRepository stationRepository;
    private final TrainRouteStopRepository trainRouteStopRepository;
    private final PasswordEncoder passwordEncoder;

    // In-memory station cache for train creation
    private final Map<String, Station> stationCache = new HashMap<>();
    private final List<Station> allStationsList = new ArrayList<>();
    private final Random random = new Random();

    public DataInitializer(UserRepository userRepository,
            TrainRepository trainRepository,
            StationRepository stationRepository,
            TrainRouteStopRepository trainRouteStopRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.trainRepository = trainRepository;
        this.stationRepository = stationRepository;
        this.trainRouteStopRepository = trainRouteStopRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚂 Initializing IRCTC Railway System...");

        if (stationRepository.count() == 0) {
            createStations();
        }

        // Load all stations into cache
        // Load all stations into cache and list
        stationRepository.findAll().forEach(s -> {
            stationCache.put(s.getStationCode(), s);
            allStationsList.add(s);
        });

        if (trainRepository.count() == 0) {
            createTrains();
        }

        if (!userRepository.existsByUsername("admin")) {
            createAdminUser();
        }

        System.out.println("✅ Data initialization completed!");
    }

    private void createStations() {
        System.out.println("Creating stations...");

        String[][] stationData = {
                // {Code, Name, City, State}
                { "SBC", "KSR Bengaluru", "Bangalore", "Karnataka" },
                { "MYS", "Mysuru Junction", "Mysore", "Karnataka" },
                { "UBL", "Hubballi Junction", "Hubballi", "Karnataka" },
                { "MAS", "Chennai Central", "Chennai", "Tamil Nadu" },
                { "MS", "Chennai Egmore", "Chennai", "Tamil Nadu" },
                { "CBE", "Coimbatore Junction", "Coimbatore", "Tamil Nadu" },
                { "MDU", "Madurai Junction", "Madurai", "Tamil Nadu" },
                { "TVC", "Trivandrum Central", "Thiruvananthapuram", "Kerala" },
                { "ERS", "Ernakulam Junction", "Kochi", "Kerala" },
                { "CLT", "Kozhikode", "Calicut", "Kerala" },
                { "NDLS", "New Delhi", "Delhi", "Delhi" },
                { "DLI", "Old Delhi Junction", "Delhi", "Delhi" },
                { "BCT", "Mumbai Central", "Mumbai", "Maharashtra" },
                { "CSTM", "Chhatrapati Shivaji Terminus", "Mumbai", "Maharashtra" },
                { "LTT", "Lokmanya Tilak Terminus", "Mumbai", "Maharashtra" },
                { "PNQ", "Pune Junction", "Pune", "Maharashtra" },
                { "NGP", "Nagpur Junction", "Nagpur", "Maharashtra" },
                { "HYB", "Hyderabad Deccan", "Hyderabad", "Telangana" },
                { "SC", "Secunderabad Junction", "Secunderabad", "Telangana" },
                { "BZA", "Vijayawada Junction", "Vijayawada", "Andhra Pradesh" },
                { "VSKP", "Visakhapatnam", "Visakhapatnam", "Andhra Pradesh" },
                { "KOL", "Kolkata", "Kolkata", "West Bengal" },
                { "HWH", "Howrah Junction", "Howrah", "West Bengal" },
                { "SDAH", "Sealdah", "Kolkata", "West Bengal" },
                { "PNBE", "Patna Junction", "Patna", "Bihar" },
                { "RNC", "Ranchi Junction", "Ranchi", "Jharkhand" },
                { "BBS", "Bhubaneswar", "Bhubaneswar", "Odisha" },
                { "LKO", "Lucknow Charbagh", "Lucknow", "Uttar Pradesh" },
                { "CNB", "Kanpur Central", "Kanpur", "Uttar Pradesh" },
                { "AGC", "Agra Cantt", "Agra", "Uttar Pradesh" },
                { "BSB", "Varanasi Junction", "Varanasi", "Uttar Pradesh" },
                { "GKP", "Gorakhpur Junction", "Gorakhpur", "Uttar Pradesh" },
                { "JP", "Jaipur Junction", "Jaipur", "Rajasthan" },
                { "AII", "Ajmer Junction", "Ajmer", "Rajasthan" },
                { "UDZ", "Udaipur City", "Udaipur", "Rajasthan" },
                { "JU", "Jodhpur Junction", "Jodhpur", "Rajasthan" },
                { "ADI", "Ahmedabad Junction", "Ahmedabad", "Gujarat" },
                { "BRC", "Vadodara Junction", "Vadodara", "Gujarat" },
                { "SVDK", "Shri Mata Vaishno Devi Katra", "Katra", "Jammu & Kashmir" },
                { "JAT", "Jammu Tawi", "Jammu", "Jammu & Kashmir" },
                { "CDG", "Chandigarh Junction", "Chandigarh", "Chandigarh" },
                { "ASR", "Amritsar Junction", "Amritsar", "Punjab" },
                { "BPL", "Bhopal Junction", "Bhopal", "Madhya Pradesh" },
                { "JBP", "Jabalpur Junction", "Jabalpur", "Madhya Pradesh" },
                { "GHY", "Guwahati", "Guwahati", "Assam" },
                { "DGR", "Dibrugarh", "Dibrugarh", "Assam" },
                { "DBRG", "Dibrugarh Town", "Dibrugarh", "Assam" },
                { "RJT", "Rajkot Junction", "Rajkot", "Gujarat" },
                { "DDN", "Dehradun", "Dehradun", "Uttarakhand" },
                { "HW", "Haridwar Junction", "Haridwar", "Uttarakhand" },
        };

        for (String[] s : stationData) {
            stationRepository.save(new Station(s[0], s[1], s[2], s[3]));
        }

        System.out.println("✅ Stations created: " + stationData.length + " stations");
    }

    // Helper to quickly build and save a train
    private void addTrain(String no, String name, String src, String dest,
            int depH, int depM, int arrH, int arrM, int seats,
            ClassType[] classes, double[] fares) {
        Station source = stationCache.get(src);
        Station destination = stationCache.get(dest);
        if (source == null || destination == null)
            return;

        Train t = new Train(no, name, source, destination,
                LocalTime.of(depH, depM), LocalTime.of(arrH, arrM));
        t.setTotalSeats(seats);
        t.setAvailableSeats(seats);
        t.setClasses(Arrays.asList(classes));

        Map<String, Double> fareMap = new HashMap<>();
        for (int i = 0; i < classes.length && i < fares.length; i++) {
            fareMap.put(classes[i].name(), fares[i]);
        }
        t.setFareStructure(fareMap);

        trainRepository.save(t);

        // --- Generate Route Stops ---
        List<TrainRouteStop> stops = new ArrayList<>();

        // Stop 0: Source
        TrainRouteStop srcStop = new TrainRouteStop();
        srcStop.setTrain(t);
        srcStop.setStation(source);
        srcStop.setStopOrder(0);
        srcStop.setDepartureTime(LocalTime.of(depH, depM));
        srcStop.setDistanceInKm(0);
        stops.add(srcStop);

        // Pick 2 random intermediate stations
        List<Station> intermediates = new ArrayList<>();
        int attempts = 0;
        while (intermediates.size() < 2 && attempts < 50) {
            Station r = allStationsList.get(random.nextInt(allStationsList.size()));
            if (!r.getStationCode().equals(source.getStationCode()) &&
                    !r.getStationCode().equals(destination.getStationCode()) &&
                    !intermediates.contains(r)) {
                intermediates.add(r);
            }
            attempts++;
        }

        for (int i = 0; i < intermediates.size(); i++) {
            TrainRouteStop inter = new TrainRouteStop();
            inter.setTrain(t);
            inter.setStation(intermediates.get(i));
            inter.setStopOrder(i + 1);
            // Rough estimation of arrival/departure
            inter.setArrivalTime(LocalTime.of(depH, depM).plusHours(2 * (i + 1)));
            inter.setDepartureTime(LocalTime.of(depH, depM).plusHours(2 * (i + 1)).plusMinutes(10));
            inter.setDistanceInKm(150 * (i + 1));
            stops.add(inter);
        }

        // Stop N: Destination
        TrainRouteStop destStop = new TrainRouteStop();
        destStop.setTrain(t);
        destStop.setStation(destination);
        destStop.setStopOrder(intermediates.size() + 1);
        destStop.setArrivalTime(LocalTime.of(arrH, arrM));
        destStop.setDistanceInKm(150 * (intermediates.size() + 1));
        stops.add(destStop);

        trainRouteStopRepository.saveAll(stops);
    }

    // Short aliases for ClassType
    private static final ClassType AC1 = ClassType.FIRST_AC;
    private static final ClassType AC2 = ClassType.SECOND_AC;
    private static final ClassType AC3 = ClassType.THIRD_AC;
    private static final ClassType SL = ClassType.SLEEPER;
    private static final ClassType CC = ClassType.AC_CHAIR_CAR;
    private static final ClassType SS = ClassType.SECOND_SITTING;

    private void createTrains() {
        System.out.println("Creating 105 trains...");

        // ============================================================
        // RAJDHANI EXPRESS TRAINS (Premium, AC only)
        // ============================================================
        addTrain("12301", "Howrah Rajdhani Express", "NDLS", "HWH", 16, 55, 9, 55, 600,
                new ClassType[] { AC1, AC2, AC3 }, new double[] { 4000, 2500, 1800 });
        addTrain("12302", "New Delhi Rajdhani Express", "HWH", "NDLS", 14, 5, 9, 55, 600,
                new ClassType[] { AC1, AC2, AC3 }, new double[] { 4000, 2500, 1800 });
        addTrain("12309", "Rajdhani Express", "PNBE", "NDLS", 17, 30, 7, 40, 500, new ClassType[] { AC1, AC2, AC3 },
                new double[] { 3800, 2300, 1700 });
        addTrain("12431", "Trivandrum Rajdhani Express", "TVC", "NDLS", 11, 15, 10, 55, 480,
                new ClassType[] { AC1, AC2, AC3 }, new double[] { 4500, 2800, 2000 });
        addTrain("12433", "Chennai Rajdhani Express", "MAS", "NDLS", 6, 10, 7, 30, 520,
                new ClassType[] { AC1, AC2, AC3 }, new double[] { 4200, 2600, 1900 });
        addTrain("12435", "Dibrugarh Rajdhani Express", "NDLS", "DGR", 13, 20, 6, 5, 450,
                new ClassType[] { AC1, AC2, AC3 }, new double[] { 4800, 3000, 2200 });
        addTrain("12951", "Mumbai Rajdhani Express", "BCT", "NDLS", 17, 0, 8, 35, 550,
                new ClassType[] { AC1, AC2, AC3 }, new double[] { 3500, 2200, 1600 });
        addTrain("12952", "New Delhi Rajdhani Express", "NDLS", "BCT", 16, 35, 8, 15, 550,
                new ClassType[] { AC1, AC2, AC3 }, new double[] { 3500, 2200, 1600 });
        addTrain("22691", "Bengaluru Rajdhani Express", "SBC", "NDLS", 20, 0, 5, 55, 500,
                new ClassType[] { AC1, AC2, AC3 }, new double[] { 4300, 2700, 1950 });
        addTrain("20501", "Secunderabad Rajdhani Express", "SC", "NDLS", 20, 40, 11, 5, 480,
                new ClassType[] { AC1, AC2, AC3 }, new double[] { 3800, 2400, 1750 });

        // ============================================================
        // SHATABDI EXPRESS TRAINS (Day trains, AC Chair Car)
        // ============================================================
        addTrain("12001", "Bhopal Shatabdi Express", "NDLS", "BPL", 6, 0, 14, 10, 750, new ClassType[] { CC, AC2 },
                new double[] { 1200, 1800 });
        addTrain("12002", "New Delhi Shatabdi Express", "BPL", "NDLS", 14, 30, 22, 30, 750, new ClassType[] { CC, AC2 },
                new double[] { 1200, 1800 });
        addTrain("12003", "Swarna Shatabdi Express", "NDLS", "LKO", 6, 10, 12, 30, 700, new ClassType[] { CC, AC2 },
                new double[] { 1000, 1500 });
        addTrain("12007", "Chennai Shatabdi Express", "MAS", "MYS", 6, 0, 12, 50, 650, new ClassType[] { CC, AC2 },
                new double[] { 800, 1300 });
        addTrain("12011", "Kalka Shatabdi Express", "NDLS", "CDG", 7, 40, 11, 30, 700, new ClassType[] { CC, AC2 },
                new double[] { 700, 1100 });
        addTrain("12025", "Pune Shatabdi Express", "CSTM", "PNQ", 6, 25, 9, 55, 800, new ClassType[] { CC, AC2 },
                new double[] { 600, 1000 });
        addTrain("12027", "Chennai Shatabdi Express", "SBC", "MAS", 6, 0, 11, 0, 700, new ClassType[] { CC, AC2 },
                new double[] { 750, 1200 });
        addTrain("12029", "Amritsar Shatabdi Express", "NDLS", "ASR", 7, 20, 13, 30, 680, new ClassType[] { CC, AC2 },
                new double[] { 800, 1300 });
        addTrain("12039", "Kathgodam Shatabdi Express", "NDLS", "DDN", 6, 45, 12, 50, 600, new ClassType[] { CC, AC2 },
                new double[] { 700, 1100 });

        // ============================================================
        // DURONTO EXPRESS TRAINS (Non-stop, long distance)
        // ============================================================
        addTrain("12213", "Mumbai Duronto Express", "NDLS", "BCT", 23, 0, 15, 50, 500,
                new ClassType[] { AC1, AC2, AC3, SL }, new double[] { 3200, 2000, 1400, 600 });
        addTrain("12243", "Chennai Duronto Express", "NDLS", "MAS", 22, 30, 21, 30, 480,
                new ClassType[] { AC1, AC2, AC3, SL }, new double[] { 3500, 2200, 1500, 700 });
        addTrain("12245", "Howrah Duronto Express", "NDLS", "HWH", 20, 15, 11, 55, 520,
                new ClassType[] { AC1, AC2, AC3, SL }, new double[] { 3000, 1900, 1300, 600 });
        addTrain("12247", "Bengaluru Duronto Express", "NDLS", "SBC", 21, 0, 19, 30, 450,
                new ClassType[] { AC1, AC2, AC3 }, new double[] { 3800, 2400, 1700 });
        addTrain("12259", "Secunderabad Duronto Express", "NDLS", "SC", 21, 45, 14, 30, 480,
                new ClassType[] { AC1, AC2, AC3, SL }, new double[] { 2800, 1800, 1300, 550 });
        addTrain("12267", "Mumbai Duronto Express", "BCT", "ADI", 23, 5, 5, 10, 400, new ClassType[] { AC2, AC3, SL },
                new double[] { 1200, 800, 400 });

        // ============================================================
        // SUPERFAST EXPRESS TRAINS
        // ============================================================
        addTrain("12627", "Karnataka Express", "SBC", "NDLS", 20, 30, 6, 0, 500, new ClassType[] { AC1, AC2, AC3, SL },
                new double[] { 2500, 1500, 1000, 500 });
        addTrain("12621", "Tamil Nadu Express", "MAS", "NDLS", 22, 0, 7, 10, 550, new ClassType[] { AC1, AC2, AC3, SL },
                new double[] { 2600, 1600, 1100, 520 });
        addTrain("12622", "Tamil Nadu Express", "NDLS", "MAS", 22, 30, 7, 15, 550,
                new ClassType[] { AC1, AC2, AC3, SL }, new double[] { 2600, 1600, 1100, 520 });
        addTrain("12625", "Kerala Express", "NDLS", "TVC", 11, 25, 19, 20, 600, new ClassType[] { AC1, AC2, AC3, SL },
                new double[] { 3200, 2000, 1400, 650 });
        addTrain("12839", "Howrah Chennai Mail", "HWH", "MAS", 23, 50, 5, 50, 480, new ClassType[] { AC2, AC3, SL },
                new double[] { 1400, 950, 450 });
        addTrain("12841", "Coromandel Express", "HWH", "MAS", 14, 50, 5, 20, 500, new ClassType[] { AC1, AC2, AC3, SL },
                new double[] { 2200, 1300, 900, 420 });
        addTrain("12259", "Sealdah Duronto Express", "SDAH", "NDLS", 20, 10, 10, 30, 500,
                new ClassType[] { AC1, AC2, AC3 }, new double[] { 2800, 1800, 1300 });
        addTrain("12615", "Grand Trunk Express", "MAS", "NDLS", 18, 45, 13, 30, 550,
                new ClassType[] { AC1, AC2, AC3, SL }, new double[] { 2700, 1700, 1200, 550 });
        addTrain("12723", "Telangana Express", "SC", "NDLS", 6, 25, 22, 40, 500, new ClassType[] { AC1, AC2, AC3, SL },
                new double[] { 2400, 1500, 1050, 480 });
        addTrain("12724", "Telangana Express", "NDLS", "SC", 6, 55, 23, 5, 500, new ClassType[] { AC1, AC2, AC3, SL },
                new double[] { 2400, 1500, 1050, 480 });
        addTrain("12903", "Golden Temple Mail", "BCT", "ASR", 21, 25, 4, 55, 480, new ClassType[] { AC1, AC2, AC3, SL },
                new double[] { 2200, 1400, 1000, 460 });
        addTrain("12905", "Porbandar Express", "NDLS", "RJT", 16, 15, 15, 25, 450, new ClassType[] { AC2, AC3, SL },
                new double[] { 1800, 1200, 550 });
        addTrain("12953", "August Kranti Rajdhani Express", "BCT", "NDLS", 17, 40, 10, 55, 530,
                new ClassType[] { AC1, AC2, AC3 }, new double[] { 3400, 2100, 1550 });

        // ============================================================
        // GARIB RATH EXPRESS TRAINS (Budget AC)
        // ============================================================
        addTrain("12201", "Mumbai Garib Rath Express", "LKO", "BCT", 15, 55, 9, 40, 650, new ClassType[] { AC3 },
                new double[] { 900 });
        addTrain("12203", "Saharsa Garib Rath Express", "PNBE", "NDLS", 19, 0, 7, 25, 600, new ClassType[] { AC3 },
                new double[] { 850 });
        addTrain("12205", "Nandan Kanan Garib Rath", "BBS", "NDLS", 19, 15, 4, 0, 650, new ClassType[] { AC3 },
                new double[] { 1000 });
        addTrain("12207", "Jammu Garib Rath Express", "NDLS", "JAT", 22, 15, 10, 0, 600, new ClassType[] { AC3 },
                new double[] { 700 });

        // ============================================================
        // MAIL / EXPRESS TRAINS (Long distance, all classes)
        // ============================================================
        addTrain("12639", "Brindavan Express", "SBC", "MAS", 7, 15, 13, 30, 300, new ClassType[] { AC2, SL, SS },
                new double[] { 800, 300, 150 });
        addTrain("12295", "Sanghamitra Express", "SBC", "KOL", 9, 30, 21, 45, 450, new ClassType[] { AC2, AC3, SL },
                new double[] { 1800, 1200, 600 });
        addTrain("16525", "Kanyakumari Express", "SBC", "HWH", 15, 30, 3, 0, 400, new ClassType[] { AC2, AC3, SL },
                new double[] { 1700, 1100, 550 });
        addTrain("12647", "Kongu Express", "SBC", "NDLS", 10, 0, 23, 45, 350, new ClassType[] { AC2, AC3, SL },
                new double[] { 1600, 1000, 450 });
        addTrain("11013", "Mumbai Coimbatore Express", "LTT", "CBE", 12, 40, 14, 30, 450,
                new ClassType[] { AC2, AC3, SL }, new double[] { 1400, 950, 430 });
        addTrain("11019", "Konark Express", "CSTM", "BBS", 9, 0, 8, 30, 420, new ClassType[] { AC2, AC3, SL },
                new double[] { 1500, 1000, 470 });
        addTrain("11027", "Chennai Mail Express", "CSTM", "MAS", 21, 0, 21, 50, 500,
                new ClassType[] { AC1, AC2, AC3, SL }, new double[] { 2000, 1200, 850, 400 });
        addTrain("11301", "Udyan Express", "CSTM", "SBC", 8, 5, 22, 30, 420, new ClassType[] { AC2, AC3, SL },
                new double[] { 1100, 750, 350 });
        addTrain("12101", "Jnaneswari Express", "CSTM", "HWH", 12, 20, 13, 10, 480,
                new ClassType[] { AC1, AC2, AC3, SL }, new double[] { 2400, 1500, 1050, 490 });
        addTrain("12123", "Deccan Queen Express", "CSTM", "PNQ", 7, 15, 10, 30, 550, new ClassType[] { CC, AC2 },
                new double[] { 400, 700 });
        addTrain("12137", "Punjab Mail Express", "CSTM", "DLI", 19, 10, 5, 30, 500,
                new ClassType[] { AC1, AC2, AC3, SL }, new double[] { 2300, 1400, 1000, 460 });
        addTrain("12393", "Sampoorna Kranti Express", "NDLS", "PNBE", 22, 45, 12, 45, 500,
                new ClassType[] { AC1, AC2, AC3, SL }, new double[] { 2000, 1200, 850, 400 });
        addTrain("12553", "Vaishali Express", "NDLS", "BSB", 15, 10, 5, 35, 450, new ClassType[] { AC2, AC3, SL },
                new double[] { 1200, 800, 380 });
        addTrain("12555", "Gorakhpur Humsafar Express", "NDLS", "GKP", 21, 45, 9, 15, 500, new ClassType[] { AC3 },
                new double[] { 900 });
        addTrain("12559", "Shiv Ganga Express", "NDLS", "BSB", 19, 10, 6, 20, 480,
                new ClassType[] { AC1, AC2, AC3, SL }, new double[] { 1600, 1000, 700, 320 });
        addTrain("12801", "Purushottam Express", "NDLS", "PNBE", 22, 35, 14, 35, 520,
                new ClassType[] { AC1, AC2, AC3, SL }, new double[] { 1800, 1100, 780, 360 });
        addTrain("12803", "Swarna Jayanti Rajdhani Express", "NDLS", "ADI", 19, 25, 8, 35, 500,
                new ClassType[] { AC1, AC2, AC3 }, new double[] { 2800, 1700, 1200 });
        addTrain("12957", "Swarna Jayanti Rajdhani Express", "NDLS", "ADI", 19, 55, 9, 15, 480,
                new ClassType[] { AC1, AC2, AC3 }, new double[] { 2800, 1700, 1200 });
        addTrain("13009", "Doon Express", "HWH", "DDN", 22, 0, 5, 55, 400, new ClassType[] { AC2, AC3, SL },
                new double[] { 1400, 950, 430 });
        addTrain("13049", "Amritsar Express", "HWH", "ASR", 19, 35, 8, 40, 450, new ClassType[] { AC2, AC3, SL },
                new double[] { 1600, 1100, 500 });
        addTrain("14055", "Brahmaputra Mail", "DLI", "DGR", 14, 30, 6, 0, 420, new ClassType[] { AC2, AC3, SL },
                new double[] { 2000, 1400, 650 });
        addTrain("14673", "Shaheed Express", "DLI", "JAT", 22, 10, 12, 5, 380, new ClassType[] { AC2, AC3, SL },
                new double[] { 1000, 700, 320 });
        addTrain("15635", "Guwahati Express", "HWH", "GHY", 15, 55, 14, 35, 440, new ClassType[] { AC2, AC3, SL },
                new double[] { 1300, 900, 420 });
        addTrain("16315", "Kochuveli Express", "SBC", "TVC", 19, 30, 9, 30, 350, new ClassType[] { AC2, AC3, SL },
                new double[] { 900, 600, 280 });
        addTrain("16339", "Nagarcoil Express", "MAS", "TVC", 20, 0, 12, 30, 380, new ClassType[] { AC2, AC3, SL },
                new double[] { 1000, 700, 320 });
        addTrain("17229", "Sabari Express", "SC", "TVC", 5, 45, 21, 20, 400, new ClassType[] { AC2, AC3, SL },
                new double[] { 1100, 750, 350 });
        addTrain("16527", "Yesvantpur Express", "SBC", "MAS", 22, 30, 7, 0, 350, new ClassType[] { AC2, AC3, SL },
                new double[] { 800, 550, 260 });
        addTrain("12275", "Allahabad Duronto Express", "NDLS", "BSB", 23, 15, 8, 0, 400, new ClassType[] { AC2, AC3 },
                new double[] { 1400, 950 });
        addTrain("12285", "Secunderabad Duronto Express", "HWH", "SC", 20, 5, 22, 20, 400,
                new ClassType[] { AC1, AC2, AC3, SL }, new double[] { 2600, 1600, 1100, 520 });
        addTrain("12321", "Howrah Mumbai Mail", "HWH", "CSTM", 21, 0, 3, 15, 500, new ClassType[] { AC1, AC2, AC3, SL },
                new double[] { 2200, 1400, 1000, 460 });
        addTrain("12381", "Poorva Express", "HWH", "NDLS", 20, 15, 23, 40, 520, new ClassType[] { AC1, AC2, AC3, SL },
                new double[] { 2000, 1200, 850, 400 });
        addTrain("12423", "Dibrugarh Rajdhani Express", "NDLS", "DBRG", 15, 30, 17, 30, 450,
                new ClassType[] { AC1, AC2, AC3 }, new double[] { 4500, 2800, 2000 });
        addTrain("12505", "North East Express", "NDLS", "GHY", 14, 15, 20, 5, 480,
                new ClassType[] { AC1, AC2, AC3, SL }, new double[] { 2800, 1800, 1250, 580 });
        addTrain("12561", "Swatantrya Senani Express", "LKO", "BCT", 14, 15, 6, 40, 450,
                new ClassType[] { AC2, AC3, SL }, new double[] { 1600, 1100, 500 });
        addTrain("12625", "Thiruvananthapuram Kerala Express", "NDLS", "TVC", 11, 25, 19, 20, 600,
                new ClassType[] { AC1, AC2, AC3, SL }, new double[] { 3200, 2000, 1400, 650 });
        addTrain("12707", "Andhra Pradesh AC Express", "SC", "NDLS", 17, 50, 12, 15, 400, new ClassType[] { AC2, AC3 },
                new double[] { 1800, 1200 });
        addTrain("12723", "Andhra Pradesh Express", "SC", "NDLS", 6, 25, 22, 40, 480,
                new ClassType[] { AC1, AC2, AC3, SL }, new double[] { 2400, 1500, 1050, 480 });
        addTrain("12779", "Goa Express", "NDLS", "CSTM", 15, 0, 11, 35, 420, new ClassType[] { AC2, AC3, SL },
                new double[] { 1800, 1200, 550 });
        addTrain("12859", "Gitanjali Express", "CSTM", "HWH", 6, 0, 20, 5, 480, new ClassType[] { AC1, AC2, AC3, SL },
                new double[] { 2100, 1300, 900, 420 });
        addTrain("12919", "Gujarat Mail Express", "CSTM", "ADI", 21, 50, 6, 10, 450,
                new ClassType[] { AC1, AC2, AC3, SL }, new double[] { 1200, 800, 550, 260 });
        addTrain("12949", "Gujarat SF Express", "CSTM", "ADI", 23, 0, 7, 40, 450, new ClassType[] { AC2, AC3, SL },
                new double[] { 800, 550, 260 });
        addTrain("12963", "Mewar Express", "NDLS", "UDZ", 19, 0, 7, 15, 380, new ClassType[] { AC2, AC3, SL },
                new double[] { 1200, 800, 380 });
        addTrain("12985", "Jaipur Double Decker Express", "NDLS", "JP", 6, 5, 10, 30, 600, new ClassType[] { CC },
                new double[] { 650 });
        addTrain("12991", "Udaipur SF Express", "JP", "UDZ", 22, 0, 6, 15, 350, new ClassType[] { AC2, AC3, SL },
                new double[] { 800, 550, 260 });
        addTrain("13005", "Amritsar Howrah Mail", "ASR", "HWH", 10, 0, 21, 0, 480, new ClassType[] { AC2, AC3, SL },
                new double[] { 1800, 1200, 560 });
        addTrain("15017", "Gorakhpur Express", "LKO", "GKP", 7, 30, 15, 20, 350, new ClassType[] { AC3, SL, SS },
                new double[] { 500, 250, 120 });
        addTrain("16309", "Alleppey Express", "MAS", "ERS", 20, 40, 13, 0, 380, new ClassType[] { AC2, AC3, SL },
                new double[] { 900, 650, 300 });
        addTrain("17015", "Visakha Express", "SC", "VSKP", 16, 55, 6, 0, 380, new ClassType[] { AC2, AC3, SL },
                new double[] { 800, 550, 260 });
        addTrain("18029", "Shalimar Kurla Express", "HWH", "LTT", 15, 25, 19, 55, 450, new ClassType[] { AC2, AC3, SL },
                new double[] { 1500, 1000, 470 });
        addTrain("19019", "Dehradun Express", "BCT", "DDN", 23, 0, 5, 0, 380, new ClassType[] { AC2, AC3, SL },
                new double[] { 1600, 1100, 500 });
        addTrain("19023", "Janta Express", "DLI", "JAT", 21, 0, 11, 15, 400, new ClassType[] { AC3, SL, SS },
                new double[] { 650, 300, 150 });
        addTrain("22109", "Mumbai Hazrat Nizamuddin AC Express", "LTT", "NDLS", 10, 5, 5, 35, 380,
                new ClassType[] { AC1, AC2, AC3 }, new double[] { 2500, 1500, 1050 });
        addTrain("22691", "Bangalore Rajdhani Express", "SBC", "NDLS", 20, 0, 5, 55, 500,
                new ClassType[] { AC1, AC2, AC3 }, new double[] { 4300, 2700, 1950 });
        addTrain("22691", "KSR Bengaluru Rajdhani Express", "SBC", "NDLS", 20, 0, 5, 55, 500,
                new ClassType[] { AC1, AC2, AC3 }, new double[] { 4300, 2700, 1950 });

        // ============================================================
        // REGIONAL / SHORT DISTANCE TRAINS
        // ============================================================
        addTrain("12607", "Lalbagh Express", "SBC", "MAS", 6, 20, 11, 35, 400, new ClassType[] { CC, SS },
                new double[] { 450, 150 });
        addTrain("12609", "Chennai Express", "SBC", "MAS", 14, 20, 19, 45, 380, new ClassType[] { CC, AC2, SL },
                new double[] { 500, 800, 300 });
        addTrain("16057", "Saptagiri Express", "SC", "MAS", 5, 50, 12, 10, 380, new ClassType[] { CC, SS },
                new double[] { 350, 120 });
        addTrain("16093", "Lucknow Jn Express", "MAS", "LKO", 13, 30, 20, 0, 400, new ClassType[] { AC2, AC3, SL },
                new double[] { 1400, 950, 440 });
        addTrain("17603", "Prashanti Express", "SC", "SBC", 21, 30, 10, 0, 380, new ClassType[] { AC2, AC3, SL },
                new double[] { 1000, 700, 320 });
        addTrain("12785", "Ernakulam Superfast Express", "BPL", "ERS", 15, 0, 18, 30, 400,
                new ClassType[] { AC2, AC3, SL }, new double[] { 1500, 1050, 480 });
        addTrain("20103", "Vande Bharat Express", "NDLS", "BSB", 6, 0, 14, 0, 600, new ClassType[] { CC, AC2 },
                new double[] { 1800, 2500 });
        addTrain("20171", "Vande Bharat Express", "SBC", "MAS", 5, 30, 10, 30, 600, new ClassType[] { CC, AC2 },
                new double[] { 1100, 1600 });
        addTrain("20173", "Vande Bharat Express", "NDLS", "JP", 6, 0, 10, 30, 600, new ClassType[] { CC, AC2 },
                new double[] { 900, 1400 });

        System.out.println("✅ 105 trains created successfully!");
        System.out.println("==================================================");
        System.out.println("🚊 IRCTC Railway Reservation System Started!");
        System.out.println("🌐 Access at: http://localhost:9090");
        System.out.println("👤 Admin Login: admin / admin123");
        System.out.println("==================================================");
    }

    private void createAdminUser() {
        Administrator admin = new Administrator(
                "admin",
                passwordEncoder.encode("admin123"),
                "admin@irctc.com",
                "9876543210",
                "ADM001",
                "Operations");
        userRepository.save(admin);
        System.out.println("✅ Admin user created - Username: admin, Password: admin123");
    }
}