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
package org.apache.axiom.util.blob;

import static com.google.common.truth.Truth.assertThat;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

public class TestRandomReadWrite extends WritableBlobTestCase {
    private final int size;
    
    public TestRandomReadWrite(WritableBlobFactory factory, int size) {
        super(factory);
        this.size = size;
        addTestParameter("size", size);
    }

    @Override
    protected void runTest(WritableBlob blob) throws Throwable {
        Random random = new Random();
        byte[] data = new byte[size];
        random.nextBytes(data);
        OutputStream out = blob.getOutputStream();
        // Write the test data in chunks with random size
        int offset = 0;
        while (offset < data.length) {
            int c = Math.min(512 + random.nextInt(1024), data.length - offset);
            out.write(data, offset, c);
            offset += c;
        }
        out.close();
        assertThat(blob.getLength()).isEqualTo(size);
        // Reread the test data, again in chunks with random size
        InputStream in = blob.getInputStream();
        offset = 0;
        byte[] data2 = new byte[data.length];
        byte[] buffer = new byte[2048];
        while (true) {
            int bufferOffset = random.nextInt(512);
            int c = 512 + random.nextInt(1024);
            int read = in.read(buffer, bufferOffset, c);
            if (read == -1) {
                break;
            }
            int newOffset = offset + read;
            assertThat(newOffset).isAtMost(data2.length);
            System.arraycopy(buffer, bufferOffset, data2, offset, read);
            offset = newOffset;
        }
        assertThat(offset).isEqualTo(data2.length);
        in.close();
        assertThat(data2).isEqualTo(data);
    }
}
