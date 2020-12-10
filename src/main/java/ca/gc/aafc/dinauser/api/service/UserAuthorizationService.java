package ca.gc.aafc.dinauser.api.service;

import ca.gc.aafc.dina.service.DinaAuthorizationService;
import org.springframework.stereotype.Service;

@Service
public class UserAuthorizationService implements DinaAuthorizationService {

  @Override
  public void authorizeCreate(Object entity) {
    throw new IllegalArgumentException("nononono");
  }

  @Override
  public void authorizeUpdate(Object entity) {
    throw new IllegalArgumentException("nononono");
  }

  @Override
  public void authorizeDelete(Object entity) {
    throw new IllegalArgumentException("nononono");
  }
}
