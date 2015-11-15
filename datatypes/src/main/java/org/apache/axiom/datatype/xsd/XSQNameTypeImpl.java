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
package org.apache.axiom.datatype.xsd;

import java.text.ParseException;

import javax.xml.namespace.QName;

import org.apache.axiom.datatype.ContextAccessor;
import org.apache.axiom.datatype.UnexpectedCharacterException;
import org.apache.axiom.datatype.UnexpectedEndOfStringException;

final class XSQNameTypeImpl implements XSQNameType {
    public <S,O> QName parse(String literal, ContextAccessor<S,O> contextAccessor, S contextObject, O options)
            throws ParseException {
        int len = literal.length();
        int start = -1;
        int end = -1;
        int colonIndex = -1;
        for (int index = 0; index<len; index++) {
            char c = literal.charAt(index);
            if (Util.isWhitespace(c)) {
                if (start != -1 && end == -1) {
                    end = index;
                }
            } else {
                if (start == -1) {
                    start = index;
                } else if (end != -1) {
                    throw new UnexpectedCharacterException(literal, index);
                }
                // TODO: we should check that the literal is a valid NCName
                if (literal.charAt(index) == ':') {
                    if (colonIndex != -1) {
                        throw new UnexpectedCharacterException(literal, index);
                    }
                    colonIndex = index;
                }
            }
        }
        if (start == -1) {
            throw new UnexpectedEndOfStringException(literal);
        }
        if (end == -1) {
            end = len;
        }
        String prefix;
        String localPart;
        if (colonIndex == -1) {
            prefix = "";
            localPart = literal.toString();
        } else {
            prefix = literal.substring(start, colonIndex);
            localPart = literal.substring(colonIndex+1, end);
        }
        String namespaceURI = contextAccessor.lookupNamespaceURI(contextObject, options, prefix);
        if (namespaceURI == null) {
            throw new ParseException("Unbound namespace prefix \"" + prefix + "\"", 0);
        }
        return new QName(namespaceURI, localPart, prefix);
    }

    public <S,O> String format(QName value, ContextAccessor<S,O> contextAccessor, S contextObject, O options) {
        String prefix = value.getPrefix();
        String namespaceURI = value.getNamespaceURI();
        if (!namespaceURI.equals(contextAccessor.lookupNamespaceURI(contextObject, options, prefix))) {
            String existingPrefix = contextAccessor.lookupPrefix(contextObject, options, namespaceURI);
            if (existingPrefix != null) {
                prefix = existingPrefix;
            } else {
                // TODO
                throw new RuntimeException();
            }
        }
        if (prefix.length() == 0) {
            return value.getLocalPart();
        } else {
            return prefix + ":" + value.getLocalPart();
        }
    }
}
