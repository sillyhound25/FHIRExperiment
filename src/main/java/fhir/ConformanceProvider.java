package fhir;

import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.dstu.resource.Conformance;
import ca.uhn.fhir.model.dstu.valueset.RestfulConformanceModeEnum;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.model.primitive.UriDt;
import ca.uhn.fhir.rest.annotation.Metadata;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.provider.ServerConformanceProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by davidhay on 2/08/14.
 */

public class ConformanceProvider extends ServerConformanceProvider {

    public ConformanceProvider(RestfulServer theRestfulServer) {
        super(theRestfulServer);

        super.setCache(false);
    }


    @Metadata
    public Conformance getServerConformance() {
        Conformance conf = new Conformance();
        Conformance.Rest rest = new Conformance.Rest();
        Conformance.RestSecurity security =  rest.getSecurity();
        rest.setMode(RestfulConformanceModeEnum.SERVER);
        security.setDescription("This is secure REST");
        conf.getRest().add(rest);
        // Create the extension for the authorize endpoint
        ExtensionDt extAuth = new ExtensionDt();
        extAuth.setModifier(false);
        extAuth.setUrl("http://fhir-registry.smartplatforms.org/Profile/oauth-uris#authorize");
        extAuth.setValue(new UriDt("http://localhost:8081/auth/authorize"));
        security.addUndeclaredExtension(extAuth);

        ExtensionDt extToken = new ExtensionDt();
        extToken.setModifier(false);
        extToken.setUrl("http://fhir-registry.smartplatforms.org/Profile/oauth-uris#token");
        extToken.setValue(new UriDt("http://localhost:8081/auth/token"));
        security.addUndeclaredExtension(extToken);

        return conf;
    }

}
