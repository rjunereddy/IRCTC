package com.irctc.config;

import com.irctc.model.*;
import com.irctc.model.enums.ClassType;
import com.irctc.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;

@Component
@Transactional
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final com.irctc.repository.PassengerRepository passengerRepository;
    private final TrainRepository trainRepository;
    private final StationRepository stationRepository;
    private final TrainRouteStopRepository trainRouteStopRepository;
    private final PasswordEncoder passwordEncoder;

    // In-memory station cache for train creation
    private final Map<String, Station> stationCache = new HashMap<>();
    private final List<Station> allStationsList = new ArrayList<>();
    private final Random random = new Random();

    public DataInitializer(UserRepository userRepository,
            com.irctc.repository.PassengerRepository passengerRepository,
            TrainRepository trainRepository,
            StationRepository stationRepository,
            TrainRouteStopRepository trainRouteStopRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passengerRepository = passengerRepository;
        this.trainRepository = trainRepository;
        this.stationRepository = stationRepository;
        this.trainRouteStopRepository = trainRouteStopRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚂 Initializing IRCTC Railway System...");

        if (stationRepository.count() < 130 || !stationRepository.existsById("CT")) {
            createStations();
        }

        // Load all stations into cache and list
        stationCache.clear();
        allStationsList.clear();
        stationRepository.findAll().forEach(s -> {
            stationCache.put(s.getStationCode(), s);
            allStationsList.add(s);
        });

        if (trainRepository.count() == 0 || !trainRepository.existsById("12701")) {
            createTrains();
        }

        // Check if train routes need upgrading to realistic multi-station corridors
        long trainCount = trainRepository.count();
        long stopCount = trainRouteStopRepository.count();
        if (trainCount > 0 && (stopCount < trainCount * 8 || !trainRepository.existsById("12701"))) {
            upgradeTrainRoutes();
        }

        if (!userRepository.existsByUsername("admin")) {
            createAdminUser();
        }
        if (!userRepository.existsByUsername("tte")) {
            createDemoStaffUser("tte", "tte123", "tte@irctc.co.in", "9876543212", com.irctc.model.enums.UserRole.TICKET_COLLECTOR);
        }
        if (!userRepository.existsByUsername("stationmaster")) {
            createDemoStaffUser("stationmaster", "sm123", "sm@irctc.co.in", "9876543213", com.irctc.model.enums.UserRole.STATION_MASTER);
        }
        if (!passengerRepository.existsByUsername("traveler")) {
            userRepository.findByUsername("traveler").ifPresent(userRepository::delete);
            Passenger traveler = new Passenger(
                "traveler",
                passwordEncoder.encode("user123"),
                "traveler@irctc.co.in",
                "9876543214",
                "Rajesh Kumar",
                28,
                "MALE"
            );
            passengerRepository.save(traveler);
            System.out.println("✅ Passenger traveler created!");
        }

        System.out.println("✅ Data initialization completed!");
    }

    private void createStations() {
        System.out.println("Creating comprehensive Indian Railway stations...");

        String[][] stationData = {
                // {Code, Name, City, State}
                // --- Karnataka ---
                { "SBC", "KSR Bengaluru", "Bangalore", "Karnataka" },
                { "BNC", "Bengaluru Cantt", "Bangalore", "Karnataka" },
                { "YPR", "Yesvantpur Junction", "Bangalore", "Karnataka" },
                { "KJM", "Krishnarajapuram", "Bangalore", "Karnataka" },
                { "WFD", "Whitefield", "Bangalore", "Karnataka" },
                { "BWT", "Bangarapet Junction", "Bangarapet", "Karnataka" },
                { "MYS", "Mysuru Junction", "Mysore", "Karnataka" },
                { "UBL", "Hubballi Junction", "Hubballi", "Karnataka" },
                { "BGM", "Belagavi", "Belgaum", "Karnataka" },
                { "DVG", "Davangere", "Davangere", "Karnataka" },
                { "ASK", "Arsikere Junction", "Arsikere", "Karnataka" },
                { "TK", "Tumakuru", "Tumakuru", "Karnataka" },
                { "RC", "Raichur Junction", "Raichur", "Karnataka" },
                { "YG", "Yadgir", "Yadgir", "Karnataka" },
                { "WADI", "Wadi Junction", "Wadi", "Karnataka" },
                { "KLBG", "Kalaburagi", "Gulbarga", "Karnataka" },
                { "MAQ", "Mangaluru Central", "Mangalore", "Karnataka" },
                { "MAJN", "Mangaluru Junction", "Mangalore", "Karnataka" },

                // --- Tamil Nadu ---
                { "MAS", "Chennai Central", "Chennai", "Tamil Nadu" },
                { "MS", "Chennai Egmore", "Chennai", "Tamil Nadu" },
                { "PER", "Perambur", "Chennai", "Tamil Nadu" },
                { "TRL", "Tiruvallur", "Tiruvallur", "Tamil Nadu" },
                { "AJJ", "Arakkonam Junction", "Arakkonam", "Tamil Nadu" },
                { "KPD", "Katpadi Junction", "Vellore", "Tamil Nadu" },
                { "JTJ", "Jolarpettai Junction", "Jolarpettai", "Tamil Nadu" },
                { "SA", "Salem Junction", "Salem", "Tamil Nadu" },
                { "ED", "Erode Junction", "Erode", "Tamil Nadu" },
                { "TUP", "Tiruppur", "Tiruppur", "Tamil Nadu" },
                { "CBE", "Coimbatore Junction", "Coimbatore", "Tamil Nadu" },
                { "MDU", "Madurai Junction", "Madurai", "Tamil Nadu" },
                { "TPJ", "Tiruchirappalli Junction", "Trichy", "Tamil Nadu" },
                { "DG", "Dindigul Junction", "Dindigul", "Tamil Nadu" },
                { "TEN", "Tirunelveli Junction", "Tirunelveli", "Tamil Nadu" },
                { "NCJ", "Nagercoil Junction", "Nagercoil", "Tamil Nadu" },
                { "CAPE", "Kanniyakumari", "Kanyakumari", "Tamil Nadu" },

                // --- Kerala ---
                { "TVC", "Trivandrum Central", "Thiruvananthapuram", "Kerala" },
                { "KCVL", "Kochuveli", "Thiruvananthapuram", "Kerala" },
                { "QLN", "Kollam Junction", "Kollam", "Kerala" },
                { "KYJ", "Kayamkulam Junction", "Kayamkulam", "Kerala" },
                { "MVLK", "Mavelikara", "Mavelikara", "Kerala" },
                { "CNGR", "Chengannur", "Chengannur", "Kerala" },
                { "TRVL", "Tiruvalla", "Tiruvalla", "Kerala" },
                { "KTYM", "Kottayam", "Kottayam", "Kerala" },
                { "ERS", "Ernakulam Junction", "Kochi", "Kerala" },
                { "ERN", "Ernakulam Town", "Kochi", "Kerala" },
                { "AWY", "Aluva", "Kochi", "Kerala" },
                { "TCR", "Thrissur", "Thrissur", "Kerala" },
                { "SRR", "Shoranur Junction", "Shoranur", "Kerala" },
                { "TIR", "Tirur", "Tirur", "Kerala" },
                { "CLT", "Kozhikode", "Calicut", "Kerala" },
                { "CAN", "Kannur", "Cannanore", "Kerala" },
                { "KGQ", "Kasaragod", "Kasaragod", "Kerala" },

                // --- Andhra Pradesh & Telangana ---
                { "HYB", "Hyderabad Deccan", "Hyderabad", "Telangana" },
                { "SC", "Secunderabad Junction", "Secunderabad", "Telangana" },
                { "KZJ", "Kazipet Junction", "Kazipet", "Telangana" },
                { "WL", "Warangal", "Warangal", "Telangana" },
                { "KMT", "Khammam", "Khammam", "Telangana" },
                { "RDM", "Ramagundam", "Ramagundam", "Telangana" },
                { "BZA", "Vijayawada Junction", "Vijayawada", "Andhra Pradesh" },
                { "TEL", "Tenali Junction", "Tenali", "Andhra Pradesh" },
                { "CLX", "Chirala", "Chirala", "Andhra Pradesh" },
                { "OGL", "Ongole", "Ongole", "Andhra Pradesh" },
                { "NLR", "Nellore", "Nellore", "Andhra Pradesh" },
                { "GDR", "Gudur Junction", "Gudur", "Andhra Pradesh" },
                { "RU", "Renigunta Junction", "Tirupati", "Andhra Pradesh" },
                { "GTL", "Guntakal Junction", "Guntakal", "Andhra Pradesh" },
                { "AD", "Adoni", "Adoni", "Andhra Pradesh" },
                { "MALM", "Mantralayam Road", "Mantralayam", "Andhra Pradesh" },
                { "ATP", "Anantapur", "Anantapur", "Andhra Pradesh" },
                { "DMM", "Dharmavaram Junction", "Dharmavaram", "Andhra Pradesh" },
                { "EE", "Eluru", "Eluru", "Andhra Pradesh" },
                { "RJY", "Rajahmundry", "Rajahmundry", "Andhra Pradesh" },
                { "SLO", "Samalkot Junction", "Samalkot", "Andhra Pradesh" },
                { "VSKP", "Visakhapatnam", "Visakhapatnam", "Andhra Pradesh" },
                { "VZM", "Vizianagaram Junction", "Vizianagaram", "Andhra Pradesh" },
                { "CHE", "Srikakulam Road", "Srikakulam", "Andhra Pradesh" },
                { "KPN", "Kuppam", "Kuppam", "Andhra Pradesh" },

                // --- Maharashtra ---
                { "BCT", "Mumbai Central", "Mumbai", "Maharashtra" },
                { "CSTM", "Chhatrapati Shivaji Terminus", "Mumbai", "Maharashtra" },
                { "LTT", "Lokmanya Tilak Terminus", "Mumbai", "Maharashtra" },
                { "BVI", "Borivali", "Mumbai", "Maharashtra" },
                { "DR", "Dadar Central", "Mumbai", "Maharashtra" },
                { "TNA", "Thane", "Thane", "Maharashtra" },
                { "KYN", "Kalyan Junction", "Kalyan", "Maharashtra" },
                { "KJT", "Karjat Junction", "Karjat", "Maharashtra" },
                { "LNL", "Lonavala", "Lonavala", "Maharashtra" },
                { "PNQ", "Pune Junction", "Pune", "Maharashtra" },
                { "DD", "Daund Junction", "Daund", "Maharashtra" },
                { "KWV", "Kurduvadi Junction", "Kurduvadi", "Maharashtra" },
                { "SUR", "Solapur Junction", "Solapur", "Maharashtra" },
                { "ANG", "Ahmednagar", "Ahmednagar", "Maharashtra" },
                { "KPG", "Kopargaon", "Kopargaon", "Maharashtra" },
                { "MMR", "Manmad Junction", "Manmad", "Maharashtra" },
                { "JL", "Jalgaon Junction", "Jalgaon", "Maharashtra" },
                { "BSL", "Bhusaval Junction", "Bhusaval", "Maharashtra" },
                { "AK", "Akola Junction", "Akola", "Maharashtra" },
                { "BD", "Badnera Junction", "Amravati", "Maharashtra" },
                { "WR", "Wardha Junction", "Wardha", "Maharashtra" },
                { "NGP", "Nagpur Junction", "Nagpur", "Maharashtra" },
                { "CD", "Chandrapur", "Chandrapur", "Maharashtra" },
                { "BPQ", "Balharshah Junction", "Balharshah", "Maharashtra" },
                { "G", "Gondia Junction", "Gondia", "Maharashtra" },
                { "IGP", "Igatpuri", "Igatpuri", "Maharashtra" },
                { "NK", "Nashik Road", "Nashik", "Maharashtra" },

                // --- Delhi, Haryana, Punjab, J&K & Uttarakhand ---
                { "NDLS", "New Delhi", "Delhi", "Delhi" },
                { "DLI", "Old Delhi Junction", "Delhi", "Delhi" },
                { "NZM", "Hazrat Nizamuddin", "Delhi", "Delhi" },
                { "FDB", "Faridabad", "Faridabad", "Haryana" },
                { "GGN", "Gurgaon", "Gurgaon", "Haryana" },
                { "RE", "Rewari Junction", "Rewari", "Haryana" },
                { "SNP", "Sonipat Junction", "Sonipat", "Haryana" },
                { "PNP", "Panipat Junction", "Panipat", "Haryana" },
                { "KUN", "Karnal", "Karnal", "Haryana" },
                { "KKDE", "Kurukshetra Junction", "Kurukshetra", "Haryana" },
                { "UMB", "Ambala Cantt Junction", "Ambala", "Haryana" },
                { "CDG", "Chandigarh Junction", "Chandigarh", "Chandigarh" },
                { "LDH", "Ludhiana Junction", "Ludhiana", "Punjab" },
                { "JRC", "Jalandhar Cantt Junction", "Jalandhar", "Punjab" },
                { "ASR", "Amritsar Junction", "Amritsar", "Punjab" },
                { "PTKC", "Pathankot Cantt", "Pathankot", "Punjab" },
                { "KTHU", "Kathua", "Kathua", "Jammu & Kashmir" },
                { "JAT", "Jammu Tawi", "Jammu", "Jammu & Kashmir" },
                { "UHP", "Udhampur", "Udhampur", "Jammu & Kashmir" },
                { "SVDK", "Shri Mata Vaishno Devi Katra", "Katra", "Jammu & Kashmir" },
                { "DDN", "Dehradun", "Dehradun", "Uttarakhand" },
                { "HW", "Haridwar Junction", "Haridwar", "Uttarakhand" },

                // --- Gujarat & Rajasthan ---
                { "VAPI", "Vapi", "Vapi", "Gujarat" },
                { "BL", "Valsad", "Valsad", "Gujarat" },
                { "ST", "Surat", "Surat", "Gujarat" },
                { "BH", "Bharuch Junction", "Bharuch", "Gujarat" },
                { "BRC", "Vadodara Junction", "Vadodara", "Gujarat" },
                { "ANND", "Anand Junction", "Anand", "Gujarat" },
                { "ND", "Nadiad Junction", "Nadiad", "Gujarat" },
                { "ADI", "Ahmedabad Junction", "Ahmedabad", "Gujarat" },
                { "RJT", "Rajkot Junction", "Rajkot", "Gujarat" },
                { "KOTA", "Kota Junction", "Kota", "Rajasthan" },
                { "SWM", "Sawai Madhopur Junction", "Sawai Madhopur", "Rajasthan" },
                { "BTE", "Bharatpur Junction", "Bharatpur", "Rajasthan" },
                { "AWR", "Alwar Junction", "Alwar", "Rajasthan" },
                { "BKI", "Bandikui Junction", "Bandikui", "Rajasthan" },
                { "DO", "Dausa", "Dausa", "Rajasthan" },
                { "GADJ", "Gandhinagar Jaipur", "Jaipur", "Rajasthan" },
                { "JP", "Jaipur Junction", "Jaipur", "Rajasthan" },
                { "KSG", "Kishangarh", "Kishangarh", "Rajasthan" },
                { "AII", "Ajmer Junction", "Ajmer", "Rajasthan" },
                { "BER", "Beawar", "Beawar", "Rajasthan" },
                { "MJ", "Marwar Junction", "Marwar", "Rajasthan" },
                { "PMY", "Pali Marwar", "Pali", "Rajasthan" },
                { "JU", "Jodhpur Junction", "Jodhpur", "Rajasthan" },
                { "UDZ", "Udaipur City", "Udaipur", "Rajasthan" },

                // --- Madhya Pradesh & Chhattisgarh ---
                { "BPL", "Bhopal Junction", "Bhopal", "Madhya Pradesh" },
                { "BINA", "Bina Junction", "Bina", "Madhya Pradesh" },
                { "ET", "Itarsi Junction", "Itarsi", "Madhya Pradesh" },
                { "KNW", "Khandwa Junction", "Khandwa", "Madhya Pradesh" },
                { "GWL", "Gwalior Junction", "Gwalior", "Madhya Pradesh" },
                { "JBP", "Jabalpur Junction", "Jabalpur", "Madhya Pradesh" },
                { "RTM", "Ratlam Junction", "Ratlam", "Madhya Pradesh" },
                { "UJN", "Ujjain Junction", "Ujjain", "Madhya Pradesh" },
                { "INDB", "Indore Junction", "Indore", "Madhya Pradesh" },
                { "DURG", "Durg Junction", "Durg", "Chhattisgarh" },
                { "R", "Raipur Junction", "Raipur", "Chhattisgarh" },
                { "BSP", "Bilaspur Junction", "Bilaspur", "Chhattisgarh" },

                // --- Uttar Pradesh & Bihar ---
                { "GZB", "Ghaziabad Junction", "Ghaziabad", "Uttar Pradesh" },
                { "ALJN", "Aligarh Junction", "Aligarh", "Uttar Pradesh" },
                { "TDL", "Tundla Junction", "Tundla", "Uttar Pradesh" },
                { "AGC", "Agra Cantt", "Agra", "Uttar Pradesh" },
                { "MTJ", "Mathura Junction", "Mathura", "Uttar Pradesh" },
                { "CNB", "Kanpur Central", "Kanpur", "Uttar Pradesh" },
                { "FTP", "Fatehpur", "Fatehpur", "Uttar Pradesh" },
                { "PRYJ", "Prayagraj Junction", "Prayagraj", "Uttar Pradesh" },
                { "MZP", "Mirzapur", "Mirzapur", "Uttar Pradesh" },
                { "VGLJ", "Virangana Lakshmibai Jhansi", "Jhansi", "Uttar Pradesh" },
                { "LKO", "Lucknow Charbagh", "Lucknow", "Uttar Pradesh" },
                { "MB", "Moradabad Junction", "Moradabad", "Uttar Pradesh" },
                { "BE", "Bareilly Junction", "Bareilly", "Uttar Pradesh" },
                { "SPN", "Shahjahanpur", "Shahjahanpur", "Uttar Pradesh" },
                { "AYC", "Ayodhya Cantt", "Ayodhya", "Uttar Pradesh" },
                { "AY", "Ayodhya Dham", "Ayodhya", "Uttar Pradesh" },
                { "BST", "Basti", "Basti", "Uttar Pradesh" },
                { "GKP", "Gorakhpur Junction", "Gorakhpur", "Uttar Pradesh" },
                { "DDU", "Pt. Deen Dayal Upadhyaya Junction", "Mughalsarai", "Uttar Pradesh" },
                { "BSB", "Varanasi Junction", "Varanasi", "Uttar Pradesh" },
                { "BXR", "Buxar", "Buxar", "Bihar" },
                { "ARA", "Ara Junction", "Ara", "Bihar" },
                { "PNBE", "Patna Junction", "Patna", "Bihar" },
                { "MKA", "Mokama", "Mokama", "Bihar" },
                { "KIUL", "Kiul Junction", "Kiul", "Bihar" },
                { "GAYA", "Gaya Junction", "Gaya", "Bihar" },

                // --- Odisha, Jharkhand, West Bengal & North East ---
                { "JSME", "Jasidih Junction", "Jasidih", "Jharkhand" },
                { "MDP", "Madhupur Junction", "Madhupur", "Jharkhand" },
                { "DHN", "Dhanbad Junction", "Dhanbad", "Jharkhand" },
                { "RNC", "Ranchi Junction", "Ranchi", "Jharkhand" },
                { "TATA", "Tatanagar Junction", "Jamshedpur", "Jharkhand" },
                { "ROU", "Rourkela Junction", "Rourkela", "Odisha" },
                { "CTC", "Cuttack Junction", "Cuttack", "Odisha" },
                { "BBS", "Bhubaneswar", "Bhubaneswar", "Odisha" },
                { "KUR", "Khurda Road Junction", "Khurda", "Odisha" },
                { "BAM", "Brahmapur", "Brahmapur", "Odisha" },
                { "BLS", "Baleshwar", "Balasore", "Odisha" },
                { "BHC", "Bhadrak", "Bhadrak", "Odisha" },
                { "CRJ", "Chittaranjan", "Chittaranjan", "West Bengal" },
                { "ASN", "Asansol Junction", "Asansol", "West Bengal" },
                { "DGR", "Durgapur", "Durgapur", "West Bengal" },
                { "BWN", "Barddhaman Junction", "Barddhaman", "West Bengal" },
                { "SRC", "Santragachi Junction", "Howrah", "West Bengal" },
                { "KGP", "Kharagpur Junction", "Kharagpur", "West Bengal" },
                { "HWH", "Howrah Junction", "Howrah", "West Bengal" },
                { "KOL", "Kolkata", "Kolkata", "West Bengal" },
                { "SDAH", "Sealdah", "Kolkata", "West Bengal" },
                { "MLDT", "Malda Town", "Malda", "West Bengal" },
                { "NJP", "New Jalpaiguri Junction", "Siliguri", "West Bengal" },
                { "GHY", "Guwahati", "Guwahati", "Assam" },
                { "DBRG", "Dibrugarh", "Dibrugarh", "Assam" },

                // --- Karnataka & Telangana Hyderabad Corridor Stations ---
                { "CT", "Chittapur", "Chittapur", "Karnataka" },
                { "SEM", "Sedam", "Sedam", "Karnataka" },
                { "TDU", "Tandur", "Tandur", "Telangana" },
                { "VKB", "Vikarabad Junction", "Vikarabad", "Telangana" },
                { "LPI", "Lingampalli", "Hyderabad", "Telangana" },
                { "BMT", "Begumpet", "Hyderabad", "Telangana" },
                { "KCG", "Kacheguda", "Hyderabad", "Telangana" },
                { "KRNT", "Kurnool City", "Kurnool", "Andhra Pradesh" },
                { "MBNR", "Mahbubnagar", "Mahbubnagar", "Telangana" },
                { "GWD", "Gadwal Junction", "Gadwal", "Telangana" },
                { "DHNE", "Dhone Junction", "Dhone", "Andhra Pradesh" },
                { "JCL", "Jadcherla", "Jadcherla", "Telangana" },
                { "SHNR", "Shadnagar", "Shadnagar", "Telangana" }
        };

        for (String[] s : stationData) {
            Station station = stationRepository.findById(s[0]).orElse(new Station(s[0], s[1], s[2], s[3]));
            boolean isJunc = s[1].contains("Junction") || s[1].contains("Central") || s[1].contains("Terminus") 
                || List.of("KLBG", "SC", "HYB", "NDLS", "SBC", "MAS", "BCT", "HWH", "BZA", "SUR", "PNQ", "GTL", "VKB", "KCG").contains(s[0]);
            station.setJunction(isJunc);
            station.setPlatformCount(isJunc ? 6 : (s[0].equals("SEM") || s[0].equals("CT") ? 2 : 3));
            stationRepository.save(station);
            stationCache.put(s[0], station);
        }

        System.out.println("✅ Stations created: " + stationData.length + " stations across India");
    }

    // =========================================================================
    // REALISTIC RAILWAY CORRIDOR DEFINITIONS
    // =========================================================================
    private static final List<List<String>> CORRIDORS = List.of(
            // 1. Bangalore - Trivandrum Corridor (Southern Railway)
            List.of("SBC", "KJM", "BWT", "KPN", "JTJ", "SA", "ED", "TUP", "CBE", "PGT", "TCR", "AWY", "ERN", "KTYM", "TRVL", "CNGR", "MVLK", "KYJ", "QLN", "KCVL", "TVC"),

            // 2. Mumbai - Delhi Corridor (Western Railway)
            List.of("BCT", "BVI", "VAPI", "BL", "ST", "BH", "BRC", "ANND", "RTM", "KOTA", "SWM", "BTE", "MTJ", "FDB", "NZM", "NDLS"),

            // 3. Delhi - Howrah Grand Chord (Northern & Eastern Railway)
            List.of("NDLS", "GZB", "ALJN", "TDL", "CNB", "FTP", "PRYJ", "MZP", "DDU", "BXR", "ARA", "PNBE", "MKA", "KIUL", "JSME", "MDP", "CRJ", "ASN", "DGR", "BWN", "HWH"),

            // 4. Chennai - Delhi Grand Trunk Corridor
            List.of("MAS", "GDR", "NLR", "OGL", "CLX", "TEL", "BZA", "KMT", "WL", "KZJ", "RDM", "BPQ", "CD", "WR", "NGP", "ET", "BPL", "BINA", "VGLJ", "GWL", "AGC", "MTJ", "FDB", "NDLS"),

            // 5. Bangalore - Delhi South-North Corridor
            List.of("SBC", "YPR", "DMM", "ATP", "GTL", "AD", "MALM", "RC", "YG", "WADI", "KLBG", "SUR", "DD", "ANG", "KPG", "MMR", "JL", "BSL", "KNW", "ET", "BPL", "BINA", "VGLJ", "GWL", "AGC", "MTJ", "NDLS"),

            // 6. Bangalore - Chennai Corridor
            List.of("SBC", "BNC", "KJM", "WFD", "BWT", "KPN", "JTJ", "KPD", "AJJ", "TRL", "PER", "MAS"),

            // 7. Mumbai - Chennai Corridor
            List.of("CSTM", "DR", "TNA", "KYN", "KJT", "LNL", "PNQ", "DD", "KWV", "SUR", "KLBG", "WADI", "YG", "RC", "MALM", "AD", "GTL", "RU", "AJJ", "PER", "MAS"),

            // 8. Howrah - Chennai Coromandel Corridor
            List.of("HWH", "SRC", "KGP", "BLS", "BHC", "CTC", "BBS", "KUR", "BAM", "CHE", "VZM", "VSKP", "RJY", "SLO", "EE", "BZA", "TEL", "CLX", "OGL", "NLR", "GDR", "MAS"),

            // 9. Mumbai - Howrah Central Corridor (Gitanjali)
            List.of("CSTM", "DR", "TNA", "KYN", "IGP", "NK", "MMR", "JL", "BSL", "AK", "BD", "WR", "NGP", "G", "DURG", "R", "BSP", "ROU", "TATA", "KGP", "HWH"),

            // 10. Delhi - Jammu - Katra Corridor
            List.of("NDLS", "SNP", "PNP", "KUN", "KKDE", "UMB", "LDH", "JRC", "PTKC", "KTHU", "JAT", "UHP", "SVDK"),

            // 11. Mumbai - Ahmedabad Corridor
            List.of("BCT", "BVI", "VAPI", "BL", "ST", "BH", "BRC", "ANND", "ND", "ADI"),

            // 12. Delhi - Jaipur - Jodhpur Corridor
            List.of("NDLS", "GGN", "RE", "AWR", "BKI", "DO", "GADJ", "JP", "KSG", "AII", "BER", "MJ", "PMY", "JU"),

            // 13. Delhi - Lucknow - Gorakhpur Corridor
            List.of("NDLS", "GZB", "MB", "BE", "SPN", "LKO", "AYC", "AY", "BST", "GKP"),

            // 14. Howrah - Guwahati - Dibrugarh Corridor
            List.of("HWH", "BWN", "MLDT", "NJP", "GHY", "DBRG"),

            // 15. Delhi - Amritsar Corridor
            List.of("NDLS", "SNP", "PNP", "UMB", "LDH", "JRC", "ASR"),

            // 16. Mumbai - Pune Intercity Corridor
            List.of("CSTM", "DR", "TNA", "KYN", "KJT", "LNL", "PNQ"),

            // 17. Secunderabad - Delhi Corridor
            List.of("SC", "KZJ", "WL", "RDM", "BPQ", "CD", "NGP", "ET", "BPL", "BINA", "VGLJ", "GWL", "AGC", "MTJ", "NDLS"),

            // 18. Secunderabad - Trivandrum (Sabari) Corridor
            List.of("SC", "BZA", "TEL", "OGL", "NLR", "RU", "KPD", "JTJ", "SA", "ED", "TUP", "CBE", "PGT", "TCR", "AWY", "ERN", "KTYM", "CNGR", "QLN", "TVC"),

            // 19. Bangalore - Howrah Corridor
            List.of("SBC", "KJM", "BWT", "KPD", "RU", "BZA", "RJY", "VSKP", "BAM", "BBS", "CTC", "BLS", "KGP", "HWH"),

            // 20. Delhi - Varanasi Corridor
            List.of("NDLS", "GZB", "ALJN", "CNB", "FTP", "PRYJ", "MZP", "BSB"),

            // 21. Delhi - Bhopal Shatabdi Corridor
            List.of("NDLS", "MTJ", "AGC", "GWL", "VGLJ", "BINA", "BPL"),

            // 22. Chennai - Mysore Shatabdi Corridor
            List.of("MAS", "PER", "AJJ", "KPD", "JTJ", "BWT", "KJM", "SBC", "MYS"),

            // 23. Kalaburagi - Secunderabad / Hyderabad Mainline Corridor
            List.of("KLBG", "WADI", "CT", "SEM", "TDU", "VKB", "LPI", "BMT", "SC", "HYB"),

            // 24. Mumbai - Pune - Solapur - Kalaburagi - Hyderabad Corridor (Hussain Sagar Route)
            List.of("CSTM", "DR", "TNA", "KYN", "LNL", "PNQ", "DD", "KWV", "SUR", "KLBG", "WADI", "CT", "SEM", "TDU", "VKB", "LPI", "BMT", "SC", "HYB"),

            // 25. Bangalore - Guntakal - Raichur - Kalaburagi - Hyderabad Corridor
            List.of("SBC", "YPR", "DMM", "ATP", "GTL", "AD", "MALM", "RC", "YG", "WADI", "KLBG", "SEM", "TDU", "VKB", "LPI", "BMT", "SC", "HYB"),

            // 26. Bangalore - Kurnool - Kacheguda (Hyderabad) Corridor
            List.of("SBC", "YPR", "DMM", "ATP", "DHNE", "KRNT", "GWD", "MBNR", "JCL", "SHNR", "KCG", "SC"),

            // 27. Mumbai - Pune - Kalaburagi - Secunderabad - Visakhapatnam - Bhubaneswar (Konark Route)
            List.of("CSTM", "DR", "TNA", "KYN", "LNL", "PNQ", "DD", "SUR", "KLBG", "WADI", "TDU", "VKB", "SC", "KZJ", "WL", "KMT", "BZA", "EE", "RJY", "SLO", "VSKP", "VZM", "CHE", "BAM", "KUR", "BBS")
    );

    @Transactional
    public void upgradeTrainRoutes() {
        System.out.println("🔄 Upgrading train routes with realistic multi-station corridors...");
        trainRouteStopRepository.deleteAll();

        List<Object[]> trainInfos = trainRepository.findTrainRouteBasicInfo();
        for (Object[] row : trainInfos) {
            String trainNo = (String) row[0];
            String srcCode = (String) row[1];
            String destCode = (String) row[2];
            LocalTime dep = (LocalTime) row[3];
            LocalTime arr = (LocalTime) row[4];

            Train trainProxy = trainRepository.getReferenceById(trainNo);
            List<TrainRouteStop> stops = buildRealisticStopsForInfo(trainProxy, srcCode, destCode, dep, arr);
            trainRouteStopRepository.saveAll(stops);
        }
        System.out.println("✅ Routes upgraded to " + trainRouteStopRepository.count() + " realistic corridor stops!");
    }

    /**
     * Builds authentic sequential stops for a train based on real railway corridors.
     */
    private List<TrainRouteStop> buildRealisticStopsForTrain(Train train) {
        String srcCode = train.getSourceStation().getStationCode();
        String destCode = train.getDestinationStation().getStationCode();
        return buildRealisticStopsForInfo(train, srcCode, destCode, train.getDepartureTime(), train.getArrivalTime());
    }

    private List<TrainRouteStop> buildRealisticStopsForInfo(Train train, String srcCode, String destCode, LocalTime dep, LocalTime arr) {
        List<String> matchedStationCodes = findCorridorSequence(srcCode, destCode);

        // Fallback: if no direct corridor sequence matched, build an 8-station junction route
        if (matchedStationCodes == null || matchedStationCodes.size() < 2) {
            matchedStationCodes = buildFallbackRoute(srcCode, destCode);
        }

        List<TrainRouteStop> stops = new ArrayList<>();
        int totalStops = matchedStationCodes.size();

        if (dep == null) dep = LocalTime.of(8, 0);
        if (arr == null) arr = LocalTime.of(20, 0);

        int depSec = dep.toSecondOfDay();
        int arrSec = arr.toSecondOfDay();
        if (arrSec <= depSec) arrSec += 86400; // Overnight journey
        int totalSec = arrSec - depSec;

        int estimatedTotalKm = Math.max(120, (totalStops - 1) * 65);

        for (int i = 0; i < totalStops; i++) {
            String code = matchedStationCodes.get(i);
            Station st = stationCache.get(code);
            if (st == null) {
                st = stationRepository.findById(code).orElse(null);
            }
            if (st == null) continue;

            TrainRouteStop stop = new TrainRouteStop();
            stop.setTrain(train);
            stop.setStation(st);
            stop.setStopOrder(i);

            int dist = (int) Math.round(((double) i / (totalStops - 1)) * estimatedTotalKm);
            stop.setDistanceInKm(dist);

            if (i == 0) {
                // Source station
                stop.setDepartureTime(dep);
                stop.setPlatformNumber("1");
                stop.setHaltMinutes(0);
            } else if (i == totalStops - 1) {
                // Destination station
                stop.setArrivalTime(arr);
                stop.setPlatformNumber(st.isMajorStation() ? "2" : "1");
                stop.setHaltMinutes(0);
            } else {
                // Intermediate station
                double fraction = (double) i / (totalStops - 1);
                int intermediateSec = depSec + (int) (fraction * totalSec);
                int halt = st.getDefaultHaltMinutes();
                int haltSec = halt * 60;
                LocalTime arrT = LocalTime.ofSecondOfDay(intermediateSec % 86400);
                LocalTime depT = LocalTime.ofSecondOfDay((intermediateSec + haltSec) % 86400);
                stop.setArrivalTime(arrT);
                stop.setDepartureTime(depT);
                int maxPlat = (st.getPlatformCount() != null && st.getPlatformCount() > 1) ? st.getPlatformCount() : 2;
                stop.setPlatformNumber(String.valueOf(1 + (i % maxPlat)));
                stop.setHaltMinutes(halt);
            }

            stops.add(stop);
        }

        return stops;
    }

    private List<String> findCorridorSequence(String src, String dest) {
        for (List<String> corridor : CORRIDORS) {
            int srcIdx = corridor.indexOf(src);
            int destIdx = corridor.indexOf(dest);

            if (srcIdx != -1 && destIdx != -1 && srcIdx != destIdx) {
                if (srcIdx < destIdx) {
                    // Forward route slice
                    return new ArrayList<>(corridor.subList(srcIdx, destIdx + 1));
                } else {
                    // Reverse route slice
                    List<String> sub = new ArrayList<>(corridor.subList(destIdx, srcIdx + 1));
                    Collections.reverse(sub);
                    return sub;
                }
            }
        }
        return null;
    }

    private List<String> buildFallbackRoute(String src, String dest) {
        List<String> route = new ArrayList<>();
        route.add(src);

        // Major national hub junctions to interpolate realistic connectivity
        String[] hubs = { "BPL", "NGP", "ET", "VGLJ", "BZA", "KPD", "JTJ", "SUR", "BSL", "CNB" };
        for (String hub : hubs) {
            if (!hub.equals(src) && !hub.equals(dest) && stationCache.containsKey(hub)) {
                route.add(hub);
                if (route.size() >= 7) break;
            }
        }

        route.add(dest);
        return route;
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

        // Generate and save authentic multi-stop corridor route stops
        List<TrainRouteStop> stops = buildRealisticStopsForTrain(t);
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

        // ============================================================
        // KALABURAGI - HYDERABAD / SECUNDERABAD CONNECTIVITY TRAINS
        // ============================================================
        addTrain("12701", "Hussain Sagar SF Express", "CSTM", "HYB", 21, 50, 12, 10, 600,
                new ClassType[] { AC1, AC2, AC3, SL }, new double[] { 2200, 1400, 1000, 450 });
        addTrain("12702", "Hussain Sagar SF Express", "HYB", "CSTM", 14, 50, 4, 55, 600,
                new ClassType[] { AC1, AC2, AC3, SL }, new double[] { 2200, 1400, 1000, 450 });
        addTrain("17031", "Mumbai Hyderabad Express", "CSTM", "HYB", 14, 10, 5, 30, 550,
                new ClassType[] { AC2, AC3, SL }, new double[] { 1350, 950, 420 });
        addTrain("17032", "Hyderabad Mumbai Express", "HYB", "CSTM", 20, 40, 13, 5, 550,
                new ClassType[] { AC2, AC3, SL }, new double[] { 1350, 950, 420 });
        addTrain("12025", "Pune Secunderabad Shatabdi Express", "PNQ", "SC", 6, 0, 14, 20, 700,
                new ClassType[] { CC, AC2 }, new double[] { 1050, 1650 });
        addTrain("12026", "Secunderabad Pune Shatabdi Express", "SC", "PNQ", 14, 45, 23, 10, 700,
                new ClassType[] { CC, AC2 }, new double[] { 1050, 1650 });
        addTrain("22232", "Kalaburagi Hyderabad Vande Bharat", "KLBG", "HYB", 5, 15, 9, 30, 500,
                new ClassType[] { CC, AC2 }, new double[] { 650, 1150 });
        addTrain("22231", "Hyderabad Kalaburagi Vande Bharat", "HYB", "KLBG", 17, 0, 21, 15, 500,
                new ClassType[] { CC, AC2 }, new double[] { 650, 1150 });
        addTrain("11019", "Konark Express", "CSTM", "BBS", 14, 0, 23, 15, 520,
                new ClassType[] { AC2, AC3, SL }, new double[] { 1600, 1100, 490 });
        addTrain("11020", "Konark Express", "BBS", "CSTM", 15, 20, 3, 55, 520,
                new ClassType[] { AC2, AC3, SL }, new double[] { 1600, 1100, 490 });
        addTrain("12793", "Rayalaseema Express", "RU", "SC", 17, 30, 6, 15, 480,
                new ClassType[] { AC2, AC3, SL }, new double[] { 1200, 850, 380 });

        System.out.println("✅ 116 trains created successfully!");
        System.out.println("==================================================");
        System.out.println("🚊 IRCTC Railway Reservation System Started!");
        System.out.println("🌐 Access at: http://localhost:9090");
        System.out.println("👤 Admin: admin / admin123");
        System.out.println("🎫 TTE (Ticket Collector): tte / tte123");
        System.out.println("🚉 Station Master: stationmaster / sm123");
        System.out.println("🧑 Traveler: traveler / user123");
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

    private void createDemoStaffUser(String username, String password, String email, String phone, com.irctc.model.enums.UserRole role) {
        User user = new User(username, passwordEncoder.encode(password), email, phone, role);
        userRepository.save(user);
        System.out.println("✅ " + role + " created - Username: " + username + ", Password: " + password);
    }
}