package profile;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu.resource.Profile;
import ca.uhn.fhir.model.primitive.IdDt;

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
    public IResource getResource(HttpServletRequest theRequest, IdDt idDt) {

        String resourceType = idDt.getResourceType().toLowerCase();
        String id = idDt.getIdPart();

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
}
