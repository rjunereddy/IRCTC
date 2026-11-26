// src/main/java/com/irctc/pattern/factory/FareCalculatorFactory.java
package com.irctc.pattern.factory;

import com.irctc.model.enums.QuotaType;
import org.springframework.stereotype.Component;

@Component
public class FareCalculatorFactory {
    
    private final GeneralFareCalculator generalCalculator;
    private final TatkalFareCalculator tatkalCalculator;
    private final SeniorCitizenFareCalculator seniorCitizenCalculator;
    
    // Constructor injection (no @Autowired field needed)
    public FareCalculatorFactory(GeneralFareCalculator generalCalculator,
                                  TatkalFareCalculator tatkalCalculator,
                                  SeniorCitizenFareCalculator seniorCitizenCalculator) {
        this.generalCalculator = generalCalculator;
        this.tatkalCalculator = tatkalCalculator;
        this.seniorCitizenCalculator = seniorCitizenCalculator;
    }
    
    public FareCalculator getCalculator(QuotaType quotaType) {
        if (QuotaType.TATKAL.equals(quotaType)) {
            return tatkalCalculator;
        } else if (QuotaType.SENIOR_CITIZEN.equals(quotaType)) {
            return seniorCitizenCalculator;
        } else {
            return generalCalculator;
        }
    }
}