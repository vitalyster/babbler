/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Christian Schudt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package rocks.xmpp.nio.codec;

import java.io.Reader;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.xml.XMLConstants;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.fasterxml.aalto.AsyncByteBufferFeeder;
import com.fasterxml.aalto.AsyncXMLInputFactory;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.stax.InputFactoryImpl;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.net.ReaderInterceptor;
import rocks.xmpp.core.net.ReaderInterceptorChain;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.core.stream.model.StreamError;
import rocks.xmpp.core.stream.model.StreamErrorException;
import rocks.xmpp.core.stream.model.StreamHeader;
import rocks.xmpp.core.stream.model.errors.Condition;

/**
 * Decodes a stream of byte buffers to XMPP elements.
 *
 * <p>Decoding is thread-safe, as long as the supplied {@link Unmarshaller} is not shared by another thread, e.g. if a
 * {@linkplain ThreadLocal thread-local} {@link Unmarshaller} is supplied.</p>
 *
 * <p>Stream restarts can be achieved by using the {@link #restart()} methods. Decoding and restarts are thread-safe,
 * i.e. can be called by different threads.</p>
 *
 * @author Christian Schudt
 */
public final class XmppStreamDecoder implements ReaderInterceptor {

    private static final AsyncXMLInputFactory XML_INPUT_FACTORY = new InputFactoryImpl();

    private final Function<Locale, Unmarshaller> unmarshaller;

    private byte[] byteStream = new byte[0];

    private AsyncXMLStreamReader<AsyncByteBufferFeeder> xmlStreamReader;

    private StreamHeader streamHeader;

    private long elementEnd;

    private String element = "";

    private StreamElement streamElement;

    /**
     * Creates the XMPP decoder.
     *
     * <p>Because {@link Unmarshaller} is not thread-safe, it is recommended to pass a {@code
     * ThreadLocal<Unmarshaller>} to this constructor, which ensures thread-safety during unmarshalling.</p>
     *
     * @param unmarshaller Supplies the unmarshaller which will convert XML to objects.
     */
    public XmppStreamDecoder(final Function<Locale, Unmarshaller> unmarshaller) {
        this.unmarshaller = unmarshaller;
        this.restart();
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    /**
     * Decodes a stream of byte buffers to XMPP elements.
     *
     * @param in  The byte buffer which was read from the channel. It must be ready to read, i.e. flipped.
     * @param out Consumes any decoded elements as string and as unmarshalled object.
     * @throws StreamErrorException If parsing XML fails or any other stream error occurred (e.g. invalid XML).
     */
    public final synchronized void decode(final ByteBuffer in, final BiConsumer<String, StreamElement> out)
            throws StreamErrorException {

        // Append the buffer to stream
        byte[] b = new byte[in.remaining()];
        in.get(b);
        byteStream = concat(byteStream, b);

        // Rewind the buffer, so that it can be read again by the XMLStreamReader.
        in.rewind();

        try {

            // Feed the reader with the read bytes.
            xmlStreamReader.getInputFeeder().feedInput(in);
            int type;
            while ((type = xmlStreamReader.next()) != XMLStreamConstants.END_DOCUMENT
                    && type != AsyncXMLStreamReader.EVENT_INCOMPLETE) {

                switch (type) {

                    case XMLStreamConstants.START_ELEMENT:
                        // Only care for the stream header.
                        // Every other start element will be read by JAXB.
                        if (xmlStreamReader.getDepth() == 1) {

                            // Validate namespace URI.
                            final String namespaceUri = xmlStreamReader.getNamespaceURI();
                            if (!StreamHeader.STREAM_NAMESPACE.equals(namespaceUri)) {
                                throw new StreamErrorException(new StreamError(Condition.INVALID_NAMESPACE,
                                        "Invalid stream namespace '" + namespaceUri + "'", Locale.US));
                            }

                            // Validate local name.
                            final String localName = xmlStreamReader.getLocalName();
                            if (!StreamHeader.LOCAL_NAME.equals(localName)) {
                                throw new StreamErrorException(new StreamError(Condition.INVALID_XML,
                                        "Invalid stream element '" + localName + "'", Locale.US));
                            }

                            final String version =
                                    xmlStreamReader.getAttributeValue(XMLConstants.DEFAULT_NS_PREFIX, "version");
                            final String from =
                                    xmlStreamReader.getAttributeValue(XMLConstants.DEFAULT_NS_PREFIX, "from");
                            final String to = xmlStreamReader.getAttributeValue(XMLConstants.DEFAULT_NS_PREFIX, "to");
                            final String id = xmlStreamReader.getAttributeValue(XMLConstants.DEFAULT_NS_PREFIX, "id");
                            final String lang = xmlStreamReader.getAttributeValue(XMLConstants.XML_NS_URI, "lang");
                            final String contentNamespace =
                                    xmlStreamReader.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX);
                            final List<QName> additionalNamespaces = new ArrayList<>();

                            int namespaceCount = xmlStreamReader.getNamespaceCount();
                            if (namespaceCount > 2) {
                                for (int i = 0; i < namespaceCount; i++) {
                                    String namespace = xmlStreamReader.getNamespaceURI(i);
                                    if (!StreamHeader.STREAM_NAMESPACE.equals(namespace) && !Objects
                                            .equals(namespace, contentNamespace)) {
                                        additionalNamespaces
                                                .add(new QName(namespace, "", xmlStreamReader.getNamespacePrefix(i)));
                                    }
                                }
                            }

                            elementEnd = xmlStreamReader.getLocationInfo().getEndingByteOffset();

                            // Store the stream header so that it can be reused while unmarshalling further bytes.
                            final String streamHeaderStr =
                                    new String(byteStream, 0, (int) elementEnd, StandardCharsets.UTF_8);

                            // Copy the rest of the stream.
                            // From now on, only store the XML stream without the stream header.
                            byteStream = Arrays.copyOfRange(byteStream, (int) elementEnd, byteStream.length);

                            streamHeader = StreamHeader.create(
                                    from != null ? Jid.ofEscaped(from) : null,
                                    to != null ? Jid.ofEscaped(to) : null,
                                    id,
                                    version,
                                    lang != null ? Locale.forLanguageTag(lang) : null,
                                    contentNamespace,
                                    additionalNamespaces.toArray(new QName[0]));

                            element = streamHeaderStr;
                            streamElement = streamHeader;

                            out.accept(streamHeaderStr, streamHeader);
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        // Only care for the root element (<stream:stream/>) and first level elements (e.g. stanzas).
                        if (xmlStreamReader.getDepth() < 3) {
                            // A full XML element has been read from the channel.
                            // Now we can unmarshal it.

                            // Get the current end position
                            final long end = xmlStreamReader.getLocationInfo().getEndingByteOffset();
                            // Then determine the element length (offset since the last end element)
                            final int elementLength = (int) (end - elementEnd);
                            // Store the new end position for the next iteration.
                            elementEnd = end;

                            // Get the element from the stream.
                            element = new String(byteStream, 0, elementLength, StandardCharsets.UTF_8);

                            if (xmlStreamReader.getDepth() == 1) {
                                // The client has sent the closing </stream:stream> element.
                                streamElement = StreamHeader.CLOSING_STREAM_TAG;
                                out.accept(element, StreamHeader.CLOSING_STREAM_TAG);
                            } else {

                                //xmlStream.delete(0, element.length());
                                byteStream = Arrays.copyOfRange(byteStream, elementLength, byteStream.length);

                                // Create a partial stream, which always consists of the stream header
                                // (to have namespace declarations) and the current element.
                                // Add one more byte to prevent EOF Exception.
                                String partialStream = streamHeader + element + ' ';
                                XMLStreamReader reader = null;

                                try (Reader stringReader = new StringReader(partialStream)) {
                                    reader = XML_INPUT_FACTORY.createXMLStreamReader(stringReader);
                                    // Move the reader to the stream header (<stream:stream>)
                                    reader.next();
                                    // Move the reader to the next element after the stream header.
                                    int t = reader.next();
                                    // Usually we should be at the next start element now, unless there are characters
                                    // between the elements.
                                    // Make sure, we are at the start element before unmarshalling.
                                    while (reader.hasNext() && t != XMLStreamConstants.START_ELEMENT) {
                                        t = reader.next();
                                    }
                                    streamElement = (StreamElement) unmarshaller.apply(streamHeader.getLanguage())
                                            .unmarshal(reader);
                                    out.accept(element, streamElement);
                                } finally {
                                    if (reader != null) {
                                        reader.close();
                                    }
                                }
                            }
                        }
                        break;
                    case XMLStreamConstants.PROCESSING_INSTRUCTION:
                    case XMLStreamConstants.COMMENT:
                    case XMLStreamConstants.ENTITY_REFERENCE:
                    case XMLStreamConstants.DTD:
                    case XMLStreamConstants.NOTATION_DECLARATION:
                    case XMLStreamConstants.ENTITY_DECLARATION:
                        throw new StreamErrorException(new StreamError(Condition.RESTRICTED_XML));
                    default:
                        break;
                }
            }
        } catch (StreamErrorException e) {
            throw e;
        } catch (XMLStreamException e) {
            throw new StreamErrorException(new StreamError(Condition.NOT_WELL_FORMED), e);
        } catch (Exception e) {
            throw new StreamErrorException(new StreamError(Condition.INTERNAL_SERVER_ERROR), e);
        } finally {
            // Set the new position to the limit, the feeder doesn't do that for us.
            in.position(in.limit());
        }
    }

    /**
     * Restarts the stream, i.e. a new reader will be created.
     */
    public final synchronized void restart() {
        byteStream = new byte[0];
        xmlStreamReader = XML_INPUT_FACTORY.createAsyncForByteBuffer();
        elementEnd = 0;
    }

    @Override
    public final synchronized void process(final Reader reader, final Consumer<StreamElement> streamElementListener,
                              final ReaderInterceptorChain chain) throws Exception {
        char[] chars = new char[element.length()];
        int n = reader.read(chars, 0, element.length());
        if (n > -1) {
            element = "";
            streamElementListener.accept(streamElement);
            chain.proceed(reader, streamElementListener);
        }
    }
}