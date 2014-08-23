package fhir;

/**
 * The Patient provider
 */

import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu.composite.QuantityDt;
import ca.uhn.fhir.model.dstu.resource.FamilyHistory;
import ca.uhn.fhir.model.dstu.resource.OperationOutcome;
import ca.uhn.fhir.model.dstu.resource.Patient;
import ca.uhn.fhir.model.dstu.resource.Observation;


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
public class FamilyHistoryResourceProvider implements IResourceProvider {
    //return a single Patient by ID
    @Read()
    public FamilyHistory getResourceById(@IdParam IdDt theId) {

        System.out.println("Get patient "+ theId.toString());
        Patient patient = new Patient();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -18);         //18 years old
        patient.setBirthDate(new DateTimeDt(cal.getTime()));
        patient.addName().addFamily("Power");
        patient.getName().get(0).addGiven("Cold");
        patient.setGender(AdministrativeGenderCodesEnum.M);

        return null;
    }

    private MyMongo _myMongo;

    //constructor to pass instance of MongoClient across - actually, could this be in the Context???
    public FamilyHistoryResourceProvider(MyMongo myMongo){
        _myMongo = myMongo;
    }

    @Override
    public Class<FamilyHistory> getResourceType() {
        return FamilyHistory.class;
    }

    // read a single resource by id
    //http://www.mkyong.com/java/how-to-modify-date-time-date-manipulation-java/




    //get a patients Patients
    @Search()
    public List<IResource> getFamilyHistoryBySubject(@RequiredParam(name = FamilyHistory.SP_SUBJECT) StringDt theName,
                                               HttpServletRequest theRequest,
                                               HttpServletResponse theResponse) {
        List<IResource> lst = new ArrayList<IResource>();

        if (_myMongo != null) {
            _myMongo.addFhirResourceListToLog(lst);
        }

        return lst;

    }





}

