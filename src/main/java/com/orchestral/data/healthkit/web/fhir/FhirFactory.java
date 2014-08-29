package com.orchestral.data.healthkit.web.fhir;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu.resource.Observation;
import com.orchestral.data.healthkit.web.Constants;
import com.orchestral.data.healthkit.web.ObservationProcessorVO;
import com.orchestral.data.healthkit.web.data.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Factory object to return a populated instance of a POJO for persistance to Data Platform...
 */
public class FhirFactory {


    //todo refactor all params to a VO
    public static IDapPojo getPojo(ObservationProcessorVO vo)
    //public static IDapPojo getPojo(String code, Date startDate, Date endDate, Float value,
      //                             String units,List<Observation> lstResources, Map<String,IResource> mapIDs)
            throws Exception{


        //would like to use a Switch, but doesn't seem to work for String...
        if (vo.code.equals(Constants.BloodGlucoseCode)) {
            BloodGlucose bloodGlucose = new BloodGlucose(vo.startDate,vo.value,vo.units, UUID.randomUUID().toString());
            return bloodGlucose;
        } else if (vo.code.equals(Constants.WeightCode)) {
            Weight weight = new Weight(vo.startDate,vo.value,vo.units, UUID.randomUUID().toString());
            return weight;
        } else if (vo.code.equals(Constants.HeartRateCode)) {
            HeartRate heartRate = new HeartRate(vo.startDate,(int) Math.round(vo.value),UUID.randomUUID().toString());
            return heartRate;
        } else if (vo.code.equals(Constants.BloodPressureCode)) {
            //BloodPressure bloodPressure = new BloodPressure(startDate,)
            return null;
        } else if (vo.code.equals(Constants.SystolicBloodPressureCode) || vo.code.equals(Constants.DiastolicBloodPressureCode)) {
            return null;
        } else {
            throw new Exception("There was an Observation with an unknown code: " + vo.code);
            //return null;
        }
    }

    private BloodPressure getBloodPressure(ObservationProcessorVO vo) {
       // BloodPressure bloodPressure = new
        return null;
    }

}

