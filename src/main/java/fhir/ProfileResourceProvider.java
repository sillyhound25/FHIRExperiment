package fhir;

/**
 * The Patient provider
 */

import ca.uhn.fhir.model.dstu.resource.*;


import ca.uhn.fhir.model.dstu.valueset.IssueSeverityEnum;
import ca.uhn.fhir.model.primitive.IdDt;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import profile.FileResourceProvider;
import profile.IResourceProvider;
import util.MyMongo;


import javax.servlet.http.HttpServletRequest;


/**
 * The provider for Profile Resources...
 */
public class ProfileResourceProvider implements ca.uhn.fhir.rest.server.IResourceProvider {
    //return a single Patient by ID
    @Read()
    public Profile getResourceById(@IdParam IdDt theId,HttpServletRequest theRequest) {

        //assume profile resources are saved on the file system...
        //later could provide an implementation of the interface that points to an external server...
        IResourceProvider resourceProvider = new FileResourceProvider();
        System.out.println(theId.getValue());
        return (Profile) resourceProvider.getResource(theRequest, theId);

    }

    private MyMongo _myMongo;

    //constructor to pass instance of MongoClient across - actually, could this be in the Context???
    public ProfileResourceProvider(MyMongo myMongo){
        _myMongo = myMongo;
    }



    @Update
    public MethodOutcome updateProfile(@IdParam IdDt theId, @ResourceParam Profile theProfile,HttpServletRequest theRequest) {


        IResourceProvider resourceProvider = new FileResourceProvider();
        System.out.println(theId.getValue());
        return resourceProvider.putResource(theRequest,theProfile,theId);



    }

    @Override
    public Class<Profile> getResourceType() {
        return Profile.class;
    }


}

