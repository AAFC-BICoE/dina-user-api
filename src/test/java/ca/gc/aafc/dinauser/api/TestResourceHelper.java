package ca.gc.aafc.dinauser.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TestResourceHelper {

  private static final ObjectMapper OM = new ObjectMapper();

  /**
   * move me to base-api testsupport TestResourceHelper
   * @param filePath
   * @return
   */
  public static Map<String, Object> readContentAsJsonMap(String filePath) {
    try {
      Path filename = Path.of(filePath);
      String jsonString = Files.readString(filename);

      return OM.readValue(jsonString, new TypeReference<HashMap<String, Object>>() {});
    } catch (IOException e) {
      Assertions.fail("Unable to read saved search data file from test resources.");
    }
    return null;
  }
}
