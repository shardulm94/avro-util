/*
 * Copyright 2023 LinkedIn Corp.
 * Licensed under the BSD 2-Clause License (the "License").
 * See License in the project root for license information.
 */

package org.apache.avro.io;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;


public class Avro110BinaryDecoderAccessUtilTest {
  private static final byte[] BYTES = new byte[10];
  private static final int OFFSET = 0;
  private static final int LENGTH = 10;

  @Test
  public void testNewBinaryDecoderReuse() throws Exception {
    BinaryDecoder mockedBinaryDecoder = Mockito.mock(BinaryDecoder.class);
    BinaryDecoder configuredBinaryDecoder = Mockito.mock(BinaryDecoder.class);

    Mockito.when(mockedBinaryDecoder.configure(BYTES, OFFSET, LENGTH)).thenReturn(configuredBinaryDecoder);

    BinaryDecoder returnedBinaryDecoder =
        Avro110BinaryDecoderAccessUtil.newBinaryDecoder(BYTES, OFFSET, LENGTH, mockedBinaryDecoder);

    Mockito.verify(mockedBinaryDecoder, Mockito.times(1)).configure(BYTES, OFFSET, LENGTH);

    Assert.assertEquals(returnedBinaryDecoder, configuredBinaryDecoder,
        "Verify the configured BinaryDecoder is returned");
  }

  @Test
  public void testNewBinaryDecoderNotReuse() throws Exception {
    Map<BinaryDecoder, List<Object>> constructorArgs = new HashMap<>();

    try (MockedConstruction<BinaryDecoder> mockedBinaryDecoder = Mockito.mockConstruction(BinaryDecoder.class,
        (mock, context) -> constructorArgs.put(mock, new ArrayList<>(context.arguments())))) {

      BinaryDecoder returnedBinaryDecoder = Avro110BinaryDecoderAccessUtil.newBinaryDecoder(BYTES, OFFSET, LENGTH, null);

      Assert.assertEquals(mockedBinaryDecoder.constructed().size(), 1, "Verify one BinaryDecoder is created");

      BinaryDecoder createdBinaryDecoder = mockedBinaryDecoder.constructed().get(0);

      Assert.assertEquals(returnedBinaryDecoder, createdBinaryDecoder,
          "Verify the new created BinaryDecoder is returned");

      //Verify parameters are correctly used when creating the new BinaryDecoder
      List<Object> parameters = constructorArgs.get(createdBinaryDecoder);
      Assert.assertEquals(parameters.size(), 3, "Verify the number of parameter is correct");
      Assert.assertEquals(parameters.get(0), BYTES, "Verify first parameter");
      Assert.assertEquals(parameters.get(1), OFFSET, "Verify first parameter");
      Assert.assertEquals(parameters.get(2), LENGTH, "Verify first parameter");
    }
  }

  @Test
  public void testMultiClassloader() throws Exception {

    // Get file path for Avro110BinaryDecoderAccessUtil
    URL helperClasses = Avro110BinaryDecoderAccessUtil.class.getProtectionDomain().getCodeSource().getLocation();
    // Create a new child-first classloader with just this file and the current classloader as parent
    try(URLClassLoader cl = new ChildFirstClassloader(new URL[]{ helperClasses }, Thread.currentThread().getContextClassLoader())) {
      Class<?> clazz = cl.loadClass(Avro110BinaryDecoderAccessUtil.class.getName());
      // Invoke the newBinaryDecoder method
      clazz.getMethod("newBinaryDecoder", byte[].class, int.class, int.class, BinaryDecoder.class)
          .invoke(null, BYTES, OFFSET, LENGTH, null);
    };
  }

  private static class ChildFirstClassloader extends URLClassLoader {

    private final ParentClassLoader parent;

    public ChildFirstClassloader(URL[] urls, ClassLoader parent) {
      super(urls, null);
      this.parent = new ParentClassLoader(parent);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
      try {
        return super.loadClass(name, resolve);
      } catch (ClassNotFoundException cnf) {
        return parent.loadClass(name, resolve);
      }
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
      ArrayList<URL> urls = Collections.list(super.getResources(name));
      urls.addAll(Collections.list(parent.getResources(name)));
      return Collections.enumeration(urls);
    }

    @Override
    public URL getResource(String name) {
      URL url = super.getResource(name);
      if (url != null) {
        return url;
      } else {
        return parent.getResource(name);
      }
    }
  }

  /**
   * A class loader which makes some protected methods in ClassLoader accessible.
   */
  public static class ParentClassLoader extends ClassLoader {

    static {
      ClassLoader.registerAsParallelCapable();
    }

    public ParentClassLoader(ClassLoader parent) {
      super(parent);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
      return super.findClass(name);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
      return super.loadClass(name, resolve);
    }
  }
}

