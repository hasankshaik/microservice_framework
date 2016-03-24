package uk.gov.justice.services.adapters.test.utils.builder;

import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.adapters.test.utils.builder.ActionBuilder.action;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.raml.model.Raml;
import org.raml.model.Resource;

public class RamlBuilder {
    private final List<ResourceBuilder> resourceBuilders = new ArrayList<>();
    private String version;
    private String title;
    private String baseUri;

    public static RamlBuilder raml() {
        return new RamlBuilder();
    }

    public static RamlBuilder restRamlWithDefaults() {
        return new RamlBuilder()
                .withVersion("#%RAML 0.8")
                .withTitle("Example Service")
                .withBaseUri("http://localhost:8080/warname/command/api/rest/service");
    }

    public RamlBuilder with(final ResourceBuilder resource) {
        resourceBuilders.add(resource);
        return this;
    }

    public RamlBuilder withDefaults() {
        return this.with(resource()
                .withRelativeUri("/somecontext.controller.commands")
                .with(action(POST, "application/vnd.somecontext.commands.command1+json")));
    }

    public RamlBuilder withVersion(final String version) {
        this.version = version;
        return this;
    }

    public RamlBuilder withTitle(final String title) {
        this.title = title;
        return this;
    }

    public RamlBuilder withBaseUri(final String baseUri) {
        this.baseUri = baseUri;
        return this;
    }

    public Raml build() {
        Raml raml = new Raml();

        raml.setBaseUri(baseUri);
        raml.setVersion(version);
        raml.setTitle(title);

        Map<String, Resource> resources = new HashMap<>();
        for (ResourceBuilder resourceBuilder : resourceBuilders) {
            Resource resource = resourceBuilder.build();
            resources.put(resource.getRelativeUri(), resource);
        }

        raml.setResources(resources);
        return raml;
    }
}
