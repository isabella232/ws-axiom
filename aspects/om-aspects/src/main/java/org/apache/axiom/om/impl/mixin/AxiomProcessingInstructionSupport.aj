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
package org.apache.axiom.om.impl.mixin;

import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.impl.common.AxiomSemantics;
import org.apache.axiom.om.impl.common.serializer.push.Serializer;
import org.apache.axiom.om.impl.intf.AxiomProcessingInstruction;
import org.apache.axiom.om.impl.stream.StreamException;

public aspect AxiomProcessingInstructionSupport {
    public final int AxiomProcessingInstruction.getType() {
        return PI_NODE;
    }

    public final void AxiomProcessingInstruction.setTarget(String target) {
        coreSetTarget(target);
    }

    public final String AxiomProcessingInstruction.getValue() {
        return coreGetCharacterData().toString();
    }

    public final void AxiomProcessingInstruction.setValue(String value) {
        coreSetCharacterData(value, AxiomSemantics.INSTANCE);
    }
    
    public final void AxiomProcessingInstruction.internalSerialize(Serializer serializer, OMOutputFormat format, boolean cache) throws StreamException {
        serializer.processProcessingInstruction(coreGetTarget() + " ", coreGetCharacterData().toString());
    }
    
    public final void AxiomProcessingInstruction.buildWithAttachments() {
    }
}
