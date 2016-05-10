package uk.gov.justice.services.adapters.test.utils.builder;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.POST;
import static org.raml.model.ParamType.STRING;
import static uk.gov.justice.services.adapters.test.utils.builder.MappingBuilder.defaultMapping;
import static uk.gov.justice.services.adapters.test.utils.builder.MappingBuilder.mapping;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.Response;
import org.raml.model.parameter.QueryParameter;

/**
 * Builds RAML http action (not to be confused with framework's action)
 */
public class HttpActionBuilder {
    private static final String POST_MAPPING_ANNOTATION = "...\n" +
            "(mapping):\n" +
            "    requestType: application/vnd.structure.command.test-cmd+json\n" +
            "    name: structure.test-cmd\n" +
            "...\n";

    private static final String GET_MAPPING_ANNOTATION = "...\n" +
            "(mapping):\n" +
            "    responseType: application/vnd.ctx.query.defquery+json\n" +
            "    name: ctx.test-cmd\n" +
            "...\n";
    private static final String MAPPINGS_BOUNDARY = "...\n";

    private final Map<String, MimeType> body = new HashMap<>();
    private final Map<String, QueryParameter> queryParameters = new HashMap<>();
    private ActionType actionType;
    private List<Response> responses = new LinkedList<>();
    private List<MappingBuilder> mappings = new LinkedList<>();
    private String description;

    public static HttpActionBuilder httpAction() {
        return new HttpActionBuilder();
    }

    public static HttpActionBuilder defaultPostAction() {
        return httpAction()
                .withHttpActionType(POST)
                .withHttpActionOfDefaultRequestType();
    }

    public static HttpActionBuilder defaultGetAction() {
        return httpAction()
                .withHttpActionType(GET)
                .withDefaultResponseType();
    }

    public static HttpActionBuilder httpAction(final ActionType actionType, final String... mimeTypes) {
        HttpActionBuilder httpActionBuilder = new HttpActionBuilder()
                .withHttpActionType(actionType)
                .with(defaultMapping());
        for (String mimeType : mimeTypes) {
            httpActionBuilder = httpActionBuilder.withMediaType(mimeType);
        }
        return httpActionBuilder;
    }

    public HttpActionBuilder withHttpActionType(final ActionType actionType) {
        this.actionType = actionType;
        return this;
    }

    public HttpActionBuilder withHttpActionOfDefaultRequestType() {
        return withMediaType("application/vnd.ctx.command.defcmd+json")
                .with(mapping()
                        .withName("action1")
                        .withRequestType("application/vnd.ctx.command.defcmd+json"));
    }

    public HttpActionBuilder withDefaultResponseType() {
        return withResponseTypes("application/vnd.ctx.query.defquery+json")
                .with(mapping()
                        .withName("action1")
                        .withResponseType("application/vnd.ctx.query.defquery+json"));
    }

    public HttpActionBuilder withResponseTypes(final String... responseTypes) {
        return withHttpActionResponse(new Response(), responseTypes);
    }

    public HttpActionBuilder withHttpActionResponse(final Response response, final String... responseTypes) {
        Map<String, MimeType> respBody = new HashMap<>();
        for (String responseType : responseTypes) {
            respBody.put(responseType, new MimeType(responseType));
        }
        response.setBody(respBody);
        responses.add(response);
        return this;
    }

    public HttpActionBuilder withQueryParameters(final QueryParameter... queryParameters) {
        stream(queryParameters).forEach(queryParameter -> this.queryParameters.put(queryParameter.getDisplayName(), queryParameter));
        return this;
    }

    public HttpActionBuilder withQueryParameters(final String... paramNames) {
        stream(paramNames).forEach(paramName -> {
            QueryParameter queryParameter = new QueryParameter();
            queryParameter.setDisplayName(paramName);
            queryParameter.setType(STRING);
            queryParameter.setRequired(true);
            this.queryParameters.put(paramName, queryParameter);
        });

        return this;
    }

    public HttpActionBuilder withOptionalQueryParameters(final String... paramNames) {
        stream(paramNames).forEach(paramName -> {
            QueryParameter queryParameter = new QueryParameter();
            queryParameter.setDisplayName(paramName);
            queryParameter.setType(STRING);
            queryParameter.setRequired(false);
            this.queryParameters.put(paramName, queryParameter);
        });

        return this;
    }

    public HttpActionBuilder withMediaType(final MimeType mimeType) {
        body.put(mimeType.toString(), mimeType);
        return this;
    }

    public HttpActionBuilder withMediaType(final String stringMimeType) {
        return withMediaType(new MimeType(stringMimeType));
    }

    public HttpActionBuilder withDescription(final String description) {
        this.description = description;
        return this;
    }

    public HttpActionBuilder with(final MappingBuilder mapping) {
        this.mappings.add(mapping);
        return this;
    }

    public Action build() {
        final Action action = new Action();
        action.setType(actionType);

        if (description != null) {
            action.setDescription(description);
        } else if (isNotEmpty(mappings)) {
            String description =
                    format("%s%s%s",
                            MAPPINGS_BOUNDARY,
                            mappings.stream().map(MappingBuilder::build).collect(joining()),
                            MAPPINGS_BOUNDARY);
            action.setDescription(description);
        }


        action.setBody(body);

        HashMap<String, Response> responsesMap = new HashMap<>();
        this.responses.forEach(r -> responsesMap.put("200", r));
        action.setResponses(responsesMap);

        action.setQueryParameters(queryParameters);


        return action;
    }
}
