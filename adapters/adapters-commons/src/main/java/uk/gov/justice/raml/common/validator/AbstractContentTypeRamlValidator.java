package uk.gov.justice.raml.common.validator;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static org.apache.commons.lang.StringUtils.capitalize;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.Resource;

public abstract class AbstractContentTypeRamlValidator extends AbstractResourceRamlValidator {
    private final Pattern mediaTypePattern;
    private final ActionType actionType;
    private final String contentTypeDec;

    public AbstractContentTypeRamlValidator(final ActionType actionType, final String contentTypeDec, final String... componentTypes) {
        this.actionType = actionType;
        this.contentTypeDec = contentTypeDec;
        final String pipelineSeparatedComponentTypes = stream(componentTypes).collect(Collectors.joining("|"));
        mediaTypePattern = Pattern
                .compile(format("application/vnd\\.\\S+\\.(%s)\\.\\S+\\+json", pipelineSeparatedComponentTypes));
    }

    @Override
    protected void validate(final Resource resource) {
        final Action postAction = resource.getActions().get(actionType);
        if (postAction != null) {
            final Collection<MimeType> mediaTypes = mediaTypesToValidate(postAction);
            checkNonEmpty(mediaTypes);
            checkValid(mediaTypes);
        }
    }

    protected abstract Collection<MimeType> mediaTypesToValidate(final Action postAction);

    private void checkValid(final Collection<MimeType> mediaTypes) {
        mediaTypes.forEach(mt -> {
            final Matcher matcher = mediaTypePattern.matcher(mt.getType());
            if (!matcher.matches()) {
                throw new RamlValidationException(format("Invalid %s: %s", contentTypeDec, mt.getType()));
            }
        });
    }

    private void checkNonEmpty(final Collection<MimeType> mediaTypes) {
        if (mediaTypes.isEmpty()) {
            throw new RamlValidationException(format("%s not set", capitalize(contentTypeDec)));
        }
    }
}
