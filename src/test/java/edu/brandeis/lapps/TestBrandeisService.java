package edu.brandeis.lapps;

import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Container;

import java.util.Arrays;
import java.util.Map;

import static edu.brandeis.lapps.BrandeisService.containerJsonScheme;
import static org.junit.Assert.*;
import static org.lappsgrid.discriminator.Discriminators.Uri;

public class TestBrandeisService {

    protected BrandeisService service;

    protected Container wrapContainer(String plainText) {
        Container container = new Container();
        container.setSchema(containerJsonScheme);
        container.setText(plainText);
        container.setLanguage("en");
        return container;
    }

    protected Container reconstructPayload(String json) {
        return new Container(
                (Map) Serializer.parse(json, Data.class).getPayload());
    }

    public void purgeTimestamps(Container...containers) {
        Arrays.stream(containers).forEach(
                cont -> cont.getViews().forEach(
                        view -> view.setTimestamp("")));
    }

    public void testExecuteResult(Container result, boolean wantEyeball) {
        assertNotNull(result);
        for (String expectedAType : service.getMetadataPojo().getProduces().getAnnotations()) {
            String shortenedAType = BrandeisService.shortenAType(expectedAType);
            // assuming a new view added by the execution is always at the rear of views list
            assertTrue("Not containing " + shortenedAType, result.getView(result.getViews().size() - 1).contains(expectedAType));
        }

        if (wantEyeball) {
            System.out.println("<------------------------------------------------------------------------------");
            System.out.println(String.format("      %s         ", this.getClass().getName()));
            System.out.println("-------------------------------------------------------------------------------");
            System.out.println(Serializer.toPrettyJson(result));
            System.out.println("------------------------------------------------------------------------------>");
        }

    }

    public ServiceMetadata testDefaultMetadata() {
        String json = service.getMetadata();
        assertNotNull(service.getClass().getName() + ".getMetadata() returned null", json);

        Data data = Serializer.parse(json, Data.class);
        assertNotNull("Unable to parse metadata json.", data);
        assertEquals(Uri.META, data.getDiscriminator());

        ServiceMetadata metadata = new ServiceMetadata((Map) data.getPayload());

        assertEquals("Vendor is not correct", "http://www.cs.brandeis.edu/", metadata.getVendor());
        assertEquals("Name is not correct", service.getClass().getName(), metadata.getName());
        assertEquals("Version is not correct.", service.getVersion(), metadata.getVersion());
        assertEquals("License is not correct", Uri.APACHE2, metadata.getLicense());

        // for human eyeballing
        System.out.println("<------------------------------------------------------------------------------");
        System.out.println(String.format("      %s         ", this.getClass().getName()));
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println(Serializer.toPrettyJson(metadata));
        System.out.println("------------------------------------------------------------------------------>");

        // return metadata object for additional tests
        return metadata;
    }
}
