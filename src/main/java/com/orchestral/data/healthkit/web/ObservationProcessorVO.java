package com.orchestral.data.healthkit.web;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu.resource.Observation;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A value object used to pass values from an observation into the Factory that returns a POJO...
 */
public class ObservationProcessorVO {
    public String deviceName;   //the name of the device that was used. Might want to have more data
    public String code;         //the observation code
    public Date startDate;      //the date. either the appliesDateTime or appliesPeriod.start
    public Date endDate;        //appliedPeriod.end
    public Float value;         //value will always be a number in this profile. valueQuantity
    public String units;        //the units of the value
    public List<Observation> lstObservations;   //a list of all observation resources in the bundle
    public Observation currentObservation;      //the current observation that is being processed...
    public Map<String,IResource> mapIDs;        //a map of all the ID's of all resources.
    public Set<String> BPObservations;      //a set of all Systolic and Diastolic observations references by a parent. Used to detect 'orphan' Observations (ie not connected to a parent)
}
