package ca.gc.aafc.dinauser.api.openapi;

import org.apache.http.client.utils.URIBuilder;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class OpenApiConstants {
  public static final String SPEC_HOST = "raw.githubusercontent.com";
  public static final String SPEC_PATH = "DINA-Web/user-specs/main/schema/";
  public static final String SCHEME = "https";

  public static URL getOpenAPISpecsURL(String fileName) throws URISyntaxException, MalformedURLException {
    URIBuilder URI_BUILDER = new URIBuilder();
    URI_BUILDER.setScheme(SCHEME);
    URI_BUILDER.setHost(OpenApiConstants.SPEC_HOST);
    URI_BUILDER.setPath(OpenApiConstants.SPEC_PATH + fileName);
    return URI_BUILDER.build().toURL();
  }

}
