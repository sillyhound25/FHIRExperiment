package fhir;

/**
 * The Patient provider
 */

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
    //return a single Patient by ID



    private MyMongo _myMongo;
    private FhirContext _ctx;

    //constructor to pass instance of MongoClient across - actually, could this be in the Context???
    public PatientResourceProvider( MyMongo myMongo){
        _myMongo = myMongo;
       // _ctx = ctx;
     //   _ctx = new FhirContext(ListResource.class);
    }



    public void init(ServletConfig config) throws ServletException {
        //get the 'global' resources from the servlet context
       // ServletContext ctx = config.getServletContext();
        // _myMongo = (MyMongo) ctx.getAttribute("mymongo");
       // _ctx = (FhirContext) ctx.getAttribute("fhircontext");
    }

    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }

    // read a single resource by id
    //http://www.mkyong.com/java/how-to-modify-date-time-date-manipulation-java/



    @Read()
    public Patient getResourceById(@IdParam IdDt theId,
                                   HttpServletRequest theRequest,
                                   HttpServletResponse theResponse) {

        System.out.println(theId.getValueAsString());

        Patient patient = null;
        if (theId.toString().equals("Patient/dummy")) {
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
            ServletContext context = theRequest.getServletContext();// getServletContext();
            _ctx = (FhirContext) context.getAttribute("fhircontext");
            String serverBase = "http://fhir.healthintersections.com.au/open";
            IGenericClient client = _ctx.newRestfulGenericClient(serverBase);

            IResource resource = client.read(Patient.class,theId);


            return (Patient) resource;



        }



        return patient;
    }


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

        //get a patients Patients
    @Search()
    //public List<IResource> getPatientBySubject(@RequiredParam(name = Patient.SP_NAME) StringDt theName,
    public List<IResource> getPatientBySubject(@RequiredParam(name = Patient.SP_NAME) StringDt theName,
                                                 HttpServletRequest theRequest,
                                                 HttpServletResponse theResponse) {
        List<IResource> lst = new ArrayList<IResource>();

         ServletContext context = theRequest.getServletContext();// getServletContext();
        _ctx = (FhirContext) context.getAttribute("fhircontext");



        System.out.println(_ctx);

        String serverBase = "http://fhir.healthintersections.com.au/open";
        IGenericClient client = _ctx.newRestfulGenericClient(serverBase);


        Bundle bundle = client.search()
                .forResource(Patient.class)
                .where(Patient.NAME.matches().value(theName))
                .execute();


        for (BundleEntry entry : bundle.getEntries()) {
            lst.add(entry.getResource());
        }


        System.out.println("Search Patient");

       // if (_myMongo != null) {
         //   _myMongo.addFhirResourceListToLog(lst);
       // }

        //return lst;

        return lst;

        //return bundle;

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

