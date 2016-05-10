package uk.gov.justice.raml.common.mapper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.POST;

import java.util.List;

import org.junit.Test;

public class ActionMappingTest {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    @Test
    public void shouldCreateSingleMappingWithRequestTypeFromString() throws Exception {

        List<ActionMapping> mappings = ActionMapping.listOf("..." + LINE_SEPARATOR +
                "   (mapping):" + LINE_SEPARATOR +
                "      requestType: appication/vnd.aaaa+json" + LINE_SEPARATOR +
                "      name: actionA" + LINE_SEPARATOR +
                "..." + LINE_SEPARATOR);
        assertThat(mappings, hasSize(1));
        ActionMapping mapping = mappings.get(0);
        assertThat(mapping.getRequestType(), is("appication/vnd.aaaa+json"));
        assertThat(mapping.mimeTypeFor(POST), is("appication/vnd.aaaa+json"));
        assertThat(mapping.getName(), is("actionA"));
    }

    @Test
    public void shouldCreateSingleMappingWithResponseTypeFromString() throws Exception {

        List<ActionMapping> mappings = ActionMapping.listOf("..." + LINE_SEPARATOR +
                "   (mapping):" + LINE_SEPARATOR +
                "      responseType: appication/vnd.bbbb+json" + LINE_SEPARATOR +
                "      name: actionBCD" + LINE_SEPARATOR +
                "..." + LINE_SEPARATOR);

        assertThat(mappings, hasSize(1));
        ActionMapping mapping = mappings.get(0);
        assertThat(mapping.getResponseType(), is("appication/vnd.bbbb+json"));
        assertThat(mapping.mimeTypeFor(GET), is("appication/vnd.bbbb+json"));
        assertThat(mapping.getName(), is("actionBCD"));
    }

    @Test
    public void shouldCreateMappingsCollections() throws Exception {

        List<ActionMapping> mappings = ActionMapping.listOf("..." + LINE_SEPARATOR +
                "   (mapping):" + LINE_SEPARATOR +
                "      requestType: appication/vnd.aaaa+json" + LINE_SEPARATOR +
                "      name: actionA" + LINE_SEPARATOR +
                "   (mapping):" + LINE_SEPARATOR +
                "      requestType: appication/vnd.bbbbb+json" + LINE_SEPARATOR +
                "      name: actionB" + LINE_SEPARATOR +
                "..." + LINE_SEPARATOR);
        assertThat(mappings, hasSize(2));
        assertThat(mappings, hasItems(allOf(hasProperty("requestType", equalTo("appication/vnd.aaaa+json")), hasProperty("name", equalTo("actionA"))),
                allOf(hasProperty("requestType", equalTo("appication/vnd.bbbbb+json")), hasProperty("name", equalTo("actionB")))));
    }


}
