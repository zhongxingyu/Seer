 package org.thryft.protocol;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import javax.annotation.Nullable;
 
 public final class JsonRpcOutputProtocol extends ForwardingOutputProtocol {
     public JsonRpcOutputProtocol(final OutputProtocol jsonOutputProtocol) {
         this.jsonOutputProtocol = checkNotNull(jsonOutputProtocol);
     }
 
     @Override
     public void writeMessageBegin(final String name, final MessageType type,
             @Nullable final Object id) throws OutputProtocolException {
         writeStructBegin("JSON-RPC");
 
         writeFieldBegin("jsonrpc", org.thryft.protocol.Type.STRING, (short) -1);
         writeString("2.0");
         writeFieldEnd();
 
         if (id == null) {
             writeFieldBegin("id", org.thryft.protocol.Type.VOID_, (short) -1);
             writeNull();
             writeFieldEnd();
         } else if (id instanceof Integer) {
             writeFieldBegin("id", org.thryft.protocol.Type.I32, (short) -1);
             writeI32((Integer) id);
             writeFieldEnd();
         } else if (id instanceof Long) {
             writeFieldBegin("id", org.thryft.protocol.Type.I32, (short) -1);
             writeI64((Long) id);
             writeFieldEnd();
         } else if (id instanceof String) {
             writeFieldBegin("id", org.thryft.protocol.Type.STRING, (short) -1);
             writeString((String) id);
             writeFieldEnd();
         } else {
             throw new IllegalArgumentException(id.toString());
         }
 
         switch (type) {
         case CALL:
         case ONEWAY:
             writeFieldBegin("method", org.thryft.protocol.Type.STRING,
                     (short) -1);
             writeString(name);
             writeFieldEnd();
 
             writeFieldBegin("params", org.thryft.protocol.Type.VOID_,
                     (short) -1);
            writeStructBegin("params");
             break;
 
         case EXCEPTION:
             writeFieldBegin("error", org.thryft.protocol.Type.VOID_, (short) -1);
            writeStructBegin("error");
             break;
 
         case REPLY:
             writeFieldBegin("result", org.thryft.protocol.Type.VOID_,
                     (short) -1);
            writeStructBegin("result");
             break;
 
         default:
             throw new UnsupportedOperationException();
         }
     }
 
     @Override
     public void writeMessageEnd() throws OutputProtocolException {
        writeStructEnd();
         writeFieldEnd();
         writeFieldStop();
         writeStructEnd();
     }
 
     @Override
     protected OutputProtocol _delegate() {
         return jsonOutputProtocol;
     }
 
     private final OutputProtocol jsonOutputProtocol;
 }
