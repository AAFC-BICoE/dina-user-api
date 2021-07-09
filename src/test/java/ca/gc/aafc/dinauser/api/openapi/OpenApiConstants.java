package ca.gc.aafc.dinauser.api.openapi;

import lombok.SneakyThrows;
import org.apache.http.client.utils.URIBuilder;

import java.net.URL;

public class OpenApiConstants {

  public static final URL USER_PREFERENCE_URL = getOpenAPISpecsURL("userPreference.yml");
  public static final URL USER_URL = getOpenAPISpecsURL("user.yml");

  private static final String SPEC_HOST = "raw.githubusercontent.com";
  private static final String SPEC_PATH = "DINA-Web/user-specs/main/schema/";
  private static final String SCHEME = "https";

  @SneakyThrows
  public static URL getOpenAPISpecsURL(String fileName) {
    URIBuilder URI_BUILDER = new URIBuilder();
    URI_BUILDER.setScheme(SCHEME);
    URI_BUILDER.setHost(OpenApiConstants.SPEC_HOST);
    URI_BUILDER.setPath(OpenApiConstants.SPEC_PATH + fileName);
    return URI_BUILDER.build().toURL();
  }

}
