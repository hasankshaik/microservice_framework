package uk.gov.justice.services.adapter.rest;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static javax.ws.rs.HttpMethod.GET;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;


public class BasicActionMapper {

    private static final String MEDIA_TYPE_PREFIX = "application/vnd.";

    private Map<String, Map<String, String>> methodToMediatypeAndActionMap = new HashMap<>();

    protected void add(final String methodName, final String mediaType, String actionName) {
        Map<String, String> mediaTypeToActionNameMap = methodToMediatypeAndActionMap.get(methodName);
        if (mediaTypeToActionNameMap == null) {
            mediaTypeToActionNameMap = new HashMap<>();
            methodToMediatypeAndActionMap.put(methodName, mediaTypeToActionNameMap);
        }
        mediaTypeToActionNameMap.put(mediaType, actionName);

    }

    public String actionOf(final String methodName, final String httpMethod, final HttpHeaders headers) {
        final Map<String, String> mediaTypeToActionMap = methodToMediatypeAndActionMap.getOrDefault(methodName, emptyMap());
        return mediaTypeToActionMap.get(mediaTypeOf(httpMethod, headers));
    }

    private String mediaTypeOf(final String httpMethod, final HttpHeaders headers) {
        if (GET.equals(httpMethod)) {
            return headers.getAcceptableMediaTypes().stream()
                    .map(MediaType::toString)
                    .filter(mt -> mt.startsWith(MEDIA_TYPE_PREFIX))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException(format("No matching action for accept media types: %s", headers.getAcceptableMediaTypes())));
        } else {
            return headers.getMediaType().toString();
        }
    }
}
