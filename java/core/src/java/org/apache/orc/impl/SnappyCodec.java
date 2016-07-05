/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.orc.impl;

import io.airlift.compress.snappy.SnappyCompressor;
import io.airlift.compress.snappy.SnappyDecompressor;

import java.io.IOException;
import java.nio.ByteBuffer;

public class SnappyCodec extends AircompressorCodec
    implements DirectDecompressionCodec{
  private static final HadoopShims SHIMS = HadoopShims.Factory.get();

  Boolean direct = null;

  SnappyCodec() {
    super(new SnappyCompressor(), new SnappyDecompressor());
  }

  @Override
  public void decompress(ByteBuffer in, ByteBuffer out) throws IOException {
    if(in.isDirect() && out.isDirect()) {
      directDecompress(in, out);
      return;
    }
    super.decompress(in, out);
  }

  @Override
  public boolean isAvailable() {
    if (direct == null) {
      try {
        if (SHIMS.getDirectDecompressor(
            HadoopShims.DirectCompressionType.SNAPPY) != null) {
          direct = Boolean.valueOf(true);
        } else {
          direct = Boolean.valueOf(false);
        }
      } catch (UnsatisfiedLinkError ule) {
        direct = Boolean.valueOf(false);
      }
    }
    return direct.booleanValue();
  }

  @Override
  public void directDecompress(ByteBuffer in, ByteBuffer out)
      throws IOException {
    HadoopShims.DirectDecompressor decompressShim =
        SHIMS.getDirectDecompressor(HadoopShims.DirectCompressionType.SNAPPY);
    decompressShim.decompress(in, out);
    out.flip(); // flip for read
  }
}
