/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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

package rocks.xmpp.extensions.data.layout.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The implementation of the {@code <page/>} element in the {@code http://jabber.org/protocol/xdata-layout} namespace.
 * <p>
 * A page is the top-level layout container for data forms. It may contain sections, which partition the page into smaller parts.
 * </p>
 * <p>
 * Each page in a data form contains references to a field in the data form, in order to know which fields should be displayed on this page.
 * </p>
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0141.html">XEP-0141: Data Forms Layout</a>
 * @see <a href="http://xmpp.org/extensions/xep-0141.html#paging">3.1 Paging Fields</a>
 * @see Section
 * @see FieldReference
 * @see rocks.xmpp.extensions.data.model.DataForm#getPages()
 */
@XmlRootElement(name = "page")
public final class Page {

    /**
     * http://jabber.org/protocol/xdata-layout
     */
    public static final String NAMESPACE = "http://jabber.org/protocol/xdata-layout";

    @XmlElement(name = "text")
    private final List<String> text = new ArrayList<>();

    @XmlElement(name = "fieldref")
    private final List<FieldReference> fieldReferences = new ArrayList<>();

    @XmlElement(name = "section")
    private final List<Section> sections = new ArrayList<>();

    @XmlAttribute(name = "label")
    private String label;

    @XmlElement(name = "reportedref")
    private FieldReference reportedReference;

    private Page() {
    }

    /**
     * Creates a page.
     *
     * @param label           The label.
     * @param fieldReferences The field references.
     */
    public Page(String label, Collection<FieldReference> fieldReferences) {
        this(label, fieldReferences, null);
    }

    /**
     * Creates a page.
     *
     * @param label           The label.
     * @param fieldReferences The field references.
     * @param text            The text.
     */
    public Page(String label, Collection<FieldReference> fieldReferences, Collection<String> text) {
        this(label, fieldReferences, text, null);
    }

    /**
     * Creates a page.
     *
     * @param label             The label.
     * @param fieldReferences   The field references.
     * @param text              The text.
     * @param reportedReference The reference to a reported field.
     */
    public Page(String label, Collection<FieldReference> fieldReferences, Collection<String> text, FieldReference reportedReference) {
        this.label = label;
        if (text != null) {
            this.text.addAll(text);
        }
        if (fieldReferences != null) {
            this.fieldReferences.addAll(fieldReferences);
        }
        this.reportedReference = reportedReference;
    }

    /**
     * Gets additional information for the page.
     *
     * @return Additional information.
     */
    public List<String> getText() {
        return Collections.unmodifiableList(text);
    }

    /**
     * Gets the field references. These are the fields, which appear on this page.
     *
     * @return The field references.
     */
    public List<FieldReference> getFieldReferences() {
        return Collections.unmodifiableList(fieldReferences);
    }

    /**
     * Gets the sections for this page.
     *
     * @return The sections.
     */
    public List<Section> getSections() {
        return Collections.unmodifiableList(sections);
    }

    /**
     * Gets the reported field reference.
     *
     * @return The reported field reference.
     */
    public FieldReference getReportedReference() {
        return reportedReference;
    }

    /**
     * Gets the label for this page.
     *
     * @return The label.
     */
    public String getLabel() {
        return label;
    }
}
