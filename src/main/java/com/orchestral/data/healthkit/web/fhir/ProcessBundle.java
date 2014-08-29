package com.orchestral.data.healthkit.web.fhir;

import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.model.api.BundleEntry;
import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu.composite.PeriodDt;
import ca.uhn.fhir.model.dstu.composite.QuantityDt;
import ca.uhn.fhir.model.dstu.resource.Device;
import ca.uhn.fhir.model.dstu.resource.Observation;
import ca.uhn.fhir.model.dstu.resource.Patient;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import com.orchestral.data.healthkit.web.data.BloodGlucose;
import com.orchestral.data.healthkit.web.data.IDapPojo;
import dataplatform.IPojo;

import java.util.*;

/**
 * Created by davidhay on 29/08/14.
 */
public class ProcessBundle {


    public static List<IDapPojo> process(Bundle bundle) throws Exception{
        Patient patient = null;     //this will be the patient resource. There should only be one....
        Device device = null;       //this will be the device resource. There should only be one....
        String deviceName = "Unknown Device";   //this will be the name of the device...

        Map<String,IResource> lstID = new HashMap<String, IResource>();   //a map of all ID's

        List<Observation> lstObservations = new ArrayList<Observation>();

        //generate a list of the POJO's to return...
        List<IDapPojo> thePojos = new ArrayList<IDapPojo>();    //list of resources in bundle

        //first, locate the patient and device resources - we can;t assume they are first in the bundle...
        //at the same time, generate a list of resource ID's in the bundle. This is needed by Blood Pressure, and alse
        //serves as part of the overall bundle audit - all resources must have an ID, and they must all be unique
        // (actually the uniqueness constraint is just for this implementation)
        for (BundleEntry entry : bundle.getEntries()) {
            IResource resource = entry.getResource();
            String className = resource.getClass().getName();
            String ID = resource.getId().getValueAsString();
            if (ID == null) {
                throw new Exception("There was a resource of type " + className + " missing an ID");
            }

            //no duplicates in this implementation...
            if (lstID.containsKey(ID)) {
                throw new Exception("There is more than one resource with the ID '"+ID + "'");
            }
            lstID.put(ID,resource);

            if (resource instanceof Patient) {
                //don't really need to capture the patient actually, but can't bring myself not too...
                patient = (Patient) resource;

            } else if (resource instanceof Device) {
                device = (Device) resource;
                deviceName = device.getType().getCodingFirstRep().getDisplay().toString();
            } else if (resource instanceof Observation) {
                lstObservations.add((Observation) resource);
            } else {
                //this is an unknown resource.
                throw new Exception("There was an unknown Resource ("+className+") in the Bundle. Only Patient, Device and Observation are allowed");
            }
        }



        //now that we have the device resource, we can properly process the bundle. What we'll do is to iterate
        //through the entries, pulling out the Observations and generating POJO's from them.
        for (Observation observation : lstObservations) {
            //I'm pretty sure that HAPI creates child elements as we go so shouldn't throw an exception - but should check...
            String code = null;
            Date startDate = null;
            Date endDate = null;
            Float value = null;         //value will always be a number...
            String units = null;
            try {
                code = observation.getName().getCodingFirstRep().getCode().getValueAsString();
                QuantityDt qty = (QuantityDt) observation.getValue();
                value = qty.getValue().getValueAsNumber().floatValue();
                units = qty.getUnits().getValue();

                //if the 'applies' is a Period then set start date and end date. Otherwise, just set start date
                IDatatype dt = observation.getApplies();
                if (dt instanceof PeriodDt) {
                    //this is a period
                    PeriodDt periodDt = (PeriodDt) observation.getApplies();
                    startDate = periodDt.getStart().getValue();
                    endDate = periodDt.getEnd().getValue();

                } else {
                    //this is a dateTime
                    startDate = ((DateTimeDt) observation.getApplies()).getValue();
                }

                //get the pojo
                IDapPojo pojo = FhirFactory.getPojo(code,startDate,endDate,value,units);
                if (pojo != null) {
                    thePojos.add(pojo);
                } else {
                    System.out.println("Unknown code: "+code);
                }

            } catch (Exception exception) {
                throw exception;
            }
        }
        return thePojos;
    }


}