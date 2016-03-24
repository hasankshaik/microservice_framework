package uk.gov.justice.services.messaging.jms;


import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.jms.exception.JmsEnvelopeSenderException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.json.JsonObject;

import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JmsEnvelopeSenderTest {

    private static final String NAME = "test.events.something-done";

    @Mock
    private EnvelopeConverter envelopeConverter;

    @Mock
    private ConnectionFactory connectionFactory;

    @Mock
    private JsonObject metadataAsJsonObject;

    @Mock
    private JsonObject payload;

    @Mock
    private Session session;

    @Mock
    private Connection connection;

    @Mock
    private Destination destination;

    @Mock
    private MessageProducer messageProducer;

    @Mock
    private TextMessage textMessage;

    @Mock
    private Envelope envelope;

    @Mock
    private Metadata metadata;

    private JmsEnvelopeSender jmsEnvelopeSender;

    @Before
    public void setup() throws Exception {
        jmsEnvelopeSender = new JmsEnvelopeSender();
        jmsEnvelopeSender.connectionFactory = connectionFactory;
        jmsEnvelopeSender.envelopeConverter = envelopeConverter;

        when(connectionFactory.createConnection()).thenReturn(connection);
        when(envelope.metadata()).thenReturn(metadata);
        when(metadata.name()).thenReturn(NAME);
        when(session.createProducer(destination)).thenReturn(messageProducer);
        when(session.createTextMessage(anyString())).thenReturn(textMessage);
    }

    @Test
    public void shouldPublishValidEnvelopeToTheTopic() throws Exception {
        when(connection.createSession(false, AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(envelopeConverter.toMessage(envelope, session)).thenReturn(textMessage);

        jmsEnvelopeSender.send(envelope, destination);

        verify(session, times(1)).createProducer(destination);
        verify(messageProducer, times(1)).send(textMessage);
        verify(session, times(1)).close();
        verify(connection, times(1)).close();
        verify(messageProducer, times(1)).close();
    }

    @Test(expected = JmsEnvelopeSenderException.class)
    public void shouldThrowExceptionOnJmsException() throws JMSException {
        doThrow(JMSException.class).when(connection).createSession(false, AUTO_ACKNOWLEDGE);

        jmsEnvelopeSender.send(envelope, destination);
    }

}
