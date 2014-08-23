package fhir;


import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu.composite.QuantityDt;
import ca.uhn.fhir.model.dstu.resource.Observation;


import ca.uhn.fhir.model.dstu.valueset.*;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.StringDt;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.mongodb.MongoClient;
import fhir.model.MyPatient;
import util.MyMongo;
import util.Utility;

import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


import javax.json.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * The provider for Observation Resources...
 */

public class ObservationResourceProvider implements IResourceProvider {



    @Search()
    public List<IResource> getObservationBySubject(@RequiredParam(name = Observation.SP_SUBJECT) StringDt theSubject) {
        List<Observation> lstObservations = new ArrayList<Observation>();

        System.out.println("getting observations: Subject:" + theSubject);

        return _myMongo.findResource("Observation");
    }

    /*
    @Search()
    public List<Observation> getObservationBySubject(@RequiredParam(name = Observation.SP_SUBJECT) StringDt theSubject,
                                                     @RequiredParam(name = Observation.SP_NAME) TokenOrListParam theObsNames) {
        List<Observation> lstObservations = new ArrayList<Observation>();

        System.out.println("getting observations: Subject:"+theSubject);

        //emulates calling an existing non-FHIR REST service and getting a JSON object back...
        //we'd probably pass across the theObsNames to only return the list that we want
        InputStream is = null;
        try {
            URL url = new URL("http://localhost:4001/dataplatform/obs/"+theSubject);
            URLConnection conn = url.openConnection();
            conn.connect();
            is = conn.getInputStream();
            JsonReader rdr = Json.createReader(is);
            JsonObject obj = rdr.readObject();
            //assume that the json structure is {data[{id:,unit:,code:,value:,date: }]}

            JsonArray results = obj.getJsonArray("data");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            for (JsonObject result : results.getValuesAs(JsonObject.class)) {
                //get the basic data for the Observation
                String id = result.getJsonString("id").getString();
                String unit = result.getJsonString("unit").getString();
                String code = result.getJsonString("code").getString();
                String display = result.getJsonString("display").getString();
                double value =  result.getJsonNumber("value").doubleValue();
                Date date = format.parse(result.getJsonString("date").getString());

                System.out.println(value + " " + code);

                //create an Observation resource
                Observation obs = new Observation();
                obs.setId(id);
                obs.setApplies(new DateTimeDt(date));
                obs.setName(new CodeableConceptDt("http://loinc.org", code));
                QuantityDt quantityDt = new QuantityDt(value).setUnits(unit).setSystem("http://unitsofmeasure.org").setCode(unit);
                obs.setValue(quantityDt);
                obs.setStatus(ObservationStatusEnum.FINAL);
                obs.setReliability(ObservationReliabilityEnum.OK);

                //the text...
                obs.getText().setDiv(result.getJsonString("date").getString() + " " + display + " " + value);
                obs.getText().setStatus(NarrativeStatusEnum.GENERATED);

                //and add to the list...
                lstObservations.add(obs);
            }
        } catch (Exception ex) {
            System.out.println("Error during REST call " + ex.toString());
        }
        finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception ex){               //was going to throw an exception - but that seems to need to be in a try/catch loop, which seems to defeat the purpose...
                System.out.println("Exception closing stream "+ ex.getMessage());
            }
        }
        return lstObservations;

    }

    */


    private MyMongo _myMongo;

    //constructor to pass instance of MongoClient across - actually, could this be in the Context???
    public ObservationResourceProvider(MyMongo myMongo){
        _myMongo = myMongo;
    }

    @Override
    public Class<Observation> getResourceType() {
        return Observation.class;
    }

    // read a single resource by id
    @Read()
    public Observation getResourceById(@IdParam IdDt theId) {
        Observation obs = new Observation();

        obs.setName(new CodeableConceptDt("mysys","myVal"));
        obs.setStatus(ObservationStatusEnum.FINAL);
        obs.setReliability(ObservationReliabilityEnum.OK);
        return obs;
    }







    //get observations of a particular type for a patient
    //sample: /Observation?subject=<>&name=<>
    //the return is List<IResource> and not List<Observation> as we include the Patient resource in the response as well...
    //note that I've included the request/response as these may be needed subsequently.





    /*


//get observations of a particular type for a patient
    //sample: /Observation?subject=<>&name=<>
    //the return is List<IResource> and not List<Observation> as we include the Patient resource in the response as well...
    //note that I've included the request/response as these may be needed subsequently.
    @Search()
    public List<IResource> getObservationBySubject(@RequiredParam(name = Observation.SP_SUBJECT) StringDt theSubject,
                                                   @OptionalParam(name = Observation.SP_NAME) TokenOrListParam theObsName,
                                                    HttpServletRequest theRequest,
                                                    HttpServletResponse theResponse) {
        List<IResource> lst = new ArrayList<IResource>();

        System.out.println("Observation " + theObsName);

        //Bundle lst = new Bundle();
        //emulates calling the DataPlatform REST service and getting a JSON object back...
        //http://www.oracle.com/technetwork/articles/java/json-1973242.html
        InputStream is = null;
       try {
           //this will be the address of the Data Platform endpoint.
           //In practice, there will be other headers (eg for security/privacy) that are necessary to pass across...
           URL url = new URL("http://localhost:4001/dataplatform/obs");
           URLConnection conn = url.openConnection();
           conn.addRequestProperty("x_mine","value");
           conn.connect();
           is = conn.getInputStream();
            //is = url.openStream();
            JsonReader rdr = Json.createReader(is);
            JsonObject obj = rdr.readObject();
           //assume that the json structure is {data[{date: value: id: }]}

           //generate the Patient resource
           JsonObject patientNode = obj.getJsonObject("patient");
           MyPatient myPatient = new MyPatient();

           //data from the incoming json...
           String patientId = patientNode.getJsonString("id").getString();
           String name = patientNode.getJsonString("name").getString();

           String iwi = Utility.safeGetJsonValue(patientNode, "iwi");
           System.out.println(iwi);

           myPatient.setId(patientId);
           myPatient.mySetText(name);
           myPatient.addName().setText(name);

           if (iwi != null) {
               System.out.println("setting iwi");
               myPatient.setIwi(new StringDt(iwi));
           }
            //and add it to the response...
           //lst.add(myPatient);


           JsonArray results = obj.getJsonArray("data");

           //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
           SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

           for (JsonObject result : results.getValuesAs(JsonObject.class)) {
               System.out.println(result.getJsonNumber("value"));
               System.out.println(result.getJsonString("date"));

               String unit = result.getJsonString("unit").getString();
               String code = result.getJsonString("code").getString();
               System.out.println(unit + " " + code);

               double value =  result.getJsonNumber("value").doubleValue();
               Date date = format.parse(result.getJsonString("date").getString());
               String id = result.getJsonString("id").getString();

               Observation obs1 = new Observation();

               obs1.setName(new CodeableConceptDt("http://loinc.org", code));
               obs1.setStatus(ObservationStatusEnum.FINAL);
               obs1.setReliability(ObservationReliabilityEnum.OK);
               obs1.getText().setDiv("from getObservationBySubject. subject=" + theSubject + " obsname = " + theObsName);
               obs1.getText().setStatus(NarrativeStatusEnum.GENERATED);

               QuantityDt quantityDt = new QuantityDt(value).setUnits(unit).setSystem("http://unitsofmeasure.org").setCode(unit);

               obs1.setValue(quantityDt);

               obs1.setApplies(new DateTimeDt(date));
               obs1.setId(id);

               lst.add(obs1);

           }

         } catch (Exception ex) {
           System.out.println("Error during REST call " + ex.toString());
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

        //log the response. This is probably not the right place for this (you'd do it in data platform) , but good to know how it works...

        //this.

        //String json = _ctx.newJsonParser().encodeBundleToString(bundle)

        if (_myMongo != null) {
            _myMongo.addFhirResourceListToLog(lst);
        }

       return lst;

    }



    *
    * */


    /*
    //a sample search that doesn't do anything...
    @Search()
    public List<Observation> getObservation(@RequiredParam(name = Observation.SP_NAME) StringDt theObservationName) {
        Observation obs = new Observation();

        obs.setName(new CodeableConceptDt("mysys","myVal"));
        obs.setStatus(ObservationStatusEnum.FINAL);
        obs.setReliability(ObservationReliabilityEnum.OK);
        obs.getText().setDiv("from getObservation");
        obs.getText().setStatus(NarrativeStatusEnum.GENERATED);
        obs.setId("myId");

        return Collections.singletonList(obs);
    }

    */




    }

