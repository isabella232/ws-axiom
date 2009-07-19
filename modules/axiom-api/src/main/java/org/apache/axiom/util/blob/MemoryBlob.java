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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.axiom.attachments.impl.BufferUtils;
import org.apache.axiom.ext.io.StreamCopyException;

public class MemoryBlob implements WritableBlob {
    final static int BUFFER_SIZE = BufferUtils.BUFFER_LEN;
    
    class OutputStreamImpl extends BlobOutputStream {
        public WritableBlob getBlob() {
            return MemoryBlob.this;
        }

        public void write(byte[] b, int off, int len) throws IOException {
           int total = 0;
           while (total < len) {
               int copy = Math.min(len-total, BUFFER_SIZE-index);
               System.arraycopy(b, off, currBuffer, index, copy);
               total += copy;
               index += copy;
               off += copy;
               if (index >= BUFFER_SIZE) {
                   addBuffer();
               }
           }
        }

        public void write(byte[] b) throws IOException {
            this.write(b, 0, b.length);
        }
        
        byte[] writeByte = new byte[1];
        public void write(int b) throws IOException {
            writeByte[0] = (byte) b;
            this.write(writeByte, 0, 1);
        }

        public void close() throws IOException {
            outputStream = null;
            committed = true;
        }
    }

    class InputStreamImpl extends InputStream {
        private final int size;
        private int i;
        private int currIndex;
        private int totalIndex;
        private int mark;
        private byte[] currBuffer;
        private byte[] read_byte = new byte[1];
        
        public InputStreamImpl() {
            size = (int)getLength();
            currBuffer = (byte[]) data.get(0);
        }

        public int read() throws IOException {
            int read = read(read_byte);

            if (read < 0) {
                return -1;
            } else {
                return read_byte[0] & 0xFF;
            }
        }

        public int available() throws IOException {
            return size - totalIndex;
        }


        public synchronized void mark(int readlimit) {
            mark = totalIndex;
        }

        public boolean markSupported() {
            return true;
        }

        public int read(byte[] b, int off, int len) throws IOException {
            int total = 0;
            if (totalIndex >= size) {
                return -1;
            }
            while (total < len && totalIndex < size) {
                int copy = Math.min(len - total, BUFFER_SIZE - currIndex);
                copy = Math.min(copy, size - totalIndex);
                System.arraycopy(currBuffer, currIndex, b, off, copy);
                total += copy;
                currIndex += copy;
                totalIndex += copy;
                off += copy;
                if (currIndex >= BUFFER_SIZE) {
                    if (i+1 < data.size()) {
                        currBuffer = (byte[]) data.get(i+1);
                        i++;
                        currIndex = 0;
                    } else {
                        currBuffer = null;
                        currIndex = BUFFER_SIZE;
                    } 
                }
            }
            return total;
        }

        public int read(byte[] b) throws IOException {
            return this.read(b, 0, b.length);
        }

        public synchronized void reset() throws IOException {
            i = mark / BUFFER_SIZE;
            currIndex = mark - (i * BUFFER_SIZE);
            currBuffer = (byte[]) data.get(i);
            totalIndex = mark;
        }
    }
    
    List data; // null here indicates the blob is in state NEW
    int index;
    byte[] currBuffer;
    OutputStreamImpl outputStream;
    boolean committed;
    
    private void init() {
        data = new ArrayList();
        addBuffer();
    }
    
    void addBuffer() {
        currBuffer = new byte[BUFFER_SIZE];
        data.add(currBuffer);
        index = 0;
    }
    
    public long getLength() {
        return (BUFFER_SIZE * (data.size()-1)) + index;
    }

    public BlobOutputStream getOutputStream() {
        if (data != null) {
            throw new IllegalStateException();
        } else {
            init();
            return outputStream = new OutputStreamImpl();
        }
    }

    public long readFrom(InputStream in, long length, boolean commit) throws StreamCopyException {
        if (data == null) {
            init();
        }
        
        if (length == -1) {
            length = Long.MAX_VALUE;
        }
        long bytesReceived = 0;
        
        // Now directly write to the buffers
        boolean done = false;
        while (!done) {
            
            // Don't get more than will fit in the current buffer
            int len = (int) Math.min(BUFFER_SIZE - index, length-bytesReceived);
            
            // Now get the bytes
            int bytesRead;
            try {
                bytesRead = in.read(currBuffer, index, len);
            } catch (IOException ex) {
                throw new StreamCopyException(StreamCopyException.READ, ex);
            }
            if (bytesRead >= 0) {
                bytesReceived += bytesRead;
                index += bytesRead;
                if (index >= BUFFER_SIZE) {
                    addBuffer();
                }
                if (bytesReceived >= length) {
                    done = true;
                }
            } else {
                done = true;
            }
        }
        
        committed = commit;
        
        return bytesReceived;
    }

    public long readFrom(InputStream in, long length) throws StreamCopyException {
        return readFrom(in, length, data == null);
    }

    public InputStream getInputStream() throws IOException {
        if (!committed) {
            throw new IllegalStateException();
        } else {
            return new InputStreamImpl();
        }
    }

    public void writeTo(OutputStream os) throws IOException {
        int size = (int)getLength();
        if (data != null) {
            int numBuffers = data.size();
            for (int j = 0; j < numBuffers-1; j ++) {
                os.write( (byte[]) data.get(j), 0, BUFFER_SIZE);
            }
            if (numBuffers > 0) {
                int writeLimit = size - ((numBuffers-1) * BUFFER_SIZE);
                os.write( (byte[]) data.get(numBuffers-1), 0, writeLimit);
            }
        }
    }
}
