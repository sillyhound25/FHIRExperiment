package fhir.model;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.dstu.composite.QuantityDt;
import ca.uhn.fhir.model.dstu.resource.Encounter;
import ca.uhn.fhir.model.dstu.resource.Observation;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.IdDt;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;

/**
 * Created by davidhay on 24/08/14.
 */
@ResourceDef(name="Observation", id="myObservation")
public class MyObservation extends Observation {

    //doing this rather than a constructor to allow for different XML formats to be used...
    //doubtless more elegant ways of doing this...
    public void populateFromSimpleXml(DocumentBuilder builder,Document doc) {

        //this.setId(new IdDt("100"));

        NodeList children = doc.getDocumentElement().getChildNodes();
        //NodeList children = l.item(0).getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Element element = (Element) children.item(i);

            String tagName = element.getTagName();
            String Value = element.getTextContent();

            //populate Observation Object...
            if (tagName == "PatientIdentifier") {
                //this is the identifier of the patient
            } else if (tagName == "Value") {
                this.setValue(new QuantityDt( Double.parseDouble(Value)));
            } else if (tagName == "fhirID") {
                this.setId(new IdDt(Value));
            } else if (tagName == "Date") {
                this.setApplies(new DateTimeDt(Value));
            }
        }

    }


}
