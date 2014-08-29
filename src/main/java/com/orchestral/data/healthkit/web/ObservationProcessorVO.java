package com.orchestral.data.healthkit.web;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu.resource.Observation;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * A value object used to pass values from an observation into the Factory that returns a POJO...
 */
public class ObservationProcessorVO {
    public String code;
    public Date startDate;
    public Date endDate;
    public Float value;         //value will always be a number...
    public String units;
    public List<Observation> lstObservations;
    public Map<String,IResource> mapIDs;
}
