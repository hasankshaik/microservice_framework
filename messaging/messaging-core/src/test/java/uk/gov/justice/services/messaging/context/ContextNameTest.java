package uk.gov.justice.services.messaging.context;

import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.messaging.exception.InvalidNameException;

import org.junit.Test;

public class ContextNameTest {

    @Test
    public void shouldBeAWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(ContextName.class);
    }

    @Test
    public void shouldReturnContextName() {
        assertThat(ContextName.fromName("test-context.commands.test-command"), equalTo("test-context"));
    }

    @Test(expected = InvalidNameException.class)
    public void shouldThrowExceptionWithInvalidActionName() {
        ContextName.fromName("test-context-commands-test-command");
    }

}
