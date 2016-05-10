package uk.gov.justice.services.adapters.rest.generator;


import static java.util.Collections.emptyMap;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.adapters.test.utils.builder.HttpActionBuilder.defaultGetAction;
import static uk.gov.justice.services.adapters.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.adapters.test.utils.builder.MappingBuilder.mapping;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.adapters.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;

import uk.gov.justice.raml.common.validator.RamlValidationException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RestAdapterGenerator_ActionMapperErrorHandlingTest extends BaseRestAdapterGeneratorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldThrowExceptionIfMappingInDescriptionFieldSyntacticallyIncorrect() throws Exception {
        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid action mapping in RAML file");

        generator.run(
                restRamlWithDefaults()
                        .with(resource("/user")
                                .with(defaultGetAction()
                                        .withDescription("........ aaa incorrect mapping")
                                )

                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
    }

    @Test
    public void shouldThrowExceptionIfMappingEmpty() throws Exception {
        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid action mapping in RAML file");

        generator.run(
                restRamlWithDefaults()
                        .with(resource("/user")
                                .with(defaultGetAction()
                                        .withDescription("...\n...\n")
                                )

                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
    }

    @Test
    public void shouldThrowExceptionIfMappingNull() throws Exception {
        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid action mapping in RAML file");

        generator.run(
                restRamlWithDefaults()
                        .with(resource("/user")
                                .with(httpAction()
                                        .withHttpActionType(GET)
                                        .withResponseTypes("application/vnd.ctx.query.defquery+json")
                                )
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
    }


    @Test
    public void shouldThrowExceptionIfNameNotInMapping() throws Exception {
        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid RAML file. Action name not defined in mapping");

        generator.run(
                restRamlWithDefaults()
                        .with(resource("/user")
                                .with(defaultGetAction()
                                        .withDescription("...\n" +
                                                "(mapping):\n" +
                                                "    responseType: application/vnd.structure.command.test-cmd+json\n" +
                                                "...\n")
                                )

                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
    }

    @Test
    public void shouldThrowExceptionIfNoMediaTypeSetInMapping() throws Exception {
        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid RAML file. Media type not defined in mapping");

        generator.run(
                restRamlWithDefaults()
                        .with(resource("/user")
                                .with(defaultGetAction()
                                        .withDescription("...\n" +
                                                "(mapping):\n" +
                                                "    name: nameABC\n" +
                                                "...\n")
                                )

                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
    }

    @Test
    public void shouldThrowExceptionIfMediaTypeNotMappedInPOSTHttpAction() throws Exception {
        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid RAML file. Media type(s) not mapped to an action: [application/vnd.ctx.command.somemediatype2+json]");


        generator.run(
                restRamlWithDefaults()
                        .with(resource("/case")
                                .with(httpAction(POST)
                                        .with(mapping()
                                                .withName("contextC.someAction")
                                                .withRequestType("application/vnd.ctx.command.somemediatype1+json"))
                                        .withMediaType("application/vnd.ctx.command.somemediatype1+json")
                                        .withMediaType("application/vnd.ctx.command.somemediatype2+json")
                                )

                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
    }

    @Test
    public void shouldThrowExceptionIfMediaTypeNotMappedInGETHttpAction() throws Exception {
        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid RAML file. Media type(s) not mapped to an action: [application/vnd.mediatype3+json]");

        generator.run(
                restRamlWithDefaults().with(
                        resource("/user")
                                .with(httpAction(GET)
                                        .with(mapping()
                                                .withName("contextA.someAction")
                                                .withResponseType("application/vnd.mediatype1+json"))
                                        .with(mapping()
                                                .withName("contextA.someOtherAction")
                                                .withResponseType("application/vnd.mediatype2+json"))
                                        .withResponseTypes(
                                                "application/vnd.mediatype1+json",
                                                "application/vnd.mediatype2+json",
                                                "application/vnd.mediatype3+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));
    }

}
