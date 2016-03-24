package uk.gov.justice.services.adapters.rest.generator;

import uk.gov.justice.raml.core.GeneratorConfig;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static java.nio.file.Paths.get;

/**
 * Unit tests for the {@link JaxRsCodeGenerator} class.
 */
public class JaxRsCodeGeneratorTest {

    private static final String BASE_PACKAGE = "org.raml.test";

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionIfOutputDirectoryDoesNotExist() throws Exception {

        GeneratorConfig config = new GeneratorConfig(
                get(outputFolder.getRoot().getAbsolutePath()),
                get(outputFolder.getRoot().getAbsolutePath(), "blah"),
                BASE_PACKAGE);

        JaxRsCodeGenerator generator = new JaxRsCodeGenerator(config);
        generator.generate();
    }
}
