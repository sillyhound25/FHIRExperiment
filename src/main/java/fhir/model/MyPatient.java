package fhir.model;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.dstu.resource.Patient;
import ca.uhn.fhir.model.dstu.valueset.NarrativeStatusEnum;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.util.ElementUtil;

/**
 * Overrided Patient to add extension...
 */
@ResourceDef(name="Patient", id="myPatient")
public class MyPatient extends Patient {

    @Child(name="iwi")
    @Extension(url="http://fhir.orionhealth.com/profile/patient#iwi", definedLocally=false, isModifier=false)
    @Description(shortDefinition="The patients iwi (tribe)")
    private StringDt myIwi;

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && ElementUtil.isEmpty(myIwi);
    }



    /** Getter for iwi */
    public StringDt getIwi() {
        if (myIwi == null) {
            myIwi = new StringDt();
        }
        return myIwi;
    }

    /** Setter for iwi */
    public void setIwi(StringDt theIwi) {
        myIwi = theIwi;
    }

    //helper method to set the text. (could make an automatic process from resource data)
    public void mySetText(String txt){
        this.getText().setDiv(txt);
        this.getText().setStatus(NarrativeStatusEnum.GENERATED);
    }

}
