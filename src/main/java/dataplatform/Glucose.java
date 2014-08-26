package dataplatform;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu.composite.QuantityDt;
import ca.uhn.fhir.model.dstu.resource.Device;
import ca.uhn.fhir.model.dstu.resource.Observation;
import ca.uhn.fhir.model.primitive.DateTimeDt;

import java.util.Date;

/**
 * A POJO to represent a glucose observation inserted into a
 */
public class Glucose implements IPojo {
    private Date date;
    private Float value;
    private String deviceName;      //we'll just have the device name for now...
    private Device _deviceResource;     //the device resource. we need the name from that....

    public void setDevice(Device device) {
        _deviceResource = device;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    //populate the pojo properties...
    @Override
    public void populate(IResource resource) {
        //again - pretty sure that the HAPI library means we don't get exceptions, but should check...

        Observation observation = (Observation) resource;   //we know that this is an observation...
        this.date = ((DateTimeDt) observation.getApplies()).getValue();
        QuantityDt qty = (QuantityDt) observation.getValue();
        this.value = qty.getValue().getValueAsNumber().floatValue();
        //test this with an absent type property and all OK...
        this.deviceName = this._deviceResource.getType().getCodingFirstRep().getDisplay().toString();
    }

    @Override
    //return the modelname that this pojo should be persisted to...
    public String getModelName() {
        return "Glucose";
    }



    public Date getDate() {
        return date;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }


}
