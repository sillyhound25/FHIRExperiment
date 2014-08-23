package fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.*;
import ca.uhn.fhir.model.dstu.resource.OperationOutcome;
import ca.uhn.fhir.model.dstu.resource.Profile;
import ca.uhn.fhir.model.dstu.valueset.IssueSeverityEnum;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.Transaction;
import ca.uhn.fhir.rest.annotation.TransactionParam;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import fhir.server.GlucoseBundleProcessor;
import fhir.server.Validator;
import util.MyMongo;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Process a transaction
 */

public class TransactionProvider {
    private MyMongo _myMongo;
    private FhirContext _ctx;

    //constructor to pass instance of MongoClient across - actually, could this be in the Context
    public TransactionProvider(MyMongo myMongo){
        _myMongo = myMongo;
    }

    /* Process a transaction. We will assume that the only bundle we get is a list of glucose values
    * at the moment. */
    @Transaction
    public List<IResource> transaction(@TransactionParam Bundle bundle) {

        //The profile will be in a bundle tag (ie a 'category' feed element)
        String profileName = null;
        TagList tagList = bundle.getCategories();
        for (Tag tag : tagList) {
            if (tag.getScheme().contains("profile")) {  //probably a better way to do this...
                profileName = tag.getTerm();
            }
        }

        //we must have a profile...
        if (profileName == null) {
            OperationOutcome oo = new OperationOutcome();
            OperationOutcome.Issue issue =  oo.addIssue();
            issue.setSeverity(IssueSeverityEnum.ERROR);
            issue.setDetails("Only profiled bundles can be processed");
            throw new UnprocessableEntityException("Sorry Dave, I can't allow that",oo);
        }

        //the profile reference is a URI like http://localhost/Profile/glucose...
        //we'd check here that this was a profile that we accept. Potentially, we
        //could invoke different processors depending on the profile. There's doubtless
        //some clever way of doing that that means we can add processors without a complete
        //recompile...

        //The processor class for this profile. Ignore the reference to mongo for this purpose...
        GlucoseBundleProcessor glucoseBundleProcessor = new GlucoseBundleProcessor(_myMongo);

        //generate the simple XML objects for insertion into the database
        List<org.w3c.dom.Document> lst = glucoseBundleProcessor.generateSimpleXML(bundle);

        //Here is where the insert would occur...
        TransformerFactory factory = TransformerFactory.newInstance();
        try {
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            for (org.w3c.dom.Document doc : lst) {
                StringWriter writer = new StringWriter();
                transformer.transform(new DOMSource(doc), new StreamResult(writer));
                String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
                System.out.println(output);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //just return the resources we received. We do want to think about assigning ID's at some point...
        //HAPI will turn these into a conformant bundle...
        List<IResource> lst2 = new ArrayList<IResource>();
        for (BundleEntry entry : bundle.getEntries()) {
            lst2.add(entry.getResource());
        }
        return lst2;

    }
}

/*
public List<IResource> transaction(@TransactionParam Bundle bundle) {
        //public List<IResource> transaction(@TransactionParam List<IResource> theResources) {

        String profileName = null;
        TagList tagList = bundle.getCategories();
        for (Tag tag : tagList) {
            if (tag.getScheme().contains("profile")) {
                profileName = tag.getTerm();
            }
        }

        //we must have a profile...
        if (profileName == null) {
            OperationOutcome oo = new OperationOutcome();
            OperationOutcome.Issue issue =  oo.addIssue();
            issue.setSeverity(IssueSeverityEnum.ERROR);
            issue.setDetails("Only profiles bundles can be processed");
            throw new UnprocessableEntityException("Sorry Dave, I can't allow that",oo);
        }

        //the profile reference is a URI like http://localhost/Profile/glucose...
        String[] ar = profileName.split("/");
        IdDt  id = new IdDt("Profile",ar[ar.length-1]);
        Profile profile = (Profile) _myMongo.getResource(id);


        //This is optional -
        Validator validator = new Validator();

        if (! validator.validateBundle(bundle,profile)) {
            OperationOutcome oo = new OperationOutcome();
            OperationOutcome.Issue issue =  oo.addIssue();
            issue.setSeverity(IssueSeverityEnum.ERROR);
            issue.setDetails("The bundle failed validation");
            throw new UnprocessableEntityException("The bundle failed validation",oo);
        }

        //assume that this is a bundle containing glucose results. Would use a profile to determine this...
        GlucoseBundleProcessor glucoseBundleProcessor = new GlucoseBundleProcessor(_myMongo);

        //generate the simple XML objects for insertion into the database
        List<org.w3c.dom.Document> lst = glucoseBundleProcessor.generateSimpleXML(bundle);



        TransformerFactory factory = TransformerFactory.newInstance();
        try {
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            for (org.w3c.dom.Document doc : lst) {
                StringWriter writer = new StringWriter();
                transformer.transform(new DOMSource(doc), new StreamResult(writer));
                String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
                System.out.println(output);
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //just return the resources we received. We do want to think about assigning ID's at some point...
        List<IResource> lst2 = new ArrayList<IResource>();
        for (BundleEntry entry : bundle.getEntries()) {
            lst2.add(entry.getResource());
        }
        return lst2;

    }
}
*
* */