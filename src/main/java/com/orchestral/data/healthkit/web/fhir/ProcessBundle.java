package com.orchestral.data.healthkit.web.fhir;

import java.util.*;

import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.model.api.BundleEntry;
import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu.composite.PeriodDt;
import ca.uhn.fhir.model.dstu.composite.QuantityDt;
import ca.uhn.fhir.model.dstu.resource.Device;
import ca.uhn.fhir.model.dstu.resource.Observation;
import ca.uhn.fhir.model.dstu.resource.OperationOutcome;
import ca.uhn.fhir.model.dstu.resource.Patient;
import ca.uhn.fhir.model.dstu.valueset.IssueSeverityEnum;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.orchestral.data.healthkit.web.Constants;
import com.orchestral.data.healthkit.web.ObservationProcessorVO;
import com.orchestral.data.healthkit.web.data.BaseMeasurement;
import com.orchestral.data.healthkit.web.data.BloodGlucose;
import dataplatform.IPojo;



/**
 * Created by davidhay on 29/08/14.
 */
public class ProcessBundle {

    //todo - may need to add the current patient identity (from the URL) into the call
    //so that the patient resource in the bundle can be compared...

    //had to add 'throws Exception' so final throw would compile...
    public static List<BaseMeasurement> process(final Bundle bundle) throws Exception,BaseServerResponseException{
        Patient patient = null;     //this will be the patient resource. There should only be one....
        Device device = null;       //this will be the device resource. There should only be one....
        String deviceName = "Unknown Device";   //this will be the name of the device...

        Set<String> BPObservations = new HashSet<String>();     //the systolic and diastolic observations...

       // patient.

        Map<String,IResource> mapIDs = new HashMap<String, IResource>();   //a map of all ID's

        final List<Observation> lstObservations = new ArrayList<Observation>();

        //generate a list of the POJO's to return...
        final List<BaseMeasurement> thePojos = new ArrayList<BaseMeasurement>();    //list of resources in bundle

        //first, locate the patient and device resources - we can;t assume they are first in the bundle...
        //at the same time, generate a list of resource ID's in the bundle. This is needed by Blood Pressure, and also
        //serves as part of the overall bundle audit - all resources must have an ID, and they must all be unique
        // (actually the uniqueness constraint is just for this implementation)
        for (BundleEntry entry : bundle.getEntries()) {
            final IResource resource = entry.getResource();
            String className = resource.getClass().getName();
            String ID = resource.getId().getValueAsString();
            if (ID == null) {
                throw new UnprocessableEntityException("There was a resource of type " + className + " missing an ID");
            }

            //no duplicates in this implementation...
            if (mapIDs.containsKey(ID)) {
                throw new UnprocessableEntityException("There is more than one resource with the ID '"+ID + "'");
            }
            mapIDs.put(ID,resource);

            if (resource instanceof Patient) {
                //need to check that the contents of the bundle match the patients whose idetifier is in the url...
                patient = (Patient) resource;
                IdentifierDt identifierDt = patient.getIdentifierFirstRep();

                //todo >>>>>>>>  here (or somewhere better) is where to check the patient identity
                if (1==2){
                    throw new UnprocessableEntityException("The identity of the patient resource in the bundle is not" +
                            " the same as that in the URL");
                }



            } else if (resource instanceof Device) {
                device = (Device) resource;
                deviceName = device.getType().getCodingFirstRep().getDisplay().toString();
            } else if (resource instanceof Observation) {
                lstObservations.add((Observation) resource);

                //if this is a Blood Pressure, then get the Systolic and Diastolic Observations that it refers to.
                //we'll use this to detect 'orphan' resoruces - that are not attached to a BP. We just need to add
                //them to the list - the routine that creates teh BloodPressure object will validate themmmm
                Observation observation = (Observation) resource;
                try {
                    if (observation.getName().getCodingFirstRep().getCode().getValueAsString().equals(Constants.BloodPressureCode)) {
                        List<Observation.Related> lstRelated = observation.getRelated();
                        for (Observation.Related related : lstRelated) {
                            String id = related.getTarget().getReference().getValueAsString();
                            BPObservations.add(id);
                        }
                    }
                } catch (Exception ex) {
                    String message = "There was an error collecting the 'child' observations of a Blood Pressure: " + ex.getMessage();
                    throw new UnprocessableEntityException(message);
                }

            } else {
                //this is an unknown resource.
                OperationOutcome operationOutcome = ProcessBundle.getOperationOutcome(IssueSeverityEnum.FATAL,
                        "There was an unknown Resource ("+className+") in the Bundle. Only Patient, Device and Observation are allowed");
                throw new UnprocessableEntityException(operationOutcome);
            }
        }

        //now that we have the device resource, we can properly process the bundle. What we'll do is to iterate
        //through the entries, pulling out the Observations and generating POJO's from them.
        for (Observation observation : lstObservations) {
            //use a ValueObject to pass the extracted values to.
            ObservationProcessorVO vo = new ObservationProcessorVO();
            vo.lstObservations = lstObservations;
            vo.mapIDs = mapIDs;
            vo.currentObservation = observation;
            vo.deviceName = deviceName;
            vo.BPObservations = BPObservations;         //all of the observations related to a BP Observation
            //Extract the key properties from the Observation.
            // I'm pretty sure that HAPI creates child elements as we go so shouldn't throw an exception
            // - but will check with the try/catch anyway...
            try {
                vo.code = observation.getName().getCodingFirstRep().getCode().getValueAsString();
                QuantityDt qty = (QuantityDt) observation.getValue();
                if (qty == null) {
                    OperationOutcome operationOutcome = ProcessBundle.getOperationOutcome(IssueSeverityEnum.FATAL,
                            "There is an Observation with no valueQuantity element");
                    throw new UnprocessableEntityException(operationOutcome);
                }
                vo.value = qty.getValue().getValueAsNumber().floatValue();
                vo.units = qty.getUnits().getValue();

                //if the 'applies' is a Period then set start date and end date. Otherwise, just set start date
                final IDatatype dt = observation.getApplies();
                if (dt == null) {
                    OperationOutcome operationOutcome = ProcessBundle.getOperationOutcome(IssueSeverityEnum.FATAL,
                            "There is an Observation with no date[x] element");
                    throw new UnprocessableEntityException(operationOutcome);
                }

                if (dt instanceof PeriodDt) {
                    //this is a period
                    PeriodDt periodDt = (PeriodDt) observation.getApplies();
                    vo.startDate = periodDt.getStart().getValue();
                    vo.endDate = periodDt.getEnd().getValue();

                } else {
                    //this is a dateTime
                    vo.startDate = ((DateTimeDt) observation.getApplies()).getValue();
                }

                //Note that this method will throw an exception if there is a code it doesn't recognize.
                //(and the try/catch will surface that)
                //But, there still may not be a pojo - for example, Blood Pressure is actually 3 observations -
                //a 'parent' obs, and then the systolic & diastolic. The first will return a pojo, the others won't.
                FhirFactory fhirFactory = new FhirFactory();
                final BaseMeasurement pojo = fhirFactory.getPojo(vo);


                //if pojo is null, then that measn there was a code that didn;t result in a pojo
                //like systolic BP for example...  ie it's not an error...
                if (pojo != null) {
                    thePojos.add(pojo);
                }

            } catch (final Exception exception) {
                throw exception;
            }
        }
        return thePojos;
    }

    /*
    Generate an OperationOutcome resource. Also used by the FhirFactory class (?is this a good pattern???)
     */
public static OperationOutcome getOperationOutcome(IssueSeverityEnum severity,String message) {
    final OperationOutcome operationOutcome = new OperationOutcome();
    final OperationOutcome.Issue issue = operationOutcome.addIssue();
    issue.setSeverity(severity);
    issue.setDetails(message);
    return  operationOutcome;
}


}
