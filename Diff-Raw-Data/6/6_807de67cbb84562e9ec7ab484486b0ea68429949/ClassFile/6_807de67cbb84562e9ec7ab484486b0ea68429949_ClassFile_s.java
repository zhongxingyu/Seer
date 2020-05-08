 package org.jackie.jclassfile.model;
 
 import org.jackie.jclassfile.constantpool.ConstantPool;
 import org.jackie.jclassfile.constantpool.impl.ClassRef;
 import org.jackie.jclassfile.flags.Flags;
 import org.jackie.jclassfile.util.Helper;
 import org.jackie.utils.Log;
 import static org.jackie.utils.CollectionsHelper.iterable;
 import static org.jackie.utils.CollectionsHelper.sizeof;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Patrik Beno
  */
 public class ClassFile extends Base implements ClassFileProvider {
    /*
 ClassFile {
     	u4 magic;
     	u2 minor_version;
     	u2 major_version;
     	u2 constant_pool_count;
     	cp_info constant_pool[constant_pool_count-1];
     	u2 access_flags;
     	u2 this_class;
     	u2 super_class;
     	u2 interfaces_count;
     	u2 interfaces[interfaces_count];
     	u2 fields_count;
     	field_info fields[fields_count];
     	u2 methods_count;
     	method_info methods[methods_count];
     	u2 attributes_count;
     	attribute_info attributes[attributes_count];
     }
     */
 
    int magic = 0xCAFEBABE;
 	int minor;
 	int major = 50;
 
 	ConstantPool pool;
 
 	Flags flags;
 	ClassRef classname;
 	ClassRef superclass;
 	List<ClassRef> interfaces;
 
 	List<FieldInfo> fields;
 	List<MethodInfo> methods;
 	List<AttributeInfo> attributes;
 
 	{
 		pool = new ConstantPool(this);
 		flags = new Flags();
 	}
 
 	public ClassFile classname(String clsname) {
 		classname = pool().factory().getClassRef(clsname);
 		return this;
 	}
 
 	public String classname() {
 		return classname.value();
 	}
 
 	public ClassFile superclass(String clsname) {
 		superclass = pool().factory().getClassRef(clsname);
 		return this;
 	}
 
 	public Flags flags() {
 		return flags;
 	}
 
 	///
 
 	public ClassFile classFile() {
 		return this;
 	}
 
 	public ConstantPool pool() {
 		return pool;
 	}
 
 	public void load(final DataInput in) throws IOException {
 		Log.enter();
 
 		magic = in.readInt();
 		minor = in.readUnsignedShort();
 		major = in.readUnsignedShort();
 
 		pool = new ConstantPool(this, in);
 
 		flags = new Flags(in);
 		classname = pool.getConstant(in.readUnsignedShort(), ClassRef.class);
 		superclass = pool.getConstant(in.readUnsignedShort(), ClassRef.class);
 
 		// interfaces
 		{
 			int count = in.readUnsignedShort();
 			interfaces = new ArrayList<ClassRef>(count);
 			while (count-- > 0) {
 				ClassRef iface = pool.getConstant(in.readUnsignedShort(), ClassRef.class);
 				interfaces.add(iface);
 			}
 		}
 
 		// fields
 		{
 			int count = in.readUnsignedShort();
 			fields = new ArrayList<FieldInfo>(count);
 			while (count-- > 0) {
 				FieldInfo f = new FieldInfo(this, in);
 				fields.add(f);
 			}
 		}
 
 		// methods
 		{
 			int count = in.readUnsignedShort();
 			methods = new ArrayList<MethodInfo>(count);
 			while (count-- > 0) {
 				MethodInfo m = new MethodInfo(this, in);
 				methods.add(m);
 			}
 		}
 
 		attributes = Helper.loadAttributes(this, in);
 
 		Log.leave();
 	}
 
 	public void save(DataOutput out) throws IOException {
 		Log.enter();
 
 		out.writeInt(magic);
 		out.writeShort(minor);
 		out.writeShort(major);
 
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		DataOutputStream tmpout = new DataOutputStream(baos);
 
 		flags.save(tmpout);
		classname.writeReference(tmpout);
		superclass.writeReference(tmpout);
 
 		// interfaces
 		tmpout.writeShort(sizeof(interfaces));
 		for (ClassRef i : iterable(interfaces)) {
 			i.writeReference(tmpout);
 		}
 
 		save(tmpout, fields);
 		save(tmpout, methods);
 		save(tmpout, attributes);
 
 		pool.save(out);
 
 		tmpout.close();
 		out.write(baos.toByteArray());
 
 		Log.leave();
 	}
 
 	private void save(DataOutput out, List<? extends Base> items) throws IOException {
 		out.writeShort(sizeof(items));
 		for (Base item : iterable(items)) {
 			item.save(out);
 		}
 	}
 
 
 }
