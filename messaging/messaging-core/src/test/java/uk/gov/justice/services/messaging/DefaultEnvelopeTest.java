package uk.gov.justice.services.messaging;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.DefaultEnvelope.envelopeFrom;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.json.JsonObject;

/**
 * Unit tests for the {@link DefaultEnvelope} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultEnvelopeTest {

    @Mock
    private Metadata metadata;

    @Mock
    private JsonObject payload;

    private Envelope envelope;

    @Before
    public void setup() {
        envelope = envelopeFrom(metadata, payload);
    }

    @Test
    public void shouldReturnMetadata() throws Exception {
        assertThat(envelope.metadata(), equalTo(metadata));
    }

    @Test
    public void shouldReturnPayload() throws Exception {
        assertThat(envelope.payload(), equalTo(payload));
    }
}
