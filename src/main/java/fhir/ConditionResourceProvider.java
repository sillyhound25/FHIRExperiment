package fhir;


import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import ca.uhn.fhir.model.dstu.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu.resource.Condition;
import ca.uhn.fhir.model.dstu.resource.Observation;


import ca.uhn.fhir.model.dstu.valueset.*;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.StringDt;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.server.IResourceProvider;
import fhir.model.MyCondition;
import fhir.model.MyPatient;
import util.MyMongo;
import util.Utility;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import javax.json.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * The provider for Observation Resources...
 */
public class ConditionResourceProvider implements IResourceProvider {


    private MyMongo _myMongo;

    //constructor to pass instance of MongoClient across - actually, could this be in the Context???
    public ConditionResourceProvider(MyMongo myMongo){
        _myMongo = myMongo;
    }

    @Override
    public Class<Condition> getResourceType() {
        return Condition.class;
    }

    // read a single resource by id
    @Read()
    public Condition getResourceById(@IdParam IdDt theId) {
        Condition cond = new Condition();


        return cond;
    }


    /*
    //should move to a super/utility class...
    private String safeGetJsonValue(JsonObject node, String inx){
        JsonString obj = node.getJsonString(inx);
        if (obj != null){
            return obj.getString();
        } else {
            return null;
        }
    }
    */

    //get a patients conditions
    @Search()
    public List<IResource> getConditionBySubject(@RequiredParam(name = Condition.SP_SUBJECT) StringDt theSubject,
                                                   HttpServletRequest theRequest,
                                                   HttpServletResponse theResponse) {
        List<IResource> lst = new ArrayList<IResource>();


        InputStream is = null;
        try {
            //this will be the address of the CDR REST services endpoint.
            //In practice, there will be other headers (eg for security/privacy) that are necessary to pass across...

            //todo - need a config object of some sort as everything in configurable...

            URL url = new URL("http://172.20.4.15/DEMO/patient/84568-4564@OHCP/problems/lists/diagnosis-genericproblems");
            URLConnection conn = url.openConnection();
            //the api key and auth would come from the incomming call.
            conn.addRequestProperty("X-OHP-Developer-API-Key","d97e75fc-6509-4697-a4f5-826cafc094e2");
            conn.addRequestProperty("Authorization","Basic am9lLm1hcnRpbjpjb25jZXJ0bw==");
            //the user domain is specific to the resource type...
            conn.addRequestProperty("X-OHP-UserDomain","Provider");
            conn.addRequestProperty("Accept","application/vnd.orchestral.cdr.problems.8_4+json");

            conn.connect();
            is = conn.getInputStream();
            JsonReader rdr = Json.createReader(is);
            JsonObject obj = rdr.readObject();          //execute the query

            //first, construct a patient resource...
            //generate the Patient resource
            JsonObject patientNode = obj.getJsonObject("patientHandle");    //this is the patient details in the service
            MyPatient myPatient = new MyPatient();
            String patientId = Utility.getPatientIDFromResolvingIdentifier(patientNode.getJsonObject("resolvingIdentifier"));
            String name = Utility.safeGetJsonValue(patientNode,"fullName");
            myPatient.setId(patientId);
            myPatient.mySetText(name);
            myPatient.addName().setText(name);
            myPatient.setBirthDate(new DateTimeDt(Utility.getDateFromOHDate(
            Utility.safeGetJsonValue(patientNode, "dateOfBirth")), TemporalPrecisionEnum.DAY));
            lst.add(myPatient);

            JsonArray conditions = obj.getJsonObject("payload").getJsonArray("problems");

            Integer ctr = 0;
            for (JsonObject cond : conditions.getValuesAs(JsonObject.class)) {
                JsonObject identifier = cond.getJsonObject("identifier");
                String type = Utility.safeGetJsonValue(identifier,"problemType");   //the type of problem. Actually - the url should only return generic problems
                JsonObject problem = identifier.getJsonObject("problemName");
                String problemName = Utility.safeGetJsonValue(problem,"description");
                MyCondition condition = new MyCondition();
                condition.getSubject().setReference("Patient/"+patientId);

                //there is a unique ID in CDR, but it isn't being surfaced in the REST service so we'll make one up...
                //apparently there are REST calls that do this, so that would be the correct one to use
                //the id would then be what we feed to the GET resource message
                condition.setId("Problem" + ctr);
                ctr ++;

                condition.mySetText(problemName);
                JsonObject problemCode = problem.getJsonObject("identifier");
                //todo - if the system is always an oid then add "urn:oid:" as a prefix
                condition.setCode(new CodeableConceptDt(Utility.safeGetJsonValue(problemCode,"codingSystem"),Utility.safeGetJsonValue(problemCode,"code")));
                lst.add(condition);
            };

        } catch (Exception ex) {
            System.out.println("Error during REST call " + ex.toString());
            ex.printStackTrace(System.out);
        }
        finally {
            try {
                is.close();
                System.out.println("Closing stream");
            } catch (Exception ex){
                //was going to throw an exception - but that seems to need to be in a try/catch loop, which seems to defeat the purpose...
                System.out.println("Exception closing stream "+ ex.getMessage());
            }
        }
        return lst;
    }


    //a sample search that doesn't do anything...
    @Search()
    public List<Observation> getConditions(@RequiredParam(name = Observation.SP_NAME) StringDt theObservationName) {
        Observation obs = new Observation();

        obs.setName(new CodeableConceptDt("mysys","myVal"));
        obs.setStatus(ObservationStatusEnum.FINAL);
        obs.setReliability(ObservationReliabilityEnum.OK);
        obs.getText().setDiv("from getObservation");
        obs.getText().setStatus(NarrativeStatusEnum.GENERATED);
        obs.setId("myId");

        return Collections.singletonList(obs);
    }




}

