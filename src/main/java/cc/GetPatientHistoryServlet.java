package cc;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.model.api.BundleEntry;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu.resource.*;
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
import java.text.SimpleDateFormat;
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
        Map<String,BundleEntry> map = new HashMap<String, BundleEntry>();

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

        //--------- family history
        Bundle obsFamhx = client.search()
                .forResource(FamilyHistory.class)
                .where(FamilyHistory.SUBJECT.hasId(patientId))
                .execute();

        addResources(allResults,obsFamhx,map);

        //--------- orders
        Bundle obsOrder = client.search()
                .forResource(Order.class)
                .where(Order.SUBJECT.hasId(patientId))
                .execute();

        addResources(allResults,obsOrder,map);

        //--------- medications
        Bundle medicationResults = client.search()
                .forResource(MedicationStatement.class)
                .where(MedicationStatement.PATIENT.hasId(patientId))
                .execute();
        addResources(allResults,medicationResults,map);


        JsonObjectBuilder json = Json.createObjectBuilder();// .build();

        JsonArrayBuilder array = Json.createArrayBuilder();//.build();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd HH:mm");

        for (Map.Entry<String, BundleEntry> entry : map.entrySet()) {
            String key = entry.getKey();
            BundleEntry bundleEntry = entry.getValue();
            IResource resource = bundleEntry.getResource();
            Date date = bundleEntry.getUpdated().getValue();


            //String date = sdf.format(new Date());


            System.out.println(date);
            //String value = entry.getValue();
            String jsonB =  _fhirContext.newXmlParser().encodeResourceToString(resource);

            array.add(Json.createObjectBuilder()
                    .add("id", key)
                    .add("date", sdf.format(date))
                    .add("value", jsonB));


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


    }

    private void addResources(Bundle allResults, Bundle newResults,Map<String,BundleEntry> map) {

        for (BundleEntry entry : newResults.getEntries()) {
            BundleEntry newEntry = allResults.addEntry();
            newEntry.setId(entry.getId());
            newEntry.setResource(entry.getResource());
            newEntry.getSummary().setValue(new ArrayList<XMLEvent>());
            newEntry.getSummary().setValueAsString("this is the summary");
            // allResults.ad  addEntry(entry);

            String id =entry.getId().toString();
            id = id.replace(_serverBase,"");

            map.put(id,entry);
        }
    }
}
