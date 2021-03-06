 /**
  *
  * Copyright (C) 2009 Global Cloud Specialists, Inc. <info@globalcloudspecialists.com>
  *
  * ====================================================================
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  * ====================================================================
  */
 package org.jclouds.aws.s3.commands;
 
 import static org.testng.Assert.assertEquals;
 
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 
 import org.jclouds.aws.AWSResponseException;
 import org.jclouds.aws.s3.S3IntegrationTest;
 import org.jclouds.aws.s3.domain.S3Object;
 import org.testng.annotations.Test;
 
 /**
  * Tests integrated functionality of all deleteObject commands.
  * <p/>
  * Each test uses a different bucket name, so it should be perfectly fine to run in parallel.
  * 
  * @author Adrian Cole
  */
 @Test(groups = { "integration", "live" }, testName = "s3.DeleteObjectIntegrationTest")
 public class DeleteObjectIntegrationTest extends S3IntegrationTest {
 
    @Test
    void deleteObjectNotFound() throws Exception {
       String bucketName = getBucketName();
       try {
          addObjectToBucket(bucketName, "test");
          assert client.deleteObject(bucketName, "test").get(10, TimeUnit.SECONDS);
       } finally {
          returnBucket(bucketName);
       }
    }
    
    @Test
    void deleteObjectWithSpaces() throws Exception {
       String bucketName = getBucketName();
       try {
          addObjectToBucket(bucketName, "p blic-read-acl");
          assert client.deleteObject(bucketName, "p blic-read-acl").get(10, TimeUnit.SECONDS);
       } finally {
          returnBucket(bucketName);
       }
    }
    
    @Test
    void deleteObjectUnicade() throws Exception {
       String bucketName = getBucketName();
       try {
          addObjectToBucket(bucketName, "pblic-read-acl");
          assert client.deleteObject(bucketName, "pblic-read-acl").get(10, TimeUnit.SECONDS);
       } finally {
          returnBucket(bucketName);
       }
    }
    
    @Test
    void deleteObjectQuestion() throws Exception {
       String bucketName = getBucketName();
       try {
         addObjectToBucket(bucketName, "p???blic-read-acl");
         assert client.deleteObject(bucketName, "p???blic-read-acl").get(10, TimeUnit.SECONDS);
       } finally {
          returnBucket(bucketName);
       }
    }
    
    @Test
    void deleteObjectNoBucket() throws Exception {
       try {
          client.deleteObject("donb", "test").get(10, TimeUnit.SECONDS);
       } catch (ExecutionException e) {
          assert e.getCause() instanceof AWSResponseException;
          assertEquals(((AWSResponseException) e.getCause()).getResponse().getStatusCode(), 404);
       }
    }
 
    @Test
    void deleteObject() throws Exception {
       String bucketName = getBucketName();
       try {
          addObjectToBucket(bucketName, "test");
          assert client.deleteObject(bucketName, "test").get(10, TimeUnit.SECONDS);
          assert client.headObject(bucketName, "test").get(10, TimeUnit.SECONDS) == S3Object.Metadata.NOT_FOUND;
       } finally {
          returnBucket(bucketName);
       }
 
    }
 }
