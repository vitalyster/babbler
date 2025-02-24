/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Christian Schudt
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

import java.util.Objects;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * A field reference is used to reference to a field in a data form. The {@link #getName() var attribute} must be equal
 * to the field's {@link rocks.xmpp.extensions.data.model.DataForm.Field#getName() var attribute}.
 *
 * <p>This class is immutable.</p>
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/extensions/xep-0141.html">XEP-0141: Data Forms Layout</a>
 * @see rocks.xmpp.extensions.data.model.DataForm#getFields()
 * @see Page#getFieldReferences()
 */
public final class FieldReference {

    @XmlAttribute(name = "var")
    private final String name;

    private FieldReference() {
        this.name = null;
    }

    /**
     * Creates a field reference.
     *
     * @param name The var attribute, which must match to a field in the data form.
     */
    public FieldReference(String name) {
        this.name = Objects.requireNonNull(name);
    }

    /**
     * Gets the var attribute. This must match to a field in the data form.
     *
     * @return The var attribute.
     */
    public final String getName() {
        return name;
    }

    @Override
    public final boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof FieldReference)) {
            return false;
        }
        FieldReference other = (FieldReference) o;
        return Objects.equals(name, other.name);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(name);
    }
}