package fhir.model;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.dstu.resource.Condition;
import ca.uhn.fhir.model.dstu.valueset.NarrativeStatusEnum;

/**
 * Created by davidhay on 28/07/14.
 */
@ResourceDef(name="Condition", id="myCondition")
public class MyCondition extends Condition {

    //helper method to set the text. (could make an automatic process from resource data)
    public void mySetText(String txt){
        this.getText().setDiv(txt);
        this.getText().setStatus(NarrativeStatusEnum.GENERATED);
    }

}
