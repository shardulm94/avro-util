/*
 * Copyright 2020 LinkedIn Corp.
 * Licensed under the BSD 2-Clause License (the "License").
 * See License in the project root for license information.
 */

package org.apache.avro.io;

import java.lang.invoke.MethodHandle;
import static org.apache.avro.io.AvroBinaryDecoderReflectiveAccessBase.constructor;
import static org.apache.avro.io.AvroBinaryDecoderReflectiveAccessBase.invokeConstructor;
import static org.apache.avro.io.AvroBinaryDecoderReflectiveAccessBase.invokeMethod;
import static org.apache.avro.io.AvroBinaryDecoderReflectiveAccessBase.method;

/**
 * this class exists to allow us access to package-private classes and methods on class {@link BinaryDecoder}
 *
 * the difference between this method and {@link DecoderFactory#binaryDecoder(byte[], int, int, BinaryDecoder)}
 * is that this method supports reusing a custom BinaryDecoder since it does not check class type of BinaryDecoder.
 */
public class Avro110BinaryDecoderAccessUtil {
  private Avro110BinaryDecoderAccessUtil() {
  }

  public static final boolean REQUIRES_REFLECTIVE_ACCESS = Avro110BinaryDecoderAccessUtil.class.getClassLoader() != BinaryDecoder.class.getClassLoader();

  public static BinaryDecoder newBinaryDecoder(byte[] bytes, int offset,
      int length, BinaryDecoder reuse) {
    if (REQUIRES_REFLECTIVE_ACCESS) {
      return newBinaryDecoderReflective(bytes, offset, length, reuse);
    } else {
      return newBinaryDecoderDirect(bytes, offset, length, reuse);
    }
  }

  public static BinaryDecoder newBinaryDecoderDirect(byte[] bytes, int offset,
      int length, BinaryDecoder reuse) {
    if (null == reuse) {
      return new BinaryDecoder(bytes, offset, length);
    } else {
      return reuse.configure(bytes, offset, length);
    }
  }

  private static final MethodHandle NEW_BINARY_DECODER = constructor(byte[].class, int.class, int.class);

  private static final MethodHandle CONFIGURE = method("configure", byte[].class, int.class, int.class);

  public static BinaryDecoder newBinaryDecoderReflective(byte[] bytes, int offset,
      int length, BinaryDecoder reuse) {
    if (null == reuse) {
      return invokeConstructor(NEW_BINARY_DECODER, bytes, offset, length);
    } else {
      return invokeMethod(CONFIGURE, reuse, bytes, offset, length);
    }
  }
}