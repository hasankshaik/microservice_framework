package uk.gov.justice.services.eventsourcing.repository.core;


import uk.gov.justice.services.eventsourcing.repository.core.exception.StoreEventRequestFailedException;
import uk.gov.justice.services.messaging.Envelope;

import java.util.UUID;
import java.util.stream.Stream;

import javax.transaction.Transactional;

/**
 * Service to store and read event streams.
 */
public interface EventRepository {

    /**
     * Get a stream of envelopes, ordered by sequence id.
     *
     * @param streamId the id of the stream to retrieve
     * @return the stream of envelopes. Never returns null.
     */
    Stream<Envelope> getByStreamId(final UUID streamId);

    /**
     * Get a stream of envelopes from a given version, ordered by sequence id.
     *
     * @param streamId   the id of the stream to retrieve
     * @param sequenceId the sequence id to read the stream from
     * @return the stream of envelopes. Never returns null.
     */
    Stream<Envelope> getByStreamIdAndSequenceId(final UUID streamId, final Long sequenceId);

    /**
     * Stores the given envelope into the event stream.
     *
     * @param envelope the envelope containing the event and the metadata.
     * @param streamId the stream id the event needs to be stored in.
     * @param version  the version at which the event is to be stored
     * @throws StoreEventRequestFailedException If there was a failure in storing the events, this
     *                                          will wrap the underlying cause.
     */
    @Transactional
    void store(final Envelope envelope, final UUID streamId, final Long version) throws StoreEventRequestFailedException;

    /**
     * Returns the latest sequence Id for the given stream id.
     *
     * @param streamId id of the stream.
     * @return latest sequence id for the stream.  Returns 0 if stream doesn't exist. Never returns
     * null.
     */
    Long getCurrentSequenceIdForStream(final UUID streamId);

}
