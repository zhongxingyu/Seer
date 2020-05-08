 /*
  * Copyright 2013 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package me.cmoz.grizzly.protobuf;
 
 import com.google.protobuf.ExtensionRegistryLite;
 import com.google.protobuf.MessageLite;
 import org.glassfish.grizzly.Buffer;
 import org.glassfish.grizzly.filterchain.AbstractCodecFilter;
 
 import lombok.*;
 
 /**
  * A filter for Google's Protocol Buffers serialization format that uses a fixed
  * length header that stores the size of a message.
  */
 public final class FixedLengthProtobufFilter extends AbstractCodecFilter<Buffer, MessageLite> {
 
     /** The default length of the fixed header. */
     public static final int DEFAULT_HEADER_LENGTH = 4;
 
     /**
     * A Protocol Buffers filter that uses the {@see DEFAULT_HEADER_LENGTH} as
      * the header length containing the size of the message.
      *
      * @param prototype The base protocol buffers serialization unit.
      */
     public FixedLengthProtobufFilter(final @NonNull MessageLite prototype) {
         this(prototype, null, DEFAULT_HEADER_LENGTH);
     }
 
     /**
     * A Protocol Buffers filter that uses the {@see DEFAULT_HEADER_LENGTH} as
      * the header length containing the size of a message with the supplied
      * {@code extensionRegistry}.
      *
      * @param prototype The base protocol buffers serialization unit.
      * @param extensionRegistry A table of known extensions, searchable by name
      *                          or field number, may be {@code null}.
      */
     public FixedLengthProtobufFilter(
             final @NonNull MessageLite prototype,
             final ExtensionRegistryLite extensionRegistry) {
         this(prototype, extensionRegistry, DEFAULT_HEADER_LENGTH);
     }
 
     /**
      * A Protocol Buffers filter that uses the supplied {@code headerLength} as
      * the header length containing the size of a message.
      *
      * @param prototype The base protocol buffers serialization unit.
      * @param headerLength The length of the header storing the size of a
      *                     message.
      */
     public FixedLengthProtobufFilter(
             final @NonNull MessageLite prototype, final int headerLength) {
         this(prototype, null, headerLength);
     }
 
     /**
      * A Protocol Buffers filter that uses the supplied {@code headerLength} as
      * the header length containing the size of a message.
      *
      * @param prototype The base protocol buffers serialization unit.
      * @param extensionRegistry A table of known extensions, searchable by name
      *                          or field number, may be {@code null}.
      * @param headerLength The length of the header storing the size of a
      *                     message.
      */
     public FixedLengthProtobufFilter(
             final @NonNull MessageLite prototype,
             final ExtensionRegistryLite extensionRegistry,
             final int headerLength) {
         super(new FixedLengthProtobufDecoder(prototype, extensionRegistry, headerLength),
                 new FixedLengthProtobufEncoder(headerLength));
     }
 
 }
