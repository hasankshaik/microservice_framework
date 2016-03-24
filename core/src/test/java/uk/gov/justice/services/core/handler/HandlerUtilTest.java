package uk.gov.justice.services.core.handler;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.handler.registry.exception.InvalidHandlerException;

import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import javax.json.JsonObject;

import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class HandlerUtilTest {

    @Test
    public void shouldBeAWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(HandlerUtil.class);
    }

    @Test
    public void shouldFindHandlerMethods() {
        List<Method> methods = HandlerUtil.findHandlerMethods(CommandHandler.class, Handles.class);
        assertThat(methods, notNullValue());
        assertThat(methods, IsCollectionWithSize.hasSize(3));
    }

    @Test(expected = InvalidHandlerException.class)
    public void shouldThrowExceptionWithInvalidHandlerMethods() {
        HandlerUtil.findHandlerMethods(InvalidHandler.class, Handles.class);
    }

    public static class CommandHandler {
        @Handles("test-context.commands.create-something")
        public void handler1(String jsonString) {
        }

        @Handles("test-context.commands.update-something")
        public void handler2(JsonObject envelopeAsJsonObject) {
        }

        @Handles("test-context.commands.delete-something")
        public void handler2(DeleteSomething deleteSomething) {
        }

        public void nonHandlerMethod(Integer command) {
        }
    }

    public static class InvalidHandler {

        public void nonHandlerMethod(String command) {
        }
    }

    public static class DeleteSomething {
        UUID someId;
    }

}
