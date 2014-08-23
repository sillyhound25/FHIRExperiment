package fhir.server;

import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.model.api.BundleEntry;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu.resource.Profile;

import java.util.HashMap;
import java.util.Map;

/**
 * validate that a given bundle conforms to a profile
 */
public class Validator {

    private boolean _result;
    private Bundle _bundle;
    private Profile _profile;
    private HashMap<String,Integer> _resourcesInBundle;

    public boolean validateBundle(Bundle bundle,Profile profile) {
        _bundle = bundle;
        _profile = profile;
        _resourcesInBundle = new HashMap<String, Integer>();
        _result = true;     //the outcome of validation...

        return true;

       // this.checkResource();
       // return _result;
    }

    //checks that required resource are present, and banned ones are not...
    private boolean checkResource(){
        boolean rtn = false;
        //create a map of the resources in this bundle - how many of each type...
        _resourcesInBundle.clear();
        for (BundleEntry entry : _bundle.getEntries()) {
            IResource resource = entry.getResource();
            String resourceType = resource.getClass().getCanonicalName();
            if (_resourcesInBundle.containsKey(resourceType)) {
                Integer cnt = (Integer)_resourcesInBundle.get(resourceType);
                cnt++;
                _resourcesInBundle.put(resourceType,cnt);
            } else {
                _resourcesInBundle.put(resourceType,1);
            }
        }

        //now, lets's see if we have what we need...
        for (Profile.Structure structure : _profile.getStructure()) {
            Profile.StructureElement element = structure.getElement().get(0);
            String path = element.getPath().toString();
            Profile.StructureElementDefinition definition = element.getDefinition();
            Integer min = definition.getMin().getValue();
            String max = definition.getMax().getValue();
            if (min > 0) {

                //this is a required resource. There must be at least one...

            }

        }

        return rtn;
    }
}
