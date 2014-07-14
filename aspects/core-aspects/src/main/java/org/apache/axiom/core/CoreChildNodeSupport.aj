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
package org.apache.axiom.core;

import org.apache.axiom.om.NodeUnavailableException;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXBuilder;

public aspect CoreChildNodeSupport {
    private CoreParentNode CoreChildNode.owner;
    private CoreChildNode CoreChildNode.nextSibling;
    private CoreChildNode CoreChildNode.previousSibling;
    
    /**
     * Check if this node has a parent.
     * 
     * @return <code>true</code> if and only if this node currently has a parent
     */
    public final boolean CoreChildNode.coreHasParent() {
        return getFlag(Flags.HAS_PARENT);
    }
    
    /**
     * Get the parent of this node.
     * 
     * @return the parent of this node or <code>null</code> if this node doesn't have a parent
     */
    public final CoreParentNode CoreChildNode.coreGetParent() {
        return getFlag(Flags.HAS_PARENT) ? owner : null;
    }
    
    public void CoreChildNode.internalSetParent(CoreParentNode parent) {
        if (parent == null) {
            throw new IllegalArgumentException();
        }
        owner = parent;
        setFlag(Flags.HAS_PARENT, true);
    }
    
    public final void CoreChildNode.internalUnsetParent(CoreDocument newOwnerDocument) {
        owner = newOwnerDocument;
        setFlag(Flags.HAS_PARENT, false);
    }
    
    public final CoreDocument CoreChildNode.coreGetOwnerDocument(boolean create) {
        CoreNode root = this;
        while (root.getFlag(Flags.HAS_PARENT)) {
            root = ((CoreChildNode)root).owner;
        }
        if (root instanceof CoreChildNode) {
            CoreChildNode rootChildNode = (CoreChildNode)root;
            CoreDocument document = (CoreDocument)rootChildNode.owner;
            if (document == null && create) {
                document = createOwnerDocument();
                rootChildNode.owner = document;
            }
            return document;
        } else {
            // We get here if the root node is a document or document fragment
            return root.coreGetOwnerDocument(create);
        }
    }
    
    public final void CoreChildNode.coreSetOwnerDocument(CoreDocument document) {
        if (getFlag(Flags.HAS_PARENT)) {
            throw new IllegalStateException();
        }
        owner = document;
    }
    
    public final CoreChildNode CoreChildNode.coreGetNextSiblingIfAvailable() {
        return nextSibling;
    }

    public final void CoreChildNode.coreSetNextSibling(CoreChildNode nextSibling) {
        this.nextSibling = nextSibling;
    }
    
    public final CoreChildNode CoreChildNode.coreGetPreviousSibling() {
        return previousSibling;
    }
    
    public final void CoreChildNode.coreSetPreviousSibling(CoreChildNode previousSibling) {
        this.previousSibling = previousSibling;
    }
    
    public final CoreChildNode CoreChildNode.coreGetNextSibling() throws OMException {
        CoreChildNode nextSibling = coreGetNextSiblingIfAvailable();
        if (nextSibling == null) {
            CoreParentNode parent = coreGetParent();
            if (parent != null && parent.getBuilder() != null) {
                switch (parent.getState()) {
                    case CoreParentNode.DISCARDED:
                        ((StAXBuilder)parent.getBuilder()).debugDiscarded(parent);
                        throw new NodeUnavailableException();
                    case CoreParentNode.INCOMPLETE:
                        do {
                            parent.buildNext();
                        } while (parent.getState() == CoreParentNode.INCOMPLETE
                                && (nextSibling = coreGetNextSiblingIfAvailable()) == null);
                }
            }
        }
        return nextSibling;
    }
}
