package com.orchestral.data.healthkit.web.fhir;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu.composite.QuantityDt;
import ca.uhn.fhir.model.dstu.resource.Observation;
import ca.uhn.fhir.model.dstu.resource.OperationOutcome;
import ca.uhn.fhir.model.dstu.valueset.IssueSeverityEnum;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.orchestral.data.healthkit.web.Constants;
import com.orchestral.data.healthkit.web.ObservationProcessorVO;
import com.orchestral.data.healthkit.web.data.*;



/**
 * Factory object to return a populated instance of a POJO for persistance to Data Platform...
 */
public class FhirFactory {


    //todo refactor all params to a VO
    public BaseMeasurement getPojo(ObservationProcessorVO vo)
            throws BaseServerResponseException {


        //would like to use a Switch, but doesn't seem to work for String...
        if (vo.code.equals(Constants.BloodGlucoseCode)) {
            BloodGlucose bloodGlucose = new BloodGlucose(vo.startDate,vo.value,vo.units, UUID.randomUUID().toString());
            return bloodGlucose;
        } else if (vo.code.equals(Constants.WeightCode)) {
            Weight weight = new Weight(vo.startDate,vo.value,vo.units, UUID.randomUUID().toString());
            return weight;
        } else if (vo.code.equals(Constants.StepCountCode)) {
            StepCount stepCount = new StepCount(vo.startDate,vo.endDate,(int) Math.round(vo.value),UUID.randomUUID().toString());
            return stepCount;
        } else if (vo.code.equals(Constants.HeartRateCode)) {
            HeartRate heartRate = new HeartRate(vo.startDate,(int) Math.round(vo.value),UUID.randomUUID().toString());
            return heartRate;
        } else if (vo.code.equals(Constants.BloodPressureCode)) {
            BloodPressure bloodPressure = this.getBloodPressure(vo);
            return bloodPressure;
        } else if (vo.code.equals(Constants.SystolicBloodPressureCode) || vo.code.equals(Constants.DiastolicBloodPressureCode)) {
            //the systolic & diastolic observations aren't converted to a POJO - they are part of the BP...
            return null;
        } else {
            OperationOutcome operationOutcome = ProcessBundle.getOperationOutcome(IssueSeverityEnum.FATAL,
                    "There was an Observation with an unknown code: " + vo.code);
            throw new UnprocessableEntityException(operationOutcome);
            //return null;
        }
    }

    //get the blood pressure pojo. This requires finding the referenced systolic & diastolic observations,,,
    //Quite a lot of this validation logic will eventually wind up in a generic validator...
    private BloodPressure getBloodPressure(ObservationProcessorVO vo) throws UnprocessableEntityException{

        Observation systolicObservation = null;
        Observation diastolicObservation = null;

        List<Observation.Related> lstRelated =  vo.currentObservation.getRelated();    //the related observations

        //iterate through the collection of related observations. There should be only 2...
        if (lstRelated.size() != 2) {
            OperationOutcome operationOutcome = ProcessBundle.getOperationOutcome(IssueSeverityEnum.FATAL,
                    "There was a Blood Pressure Observation with "+lstRelated.size() + " related items. There should be 2.");
            throw new UnprocessableEntityException(operationOutcome);
        }

        for (Observation.Related related : lstRelated) {
            String id = related.getTarget().getReference().getValueAsString();
            if (! vo.mapIDs.containsKey(id)){
                OperationOutcome operationOutcome = ProcessBundle.getOperationOutcome(IssueSeverityEnum.FATAL,
                        "There was a Blood Pressure Observation that references a non-existent resource: "+id);
                throw new UnprocessableEntityException(operationOutcome);
            }

            IResource resource = vo.mapIDs.get(id);
            Observation observation = null;
            if (! (resource instanceof Observation)) {
                String message = "There was a Blood Pressure Observation that referenced a resource that was not an Observation. " +
                        "It was a "+resource.getClass().getSimpleName() + ".";// .getName();
                OperationOutcome operationOutcome = ProcessBundle.getOperationOutcome(IssueSeverityEnum.FATAL,message);
                throw new UnprocessableEntityException(operationOutcome);
            } else {
                observation = (Observation) vo.mapIDs.get(id);
            }
            String code = observation.getName().getCodingFirstRep().getCode().getValueAsString();

            if (code.equals(Constants.SystolicBloodPressureCode)) {
                systolicObservation = observation;
            } else if (code.equals(Constants.DiastolicBloodPressureCode)) {
                diastolicObservation = observation;
            } else {
                String message="There was a Blood Pressure that referred to an Observation that was neither a systolic nor a diastolic observation";
                OperationOutcome operationOutcome = ProcessBundle.getOperationOutcome(IssueSeverityEnum.FATAL,message);
                throw new UnprocessableEntityException(operationOutcome);
            }
        }

        if (systolicObservation == null || diastolicObservation == null) {
            String message="There was a Blood Pressure that is missing either a systolic or diastolic observation";
            OperationOutcome operationOutcome = ProcessBundle.getOperationOutcome(IssueSeverityEnum.FATAL,message);
            throw new UnprocessableEntityException(operationOutcome);
        }


        //whew! now we have everything we need to create the Blood Pressure pojo.
        BloodPressure bp = new BloodPressure();
        bp.setTime(vo.startDate);   //use the time of the parent resource...
        bp.setId(UUID.randomUUID().toString());

        //QuantityDt qty1 = ((QuantityDt) systolicObservation.getValue());
        bp.setSystolicValueInMmHg(((QuantityDt) systolicObservation.getValue()).getValue().getValueAsNumber().intValue());
        bp.setDiastolicValueInMmHg(((QuantityDt) diastolicObservation.getValue()).getValue().getValueAsNumber().intValue());

        return bp;
    }

}

