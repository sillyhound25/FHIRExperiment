package profile;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu.resource.Profile;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.IdParam;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by davidhay on 20/09/14.
 */
public interface IResourceProvider {

    public IResource getResource(HttpServletRequest theRequest, IdDt idDt);
}
