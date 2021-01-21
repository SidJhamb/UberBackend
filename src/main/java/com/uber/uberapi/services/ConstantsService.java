package com.uber.uberapi.services;

import com.uber.uberapi.repositories.DBConstantsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ConstantsService {
    final DBConstantsRepository dbConstantsRepository;

    private final Map<String, String> constants = new HashMap<>();
    private static final Integer TEN_MINUTES = 60 * 10 * 1000;

    public ConstantsService(DBConstantsRepository dbConstantsRepository) {
        this.dbConstantsRepository = dbConstantsRepository;
        loadConstantsFromDB();
    }

    // loading the in memory cache
    @Scheduled(fixedRate = TEN_MINUTES)
    private void loadConstantsFromDB() {
        dbConstantsRepository.findAll().forEach(dbConstant -> {
            constants.put(dbConstant.getName(), dbConstant.getValue());
        });
    }

    public Integer getRideStartOTPExpiryMinutes(){
        return Integer.parseInt(constants.getOrDefault("rideStartOTPExpiryMinutes", TEN_MINUTES.toString()));
    }


    public String getSchedulingTopicName() {
        return constants.getOrDefault("schedulingTopicName", "schedulingServiceTopic");
    }

    public String getDriverMatchingTopicName() {
        return constants.getOrDefault("driverMatchingTopicName", "driverMatchingTopic");
    }

    public String getLocationTrackingTopicName() {
        return constants.getOrDefault("locationTrackingTopicName", "locationTrackingTopic");
    }

    public double getMaxDistanceForDriverMatching() {
        return Double.parseDouble(constants.getOrDefault("maxDistanceKmForDriverMatching", "2"));

    }

    public Integer getMaxDriverETAMinutes() {
        return Integer.parseInt(constants.getOrDefault("maxDriverETAMinutes", "10"));
    }

    public boolean getIsETABasedFilterEnabled() {
        return Boolean.parseBoolean(constants.getOrDefault("isETABasedFilterEnabled", "true"));
    }

    public boolean getIsGenderFilterEnabled() {
        return Boolean.parseBoolean(constants.getOrDefault("isGenderFilterEnabled", "true"));
    }

    public double getDefaultETASpeedKmph() {
        return Double.parseDouble(constants.getOrDefault("defaultETASpeedKmph", "30.0"));
    }
}
