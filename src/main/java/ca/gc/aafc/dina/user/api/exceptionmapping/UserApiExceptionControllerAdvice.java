package ca.gc.aafc.dina.user.api.exceptionmapping;

import org.springframework.web.bind.annotation.RestControllerAdvice;

import ca.gc.aafc.dina.exceptionmapping.JsonApiExceptionControllerAdvice;
import ca.gc.aafc.dina.user.api.repository.GroupRepository;
import ca.gc.aafc.dina.user.api.repository.UserRepository;

/**
 * Applies the {@link JsonApiExceptionControllerAdvice} handlers to the repositories that
 * are not a DinaRepositoryV2.
 */
@RestControllerAdvice(assignableTypes = {GroupRepository.class, UserRepository.class})
public class UserApiExceptionControllerAdvice extends JsonApiExceptionControllerAdvice {
}
