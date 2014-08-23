package fhir;

/**
 * The Patient provider
 */

import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu.composite.QuantityDt;
import ca.uhn.fhir.model.dstu.resource.*;


import ca.uhn.fhir.model.dstu.valueset.*;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.StringDt;

import ca.uhn.fhir.model.primitive.UriDt;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.mongodb.MongoClient;
import fhir.model.MyPatient;
import util.MyMongo;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;


import javax.json.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * The provider for Observation Resources...
 */
public class ProfileResourceProvider implements IResourceProvider {
    //return a single Patient by ID
    @Read()
    public Profile getResourceById(@IdParam IdDt theId) {



        return (Profile) _myMongo.getResource(theId);

       // return null;
    }

    private MyMongo _myMongo;

    //constructor to pass instance of MongoClient across - actually, could this be in the Context???
    public ProfileResourceProvider(MyMongo myMongo){
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
    public Class<Profile> getResourceType() {
        return Profile.class;
    }










}

