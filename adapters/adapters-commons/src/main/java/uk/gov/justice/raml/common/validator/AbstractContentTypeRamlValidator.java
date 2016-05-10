package uk.gov.justice.raml.common.validator;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.capitalize;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.Resource;

public abstract class AbstractContentTypeRamlValidator extends AbstractResourceRamlValidator {
    private final Pattern mediaTypePattern;
    private final ActionType actionType;
    private final String contentTypeDec;

    public AbstractContentTypeRamlValidator(ActionType actionType, String contentTypeDec) {
        this.actionType = actionType;
        this.contentTypeDec = contentTypeDec;
        mediaTypePattern = Pattern.compile("application/vnd\\.\\S+\\+json");
    }

    @Override
    protected void validate(final Resource resource) {
        Action postAction = resource.getActions().get(actionType);
        if (postAction != null) {
            Collection<MimeType> mediaTypes = mediaTypesToValidate(postAction);
            checkNonEmpty(mediaTypes);
            checkValid(mediaTypes);
        }
    }

    protected abstract Collection<MimeType> mediaTypesToValidate(Action postAction);

    private void checkValid(final Collection<MimeType> mediaTypes) {
        mediaTypes.forEach(mt -> {
            Matcher matcher = mediaTypePattern.matcher(mt.getType());
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
