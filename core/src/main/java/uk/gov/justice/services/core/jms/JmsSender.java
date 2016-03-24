package uk.gov.justice.services.core.jms;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.context.ContextName;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;

import java.util.Objects;

import javax.enterprise.inject.Alternative;

@Alternative
public class JmsSender implements Sender {

    private final JmsDestinations jmsDestinations;
    private final Component destinationComponent;
    private JmsEnvelopeSender jmsEnvelopeSender;

    public JmsSender(final Component destinationComponent, final JmsDestinations jmsDestinations,
                     JmsEnvelopeSender jmsEnvelopeSender) {
        this.destinationComponent = destinationComponent;
        this.jmsDestinations = jmsDestinations;
        this.jmsEnvelopeSender = jmsEnvelopeSender;
    }

    @Override
    public void send(final Envelope envelope) {
        final String contextName = ContextName.fromName(envelope.metadata().name());
        jmsEnvelopeSender.send(envelope, jmsDestinations.getDestination(destinationComponent, contextName));
    }

    @Override
    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S1067", "squid:S00122"})
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JmsSender jmsSender = (JmsSender) o;
        return destinationComponent == jmsSender.destinationComponent;
    }

    @Override
    public int hashCode() {
        return Objects.hash(destinationComponent);
    }

}
