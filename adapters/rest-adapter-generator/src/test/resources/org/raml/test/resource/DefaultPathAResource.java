package org.raml.test.resource;

import java.lang.Override;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import uk.gov.justice.services.adapter.rest.RestProcessor;
import uk.gov.justice.services.adapter.rest.ValidParameterMapBuilder;
import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.dispatcher.SynchronousDispatcher;

@Adapter(Component.COMMAND_API)
public class DefaultPathAResource implements PathAResource {
  @Inject
  RestProcessor restProcessor;

  @Context
  HttpHeaders headers;

  @Inject
  SynchronousDispatcher syncDispatcher;

  @Override
  public Response getPathA() {
    ValidParameterMapBuilder validParameterMapBuilder = new ValidParameterMapBuilder();
    return restProcessor.processSynchronously(syncDispatcher::dispatch, headers, validParameterMapBuilder.validateAndBuildMap());
  }
}
