package ca.gc.aafc.agent.api.repository;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ca.gc.aafc.agent.api.KeycloakTestConfiguration;
import ca.gc.aafc.agent.api.testsupport.factories.PersonFactory;
import ca.gc.aafc.dina.testsupport.DatabaseSupportService;
import ca.gc.aafc.dina.testsupport.factories.TestableEntityFactory;
import ca.gc.aafc.dinauser.api.dto.PersonDto;
import ca.gc.aafc.dinauser.api.entities.Person;
import ca.gc.aafc.dinauser.api.repository.PersonRepository;
import io.crnk.core.queryspec.QuerySpec;

/**
 * Test suite to validate the {@link PersonResourceRepository} correctly handles
 * CRUD operations for the {@link Person} Entity.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
public class PersonResourceRepositoryIT {

  @Inject
  private PersonRepository personResourceRepository;

  @Inject
  private DatabaseSupportService dbService;

  private Person personUnderTest;

  @BeforeEach
  public void setup() {
    personUnderTest = PersonFactory.newPerson().build();
    dbService.save(personUnderTest);
  }

  @Test
  public void create_ValidPerson_PersonPersisted() {
    PersonDto personDto = new PersonDto();
    personDto.setDisplayName(TestableEntityFactory.generateRandomNameLettersOnly(10));
    personDto.setEmail(TestableEntityFactory.generateRandomNameLettersOnly(5) + "@email.com");

    UUID uuid = personResourceRepository.create(personDto).getUuid();

    Person result = dbService.findUnique(Person.class, "uuid", uuid);
    assertEquals(personDto.getDisplayName(), result.getDisplayName());
    assertEquals(personDto.getEmail(), result.getEmail());
    assertEquals(uuid, result.getUuid());
    assertEquals(KeycloakTestConfiguration.USER_NAME, result.getCreatedBy());
  }

  @Test
  public void save_PersistedPerson_FieldsUpdated() {
    String updatedEmail = "Updated_Email@email.com";
    String updatedName = "Updated_Name";

    PersonDto updatedPerson = personResourceRepository.findOne(
      personUnderTest.getUuid(),
      new QuerySpec(PersonDto.class)
    );
    updatedPerson.setDisplayName(updatedName);
    updatedPerson.setEmail(updatedEmail);

    personResourceRepository.save(updatedPerson);

    Person result = dbService.findUnique(Person.class, "uuid", updatedPerson.getUuid());
    assertEquals(updatedName, result.getDisplayName());
    assertEquals(updatedEmail, result.getEmail());
  }

  @Test
  public void find_NoFieldsSelected_ReturnsAllFields() {
    PersonDto result = personResourceRepository.findOne(
      personUnderTest.getUuid(),
      new QuerySpec(PersonDto.class)
    );

    assertEquals(personUnderTest.getDisplayName(), result.getDisplayName());
    assertEquals(personUnderTest.getEmail(), result.getEmail());
    assertEquals(personUnderTest.getUuid(), result.getUuid());
  }

  @Test
  public void remove_PersistedPerson_PersonRemoved() {
    PersonDto persistedPerson = personResourceRepository.findOne(
      personUnderTest.getUuid(),
      new QuerySpec(PersonDto.class)
    );

    assertNotNull(dbService.find(Person.class, personUnderTest.getId()));
    personResourceRepository.delete(persistedPerson.getUuid());
    assertNull(dbService.find(Person.class, personUnderTest.getId()));
  }

}
