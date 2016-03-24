package uk.gov.justice.services.example.cakeshop.command.handler;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.Envelope;

import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.slf4j.Logger;

@ServiceComponent(COMMAND_HANDLER)
public class AddRecipeCommandHandler {

    private static final Logger LOGGER = getLogger(MakeCakeCommandHandler.class);
    private static final String FIELD_RECIPE_ID = "recipeId";

    @Inject
    EventSource eventSource;

    @Inject
    TemporaryEventUtil temporaryEventUtil;

    @Handles("cakeshop.commands.add-recipe")
    public void addRecipe(final Envelope command) throws EventStreamException {
        LOGGER.info("=============> Inside add-recipe Command Handler. RecipeId: " + command.payload().getString(FIELD_RECIPE_ID));

        final UUID recipeId = UUID.fromString(command.payload().getString(FIELD_RECIPE_ID));

        EventStream eventStream = eventSource.getStreamById(recipeId);
        final Stream<Envelope> events = temporaryEventUtil.eventsFrom(command);
        eventStream.append(events);
    }

}
