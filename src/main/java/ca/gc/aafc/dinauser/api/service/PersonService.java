package ca.gc.aafc.dinauser.api.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.dinauser.api.entities.Person;
import lombok.NonNull;

@Service
public class PersonService extends DinaService<Person> {

  public PersonService(@NonNull BaseDAO baseDAO) {
    super(baseDAO);
  }

  @Override
  protected void preCreate(Person entity) {
    entity.setUuid(UUID.randomUUID());
  }

  @Override
  protected void preDelete(Person entity) {

  }

  @Override
  protected void preUpdate(Person entity) {

  }

}
