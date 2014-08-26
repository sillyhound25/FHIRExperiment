package fhir.server;

import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.model.api.BundleEntry;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.api.ResourceMetadataKeyEnum;
import ca.uhn.fhir.model.dstu.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu.composite.QuantityDt;
import ca.uhn.fhir.model.dstu.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu.resource.*;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.model.primitive.UriDt;
import org.w3c.dom.Element;
import util.MyMongo;

import javax.swing.text.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by davidhay on 17/08/14.
 */

public class MlomBundleProcessor implements IProcessor {

    private IdentifierDt _patientIdentifier=null;

    public IdentifierDt getIdentifier() {
        return _patientIdentifier;
    }

    //generate simple XML files - one per Observation - from a profiled bundle.
    //the use of the profile means that we are dealing with a known sub-set of FHIR...
    public List<org.w3c.dom.Document> generateSimpleXML(Bundle bundle) {
        Patient patient = null;             //this will be the patient resource. There should only be one....
        List list = null;                   //this will be the list resource. There should only be one....
        Practitioner practitioner = null;   //The Practitioner who performed the reconciliation

        List<MedicationStatement> lstMedications = new ArrayList<MedicationStatement>();
        List<org.w3c.dom.Document> lstSimpleXml = new ArrayList<org.w3c.dom.Document>();

        //generate a list of resources...
        List<IResource> theResources = new ArrayList<IResource>();    //list of resources in bundle
        //first, locate the patient and device resources, and save the observation resource in an array

        for (BundleEntry entry : bundle.getEntries()) {
            IResource resource = entry.getResource();
            if (resource instanceof Patient) {
                patient = (Patient) resource;
                _patientIdentifier = patient.getIdentifierFirstRep();
            } else if (resource instanceof List) {
                list = (List) resource;
            } else if (resource instanceof Practitioner) {
                practitioner = (Practitioner) resource;
            } else if (resource instanceof MedicationStatement) {
                lstMedications.add((MedicationStatement) resource);
            } else {
                //this is an unknown resource. What do we do?
            }
        }

        if (patient == null || list == null || practitioner == null) {
            //missing resources - an error - reject
        }

        //generate the simple XML objects. Assume one per observation.
        //Only adding some sample entries to prove the concept


        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = docFactory.newDocumentBuilder();

            //need to think about what to do if there is no identifier...
            String patientIdentifier = null;
            try {
                patientIdentifier = patient.getIdentifierFirstRep().getValue().getValue();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            org.w3c.dom.Document doc = builder.newDocument();
            lstSimpleXml.add(doc);
            Element root = doc.createElement("MedicationList");
            doc.appendChild(root);

            for (MedicationStatement med : lstMedications) {
                /*

                //make up an ID to use...
                String ID = java.util.UUID.randomUUID().toString();
                Element idNode = doc.createElement("fhirID");
                idNode.appendChild(doc.createTextNode(ID));
                root.appendChild(idNode);

                Element patNode = doc.createElement("PatientIdentifier");
                patNode.appendChild(doc.createTextNode(patientIdentifier));
                root.appendChild(patNode);

                Element valueNode = doc.createElement("Value");
                //note that our profile specifies the use of a Quantity here.
                QuantityDt quantityDt = (QuantityDt) observation.getValue();
                valueNode.appendChild(doc.createTextNode(quantityDt.getValue().getValueAsString()));
                root.appendChild(valueNode);

                Element dateNode = doc.createElement("Date");
                DateTimeDt dt = (DateTimeDt) observation.getApplies();
                dateNode.appendChild(doc.createTextNode(dt.getValueAsString()));
                root.appendChild(dateNode);

                */
            }

        } catch (Exception ex) {
            //do something...
            //note that we'd want to check for an invalid cast of the Observation value
            ex.printStackTrace();
        }
        return lstSimpleXml;
    }
}
