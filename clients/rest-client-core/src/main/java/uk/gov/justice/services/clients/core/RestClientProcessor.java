package uk.gov.justice.services.clients.core;

import static java.lang.String.format;
import static javax.ws.rs.core.Response.Status.OK;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 * Helper service for processing requests for generating REST clients.
 */
public class RestClientProcessor {

    private static final String MEDIA_TYPE_PATTERN = "application/vnd.%s+json";

    @Inject
    StringToJsonObjectConverter stringToJsonObjectConverter;

    @Inject
    JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    /**
     * Make a request using the envelope provided to a specified endpoint.
     * @param definition the endpoint definition
     * @param envelope the envelope containing the payload and/or parameters to pass in the request
     * @return the response from that the endpoint returned for this request
     */
    public JsonEnvelope request(final EndpointDefinition definition, final JsonEnvelope envelope) {
        final JsonObject payload = envelope.payloadAsJsonObject();
        final Client client = ClientBuilder.newClient();

        WebTarget target = client
                .target(definition.getBaseURi())
                .path(definition.getPath());

        for (String pathParam : definition.getPathParams()) {
            target = target.resolveTemplate(pathParam, payload.getString(pathParam));
        }

        for (QueryParam queryParam : definition.getQueryParams()) {
            final String paramName = queryParam.getName();
            if (!payload.containsKey(paramName) && queryParam.isRequired()) {
                throw new IllegalStateException(format("Query parameter %s is required, but not present in envelope", paramName));
            }

            if (payload.containsKey(paramName)) {
                target = target.queryParam(paramName, payload.getString(paramName));
            }
        }

        final Invocation.Builder builder = target.request(format(MEDIA_TYPE_PATTERN, envelope.metadata().name()));
        final Response response = builder.get();
        if (response.getStatus() != OK.getStatusCode()) {
            throw new RuntimeException(format("Request Failed with code %s and reason \"%s\"", response.getStatus(),
                    response.getStatusInfo().getReasonPhrase()));
        }

        return jsonObjectEnvelopeConverter.asEnvelope(stringToJsonObjectConverter.convert(response.readEntity(String.class)));
    }

}
