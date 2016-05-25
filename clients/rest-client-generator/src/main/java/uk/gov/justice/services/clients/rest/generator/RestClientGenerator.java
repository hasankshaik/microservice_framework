package uk.gov.justice.services.clients.rest.generator;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.raml.model.ActionType.GET;
import static uk.gov.justice.services.core.annotation.Component.contains;
import static uk.gov.justice.services.core.annotation.Component.names;

import uk.gov.justice.raml.common.mapper.ActionMapping;
import uk.gov.justice.raml.core.Generator;
import uk.gov.justice.raml.core.GeneratorConfig;
import uk.gov.justice.services.clients.core.EndpointDefinition;
import uk.gov.justice.services.clients.core.QueryParam;
import uk.gov.justice.services.clients.core.RestClientHelper;
import uk.gov.justice.services.clients.core.RestClientProcessor;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.Remote;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.logging.JsonEnvelopeLoggerHelper;
import uk.gov.justice.services.messaging.logging.LoggerUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.model.Response;
import org.raml.model.parameter.QueryParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates code for a rest client.
 *
 * The generated client is a {@link ServiceComponent} with an additional {link @Remote} annotation.
 * The client will contain a method per media type within every httpAction, within every resource.
 */
public class RestClientGenerator implements Generator {

    private static final String SERVICE_COMPONENT_PROPERTY = "serviceComponent";
    private static final String REST_CLIENT_HELPER = "restClientHelper";
    private static final String REST_CLIENT_PROCESSOR = "restClientProcessor";
    private static final int NUMBER_OF_PATH_SEGMENTS = 8;
    private static final int SERVICE_PATH_SEGMENT_INDEX = 7;
    private static final int PILLAR_PATH_SEGMENT_INDEX = 4;
    private static final int TIER_PATH_SEGMENT_INDEX = 5;

    @Override
    public void run(final Raml raml, final GeneratorConfig generatorConfig) {
        final TypeSpec.Builder classSpec = classSpecOf(raml, generatorConfig);

        classSpec.addMethods(
                raml.getResources().values().stream()
                        .flatMap(this::generateCodeForResource)
                        .collect(toList())
        );

        writeToJavaFile(classSpec, generatorConfig);
    }

    private void writeToJavaFile(final TypeSpec.Builder classSpec, final GeneratorConfig generatorConfig) {
        try {
            JavaFile
                    .builder(generatorConfig.getBasePackageName(), classSpec.build())
                    .build()
                    .writeTo(generatorConfig.getOutputDirectory());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private TypeSpec.Builder classSpecOf(final Raml raml, final GeneratorConfig generatorConfig) {
        final String className = classNameOf(raml.getBaseUri());
        return classBuilder(className)
                .addModifiers(PUBLIC, FINAL)
                .addAnnotation(Remote.class)
                .addAnnotation(AnnotationSpec.builder(ServiceComponent.class)
                        .addMember("value", "$T.$L", Component.class, serviceComponentOf(generatorConfig))
                        .build())
                .addField(loggerConstantField(className))
                .addField(restClientFieldProcessor())
                .addField(restClientHelperField())
                .addField(baseUriStaticFieldOf(raml));
    }

    private FieldSpec loggerConstantField(final String className) {
        final ClassName classLoggerFactory = ClassName.get(LoggerFactory.class);
        return FieldSpec.builder(Logger.class, "LOGGER")
                .addModifiers(PRIVATE, javax.lang.model.element.Modifier.STATIC, javax.lang.model.element.Modifier.FINAL)
                .initializer(
                        CodeBlock.builder()
                                .add(format("$L.getLogger(%s.class)", className), classLoggerFactory).build()
                )
                .build();
    }

    private String serviceComponentOf(final GeneratorConfig generatorConfig) {
        final String serviceComponentProperty = generatorConfig.getGeneratorProperties().get(SERVICE_COMPONENT_PROPERTY);
        if (isEmpty(serviceComponentProperty)) {
            throw new IllegalArgumentException(format("%s generator property not set in the plugin config", SERVICE_COMPONENT_PROPERTY));
        }
        if (!contains(serviceComponentProperty)) {
            throw new IllegalArgumentException(format("%s generator property invalid. Expected one of: %s", SERVICE_COMPONENT_PROPERTY, names(", ")));
        }
        return serviceComponentProperty;
    }

    private FieldSpec restClientHelperField() {
        return FieldSpec.builder(RestClientHelper.class, REST_CLIENT_HELPER)
                .addAnnotation(Inject.class)
                .build();
    }

    private FieldSpec restClientFieldProcessor() {
        return FieldSpec.builder(RestClientProcessor.class, REST_CLIENT_PROCESSOR)
                .addAnnotation(Inject.class)
                .build();
    }

    private FieldSpec baseUriStaticFieldOf(final Raml raml) {
        return FieldSpec.builder(String.class, "BASE_URI")
                .addModifiers(Modifier.PRIVATE, FINAL, Modifier.STATIC)
                .initializer("$S", raml.getBaseUri())
                .build();
    }

    private Stream<MethodSpec> generateCodeForResource(final Resource resource) {
        return resource.getActions().values().stream()
                .flatMap(action -> generateCodeForAction(resource, action));
    }

    private Stream<MethodSpec> generateCodeForAction(final Resource resource, final Action ramlAction) {
        final List<ActionMapping> actionMappings = ActionMapping.listOf(ramlAction.getDescription());
        return mediaTypesOf(ramlAction)
                .map(mimeType ->
                        methodOf(
                                resource,
                                ramlAction,
                                mimeType,
                                mappingOf(actionMappings, mimeType, ramlAction.getType())
                        )
                );
    }

    private Stream<MimeType> mediaTypesOf(Action ramlAction) {
        switch (ramlAction.getType()) {
            case GET:
                final Response response = ramlAction.getResponses().get(valueOf(OK.getStatusCode()));
                if (response != null) {
                    return response.getBody().values().stream();
                } else {
                    return Stream.empty();
                }
            case POST:
                return ramlAction.getBody().values().stream();
            default:
                throw new IllegalStateException(format("Unsupported httpAction type %s", ramlAction.getType()));
        }
    }


    private ActionMapping mappingOf(final List<ActionMapping> actionMappings, final MimeType mimeType, final ActionType httpMethod) {
        return actionMappings.stream().filter(m -> m.mimeTypeFor(httpMethod).equals(mimeType.getType())).findAny().get();
    }

    private MethodSpec methodOf(final Resource resource,
                                final Action action,
                                final MimeType mimeType,
                                final ActionMapping mapping) {

        final ClassName classLoggerUtils = ClassName.get(LoggerUtils.class);
        final ClassName classJsonEnvelopeLoggerHelper = ClassName.get(JsonEnvelopeLoggerHelper.class);

        final String header = headerOf(mimeType);
        final ActionType actionType = action.getType();
        final String methodName = methodNameOf(actionType, header);
        final Class<?> methodReturnType = action.getType().equals(GET) ? JsonEnvelope.class : Void.class;

        final MethodSpec.Builder builder = methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Handles.class)
                        .addMember("value", "$S", mapping.getName())
                        .build())
                .addParameter(ParameterSpec.builder(JsonEnvelope.class, "envelope")
                        .addModifiers(Modifier.FINAL)
                        .build())
                .addStatement("$T.trace(LOGGER, () -> String.format(\"Handling remote REST request: %s\", $T.toEnvelopeTraceString(envelope)))",
                        classLoggerUtils, classJsonEnvelopeLoggerHelper)
                .addStatement("final String path = \"$L\"", resource.getRelativeUri())
                .addStatement("final $T<$T> pathParams = $L.extractPathParametersFromPath(path)",
                        Set.class, String.class, REST_CLIENT_HELPER)
                .addStatement("final Set<QueryParam> queryParams = new $T<$T>()",
                        HashSet.class, QueryParam.class);

        action.getQueryParameters().forEach((name, queryParameter) -> addQueryParam(builder, queryParameter, name));

        builder.addStatement("final $T def = new $T(BASE_URI, path, pathParams, queryParams, $S)",
                EndpointDefinition.class, EndpointDefinition.class, mediaTypeFrom(mapping, actionType));

        if (action.getType().equals(GET)) {
            builder.returns(methodReturnType);
            builder.addStatement("return $L.request(def, envelope)", REST_CLIENT_PROCESSOR);
        } else {
            builder.addStatement("$L.request(def, envelope)", REST_CLIENT_PROCESSOR);
        }

        return builder.build();
    }

    private String mediaTypeFrom(final ActionMapping mapping, final ActionType actionType) {
        return headerOf(actionType.equals(ActionType.GET) ? mapping.getResponseType() : mapping.getRequestType());
    }

    private void addQueryParam(final MethodSpec.Builder builder, final QueryParameter parameter, String name) {
        builder.addStatement("queryParams.add(new QueryParam(\"$L\", $L))", name, parameter.isRequired());
    }

    private String methodNameOf(final ActionType actionType, final String header) {
        String baseName = capitalize(header.replaceAll("[\\W_]", " ")).replaceAll("[\\W_]", "");

        final String actionTypeStr = actionType.name().toLowerCase();
        baseName = baseName.substring(0, 1).toUpperCase() + baseName.substring(1);
        return actionTypeStr + baseName;
    }

    private String classNameOf(final String baseUri) {
        final String[] pathSegments = baseUri.split("/");
        if (pathSegments.length != NUMBER_OF_PATH_SEGMENTS) {
            throw new IllegalArgumentException("baseUri must have 8 parts");
        }
        return format("Remote%s%s%s",
                capitalize(pathSegments[SERVICE_PATH_SEGMENT_INDEX]),
                capitalize(pathSegments[PILLAR_PATH_SEGMENT_INDEX]),
                capitalize(pathSegments[TIER_PATH_SEGMENT_INDEX]));
    }

    private String headerOf(final MimeType mimeType) {
        return headerOf(mimeType.toString());
    }

    private String headerOf(final String mimeType) {
        // Removes the application/vnd
        final String section = mimeType.substring(mimeType.indexOf('.') + 1);
        return section.substring(0, section.indexOf('+'));
    }
}
