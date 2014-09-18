package fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.model.api.BundleEntry;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu.resource.ListResource;
import ca.uhn.fhir.model.dstu.resource.OperationOutcome;
import ca.uhn.fhir.model.dstu.resource.Patient;


import ca.uhn.fhir.model.dstu.valueset.*;
import ca.uhn.fhir.model.primitive.*;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.server.IResourceProvider;
import util.MyMongo;

import java.util.*;


import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * The provider for Observation Resources...
 */
public class PatientResourceProvider implements IResourceProvider {


    private MyMongo _myMongo;
    private FhirContext _ctx;

    //private String _serverBase = "http://fhir.healthintersections.com.au/open";
    private String _serverBase = "http://fhir-dev.healthintersections.com.au/open";
    //constructor to pass instance of MongoClient across - actually, could this be in the Context???
    public PatientResourceProvider( MyMongo myMongo){
        _myMongo = myMongo; //not currently using this, but nice to have a local Db around...
    }

    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }

    //return a single patient by Id
    @Read()
    public Patient getResourceById(@IdParam IdDt theId, HttpServletRequest theRequest, HttpServletResponse theResponse) {

        Patient patient = null;
        if (theId.toString().equals("Patient/dummy")) {
            //this is a dummy patient for when a server is not available...
            System.out.println("Get patient "+ theId.toString());
            patient = new Patient();
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, -18);         //18 years old
            patient.setBirthDate(new DateTimeDt(cal.getTime()));
            patient.addName().addFamily("Power");
            patient.getName().get(0).addGiven("Cold");
            patient.setGender(AdministrativeGenderCodesEnum.M);
            patient.getText().setDiv("Cold Power  M  Age 18y");
        } else {
            //this will read the patient from the server

            //the FHIR context is saved against the servlet context
            ServletContext context = theRequest.getServletContext();// getServletContext();
            _ctx = (FhirContext) context.getAttribute("fhircontext");
            //String serverBase = "http://fhir.healthintersections.com.au/open";
            IGenericClient client = _ctx.newRestfulGenericClient(_serverBase);
            IResource resource = client.read(Patient.class,theId);
            patient = (Patient) resource;
            //return (Patient) resource;
        }

        return patient;
    }


    //find patients based on name
    @Search()
    public List<IResource> getPatientBySubject(@RequiredParam(name = Patient.SP_NAME) StringDt theName,
                                               HttpServletRequest theRequest,
                                               HttpServletResponse theResponse) {
        List<IResource> lst = new ArrayList<IResource>();
        //get the FHIR context...
        ServletContext context = theRequest.getServletContext();// getServletContext();
        _ctx = (FhirContext) context.getAttribute("fhircontext");

        //System.out.println(_ctx);



        //String serverBase = "http://fhir.healthintersections.com.au/open";
        IGenericClient client = _ctx.newRestfulGenericClient(_serverBase);

        Bundle bundle = client.search()
                .forResource(Patient.class)
                .where(Patient.NAME.matches().value(theName))
                .execute();

        //need to return a List<IResource>
        for (BundleEntry entry : bundle.getEntries()) {
            lst.add(entry.getResource());
        }


        //System.out.println("Search Patient");

        return lst;

        //return bundle;

    }

    //=============  everything below this line not used in the Clinical Connectathon app =============


    @Create
    public MethodOutcome createPatient(@ResourceParam Patient thePatient) {

  /*
   * First we might want to do business validation. The UnprocessableEntityException
   * results in an HTTP 422, which is appropriate for business rule failure
   */
      //  if (thePatient.getIdentifierFirstRep().isEmpty()) {
    /* It is also possible to pass an OperationOutcome resource
     * to the UnprocessableEntityException if you want to return
     * a custom populated OperationOutcome. Otherwise, a simple one
     * is created using the string supplied below.
     */
         //   throw new UnprocessableEntityException("No identifier supplied");
       // }

        // Save this patient to the database...
        //savePatientToDatabase(thePatient);

        // This method returns a MethodOutcome object which contains
        // the ID and Version ID for the newly saved resource
        MethodOutcome retVal = new MethodOutcome();
        retVal.setId(new IdDt("3746"));
        retVal.setVersionId(new IdDt("1"));

        _myMongo.saveResource(thePatient);

        // You can also add an OperationOutcome resource to return
        // This part is optional though:
        OperationOutcome outcome = new OperationOutcome();
        outcome.addIssue().setDetails("One minor issue detected");
        retVal.setOperationOutcome(outcome);

        return retVal;
    }


    //find patients with a given identifier...
    @Search()
    public List<IResource> getPatientByIdentifier(@RequiredParam(name = Patient.SP_IDENTIFIER) StringDt theIdentifier) {
        IdentifierDt identifier= new IdentifierDt(null,theIdentifier.toString());

        return _myMongo.findResourcesByIdentifier("Patient",identifier);
    }


/*

    @Transaction
    public List<IResource> transaction(@TransactionParam List<IResource> theResources) {
        // theResources will contain a complete bundle of all resources to persist
        // in a single transaction
        for (IResource next : theResources) {
            InstantDt deleted = (InstantDt) next.getResourceMetadata().get(ResourceMetadataKeyEnum.DELETED_AT);
            if (deleted != null && deleted.isEmpty() == false) {
                // delete this resource
            } else {
                // create or update this resource
            }
        }


        List<IResource> retVal = theResources;
        for (IResource next : theResources) {

            IdDt newId = new IdDt("Patient", "1", "2");
            next.setId(newId);
        }

        return retVal;
    }
*/


}

