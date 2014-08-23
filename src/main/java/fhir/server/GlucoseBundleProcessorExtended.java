package fhir.server;

import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.model.api.BundleEntry;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.api.ResourceMetadataKeyEnum;
import ca.uhn.fhir.model.dstu.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu.composite.QuantityDt;
import ca.uhn.fhir.model.dstu.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu.resource.Device;
import ca.uhn.fhir.model.dstu.resource.Observation;
import ca.uhn.fhir.model.dstu.resource.Patient;
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

public class GlucoseBundleProcessorExtended {

    private MyMongo _myMongo;   //the database helper class

    public GlucoseBundleProcessorExtended(MyMongo myMongo){
        _myMongo = myMongo;
    }

    //process with a Bundle...
    public List<IResource> processGlucoseUploads(Bundle bundle) {
        //generate a list of resources...
        List<IResource> theResources = new ArrayList<IResource>();    //list of resources in bundle
        for (BundleEntry entry : bundle.getEntries()) {
            theResources.add(entry.getResource());
        }

        //return this.process(theResources);

        List<IResource> lst = this.genericProcess(theResources);
        return lst;
    }


    //process with a List of resources
    public List<IResource> processGlucoseUploads(List<IResource> theResources) {

        return this.process(theResources);
    }


    //generate simple XML files - one per Observation - from a profiled bundle.
    //the use of the profile means that we are dealing with a known sub-set of FHIR...
    public List<org.w3c.dom.Document> generateSimpleXML(Bundle bundle) {
        Patient patient = null;     //this will be the patient resource. There should only be one....
        Device device = null;       //this will be the device resource. There should only be one....
        List<Observation> lstObservations = new ArrayList<Observation>();
        List<org.w3c.dom.Document> lstSimpleXml = new ArrayList<org.w3c.dom.Document>();
        //generate a list of resources...
        List<IResource> theResources = new ArrayList<IResource>();    //list of resources in bundle
        //first, locate the patient and device resources, and save the observation resource in an array
        for (BundleEntry entry : bundle.getEntries()) {
            IResource resource = entry.getResource();
            if (resource instanceof Patient) {
                patient = (Patient) resource;
            } else if (resource instanceof Device) {
                device = (Device) resource;
            } if (resource instanceof Observation) {
                lstObservations.add((Observation) resource);
            } else {
                //this is an unknown resource. What do we do?
            }
        }

        if (patient == null || device == null) {
            //missing resources - an error - reject
        }

        //generate the simple XML objects. Assume one per observation.
        //I'm sure there are more efficient ways of doing this!
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = docFactory.newDocumentBuilder();


            String patientIdentifier = patient.getIdentifierFirstRep().getValue().getValue();

            for (Observation observation : lstObservations) {
                org.w3c.dom.Document doc = builder.newDocument();
                lstSimpleXml.add(doc);
                Element root = doc.createElement("GlucoseResults");
                doc.appendChild(root);
                Element patNode = doc.createElement("PatientID");
                patNode.appendChild(doc.createTextNode(patientIdentifier));
                root.appendChild(patNode);

                Element valueNode = doc.createElement("Value");
                //note that our profile specifies the use of a Quantity here.
                QuantityDt quantityDt = (QuantityDt) observation.getValue();
                valueNode.appendChild(doc.createTextNode(quantityDt.getValue().getValueAsString()));
                root.appendChild(valueNode);


            }

        } catch (Exception ex) {
            //do something...
            //note that we'd want to check for an invalid cast of the Observation value
            ex.printStackTrace();
        }
        //return this.process(theResources);

        //List<IResource> lst = this.genericProcess(theResources);
        return lstSimpleXml;
    }




    //process a bundle of glucose results, adding them to the repository...
    private List<IResource> process(List<IResource> theResources) {
        Patient patient = null;     //this will be the patient resource. There should only be one....
        Device device = null;       //this will be the device resource. There should only be one....
        List<IResource> insertList = new ArrayList<IResource>();    //list of resource to insert...

        //First pass: assign all the CID:'s to a new ID. For more complex scenarios, we'd keep track of
        //the changes, but for this profile, we don't need to...
        //Note that this processing is highly specific to this profile !!! (For example, we ignore resources we don't expect to see)
        for (IResource resource : theResources) {
            String currentID = resource.getId().getValue();
            if (currentID.substring(0,4).equals("cid:")) {
                //Obviouslym the base URL should not be hard coded...
                String newID = "http://myUrl/" + java.util.UUID.randomUUID().toString();
                resource.setId(new IdDt(newID));    //and here's the new URL
            }

            //if this resource is a patient or device, then set the appropriate objects. We'll use these to set
            // the references in the Observations in the second pass. In real life we'd want to be sure there is only one of each...
            if (resource instanceof Patient) {
                patient = (Patient) resource;
                //we need to see if there's already a patient with this identifier. If there is - and there is one,
                //then we use that Patient rather than adding a new one.
                // This could be triggered by a 'rel=search' link on the bundle entry in a generic routine...
                IdentifierDt identifier = patient.getIdentifier().size() >0 ? patient.getIdentifier().get(0) : null;
                if (identifier != null) {
                    List<IResource> lst = _myMongo.findResourcesByIdentifier("Patient",identifier);
                    if (lst.size() == 1) {
                        //there is a single patient with that identifier...
                        patient = (Patient) lst.get(0);
                        resource.setId(patient.getId());    //set the identifier in the list. We need to return this...
                    } else if (lst.size() > 1) {
                        //here is where we ought to raise an error - we cannot know which one to use.
                    } else {
                        //if there isn't a single resource with this identifier, we need to add a new one
                        insertList.add(patient);
                    }
                } else {
                    insertList.add(patient);
                }
            }

            //look up a Device in the same way as as for a Patient
            if (resource instanceof Device) {
                List<ResourceReferenceDt> lstO = resource.getAllPopulatedChildElementsOfType(ResourceReferenceDt.class);

                device = (Device) resource;
                IdentifierDt identifier = device.getIdentifier().size() >0 ? device.getIdentifier().get(0) : null;
                if (identifier != null) {
                    List<IResource> lst = _myMongo.findResourcesByIdentifier("Device", identifier);
                    if (lst.size() == 1) {
                        device = (Device) lst.get(0);
                        resource.setId(device.getId());    //set the identifier in the list. We need to retuen this...
                    } else {
                        insertList.add(device);
                    }
                } else {
                    insertList.add(device);
                }
            }

            if (resource instanceof Observation) {
                //we always add observations...

                List<ResourceReferenceDt> lst = resource.getAllPopulatedChildElementsOfType(ResourceReferenceDt.class);
                insertList.add(resource);
            }
        }

        //Second Pass: Now we re-set all the resource references. This is very crude, and rather manual.
        // We also really ought to make sure that the patient and the device have been set.....
        for (IResource resource : theResources) {
            if (resource instanceof Observation) {
                Observation obs = (Observation) resource;

                //this will be the correct ID - either a new one from the bundle, or a pre-existing one...
                obs.setSubject(new ResourceReferenceDt(patient.getId()));

                //set the performer - there can be more than one in the spec, hence a list...
                List<ResourceReferenceDt> lstReferences = new ArrayList<ResourceReferenceDt>();
                lstReferences.add(new ResourceReferenceDt(device.getId()));
                obs.setPerformer(lstReferences);
            }
        }

        //Last pass - write out the resources
        for (IResource resource : insertList) {
            _myMongo.saveResource(resource);
        }

        //we return the bundle with the updated resourceID's - as per the spec...
        return theResources;
    }


    //generic process list
//process a bundle of glucose results, adding them to the repository...
    private List<IResource> genericProcess(List<IResource> theResources) {
        // Patient patient = null;     //this will be the patient resource. There should only be one....
        // Device device = null;       //this will be the device resource. There should only be one....
        List<IResource> insertList = new ArrayList<IResource>();    //list of resource to insert...

        Map<String,String> map = new HashMap<String,String>();


        //First pass: assign all the CID:'s to a new ID. For more complex scenarios, we'd keep track of
        //the changes, but for this profile, we don't need to...
        //Note that this processing is highly specific to this profile !!! (For example, we ignore resources we don't expect to see)
        for (IResource resource : theResources) {
            String currentID = resource.getId().getValue();


            //Class c = resource.getClass();
            String klass = resource.getClass().getCanonicalName();
            String[] ar = klass.split("\\.");


            //replace all the cid: references with
            if (currentID.substring(0, 4).equals("cid:")) {
                //assume a local resource
                String newID = "/" + ar[ar.length-1] + "/" + java.util.UUID.randomUUID().toString();
                map.put(currentID, newID);  //keep a track of the changes
                resource.setId(new IdDt(newID));    //and here's the new URL
            }

            insertList.add(resource);   //TEMP - add all for now...

        /*
        //check to see if there is a 'search' link in the bundle. If so, then set the ID to the existing one..
        Map<ResourceMetadataKeyEnum<?>, Object> m = resource.getResourceMetadata();
        if (m.containsKey(ResourceMetadataKeyEnum.LINK_SEARCH)) {
            String srch = (String) m.get(ResourceMetadataKeyEnum.LINK_SEARCH);
            //the string will be of the form: http://localhost/Patient?identifier=PRP1660
            List<IResource> lst = _myMongo.findResourcesBySearchString(srch);

            if (lst.size() == 1) {
                //there is a single patient with that identifier...
                //patient = (Patient) lst.get(0);
                //resource.setId(patient.getId());    //set the identifier in the list. We need to return this...
            } else if (lst.size() > 1) {
                //here is where we ought to raise an error - we cannot know which one to use.
            } else {
                //if there isn't a single resource with this identifier, we need to add a new one
                insertList.add(resource);
            }
        }
        */
        }

        //next pass, update the internal references - ie where a property is a resourceReference
        for (IResource resource : theResources) {
            //this is a list of all the elements that are of type ResourceReference
            List<ResourceReferenceDt> lst = resource.getAllPopulatedChildElementsOfType(ResourceReferenceDt.class);
            System.out.println(lst);
            for (ResourceReferenceDt rr : lst) {
                if (rr.getReference().getValue() != null) {
                    String ID = rr.getReference().getValue();       //this is the ID in the reference...
                    System.out.println(rr.getReference().getValue());
                    //IdDt idDt = rr.getReference();
                    if (map.containsKey(ID)) {
                        rr.getReference().setValue(map.get(ID));
                        //resource.setId(new IdDt(map.get(ID)));
                    }
                }
            }
        }

/*
    //final pass - process any inserts...
    for (IResource resource : insertList) {
        _myMongo.saveResource(resource);
    }

    */
        //System.out.println(map);

        return theResources;


        /*

        //if this resource is a patient or device, then set the appropriate objects. We'll use these to set
        // the references in the Observations in the second pass. In real life we'd want to be sure there is only one of each...
        if (resource instanceof Patient) {
            patient = (Patient) resource;
            //we need to see if there's already a patient with this identifier. If there is - and there is one,
            //then we use that Patient rather than adding a new one.
            // This could be triggered by a 'rel=search' link on the bundle entry in a generic routine...
            IdentifierDt identifier = patient.getIdentifier().size() >0 ? patient.getIdentifier().get(0) : null;
            if (identifier != null) {
                List<IResource> lst = _myMongo.findResourcesByIdentifier("Patient",identifier);
                if (lst.size() == 1) {
                    //there is a single patient with that identifier...
                    patient = (Patient) lst.get(0);
                    resource.setId(patient.getId());    //set the identifier in the list. We need to return this...
                } else if (lst.size() > 1) {
                    //here is where we ought to raise an error - we cannot know which one to use.
                } else {
                    //if there isn't a single resource with this identifier, we need to add a new one
                    insertList.add(patient);
                }
            } else {
                insertList.add(patient);
            }
        }

        //look up a Device in the same way as as for a Patient
        if (resource instanceof Device) {
            List<ResourceReferenceDt> lstO = resource.getAllPopulatedChildElementsOfType(ResourceReferenceDt.class);

            device = (Device) resource;
            IdentifierDt identifier = device.getIdentifier().size() >0 ? device.getIdentifier().get(0) : null;
            if (identifier != null) {
                List<IResource> lst = _myMongo.findResourcesByIdentifier("Device", identifier);
                if (lst.size() == 1) {
                    device = (Device) lst.get(0);
                    resource.setId(device.getId());    //set the identifier in the list. We need to retuen this...
                } else {
                    insertList.add(device);
                }
            } else {
                insertList.add(device);
            }
        }

        if (resource instanceof Observation) {
            //we always add observations...

            List<ResourceReferenceDt> lst = resource.getAllPopulatedChildElementsOfType(ResourceReferenceDt.class);


            insertList.add(resource);
        }
    }

    //Second Pass: Now we re-set all the resource references. This is very crude, and rather manual.
    // We also really ought to make sure that the patient and the device have been set.....
    for (IResource resource : theResources) {
        if (resource instanceof Observation) {
            Observation obs = (Observation) resource;

            //this will be the correct ID - either a new one from the bundle, or a pre-existing one...
            obs.setSubject(new ResourceReferenceDt(patient.getId()));

            //set the performer - there can be more than one in the spec, hence a list...
            List<ResourceReferenceDt> lstReferences = new ArrayList<ResourceReferenceDt>();
            lstReferences.add(new ResourceReferenceDt(device.getId()));
            obs.setPerformer(lstReferences);
        }
    }

    //Last pass - write out the resources
    for (IResource resource : insertList) {
        _myMongo.saveResource(resource);
    }


    */
        //we return the bundle with the updated resourceID's - as per the spec...

    }
}