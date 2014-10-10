package profile;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu.resource.OperationOutcome;
import ca.uhn.fhir.model.dstu.valueset.IssueSeverityEnum;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.MethodOutcome;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Created by davidhay on 20/09/14.
 */
public class FileResourceProvider implements IResourceProvider {
    @Override
    public IResource getResource(HttpServletRequest theRequest, IdDt theId) {

        String resourceType = theId.getResourceType().toLowerCase();
        String id = theId.getIdPart();

        ServletContext context = theRequest.getServletContext();
        FhirContext fhirContext = (FhirContext) context.getAttribute("fhircontext");
        String path = context.getRealPath("/resources/"+resourceType+"/" + id);
        File file = new File(path);

        try {
            byte[] encoded = Files.readAllBytes(file.toPath());
            String s = new String(encoded, StandardCharsets.UTF_8);
            IResource resource = fhirContext.newXmlParser().parseResource(s);
            return resource;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    @Override
    public MethodOutcome putResource(HttpServletRequest theRequest, IResource resource, IdDt theId) {

        MethodOutcome retVal = new MethodOutcome();
        retVal.setId(theId);

        final OperationOutcome operationOutcome = new OperationOutcome();
        final OperationOutcome.Issue issue = operationOutcome.addIssue();
        issue.setSeverity(IssueSeverityEnum.INFORMATION);
        issue.setDetails("message");
        retVal.setOperationOutcome(operationOutcome);
        return retVal;

    }
}
