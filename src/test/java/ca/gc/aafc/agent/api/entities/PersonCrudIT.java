package ca.gc.aafc.agent.api.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import ca.gc.aafc.agent.api.testsupport.factories.PersonFactory;
import ca.gc.aafc.dina.testsupport.DatabaseSupportService;
import ca.gc.aafc.dinauser.api.entities.Person;

/**
 * Test suite to validate {@link Person} performs as a valid Hibernate Entity.
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class PersonCrudIT {

  @Inject
  private DatabaseSupportService dbService;

  private Person personUnderTest;

  @BeforeEach
  public void setup() {
    personUnderTest = PersonFactory.newPerson().build();
    dbService.save(personUnderTest);
  }

  @Test
  public void testSave() {
    Person person = PersonFactory.newPerson().build();
    assertNull(person.getId());
    dbService.save(person);
    assertNotNull(person.getId());
  }

  @Test
  public void testFind() {
    Person fetchedPerson = dbService.find(Person.class, personUnderTest.getId());
    assertEquals(personUnderTest.getId(), fetchedPerson.getId());
    assertEquals(personUnderTest.getDisplayName(), fetchedPerson.getDisplayName());
    assertEquals(personUnderTest.getEmail(), fetchedPerson.getEmail());
    assertEquals(personUnderTest.getUuid(), fetchedPerson.getUuid());
    assertNotNull(fetchedPerson.getCreatedOn());
  }

  @Test
  public void testRemove() {
    Integer id = personUnderTest.getId();
    dbService.deleteById(Person.class, id);
    assertNull(dbService.find(Person.class, id));
  }

}
