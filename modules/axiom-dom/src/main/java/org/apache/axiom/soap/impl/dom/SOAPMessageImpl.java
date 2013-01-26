/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axiom.soap.impl.dom;

import org.apache.axiom.om.OMCloneOptions;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.common.StAXSerializer;
import org.apache.axiom.om.impl.dom.DocumentImpl;
import org.apache.axiom.om.impl.dom.NodeImpl;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPMessage;
import org.apache.axiom.soap.SOAPProcessingException;

import javax.xml.stream.XMLStreamException;

public class SOAPMessageImpl extends DocumentImpl implements SOAPMessage {

    public SOAPMessageImpl(SOAPFactory factory) {
        super(factory);
    }

    public SOAPMessageImpl(OMXMLParserWrapper parserWrapper, SOAPFactory factory) {
        super(parserWrapper, factory);
    }


    public SOAPEnvelope getSOAPEnvelope() throws SOAPProcessingException {
        return (SOAPEnvelope) getOMDocumentElement();
    }

    public void setSOAPEnvelope(SOAPEnvelope envelope) {
        setOMDocumentElement(envelope);
    }

    protected void checkDocumentElement(OMElement element) {
        if (!(element instanceof SOAPEnvelope)) {
            throw new OMException("Child not allowed; must be a SOAPEnvelope");
        }
    }

    protected void internalSerialize(StAXSerializer serializer, boolean cache,
                                     boolean includeXMLDeclaration) throws XMLStreamException {
        ((NodeImpl)getOwnerDocument().getDocumentElement()).internalSerialize(serializer, cache);
    }

    protected DocumentImpl createClone(OMCloneOptions options) {
        return new SOAPMessageImpl((SOAPFactory)getOMFactory());
    }
}
