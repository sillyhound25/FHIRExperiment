package cc;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.model.api.BundleEntry;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu.resource.AllergyIntolerance;
import ca.uhn.fhir.model.dstu.resource.MedicationStatement;
import ca.uhn.fhir.model.dstu.resource.Observation;
import ca.uhn.fhir.model.dstu.resource.Patient;
import ca.uhn.fhir.model.dstu.valueset.AdministrativeGenderCodesEnum;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.rest.client.IGenericClient;
import util.MyMongo;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * Created by davidhay on 16/09/14.
 */
@WebServlet(urlPatterns= {"/cc/pathx"}, displayName="Get Patient History")
public class GetPatientHistoryServlet extends HttpServlet {

    private MyMongo _myMongo;
    private FhirContext _fhirContext;
    private String _serverBase = "http://fhir-dev.healthintersections.com.au/open";

    @Override
    public void init(ServletConfig config) throws ServletException {
        //get the 'global' resources from the servlet context
        ServletContext ctx = config.getServletContext();
        _myMongo = (MyMongo) ctx.getAttribute("mymongo");
        _fhirContext = (FhirContext) ctx.getAttribute("fhircontext");

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        String patientId = request.getParameter("patientid");
        Bundle allResults = new Bundle();
        Map<String,String> map = new HashMap<String, String>();

        IGenericClient client = _fhirContext.newRestfulGenericClient(_serverBase);


        //--------- allergies
        Bundle allergyResults = client.search()
                .forResource(AllergyIntolerance.class)
                .where(AllergyIntolerance.SUBJECT.hasId(patientId))
                .execute();

        addResources(allResults,allergyResults,map);

        //--------- observations
        Bundle obsResults = client.search()
                .forResource(Observation.class)
                .where(Observation.SUBJECT.hasId(patientId))
                .execute();

        addResources(allResults,obsResults,map);

        //--------- medications
        Bundle medicationResults = client.search()
                .forResource(MedicationStatement.class)
                .where(MedicationStatement.PATIENT.hasId(patientId))
                .execute();
        addResources(allResults,medicationResults,map);


        JsonObjectBuilder json = Json.createObjectBuilder();// .build();

        JsonArrayBuilder array = Json.createArrayBuilder();//.build();
        //for (String profile : lstProfile) {
          //  array.add(Json.createObjectBuilder()
            //        .add("name", profile));
        //}


        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            array.add(Json.createObjectBuilder()
                    .add("id", key)
                    //.add("id", "test")
                    .add("value",value));


            //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
        }

        json.add("history",array);

        StringWriter stWriter = new StringWriter();
        JsonWriter jsonWriter = Json.createWriter(stWriter);
        jsonWriter.writeObject(json.build());
        jsonWriter.close();

        //PrintWriter out = response.getWriter();
        response.addHeader("Content-Type","application/json+fhir");

        out.println(stWriter.toString());


        /*
        for (BundleEntry entry : results.getEntries()) {
            BundleEntry newEntry = allResults.addEntry();
            newEntry.setResource(entry.getResource());
           // allResults.ad  addEntry(entry);
        }
        */
        /*
        Patient patient = new Patient();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -18);         //18 years old
        patient.setBirthDate(new DateTimeDt(cal.getTime()));
        patient.addName().addFamily("Power");
        patient.getName().get(0).addGiven("Cold");
        patient.setGender(AdministrativeGenderCodesEnum.M);
        patient.getText().setDiv("Cold Power  M  Age 18y");

        BundleEntry newEntry = allResults.addEntry();
        newEntry.setResource(patient);

        //results.addResource(patient,_fhirContext,_serverBase);

        //String json =  _fhirContext.newJsonParser().

        //for ()


        String jsonB =  _fhirContext.newJsonParser().encodeBundleToString(allResults);

        response.addHeader("Content-Type","application/json+fhir");
        out.println(jsonB);
        */

    }

    private void addResources(Bundle allResults, Bundle newResults,Map<String,String> map) {



        for (BundleEntry entry : newResults.getEntries()) {
            BundleEntry newEntry = allResults.addEntry();
            newEntry.setId(entry.getId());
            newEntry.setResource(entry.getResource());
            newEntry.getSummary().setValue(new ArrayList<XMLEvent>());
            newEntry.getSummary().setValueAsString("this is the summary");
            // allResults.ad  addEntry(entry);

            String jsonB =  _fhirContext.newXmlParser().encodeResourceToString(newEntry.getResource());

            String id =entry.getId().toString();
            id = id.replace(_serverBase,"");

            map.put(id,jsonB);
        }
    }
}
