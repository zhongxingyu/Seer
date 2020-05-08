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
 
 import com.google.protobuf.CodedInputStream;
 import com.google.protobuf.ExtensionRegistryLite;
 import com.google.protobuf.MessageLite;
 import org.glassfish.grizzly.AbstractTransformer;
 import org.glassfish.grizzly.Buffer;
 import org.glassfish.grizzly.TransformationException;
 import org.glassfish.grizzly.TransformationResult;
 import org.glassfish.grizzly.attributes.Attribute;
 import org.glassfish.grizzly.attributes.AttributeStorage;
 import org.glassfish.grizzly.utils.BufferInputStream;
 
 import java.io.IOException;
 
 import lombok.NonNull;
 import lombok.extern.slf4j.Slf4j;
 
 import static org.glassfish.grizzly.TransformationResult.createErrorResult;
 
 /**
  * Decodes Protocol Buffers messages from the input stream using a
  * {@code Varint32} encoded header to determine message size.
  */
 @Slf4j
 public class Varint32ProtobufDecoder extends AbstractTransformer<Buffer, MessageLite> {
 
     /** The error code for a failed protobuf parse of a message. */
     public static final int IO_PROTOBUF_PARSE_ERROR = 0;
     /** The error code for a malformed Varint32 header. */
     public static final int IO_VARINT32_ENCODING_ERROR = 1;
     /** The name of the decoder attribute for the size of the message. */
     public static final String MESSAGE_LENGTH_ATTR =
             "grizzly-protobuf-message-length";
 
     /** The base protocol buffers serialization unit. */
     private final MessageLite prototype;
     /** A table of known extensions, searchable by name or field number. */
     private final ExtensionRegistryLite extensionRegistry;
     /** The attribute for the length of the message. */
     private final Attribute<Integer> messageLengthAttr;
 
     /**
      * A protobuf decoder that uses a {@code Varint32} encoded header to
      * determine the size of a message to be decoded.
      *
      * @param prototype The base protocol buffers serialization unit.
      * @param extensionRegistry A table of known extensions, searchable by name
      *                          or field number, may be {@code null}.
      */
     public Varint32ProtobufDecoder(
             final @NonNull MessageLite prototype,
             final ExtensionRegistryLite extensionRegistry) {
         this.prototype = prototype;
         this.extensionRegistry = extensionRegistry;
         messageLengthAttr = attributeBuilder.createAttribute(MESSAGE_LENGTH_ATTR);
     }
 
     /** {@inheritDoc} */
     @Override
     protected TransformationResult<Buffer, MessageLite> transformImpl(
             final AttributeStorage storage, final @NonNull Buffer input)
             throws TransformationException {
         log.debug("inputRemaining={}", input.remaining());
         final BufferInputStream inputStream = new BufferInputStream(input);
 
         Integer messageLength = messageLengthAttr.get(storage);
         if (messageLength == null) {
             try {
                 messageLength = CodedInputStream.readRawVarint32(input.get(), inputStream);
                 log.debug("messageLength={}", messageLength);
                 messageLengthAttr.set(storage, messageLength);
             } catch (final IOException e) {
                 final String msg = "Error finding varint32 header size.";
                 log.warn(msg, e);
                 return createErrorResult(IO_VARINT32_ENCODING_ERROR, msg);
             }
             log.debug("inputRemaining={}", input.remaining());
         }
 
         if (input.remaining() < messageLength) {
             return TransformationResult.createIncompletedResult(input);
         }
 
         final MessageLite message;
         try {
             final byte[] buf = input.array();
             final int pos = input.position();
 
             if (extensionRegistry != null) {
                 message = prototype.getParserForType()
                         .parseFrom(buf, pos, messageLength, extensionRegistry);
             } else {
                 message = prototype.getParserForType()
                         .parseFrom(buf, pos, messageLength);
             }
        } catch (final IOException e) {
             final String msg = "Error decoding protobuf message from input stream.";
             log.warn(msg, e);
             return createErrorResult(IO_PROTOBUF_PARSE_ERROR, msg);
         }
         log.debug("inputRemaining={}", input.remaining());
 
         return TransformationResult.createCompletedResult(message, input);
     }
 
     /** {@inheritDoc} */
     @Override
     public String getName() {
         return Varint32ProtobufDecoder.class.getName();
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean hasInputRemaining(
             final AttributeStorage storage, final Buffer input) {
         return (input != null) && input.hasRemaining();
     }
 
 }
