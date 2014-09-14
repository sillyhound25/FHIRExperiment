package fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.model.api.BundleEntry;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu.resource.ListResource;
import ca.uhn.fhir.model.dstu.resource.OperationOutcome;
import ca.uhn.fhir.model.dstu.resource.Patient;


import ca.uhn.fhir.model.dstu.resource.Practitioner;
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
public class PractitionerResourceProvider implements IResourceProvider {


    private MyMongo _myMongo;
    private FhirContext _ctx;

    private String _serverBase = "http://fhir.healthintersections.com.au/open";

    //constructor to pass instance of MongoClient across - actually, could this be in the Context???
    public PractitionerResourceProvider( MyMongo myMongo){
        _myMongo = myMongo; //not currently using this, but nice to have a local Db around...
    }

    @Override
    public Class<Practitioner> getResourceType() {
        return Practitioner.class;
    }

    //return a single patient by Id
    @Read()
    public Practitioner getResourceById(@IdParam IdDt theId, HttpServletRequest theRequest, HttpServletResponse theResponse) {


        Practitioner practitioner = null;
        if (theId.toString().equals("Practitioner/dummy")) {
            practitioner = new Practitioner();
            //practitioner.setBirthDate(new DateTimeDt(cal.getTime()));
            practitioner.getName().addFamily("Jones");
            practitioner.getName().addGiven("Dale");
            practitioner.getText().setDiv("Dr Dale Jones");

            /*
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
            */
        } else {
            //this will read the patient from the server

            //the FHIR context is saved against the servlet context
            ServletContext context = theRequest.getServletContext();// getServletContext();
            _ctx = (FhirContext) context.getAttribute("fhircontext");
            //String serverBase = "http://fhir.healthintersections.com.au/open";
            IGenericClient client = _ctx.newRestfulGenericClient(_serverBase);
            IResource resource = client.read(Practitioner.class,theId);
            practitioner = (Practitioner) resource;
            //return (Patient) resource;
        }

        return practitioner;
    }


    //find patients based on name
    @Search()
    public List<IResource> getPractitionerByName(@RequiredParam(name = Practitioner.SP_NAME) StringDt theName,
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
                .forResource(Practitioner.class)
                .where(Practitioner.NAME.matches().value(theName))
                .execute();

        //need to return a List<IResource>
        for (BundleEntry entry : bundle.getEntries()) {
            lst.add(entry.getResource());
        }
        return lst;
    }



}

