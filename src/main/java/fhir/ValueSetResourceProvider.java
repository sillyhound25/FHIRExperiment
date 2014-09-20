package fhir;

/**
 * The Patient provider
 */

import ca.uhn.fhir.model.dstu.resource.*;


import ca.uhn.fhir.model.primitive.IdDt;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import profile.FileResourceProvider;
import profile.IResourceProvider;
import util.MyMongo;


import javax.servlet.http.HttpServletRequest;


/**
 * The provider for Observation Resources...
 */
public class ValueSetResourceProvider implements ca.uhn.fhir.rest.server.IResourceProvider {
    //return a single Patient by ID
    @Read()
    public ValueSet getResourceById(@IdParam IdDt theId,HttpServletRequest theRequest) {

        //assume resources are saved on the file system...
        IResourceProvider resourceProvider = new FileResourceProvider();
        System.out.println(theId.getValue());
        return (ValueSet) resourceProvider.getResource(theRequest, theId);

    }

    private MyMongo _myMongo;

    //constructor to pass instance of MongoClient across - actually, could this be in the Context???
    public ValueSetResourceProvider(MyMongo myMongo){
        _myMongo = myMongo;
    }

    @Update
    public MethodOutcome updateProfile(@IdParam IdDt theId, @ResourceParam Profile theProfile) {

        theProfile.setId(theId);

        _myMongo.saveResource(theProfile);

        MethodOutcome retVal = new MethodOutcome();
        retVal.setId(theId);
        //OperationOutcome outcome = new OperationOutcome();
        ////outcome.a
        // outcome.addIssue().setDetails("No problems");
        //retVal.setOperationOutcome(outcome);

        return retVal;
    }

    @Override
    public Class<ValueSet
            > getResourceType() {
        return ValueSet.class;
    }


}

