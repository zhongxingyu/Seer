 package de.uxnr.amf.flex.msg;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.List;
 import java.util.Vector;
 
 import de.uxnr.amf.AMF_Context;
 import de.uxnr.amf.AMF_Type;
 import de.uxnr.amf.v0.base.U8;
 import de.uxnr.amf.v3.AMF3_Type;
 import de.uxnr.amf.v3.base.UTF8;
 import de.uxnr.amf.v3.type.Object;
 
 public abstract class AbstractMessage extends Object {
 	private static final UTF8[][] names = new UTF8[][] {
 		{
 			new UTF8("body"),
 			new UTF8("clientId"),
 			new UTF8("destination"),
 			new UTF8("headers"),
 			new UTF8("messageId"),
 			new UTF8("timestamp"),
 			new UTF8("timeToLive"),
 		},
 		{
 			new UTF8("clientId"),
 			new UTF8("messageId"),
 		}
 	};
 
 	@Override
 	public void write(AMF_Context context, DataOutputStream output) throws IOException {
 		this.writeFields(context, output, AbstractMessage.names);
 	}
 
 	@Override
 	public AMF_Type read(AMF_Context context, DataInputStream input) throws IOException {
 		this.readFields(context, input, AbstractMessage.names);
 
 		return this;
 	}
 
 	protected void writeFields(AMF_Context context, DataOutputStream output, UTF8[][] names) throws IOException {
 		List<AMF3_Type> values = new Vector<AMF3_Type>();
 		List<Integer> flags = new Vector<Integer>();
 
 		for (UTF8[] name : names) {
 			int flag = 0;
 
 			for (int index = 0; index < name.length; index++) {
 				AMF3_Type value = this.get(name[index]);
 
 				if (value != null) {
 					values.add(value);
 				}
 			}
 
 			for (int index = name.length - 1; index >= 0; index--) {
 				AMF3_Type value = this.get(name[index]);
 
				flag <<= 1;
 				if (value != null) {
					flag |= 1;
 				}
 			}
 
 			flags.add(flag);
 		}
 
 		if (flags.isEmpty()) {
 			flags.add(0x00);
 		}
 
 		this.writeFlags(context, output, flags);
 
 		for (AMF3_Type value : values) {
 			AMF3_Type.writeType(context, output, value);
 		}
 	}
 
 	protected void readFields(AMF_Context context, DataInputStream input, UTF8[][] names) throws IOException {
 		List<Integer> flags = this.readFlags(context, input);
 
 		int index = 0;
 		for (int flag : flags) {
 			int reserved = 0;
 
 			if (index < names.length) {
 				for (UTF8 name : names[index++]) {
 					if (((flag >> (reserved++)) & 1) == 1) {
 						this.set(name, AMF3_Type.readType(context, input), true);
 					}
 				}
 			}
 		}
 	}
 
 	private void writeFlags(AMF_Context context, DataOutputStream output, List<Integer> flags) throws IOException {
 		U8 ubyte = new U8(0x00);
 		for (int index = 0; index < flags.size(); index++) {
 			if (index == flags.size() - 1) {
 				ubyte = new U8(flags.get(index));
 			} else {
 				ubyte = new U8(flags.get(index) | 0x80);
 			}
 			ubyte.write(context, output);
 		}
 	}
 
 	private List<Integer> readFlags(AMF_Context context, DataInputStream input) throws IOException {
 		List<Integer> flags = new Vector<Integer>();
 		U8 ubyte = new U8(0x80);
 		do {
 			ubyte = new U8(context, input);
 			flags.add(ubyte.get() & ~0x80);
 		} while ((ubyte.get() & 0x80) == 0x80);
 		return flags;
 	}
 }
