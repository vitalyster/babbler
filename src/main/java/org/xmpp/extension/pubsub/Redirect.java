package org.xmpp.extension.pubsub;

import javax.xml.bind.annotation.XmlAttribute;
import java.net.URI;

/**
 * @author Christian Schudt
 */
final class Redirect {
    @XmlAttribute(name = "uri")
    private URI uri;

    public URI getUri() {
        return uri;
    }
}