/*
 * Copyright 2024 LinkedIn Corp.
 * Licensed under the BSD 2-Clause License (the "License").
 * See License in the project root for license information.
 */

package org.apache.avro.io;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

// TODO: Make this utility class
public class AvroBinaryDecoderReflectiveAccessBase {

  private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

  public static MethodHandle constructor(Class<?>... parameterTypes) {
    try {
      Constructor<BinaryDecoder> c = BinaryDecoder.class.getDeclaredConstructor(parameterTypes);
      c.setAccessible(true);
      return LOOKUP.unreflectConstructor(c);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new IllegalStateException(
          String.format(
              "unable to find/access BinaryDecoder constructor with parameters: %s",
              Arrays.toString(parameterTypes)),
          e);
    }
  }

  public static MethodHandle method(String name, Class<?>... parameterTypes) {
    try {
      Method m = BinaryDecoder.class.getDeclaredMethod(name, parameterTypes);
      m.setAccessible(true);
      return LOOKUP.unreflect(m);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new IllegalStateException(
          String.format(
              "unable to find/access BinaryDecoder.%s with parameters: %s",
              name,
              Arrays.toString(parameterTypes)),
          e);
    }
  }

  public static BinaryDecoder invokeConstructor(MethodHandle constructor, Object... args) {
    try {
      return (BinaryDecoder) constructor.invokeWithArguments(args);
    } catch (Throwable e) {
      throw new IllegalStateException("unable to create new BinaryDecoder", e);
    }
  }

  public static <T> T invokeMethod(MethodHandle method, Object... args) {
    try {
      return (T) method.invokeWithArguments(args);
    } catch (Throwable e) {
      throw new IllegalStateException("unable to invoke BinaryDecoder method", e);
    }
  }
}
