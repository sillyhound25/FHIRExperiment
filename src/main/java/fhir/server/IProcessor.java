package fhir.server;

import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.model.dstu.composite.IdentifierDt;
import org.w3c.dom.Document;

import java.util.List;

/**
 * Created by davidhay on 25/08/14.
 */
public interface IProcessor {

    List<Document> generateSimpleXML(Bundle bundle);
    IdentifierDt  getIdentifier();    //set when the processor encounters the patient resource

}
