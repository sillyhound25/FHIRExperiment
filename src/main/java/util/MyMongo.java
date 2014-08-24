package util;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu.resource.Observation;
import ca.uhn.fhir.model.dstu.resource.Patient;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.model.primitive.UriDt;
import ca.uhn.fhir.parser.IParser;
import com.mongodb.*;
import com.mongodb.util.JSON;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonGenerator;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * MongoDB utilities
 */
public class MyMongo {
    private DB _db;
    private MongoClient _mongoClient;
    private FhirContext _fhirContext;       //needed for resource serialization...

    public MyMongo(){
        try {
            _mongoClient = new MongoClient();
            _db = _mongoClient.getDB("javaDb");
            _db.getCollectionNames();   //this will force a connection to Mongo. The above 2 lines don't...
        } catch (Exception ex) {
            System.out.println("Error connecting to Mongo " + ex.getMessage());
            _db=null;
        }

        _fhirContext = new FhirContext();

    }

    public void closeConnection() {
        _mongoClient.close();
    }

    //locate all resources with the given search uri...
    public List<IResource> findResourcesBySearchString(String srch){

        List<IResource> lst = new ArrayList<IResource>();
        return lst;
/*
        //the string will be of the form: http://localhost/Patient?identifier=PRP1660

        UriDt uri = new UriDt(srch);
        String resourceType = uri.getValue().getPath().replaceAll("/","");  //eg Patient
        String[] ar = uri.getValue().getQuery().split("=");

        String property = ar[0];    //the property we are searching on - eg identifier

        String query = ar[1];       //the search value. may contain a namespace if an identifier...

        DBCollection coll = _db.getCollection("resource");
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("resourceType", resourceType);
        if (query.contains("|")) {
            //if there's a vertical bar, then this must be an
        } else {

        }



        //String resourceType = "Patient";
        String identifierValue = "PRP1660";
        String identifierSystem = null;


        whereQuery.put("identifier.value", identifierValue);

        if (identifierSystem != null) {
             whereQuery.put("identifier.system", identifierSystem);
        }


        DBCursor cursor = coll.find(whereQuery);



        while(cursor.hasNext()) {
            BasicDBObject o = (BasicDBObject) cursor.next();
            String s = o.toString();
            Patient pat = (Patient) _fhirContext.newJsonParser().parseResource(s);
            pat.setId(new IdDt(o.getString("ID")));
            lst.add(pat);
            System.out.println("s=" + s);

        }
        return  lst;
        */
    }

    //locate all resources with the given identifier...
    public List<IResource> findResourcesByIdentifier(String resourceType,IdentifierDt identifier){
        DBCollection coll = _db.getCollection("resource");
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("resource.resourceType", resourceType);
        whereQuery.put("resource.identifier.value", identifier.getValue().getValue());

        if (identifier.getSystem().getValue() != null) {
           // whereQuery.put("identifier.system", identifier.getSystem().getValue());
        }

        DBCursor cursor = coll.find(whereQuery);

        List<IResource> lst = new ArrayList<IResource>();

        while(cursor.hasNext()) {
            BasicDBObject o = (BasicDBObject) cursor.next();
            String s = o.toString();
            try {
                IResource pat = (IResource) _fhirContext.newJsonParser().parseResource(s);
                pat.setId(new IdDt(o.getString("ID")));
                lst.add(pat);
                System.out.println("s=" + s);

            } catch (Exception ex) {
                System.out.println("Error parsing " + s);
            }
        }
        return  lst;
    }


    public List<IResource> findResource(String obsType){
        DBCollection coll = _db.getCollection("resource");
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("resourceType", obsType);
        DBCursor cursor = coll.find(whereQuery);

        List<IResource> lst = new ArrayList<IResource>();

        while(cursor.hasNext()) {
            BasicDBObject o = (BasicDBObject) cursor.next();
            String s = o.toString();
            Observation obs = (Observation) _fhirContext.newJsonParser().parseResource(s);


            obs.setId(new IdDt(o.getString("ID")));
            lst.add(obs);
            System.out.println("s=" + s);

        }
        return  lst;
    }

    public void saveResource(IResource resource) {

        DBCollection coll = _db.getCollection("resource");

        //deactivate any existing resources...
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("resource.resourceType", resource.getId().getResourceType());
        whereQuery.put("meta.ID", resource.getId().getIdPart());
        whereQuery.put("meta.active",true);

        BasicDBObject newDocument = new BasicDBObject();
        newDocument.append("$set", new BasicDBObject().append("meta.active", false));

        coll.update(whereQuery, newDocument);


        DBObject top = new BasicDBObject();

        DBObject dbObject = (DBObject) JSON.parse( _fhirContext.newJsonParser().encodeResourceToString(resource));
        top.put("resource",dbObject);
        DBObject meta = new BasicDBObject();

        meta.put("ID",resource.getId().getIdPart().toString());
        meta.put("version","1");
        meta.put("active",true);
        //meta.put("resourceType",resource.getResourceMetadata().);
        top.put("meta",meta);


        coll.insert(top);//  new BasicDBObject("resource",sResource));
    }

    public void addSimpleXml(String xml, IdentifierDt identifier){
        DBObject top = new BasicDBObject();
        top.put("data",xml);
        String sIdentifier = null;
        if (identifier.getSystem() != null) {
            sIdentifier = identifier.getSystem().getValueAsString() + "|" + identifier.getValue().toString();
        } else {
            sIdentifier = identifier.getValue().toString();
        }
        top.put("identifier",sIdentifier);
        DBCollection coll = _db.getCollection("xml");
        coll.insert(top);
    }


    //get all entries from the simple xml file with the matching identifier...
    //note: uses a substring tyope search...
    public List<String> getSimpleXml(StringDt identifier){
        List<String> lst = new ArrayList<String>();

        DBCollection coll = _db.getCollection("xml");
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("identifier",  java.util.regex.Pattern.compile(identifier.toString()));
        DBCursor cursor = coll.find(whereQuery);

        while(cursor.hasNext()) {
            BasicDBObject o = (BasicDBObject) cursor.next();
            String s = o.get("data").toString();
            lst.add(s);
        }
        return lst;

    }


    //add a new resource to the database...
    //
    //there's likelt a smarter way to get the resource type
    public void addResourceDEP(IResource resource,String type) {

        String sResource = _fhirContext.newJsonParser().encodeResourceToString(resource);
        String fix = sResource.replaceAll("\"","'");

        String collName = type + "_FHIR";
        JsonObject obj = Json.createObjectBuilder()
                .add("date", (new Date()).toString())
                .add("resource",fix)
                .build();
        DBCollection coll = _db.getCollection("resource");

        BasicDBObject document = new BasicDBObject();
        document.put("resource", fix);

        coll.insert(document);//  new BasicDBObject("resource",sResource));

    }


    //retrieve a single resource based on id
    public IResource getResource(IdDt ID) {

        DBCollection coll = _db.getCollection("resource");
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("resource.resourceType", ID.getResourceType());
        whereQuery.put("meta.ID", ID.getIdPart());
        whereQuery.put("meta.active",true);

        DBCursor cursor = coll.find(whereQuery);

        List<IResource> lst = new ArrayList<IResource>();

        while(cursor.hasNext()) {
            BasicDBObject o = (BasicDBObject) cursor.next();
            BasicDBObject r = (BasicDBObject) o.get("resource");
            BasicDBObject meta = (BasicDBObject) o.get("meta");
            String s = r.toString();
            IResource pat = (IResource) _fhirContext.newJsonParser().parseResource(s);
            pat.setId(new IdDt(meta.get("ID").toString()));
            lst.add(pat);
        }

        if (lst.size() > 0) {
            IResource resource = lst.get(0);
            return resource;
        } else {
            return null;
        }
    }



    public void addToHttpLog(String uri, Long elap){

        if (_db != null) {
            JsonObject log = Json.createObjectBuilder()
                    .add("date", (new Date()).toString())
                    .add("uri",uri)
                    .add("elap",elap)
                    .build();
            DBCollection coll = _db.getCollection("httpLog");
            coll.insert(new BasicDBObject("data",log.toString()));
        }
    }

    //record a bundle being returned...
    public void addFhirBundleToLog(Bundle bundle){
        if (_db != null) {
            DBCollection coll = _db.getCollection("log");
            String json = _fhirContext.newJsonParser().encodeBundleToString(bundle);
            coll.insert(new BasicDBObject("data",json));
        }

    }

    //take a list of resources (not an actual bundle) and parse...
    public void addFhirResourceListToLog(List<IResource> lst){
        if (_db != null) {
            DBCollection coll = _db.getCollection("log");
            IParser parser = _fhirContext.newJsonParser();


            //Writer writer = new OutputStreamWriter();

            StringWriter writer = new StringWriter();
            JsonGenerator gen = Json.createGenerator(writer);

            gen.writeStartObject();
            gen.writeStartArray("resources");


            for (IResource r : lst) {
                System.out.println(parser.encodeResourceToString(r));
                //gen.w
                gen.writeStartObject().write("resource", parser.encodeResourceToString(r)).writeEnd();
            }

            gen.writeEnd().writeEnd();
            gen.close();

            coll.insert(new BasicDBObject("data", writer.toString()));
        }

    }




    public void addFhirResourceToLog(JsonObject json){
        if (_db != null) {
            DBCollection coll = _db.getCollection("log");
            coll.insert(new BasicDBObject("data",json.toString()));
        }

    }

    public void addToLog(JsonObject json){
        if (_db != null) {
            DBCollection coll = _db.getCollection("log");
            coll.insert(new BasicDBObject("data",json.toString()));
        }

    }
}
