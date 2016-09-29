/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.metadata.extension;

import static org.mule.test.metadata.extension.MetadataConnection.CAR;
import org.mule.runtime.extension.api.annotation.ParameterGroup;
import org.mule.runtime.extension.api.annotation.Query;
import org.mule.runtime.extension.api.annotation.metadata.Content;
import org.mule.runtime.extension.api.annotation.metadata.MetadataKeyId;
import org.mule.runtime.extension.api.annotation.metadata.MetadataScope;
import org.mule.runtime.extension.api.annotation.metadata.OutputResolver;
import org.mule.runtime.extension.api.annotation.metadata.TypeResolver;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.UseConfig;
import org.mule.runtime.extension.api.introspection.streaming.PagingProvider;
import org.mule.runtime.extension.api.runtime.operation.OperationResult;
import org.mule.tck.message.StringAttributes;
import org.mule.test.metadata.extension.model.animals.Animal;
import org.mule.test.metadata.extension.model.animals.AnimalClade;
import org.mule.test.metadata.extension.model.animals.Bear;
import org.mule.test.metadata.extension.model.attribute.AbstractOutputAttributes;
import org.mule.test.metadata.extension.model.shapes.Rectangle;
import org.mule.test.metadata.extension.model.shapes.Shape;
import org.mule.test.metadata.extension.query.MetadataExtensionEntityResolver;
import org.mule.test.metadata.extension.query.MetadataExtensionQueryTranslator;
import org.mule.test.metadata.extension.query.NativeQueryOutputResolver;
import org.mule.test.metadata.extension.resolver.TestBooleanMetadataResolver;
import org.mule.test.metadata.extension.resolver.TestEnumMetadataResolver;
import org.mule.test.metadata.extension.resolver.TestInputAndOutputResolverWithKeyResolver;
import org.mule.test.metadata.extension.resolver.TestInputAndOutputResolverWithoutKeyResolverAndKeyIdParam;
import org.mule.test.metadata.extension.resolver.TestInputResolver;
import org.mule.test.metadata.extension.resolver.TestInputResolverWithKeyResolver;
import org.mule.test.metadata.extension.resolver.TestInputResolverWithoutKeyResolver;
import org.mule.test.metadata.extension.resolver.TestKeyResolver;
import org.mule.test.metadata.extension.resolver.TestMultiLevelKeyResolver;
import org.mule.test.metadata.extension.resolver.TestOutputAnyTypeResolver;
import org.mule.test.metadata.extension.resolver.TestOutputAttributesResolverWithKeyResolver;
import org.mule.test.metadata.extension.resolver.TestOutputResolverWithKeyResolver;
import org.mule.test.metadata.extension.resolver.TestOutputResolverWithoutKeyResolver;
import org.mule.test.metadata.extension.resolver.TestResolverWithCache;
import org.mule.test.metadata.extension.resolver.TestThreadContextClassLoaderResolver;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@MetadataScope(keysResolver = TestInputAndOutputResolverWithKeyResolver.class,
    inputResolver = TestInputAndOutputResolverWithKeyResolver.class,
    outputResolver = TestInputAndOutputResolverWithKeyResolver.class)
public class MetadataOperations extends MetadataOperationsParent {

  @MetadataScope(keysResolver = TestInputResolverWithKeyResolver.class,
      inputResolver = TestInputResolverWithKeyResolver.class, outputResolver = TestOutputAnyTypeResolver.class)
  public Object contentMetadataWithKeyId(@UseConfig Object object, @Connection MetadataConnection connection,
                                         @MetadataKeyId String type,
                                         @Optional @Content Object content) {
    return null;
  }

  @MetadataScope(keysResolver = TestOutputResolverWithKeyResolver.class, outputResolver = TestOutputResolverWithKeyResolver.class)
  public Object outputMetadataWithKeyId(@Connection MetadataConnection connection, @MetadataKeyId String type,
                                        @Optional @Content Object content) {
    return null;
  }

  @MetadataScope(keysResolver = TestOutputResolverWithKeyResolver.class, outputResolver = TestOutputResolverWithKeyResolver.class)
  public Object metadataKeyWithDefaultValue(@Connection MetadataConnection connection,
                                            @Optional(defaultValue = CAR) @MetadataKeyId String type,
                                            @Optional @Content Object content) {
    return type;
  }

  @MetadataScope(keysResolver = TestInputAndOutputResolverWithKeyResolver.class,
      inputResolver = TestInputAndOutputResolverWithKeyResolver.class,
      outputResolver = TestInputAndOutputResolverWithKeyResolver.class)
  public Object contentAndOutputMetadataWithKeyId(@Connection MetadataConnection connection, @MetadataKeyId String type,
                                                  @Optional @Content Object content) {
    return null;
  }

  @MetadataScope(keysResolver = TestInputAndOutputResolverWithKeyResolver.class,
      inputResolver = TestInputAndOutputResolverWithKeyResolver.class,
      outputResolver = TestInputAndOutputResolverWithKeyResolver.class)
  public Object outputOnlyWithoutContentParam(@Connection MetadataConnection connection, @MetadataKeyId String type) {
    return type;
  }

  @MetadataScope(inputResolver = TestBooleanMetadataResolver.class)
  public boolean booleanMetadataKey(@Connection MetadataConnection connection, @MetadataKeyId boolean type,
                                    @Optional @Content Object content) {
    return type;
  }

  @MetadataScope(inputResolver = TestEnumMetadataResolver.class)
  public AnimalClade enumMetadataKey(@Connection MetadataConnection connection, @MetadataKeyId AnimalClade type,
                                     @Optional @Content Object content) {
    return type;
  }

  @MetadataScope(keysResolver = TestInputAndOutputResolverWithKeyResolver.class,
      inputResolver = TestInputAndOutputResolverWithKeyResolver.class,
      outputResolver = TestInputAndOutputResolverWithKeyResolver.class)
  public void contentOnlyIgnoresOutput(@Connection MetadataConnection connection, @MetadataKeyId String type,
                                       @Optional @Content Object content) {}

  @MetadataScope(inputResolver = TestInputAndOutputResolverWithoutKeyResolverAndKeyIdParam.class,
      outputResolver = TestOutputAnyTypeResolver.class)
  public Object contentMetadataWithoutKeyId(@Connection MetadataConnection connection, @Optional @Content Object content) {
    return null;
  }

  @MetadataScope(outputResolver = TestInputAndOutputResolverWithoutKeyResolverAndKeyIdParam.class)
  public Object outputMetadataWithoutKeyId(@Connection MetadataConnection connection, @Optional @Content Object content) {
    return null;
  }

  @MetadataScope(inputResolver = TestInputAndOutputResolverWithoutKeyResolverAndKeyIdParam.class,
      outputResolver = TestInputAndOutputResolverWithoutKeyResolverAndKeyIdParam.class)
  public Object contentAndOutputMetadataWithoutKeyId(@Connection MetadataConnection connection,
                                                     @Optional @Content Object content) {
    return null;
  }

  @MetadataScope(inputResolver = TestInputResolverWithoutKeyResolver.class)
  public void contentMetadataWithoutKeysWithKeyId(@Connection MetadataConnection connection, @MetadataKeyId String type,
                                                  @Optional @Content Object content) {}

  @MetadataScope(outputResolver = TestOutputResolverWithoutKeyResolver.class)
  public Object outputMetadataWithoutKeysWithKeyId(@Connection MetadataConnection connection, @MetadataKeyId String type) {
    return null;
  }

  @MetadataScope(outputResolver = TestResolverWithCache.class, inputResolver = TestResolverWithCache.class)
  public Object contentAndOutputCacheResolver(@Connection MetadataConnection connection, @MetadataKeyId String type,
                                              @Optional @Content Object content) {
    return null;
  }

  public Object shouldInheritOperationResolvers(@Connection MetadataConnection connection, @MetadataKeyId String type,
                                                @Optional @Content Object content) {
    return null;
  }

  @MetadataScope(inputResolver = TestResolverWithCache.class, outputResolver = TestOutputAnyTypeResolver.class)
  public Object contentOnlyCacheResolver(@Connection MetadataConnection connection, @MetadataKeyId String type,
                                         @Optional @Content Object content) {
    return null;
  }

  @MetadataScope(keysResolver = TestResolverWithCache.class, outputResolver = TestResolverWithCache.class)
  public Object outputAndMetadataKeyCacheResolver(@Connection MetadataConnection connection, @MetadataKeyId String type) {
    return null;
  }

  @MetadataScope(keysResolver = TestMultiLevelKeyResolver.class, inputResolver = TestMultiLevelKeyResolver.class)
  public LocationKey simpleMultiLevelKeyResolver(@Connection MetadataConnection connection,
                                                 @ParameterGroup @MetadataKeyId LocationKey locationKey,
                                                 @Optional @Content Object content) {
    return locationKey;
  }

  @MetadataScope(outputResolver = TestOutputAnyTypeResolver.class)
  public OperationResult messageAttributesNullTypeMetadata() {
    return null;
  }

  @MetadataScope(outputResolver = TestOutputResolverWithoutKeyResolver.class)
  public OperationResult<Object, StringAttributes> messageAttributesPersonTypeMetadata(@MetadataKeyId String type) {
    return null;
  }

  @MetadataScope(keysResolver = TestThreadContextClassLoaderResolver.class)
  public void resolverTypeKeysWithContextClassLoader(@MetadataKeyId String type) {}

  @MetadataScope(inputResolver = TestThreadContextClassLoaderResolver.class)
  public void resolverContentWithContextClassLoader(@Optional @Content Object content, @MetadataKeyId String type) {}

  @MetadataScope(outputResolver = TestThreadContextClassLoaderResolver.class)
  public Object resolverOutputWithContextClassLoader(@MetadataKeyId String type) {
    return null;
  }

  //@MetadataScope(keysResolver = TestOutputAttributesResolverWithKeyResolver.class,
  //    outputResolver = TestOutputAttributesResolverWithKeyResolver.class,
  //    attributesResolver = TestOutputAttributesResolverWithKeyResolver.class)
  @OutputResolver(TestOutputAttributesResolverWithKeyResolver.class)
  public OperationResult<Object, AbstractOutputAttributes> outputAttributesWithDynamicMetadata(
    @MetadataKeyId(TestOutputAttributesResolverWithKeyResolver.class) String type) {
    return null;
  }

  @MetadataScope()
  public boolean typeWithDeclaredSubtypesMetadata(Shape plainShape, Rectangle rectangleSubtype, Animal animal) {
    return false;
  }

  @MetadataScope(inputResolver = TestInputResolverWithoutKeyResolver.class)
  public void contentParameterShouldNotGenerateMapChildElement(@Content Map<String, Object> mapContent) {}

  @MetadataScope(inputResolver = TestInputResolverWithoutKeyResolver.class)
  public void contentParameterShouldNotGenerateListChildElement(@Content List<String> listContent) {}

  @MetadataScope(inputResolver = TestInputResolverWithoutKeyResolver.class)
  public void contentParameterShouldNotGeneratePojoChildElement(@Content Bear animalContent) {}



  @MetadataScope(keysResolver = TestKeyResolver.class)
  public void notContentWithInputMetadata(@MetadataKeyId String key, @TypeResolver(TestInputResolver.class) Bear animal) {}




  @Query(translator = MetadataExtensionQueryTranslator.class,
      entityResolver = MetadataExtensionEntityResolver.class,
      nativeOutputResolver = NativeQueryOutputResolver.class)
  public String doQuery(@MetadataKeyId String query) {
    return query;
  }

  @MetadataScope()
  public PagingProvider<MetadataConnection, Animal> pagedOperationMetadata(Animal animal) {
    return new PagingProvider<MetadataConnection, Animal>() {

      @Override
      public List<Animal> getPage(MetadataConnection connection) {
        return Collections.singletonList(animal);
      }

      @Override
      public java.util.Optional<Integer> getTotalResults(MetadataConnection connection) {
        return java.util.Optional.of(1);
      }

      @Override
      public void close() throws IOException {}
    };
  }

  @MetadataScope()
  public OperationResult<Shape, AbstractOutputAttributes> outputAttributesWithDeclaredSubtypesMetadata() {
    return null;
  }
}
