package uk.gov.justice.raml.common.mapper;

import static com.google.common.base.CharMatcher.WHITESPACE;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.raml.model.ActionType.POST;

import uk.gov.justice.raml.common.validator.RamlValidationException;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.raml.model.ActionType;

/**
 * Mapping between media types and framework actions
 */
public class ActionMapping {

    private static final String MAPPING_BOUNDARY = "...";
    private static final String MAPPING_SEPARATOR = "\\(mapping\\):";
    private static final String REQUEST_TYPE_KEY = "requestType";
    private static final String RESPONSE_TYPE_KEY = "responseType";
    private static final String NAME_KEY = "name";
    private static final String INVALID_ACTION_MAPPING_ERROR_MSG = "Invalid action mapping in RAML file";
    private static final String ACTION_NAME_NOT_SET_ERROR_MSG = "Invalid RAML file. Action name not defined in mapping";
    private static final String MEDIA_TYPE_NAME_NOT_SET_ERROR_MSG = "Invalid RAML file. Media type not defined in mapping";
    private final String requestType;
    private final String responseType;
    private final String name;

    /**
     * Parses mappings string
     * @param mappingsString - mapping string from raml file:
     * <pre>
     * {@code    ...
     *  (mapping):
     *     requestType: application/vnd.people.command.create-user+json
     *     name: people.create-user
     *  (mapping):
     *     requestType: application/vnd.people.command.update-user+json
     *     name: people.update-user
     * ...
     * }
     * </pre>
     *
     * @return - collection of {@link ActionMapping} objects
     */
    public static List<ActionMapping> listOf(final String mappingsString) {
        List<ActionMapping> actionMappings = actionMappingsOf(trim(mappingsString));
        validate(actionMappings);
        return actionMappings;
    }

    private static List<ActionMapping> actionMappingsOf(final String mappingString) {
        if (mappingString != null && mappingString.startsWith(MAPPING_BOUNDARY)
                && mappingString.endsWith(MAPPING_BOUNDARY)) {
            return stream(removeStart(removeEnd(mappingString, MAPPING_BOUNDARY), MAPPING_BOUNDARY)
                    .split(MAPPING_SEPARATOR))
                    .filter(StringUtils::isNotBlank)
                    .map(ActionMapping::valueOf)
                    .collect(toList());

        }
        return emptyList();
    }

    private static ActionMapping valueOf(final String mappingString) {
        final Map<String, String> map = Splitter.on("\n")
                .omitEmptyStrings()
                .trimResults(WHITESPACE)
                .withKeyValueSeparator(": ").split(mappingString);
        return new ActionMapping(map.get(REQUEST_TYPE_KEY), map.get(RESPONSE_TYPE_KEY), map.get(NAME_KEY));
    }

    public ActionMapping(final String requestType, final String responseType, final String name) {
        this.requestType = requestType;
        this.responseType = responseType;
        this.name = name;
    }

    private static void validate(final List<ActionMapping> actionMappings) {
        if (isEmpty(actionMappings)) {
            throw new RamlValidationException(INVALID_ACTION_MAPPING_ERROR_MSG);
        }
        validateNot(actionMappings, m -> isBlank(m.getName()), ACTION_NAME_NOT_SET_ERROR_MSG);
        validateNot(actionMappings, m -> isBlank(m.getRequestType()) && isBlank(m.getResponseType()),
                MEDIA_TYPE_NAME_NOT_SET_ERROR_MSG);
    }

    private static void validateNot(final List<ActionMapping> actionMappings,
                                    final Predicate<ActionMapping> actionMappingPredicate,
                                    final String errorMessage) {
        if (actionMappings.stream().anyMatch(actionMappingPredicate)) {
            throw new RamlValidationException(errorMessage);
        }
    }

    public String getRequestType() {
        return requestType;
    }

    public String getName() {
        return name;
    }

    public String getResponseType() {
        return responseType;
    }

    public String mimeTypeFor(final ActionType httpMethod) {
        return POST.equals(httpMethod) ? getRequestType() : getResponseType();
    }
}