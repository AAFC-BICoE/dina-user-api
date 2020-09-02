package ca.gc.aafc.agent.api;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import com.google.common.collect.ImmutableMap;

import org.apache.http.client.utils.URIBuilder;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import ca.gc.aafc.dina.testsupport.BaseRestAssuredTest;
import ca.gc.aafc.dina.testsupport.factories.TestableEntityFactory;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dina.testsupport.specs.OpenAPI3Assertions;
import ca.gc.aafc.dinauser.api.entities.Person;
import io.crnk.core.engine.http.HttpStatus;
import io.restassured.response.ValidatableResponse;
import lombok.extern.log4j.Log4j2;

/**
 * Test suite to validate correct HTTP and JSON API responses for {@link Person}
 * Endpoints.
 */
@Transactional
@Log4j2
public class PersonRestJsonIT extends BaseRestAssuredTest {

  @Inject
  private EntityManagerFactory entityManagerFactory;

  public static final String API_BASE_PATH = "/api/v1/person/";
  private static final String SPEC_HOST = "raw.githubusercontent.com";
  private static final String SPEC_PATH = "DINA-Web/agent-specs/master/schema/agent.yml";
  private static final String SCHEMA_NAME = "Person";
  public static final String EMAIL_ERROR = "email must be a well-formed email address";

  protected PersonRestJsonIT() {
    super(API_BASE_PATH);
  }

  /**
   * Remove database entries after each test.
   */
  @AfterEach
  public void tearDown() {
    EntityManager em = entityManagerFactory.createEntityManager();

    EntityTransaction et = em.getTransaction();
    et.begin();

    CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
    CriteriaDelete<Person> query = criteriaBuilder.createCriteriaDelete(Person.class);
    Root<Person> root = query.from(Person.class);
    query.where(criteriaBuilder.isNotNull(root.get("uuid")));
    em.createQuery(query).executeUpdate();

    em.flush();
    et.commit();
    em.close();
  }

  @Test
  public void post_NewPerson_ReturnsOkayAndBody() {
    String displayName = "Albert";
    String email = "Albert@yahoo.com";

    ValidatableResponse response = super.sendPost("", getPostBody(displayName, email));

    assertValidResponseBodyAndCode(response, displayName, email, HttpStatus.CREATED_201)
        .body("data.id", Matchers.notNullValue());
    validateJsonSchema(response.extract().asString());
  }

  @Test
  public void post_NewPerson_ReturnsInvalidEmail() {
    String displayName = "Albert";
    String email = "AlbertYahoo.com";

    super.sendPost("", getPostBody(displayName, email), HttpStatus.UNPROCESSABLE_ENTITY_422)
        .body("errors.detail", Matchers.hasItem(EMAIL_ERROR));
  }

  @Test
  public void Patch_UpdatePerson_ReturnsOkayAndBody() {
    String id = persistPerson("person", "person@agen.ca");

    String newName = "Updated Name";
    String newEmail = "Updated@yahoo.nz";
    super.sendPatch("", id, getPostBody(newName, newEmail));

    ValidatableResponse response = super.sendGet("", id);
    assertValidResponseBodyAndCode(response, newName, newEmail, HttpStatus.OK_200);
    validateJsonSchema(response.extract().asString());
  }

  @Test
  public void get_PersistedPerson_ReturnsOkayAndBody() {
    String displayName = TestableEntityFactory.generateRandomNameLettersOnly(10);
    String email = TestableEntityFactory.generateRandomNameLettersOnly(5) + "@email.com";
    String id = persistPerson(displayName, email);

    ValidatableResponse response = super.sendGet("", id);

    assertValidResponseBodyAndCode(response, displayName, email, HttpStatus.OK_200)
        .body("data.id", Matchers.equalTo(id));
    validateJsonSchema(response.extract().asString());
  }

  @Test
  public void get_InvalidPerson_ReturnsResourceNotFound() {
    super.sendGet("", "a8098c1a-f86e-11da-bd1a-00112444be1e", HttpStatus.NOT_FOUND_404);
  }

  @Test
  public void delete_PeresistedPerson_ReturnsNoConentAndDeletes() {
    String id = persistPerson("person", "person@agen.ca");
    super.sendGet("", id);
    super.sendDelete("", id);
    super.sendGet("", id, HttpStatus.NOT_FOUND_404);
  }

  /**
   * Assert a given response contains the correct name, email, and HTTP return
   * code as given.
   *
   * @param response
   *                        - response to validate
   * @param expectedName
   *                        - expected name in the response body
   * @param expectedEmail
   *                        - expected email in the response body
   * @param httpCode
   *                        - expected HTTP response code
   * @return - A validatable response from the request.
   */
  private static ValidatableResponse assertValidResponseBodyAndCode(ValidatableResponse response,
      String expectedName, String expectedEmail, int httpCode) {
    return response.statusCode(httpCode)
        .body("data.attributes.displayName", Matchers.equalTo(expectedName))
        .body("data.attributes.email", Matchers.equalTo(expectedEmail));
  }

  /**
   * Returns a serializable JSON API Map for use with POSTED request bodies.
   *
   * @param displayName
   *                      - name for the post body
   * @param email
   *                      - email for the post body
   * @return - serializable JSON API map
   */
  private static Map<String, Object> getPostBody(String displayName, String email) {
    ImmutableMap.Builder<String, Object> objAttribMap = new ImmutableMap.Builder<>();
    objAttribMap.put("displayName", displayName);
    objAttribMap.put("email", email);
    return JsonAPITestHelper.toJsonAPIMap("person", objAttribMap.build(), null, null);
  }

  /**
   * Helper method to persist an Person with a given name and email.
   *
   * @param name
   *                - name for the person
   * @param email
   *                - email for the person
   * @return - id of the persisted person
   */
  private String persistPerson(String name, String email) {
    return super.sendPost("", getPostBody(name, email)).extract().jsonPath().get("data.id");
  }

  /**
   * Validates a given JSON response body matches the schema defined in
   * {@link PersonRestJsonIT#SCHEMA_NAME}
   *
   * @param responseJson
   *                       The response json from service
   */
  private void validateJsonSchema(String responseJson) {
    if (!Boolean.valueOf(System.getProperty("testing.skip-external-schema-validation"))) {
      try {
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme("https");
        uriBuilder.setHost(SPEC_HOST);
        uriBuilder.setPath(SPEC_PATH);
        log.info("Validating {} schema against the following response: {}", () -> SCHEMA_NAME,
            () -> responseJson);
        OpenAPI3Assertions.assertSchema(uriBuilder.build().toURL(), SCHEMA_NAME, responseJson);
      } catch (URISyntaxException | MalformedURLException e) {
        log.error(e);
      }
    } else {
      log.warn("Skipping schema validation."
          + "System property testing.skip-external-schema-validation set to true. ");
    }
  }
}