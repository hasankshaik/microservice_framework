package uk.gov.justice.services.adapters.test.utils.builder;

import static java.text.MessageFormat.format;

/**
 * Builds RAML snippet defining mapping between action and media type
 */
public class MappingBuilder {

    private static final String MAPPING_TEMPLATE =
            "(mapping):\n" +
            "    {0}: {1}\n" +
            "    name: {2}\n";

    private String requestType;
    private String responseType;
    private String name;

    public static MappingBuilder mapping() {
        return new MappingBuilder();
    }

    public static MappingBuilder defaultMapping() {
        return new MappingBuilder()
                .withRequestType("application/vnd.blah+json")
                .withName("name1");
    }

    public MappingBuilder withRequestType(final String requestType) {
        this.requestType = requestType;
        return this;
    }

    public MappingBuilder withResponseType(final String responseType) {
        this.responseType = responseType;
        return this;
    }


    public MappingBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public String build() {
        return format(MAPPING_TEMPLATE, requestType != null ? "requestType" : "responseType",
                requestType != null ? requestType : responseType, name);
    }
}
