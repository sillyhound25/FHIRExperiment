package dataplatform;

import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu.resource.Device;

/**
 * Created by davidhay on 26/08/14.
 */
public interface IPojo {
    void setDevice(Device device);  //make the device available if required.
    void populate(IResource resource); //populate based on the
    String getModelName();     //returns the name of the model to persist the object in...

}
