package com.orchestral.data.healthkit.web.fhir;

import com.orchestral.data.healthkit.web.data.BloodGlucose;
import com.orchestral.data.healthkit.web.data.HeartRate;
import com.orchestral.data.healthkit.web.data.IDapPojo;
import com.orchestral.data.healthkit.web.data.Weight;

import java.util.Date;
import java.util.UUID;

/**
 * Factory object to return a populated instance of a POJO for persistance to Data Platform...
 */
public class FhirFactory {

    public static IDapPojo getPojo(String code, Date startDate, Date endDate, Float value, String units) {
        //will move the magic numbers to constants...
        if (code.equals("43151-0")) {
            BloodGlucose bloodGlucose = new BloodGlucose(startDate,value,units, UUID.randomUUID().toString());
            return bloodGlucose;
        } else if (code.equals("3141-9")) {
            Weight weight = new Weight(startDate,value,units, UUID.randomUUID().toString());
            return weight;
        } else if (code.equals("8867-4")) {
            HeartRate heartRate = new HeartRate(startDate,(int) Math.round(value),UUID.randomUUID().toString());
            return heartRate;
        }
        return null;
    }

}

