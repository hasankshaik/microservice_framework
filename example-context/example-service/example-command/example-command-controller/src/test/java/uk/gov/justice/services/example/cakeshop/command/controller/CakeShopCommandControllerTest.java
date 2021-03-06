package uk.gov.justice.services.example.cakeshop.command.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CakeShopCommandControllerTest {

    private static final String FIELD_RECIPE_ID = "recipeId";
    private static final UUID RECIPE_ID = UUID.randomUUID();

    @Mock
    JsonEnvelope envelope;
    @Mock
    JsonObject payload;
    @Mock
    private Sender sender;
    @InjectMocks
    private CakeShopCommandController cakeShopCommandController;

    @Test
    public void shouldHandleMakeCakeCommand() throws Exception {
        when(envelope.payloadAsJsonObject()).thenReturn(payload);
        when(payload.getString(FIELD_RECIPE_ID)).thenReturn(RECIPE_ID.toString());
        cakeShopCommandController.makeCake(envelope);

        verify(sender, times(1)).send(envelope);
    }

    @Test
    public void shouldHandleAddRecipeCommand() throws Exception {
        when(envelope.payloadAsJsonObject()).thenReturn(payload);
        when(payload.getString(FIELD_RECIPE_ID)).thenReturn(RECIPE_ID.toString());

        cakeShopCommandController.addRecipe(envelope);

        verify(sender, times(1)).send(envelope);
    }

}
