 /**
  * Copyright 2013 Bjørn Remseth (la3lma@gmail.com)
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package no.rmz.blobee.rpc.client;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import com.google.protobuf.Descriptors.MethodDescriptor;
 import com.google.protobuf.MessageLite;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import no.rmz.blobeeproto.api.proto.Rpc.MethodSignature;
 
 
 public final class ResolverImpl implements MethodSignatureResolver {
 

     private final Map<MethodSignature, MethodDesc> mmap =
             new ConcurrentHashMap<MethodSignature, MethodDesc>();
 
     public ResolverImpl() {
     }
 
     public MessageLite getPrototypeForParameter(final MethodSignature methodSignature) {
         checkNotNull(methodSignature);
         return mmap.get(methodSignature).getOutputType();
     }
 
     public MessageLite getPrototypeForReturnValue(final MethodSignature methodSignature) {
         checkNotNull(methodSignature);
         return mmap.get(methodSignature).getOutputType();
     }
 
     public void addTypes(
             final MethodDescriptor md,
             final MessageLite inputType,
             final MessageLite outputType) {
         checkNotNull(inputType);
         checkNotNull(outputType);
         checkNotNull(md);
         final MethodSignature ms = getMethodSignatureFromMethodDescriptor(md);
 
         final MethodDesc methodDesc = new MethodDesc(md, inputType, outputType);
         mmap.put(ms, methodDesc);
     }
 
     public static MethodSignature getMethodSignatureFromMethodDescriptor(
             final MethodDescriptor key) {
         checkNotNull(key);
         final MethodSignature signature =
                 MethodSignature.newBuilder()
                 .setInputType(key.getInputType().getFullName().toString())
                 .setMethodName(key.getFullName())
                 .setOutputType(key.getOutputType().getFullName().toString())
                 .build();
         return signature;
     }
 }
