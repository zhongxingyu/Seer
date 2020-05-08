 /* Copyright (c) 2006-2007 Jan S. Rellermeyer
  * Information and Communication Systems Research Group (IKS),
  * Institute for Pervasive Computing, ETH Zurich.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of ETH Zurich nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package ch.ethz.iks.r_osgi.impl;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Map;
 import java.util.jar.Attributes;
 import java.util.jar.JarEntry;
 import java.util.jar.JarOutputStream;
 import java.util.jar.Manifest;
 import java.util.zip.CRC32;
 import org.objectweb.asm.AnnotationVisitor;
 import org.objectweb.asm.Attribute;
 import org.objectweb.asm.ClassReader;
 import org.objectweb.asm.ClassWriter;
 import org.objectweb.asm.ClassVisitor;
 import org.objectweb.asm.FieldVisitor;
 import org.objectweb.asm.Label;
 import org.objectweb.asm.MethodVisitor;
 import org.objectweb.asm.Opcodes;
 import org.objectweb.asm.Type;
 import org.osgi.service.log.LogService;
 
 import ch.ethz.iks.r_osgi.ChannelEndpoint;
 import ch.ethz.iks.r_osgi.RemoteOSGiService;
 import ch.ethz.iks.r_osgi.Remoting;
 import ch.ethz.iks.r_osgi.ServiceUIComponent;
 import ch.ethz.iks.slp.ServiceURL;
 
 /**
  * Bytecode manipulation magic to build proxy bundles for interfaces and smart
  * proxy (abstract) classes.
  * 
  * @author Jan S. Rellermeyer, ETH Zurich
  * @since 0.1
  */
 class ProxyGenerator implements ClassVisitor, Opcodes {
 	/**
 	 * sourceID.
 	 */
 	private String sourceID;
 
 	/**
 	 * interface class name.
 	 */
 	private String interfaceClassName;
 
 	/**
 	 * smart proxy class name.
 	 */
 	private String smartProxyClassName;
 
 	/**
 	 * name of the implemented class.
 	 */
 	private String implName;
 
 	/**
 	 * the service url.
 	 */
 	private ServiceURL serviceURL;
 
 	/**
 	 * the ASM class writer.
 	 */
 	private ClassWriter writer;
 
 	/**
 	 * the list of injections.
 	 */
 	private Map injections;
 
 	/**
 	 * the constants.
 	 */
 	private static final int[] ICONST = { ICONST_0, ICONST_1, ICONST_2,
 			ICONST_3, ICONST_4, ICONST_5 };
 
 	/**
 	 * the boxed types.
 	 */
 	private static final String[] BOXED_TYPE = { "ERROR", "java/lang/Boolean",
 			"java/lang/Character", "java/lang/Byte", "java/lang/Short",
 			"java/lang/Integer", "java/lang/Float", "java/lang/Long",
 			"java/lang/Double" };
 
 	/**
 	 * the unbox methods.
 	 */
 	private static final String[] UNBOX_METHOD = { "ERROR", "booleanValue",
 			"charValue", "byteValue", "shortValue", "intValue", "floatValue",
 			"longValue", "doubleValue" };
 
 	/**
 	 * the character table for generating a signature from an IP address.
 	 */
 	private static final char[] CHAR_TABLE = new char[] { 'a', 'b', 'c', 'd',
 			'e', 'f', 'g', 'h', 'i', 'j' };
 
 	/**
 	 * remoting interface name.
 	 */
 	private static final String REMOTING_I = Remoting.class.getName().replace(
 			'.', '/');
 
 	/**
 	 * channel endpoint interface name.
 	 */
 	private static final String ENDPOINT_I = ChannelEndpoint.class.getName()
 			.replace('.', '/');
 
 	/**
 	 * ServiceUIComponent interface name.
 	 */
 	private static final String UICOMP_I = ServiceUIComponent.class.getName()
 			.replace('.', '/');
 
 	/**
 	 * constructor.
 	 */
 	ProxyGenerator() {
 
 	}
 
 	/**
 	 * 
 	 * @param service
 	 *            ServiceURL
 	 * @param deliv
 	 *            DeliverServiceMessage
 	 * @return bundle location
 	 * @throws IOException
 	 *             in case of proxy generation error
 	 */
 	protected String generateProxyBundle(final ServiceURL service,
 			final DeliverServiceMessage deliv) throws IOException {
 		serviceURL = service;
 		sourceID = generateSourceID(service.getHost());
 
 		injections = deliv.getInjections();
 
 		byte[] bytes = deliv.getProxyName() == null ? generateProxyClass(deliv
 				.getInterfaceName(), deliv.getInterfaceClass())
 				: generateProxyClass(deliv.getInterfaceName(), deliv
 						.getInterfaceClass(), deliv.getProxyName(), deliv
 						.getProxyClass());
 
 		int pos = implName.lastIndexOf('/');
 		String fileName = pos > 0 ? implName.substring(pos) : implName;
 		String className = implName.replace('/', '.');
 		JarEntry jarEntry;
 
 		// generate Jar
 		Manifest mf = new Manifest();
 		Attributes attr = mf.getMainAttributes();
 		final String imports = deliv.getImports();
 		attr.putValue("Manifest-Version", "1.0");
 		attr.putValue("Created-By", "R-OSGi Proxy Generator");
 		attr.putValue("Bundle-Activator", className);
 		attr.putValue("Bundle-Classpath", ".");
 		attr.putValue("Import-Package",
 				"".equals(imports) ? "ch.ethz.iks.r_osgi"
 						: "ch.ethz.iks.r_osgi, " + imports);
 		if (!"".equals(deliv.getExports())) {
 			attr.putValue("Export-Package", deliv.getExports());
 		}
 		File file = RemoteOSGiServiceImpl.context.getDataFile(fileName + "_"
 				+ sourceID + ".jar");
 		JarOutputStream out = new JarOutputStream(new FileOutputStream(file),
 				mf);
 
 		CRC32 crc = new CRC32();
 		crc.update(bytes, 0, bytes.length);
 		jarEntry = new JarEntry(implName + ".class");
 		jarEntry.setSize(bytes.length);
 		jarEntry.setCrc(crc.getValue());
 
 		out.putNextEntry(jarEntry);
 		out.write(bytes, 0, bytes.length);
 		out.flush();
 		out.closeEntry();
 
 		final String[] injectionNames = (String[]) injections.keySet().toArray(
 				new String[injections.size()]);
 		// write the class injections
 		for (int i = 0; i < injectionNames.length; i++) {
 
 			final String name = injectionNames[i];
 			final byte[] data = (byte[]) injections.get(name);
 
 			crc = new CRC32();
 			crc.update(data, 0, data.length);
 			jarEntry = new JarEntry(name);
 			jarEntry.setSize(data.length);
 			jarEntry.setCrc(crc.getValue());
 
 			out.putNextEntry(jarEntry);
 			out.write(data, 0, data.length);
 			out.flush();
 			out.closeEntry();
 		}
 
 		out.flush();
 		out.finish();
 		out.close();
 		if (RemoteOSGiServiceImpl.PROXY_DEBUG) {
 			RemoteOSGiServiceImpl.log.log(LogService.LOG_DEBUG,
 					"Created Proxy Bundle " + file);
 		}
 
 		System.err.println("Proxy Bundle location: " + file.getAbsolutePath());
 
 		return file.getAbsolutePath();
 	}
 
 	/**
 	 * 
 	 * @param interfaceName
 	 *            interface name
 	 * @param interfaceClass
 	 *            interface class
 	 * @return bytes of the generated proxy class
 	 * @throws IOException
 	 *             in case of generation error
 	 */
 	private byte[] generateProxyClass(final String interfaceName,
 			final byte[] interfaceClass) throws IOException {
 		interfaceClassName = interfaceName;
 		try {
 			final ClassReader reader = new ClassReader(interfaceClass);
 			writer = new ClassWriter(true);
 			reader.accept(this, null, false);
 			interfaceClassName = null;
 			final byte[] bytes = writer.toByteArray();
 			return bytes;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	/**
 	 * 
 	 * @param interfaceName
 	 *            interface name
 	 * @param interfaceClass
 	 *            interface class
 	 * @param proxyName
 	 *            smart proxy name
 	 * @param proxyClass
 	 *            smart proxy class
 	 * @return bytes of the proxy class
 	 * @throws IOException
 	 *             in case of generation error
 	 */
 	private byte[] generateProxyClass(final String interfaceName,
 			final byte[] interfaceClass, final String proxyName,
 			final byte[] proxyClass) throws IOException {
 		interfaceClassName = interfaceName;
 		smartProxyClassName = proxyName;
 		ClassReader reader = new ClassReader(proxyClass);
 		writer = new ClassWriter(false);
 		reader.accept(this, null, false);
 		interfaceClassName = null;
 		byte[] bytes = writer.toByteArray();
 		return bytes;
 	}
 
 	/**
 	 * @param version
 	 *            version
 	 * @param access
 	 *            access
 	 * @param name
 	 *            name
 	 * @param signature
 	 *            signature
 	 * @param superName
 	 *            superName
 	 * @param interfaces
 	 *            interfaces
 	 * @see org.objectweb.asm.ClassVisitor#visit(int, int, java.lang.String,
 	 *      java.lang.String, java.lang.String, java.lang.String[])
 	 */
 	public void visit(final int version, final int access, final String name,
 			final String signature, final String superName,
 			final String[] interfaces) {
 		MethodVisitor method;
 		FieldVisitor field;
 		implName = "proxy/" + sourceID + "/" + name + "Impl";
 
 		if (RemoteOSGiServiceImpl.PROXY_DEBUG) {
 			RemoteOSGiServiceImpl.log.log(LogService.LOG_DEBUG,
					"creating proxy class " + implName);
 		}
 
 		if ((access & ACC_INTERFACE) == 0) {
 			writer.visit(V1_1, ACC_PUBLIC + ACC_SUPER, implName, null,
 					"java/lang/Object", new String[] {
 							interfaceClassName.replace('.', '/'),
 							"org/osgi/framework/BundleActivator" });
 		} else {
 			writer.visit(V1_1, ACC_PUBLIC + ACC_SUPER, implName, null,
 					"java/lang/Object", new String[] {
 							interfaceClassName.replace('.', '/'),
 							"org/osgi/framework/BundleActivator" });
 			if (RemoteOSGiServiceImpl.PROXY_DEBUG) {
 				RemoteOSGiServiceImpl.log.log(LogService.LOG_DEBUG,
 						"Creating Proxy Bundle from Interface "
 								+ interfaceClassName);
 			}
 
 			// creates a MethodWriter for the (implicit) constructor
 			method = writer
 					.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
 			method.visitVarInsn(ALOAD, 0);
 			method.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>",
 					"()V");
 			method.visitInsn(RETURN);
 			method.visitMaxs(2, 1);
 			method.visitEnd();
 
 		}
 
 		field = writer.visitField(ACC_PRIVATE, "endpoint",
 				"Lch/ethz/iks/r_osgi/ChannelEndpoint;", null, null);
 		field.visitEnd();
 
 		{
 			// TODO: factor this out, it is needed in some other methods as
 			// well.
 			final String url = serviceURL.toString();
 
 			method = writer.visitMethod(ACC_PUBLIC, "start",
 					"(Lorg/osgi/framework/BundleContext;)V", null,
 					new String[] { "java/lang/Exception" });
 			method.visitCode();
 			method.visitVarInsn(ALOAD, 1);
 			method.visitVarInsn(ALOAD, 1);
 			method.visitLdcInsn(Remoting.class.getName());
 			method
 					.visitMethodInsn(INVOKEINTERFACE,
 							"org/osgi/framework/BundleContext",
 							"getServiceReference",
 							"(Ljava/lang/String;)Lorg/osgi/framework/ServiceReference;");
 			method
 					.visitMethodInsn(INVOKEINTERFACE,
 							"org/osgi/framework/BundleContext", "getService",
 							"(Lorg/osgi/framework/ServiceReference;)Ljava/lang/Object;");
 			method.visitTypeInsn(CHECKCAST, REMOTING_I);
 			method.visitVarInsn(ASTORE, 2);
 			method.visitVarInsn(ALOAD, 0);
 			method.visitVarInsn(ALOAD, 2);
 			method.visitLdcInsn(url);
 			method.visitMethodInsn(INVOKEINTERFACE, REMOTING_I, "getEndpoint",
 					"(Ljava/lang/String;)L" + ENDPOINT_I + ";");
 			method.visitFieldInsn(PUTFIELD, implName, "endpoint", "L"
 					+ ENDPOINT_I + ";");
			method.visitVarInsn(ALOAD, 0);
 			method.visitFieldInsn(GETFIELD, implName, "endpoint", "L"
 					+ ENDPOINT_I + ";");
 			method.visitLdcInsn(url);
 			method.visitVarInsn(ALOAD, 1);
 			method.visitLdcInsn(interfaceClassName);
 			method.visitVarInsn(ALOAD, 0);
 			method.visitVarInsn(ALOAD, 0);
 			method.visitFieldInsn(GETFIELD, implName, "endpoint", "L"
 					+ ENDPOINT_I + ";");
 			method.visitLdcInsn(url);
 			method.visitMethodInsn(INVOKEINTERFACE, ENDPOINT_I,
 					"getAttributes",
 					"(Ljava/lang/String;)Ljava/util/Dictionary;");
 			method
 					.visitMethodInsn(
 							INVOKEINTERFACE,
 							"org/osgi/framework/BundleContext",
 							"registerService",
 							"(Ljava/lang/String;Ljava/lang/Object;Ljava/util/Dictionary;)Lorg/osgi/framework/ServiceRegistration;");
 			method
 					.visitMethodInsn(INVOKEINTERFACE, ENDPOINT_I,
 							"proxiedService",
 							"(Ljava/lang/String;Lorg/osgi/framework/ServiceRegistration;)V");
 			method.visitVarInsn(ALOAD, 0);
 			method.visitFieldInsn(GETFIELD, implName, "endpoint", "L"
 					+ ENDPOINT_I + ";");
 			method.visitLdcInsn(url);
 			method.visitMethodInsn(INVOKEINTERFACE, ENDPOINT_I,
 					"getAttributes",
 					"(Ljava/lang/String;)Ljava/util/Dictionary;");
 			method.visitLdcInsn(RemoteOSGiService.PRESENTATION);
 			method.visitMethodInsn(INVOKEVIRTUAL, "java/util/Dictionary",
 					"get", "(Ljava/lang/Object;)Ljava/lang/Object;");
 			method.visitTypeInsn(CHECKCAST, "java/lang/String");
 			method.visitVarInsn(ASTORE, 3);
 			method.visitVarInsn(ALOAD, 3);
 			Label l0 = new Label();
 			method.visitJumpInsn(IFNULL, l0);
 			method.visitVarInsn(ALOAD, 3);
 			method.visitMethodInsn(INVOKESTATIC, "java/lang/Class", "forName",
 					"(Ljava/lang/String;)Ljava/lang/Class;");
 			method.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class",
 					"newInstance", "()Ljava/lang/Object;");
 			method.visitTypeInsn(CHECKCAST, UICOMP_I);
 			method.visitVarInsn(ASTORE, 4);
 			method.visitVarInsn(ALOAD, 4);
 			method.visitVarInsn(ALOAD, 0);
 			method.visitVarInsn(ALOAD, 1);
 			method.visitMethodInsn(INVOKEINTERFACE, UICOMP_I, "initComponent",
 					"(Ljava/lang/Object;Lorg/osgi/framework/BundleContext;)V");
 			method.visitVarInsn(ALOAD, 1);
 			method.visitLdcInsn(ServiceUIComponent.class.getName());
 			method.visitVarInsn(ALOAD, 4);
 			method.visitVarInsn(ALOAD, 2);
 			method.visitLdcInsn(url);
 			method.visitMethodInsn(INVOKEINTERFACE, REMOTING_I,
 					"getPresentationAttributes",
 					"(Ljava/lang/String;)Ljava/util/Dictionary;");
 			method
 					.visitMethodInsn(
 							INVOKEINTERFACE,
 							"org/osgi/framework/BundleContext",
 							"registerService",
 							"(Ljava/lang/String;Ljava/lang/Object;Ljava/util/Dictionary;)Lorg/osgi/framework/ServiceRegistration;");
 			method.visitInsn(POP);
 			method.visitLabel(l0);
 			method.visitInsn(RETURN);
 			method.visitMaxs(7, 5);
 			method.visitEnd();
 		}
 		{
 			method = writer.visitMethod(ACC_PUBLIC, "stop",
 					"(Lorg/osgi/framework/BundleContext;)V", null,
 					new String[] { "java/lang/Exception" });
 			method.visitCode();
 			method.visitInsn(RETURN);
 			method.visitMaxs(0, 2);
 			method.visitEnd();
 		}
 	}
 
 	/**
 	 * @param source
 	 *            source
 	 * @param debug
 	 *            debug
 	 * @see org.objectweb.asm.ClassVisitor#visitSource(java.lang.String,
 	 *      java.lang.String)
 	 */
 	public void visitSource(final String source, final String debug) {
 		return;
 	}
 
 	/**
 	 * @param owner
 	 *            owner
 	 * @param name
 	 *            name
 	 * @param desc
 	 *            desc
 	 * @see org.objectweb.asm.ClassVisitor#visitOuterClass(java.lang.String,
 	 *      java.lang.String, java.lang.String)
 	 */
 	public void visitOuterClass(final String owner, final String name,
 			final String desc) {
 		return;
 	}
 
 	/**
 	 * @param desc
 	 *            desc
 	 * @param visible
 	 *            visible
 	 * @return AnnotationVisitor
 	 * @see org.objectweb.asm.ClassVisitor#visitAnnotation(java.lang.String,
 	 *      boolean)
 	 */
 	public AnnotationVisitor visitAnnotation(final String desc,
 			final boolean visible) {
 		writer.visitAnnotation(desc, visible);
 		return null;
 	}
 
 	/**
 	 * @param attr
 	 *            attr
 	 * @see org.objectweb.asm.ClassVisitor
 	 *      #visitAttribute(org.objectweb.asm.Attribute)
 	 */
 	public void visitAttribute(final Attribute attr) {
 		writer.visitAttribute(attr);
 	}
 
 	/**
 	 * @param name
 	 *            name
 	 * @param outerName
 	 *            outerName
 	 * @param innerName
 	 *            innerName
 	 * @param access
 	 *            access
 	 * @see org.objectweb.asm.ClassVisitor#visitInnerClass(java.lang.String,
 	 *      java.lang.String, java.lang.String, int)
 	 */
 	public void visitInnerClass(final String name, final String outerName,
 			final String innerName, final int access) {
 		writer.visitInnerClass(name, outerName, innerName, access);
 	}
 
 	/**
 	 * @param access
 	 *            access
 	 * @param name
 	 *            name
 	 * @param desc
 	 *            desc
 	 * @param signature
 	 *            signature
 	 * @param value
 	 *            value
 	 * @return FieldVisitor
 	 * @see org.objectweb.asm.ClassVisitor#visitField(int, java.lang.String,
 	 *      java.lang.String, java.lang.String, java.lang.Object)
 	 */
 	public FieldVisitor visitField(final int access, final String name,
 			final String desc, final String signature, final Object value) {
 		return writer.visitField(access, name, desc, signature, value);
 	}
 
 	/**
 	 * @param access
 	 *            access
 	 * @param name
 	 *            name
 	 * @param desc
 	 *            desc
 	 * @param signature
 	 *            signature
 	 * @param exceptions
 	 *            exceptions
 	 * @return MethodVisitor
 	 * @see org.objectweb.asm.ClassVisitor#visitMethod(int, java.lang.String,
 	 *      java.lang.String, java.lang.String, java.lang.String[])
 	 */
 	public MethodVisitor visitMethod(final int access, final String name,
 			final String desc, final String signature, final String[] exceptions) {
 		int methodAccess = access;
 
 		final Type[] args = Type.getArgumentTypes(desc);
 		boolean needsBoxing = false;
 
 		if ((methodAccess & ACC_ABSTRACT) != 0) {
 			methodAccess = (methodAccess ^ ACC_ABSTRACT);
 			MethodVisitor method = writer.visitMethod(methodAccess, name, desc,
 					signature, exceptions);
 
 			method.visitVarInsn(ALOAD, 0);
 			method.visitFieldInsn(GETFIELD, implName, "endpoint", "L"
 					+ ENDPOINT_I + ";");
 			method.visitLdcInsn(serviceURL.toString());
 			method.visitLdcInsn(name + desc);
 			if (args.length < 5) {
 				method.visitInsn(ICONST[args.length]);
 			} else {
 				method.visitIntInsn(BIPUSH, args.length);
 			}
 			method.visitTypeInsn(ANEWARRAY, "java/lang/Object");
 			int slot = 1;
 
 			// boxing of primitive type arguments
 			for (int i = 0; i < (args.length < 5 ? args.length : 5); i++) {
 				if (args[i].getSort() == Type.ARRAY
 						|| args[i].getSort() == Type.OBJECT) {
 					method.visitInsn(DUP);
 					method.visitInsn(ICONST[i]);
 					method.visitVarInsn(ALOAD, slot);
 					method.visitInsn(AASTORE);
 					slot++;
 				} else {
 					method.visitInsn(DUP);
 					method.visitInsn(ICONST[i]);
 					method.visitTypeInsn(NEW,
 							"ch/ethz/iks/r_osgi/BoxedPrimitive");
 					method.visitInsn(DUP);
 					method.visitVarInsn(args[i].getOpcode(ILOAD), slot);
 					method.visitMethodInsn(INVOKESPECIAL,
 							"ch/ethz/iks/r_osgi/BoxedPrimitive", "<init>", "("
 									+ args[i].getDescriptor() + ")V");
 					method.visitInsn(AASTORE);
 					slot += args[i].getSize();
 					needsBoxing = true;
 				}
 			}
 
 			for (int i = 5; i < args.length; i++) {
 				if (args[i].getSort() == Type.ARRAY
 						|| args[i].getSort() == Type.OBJECT) {
 					method.visitInsn(DUP);
 					method.visitIntInsn(BIPUSH, i);
 					method.visitVarInsn(ALOAD, slot);
 					method.visitInsn(AASTORE);
 					slot++;
 				} else {
 					method.visitInsn(DUP);
 					method.visitIntInsn(BIPUSH, i);
 					method.visitTypeInsn(NEW,
 							"ch/ethz/iks/r_osgi/BoxedPrimitive");
 					method.visitInsn(DUP);
 					method.visitVarInsn(args[i].getOpcode(ILOAD), slot);
 					method.visitMethodInsn(INVOKESPECIAL,
 							"ch/ethz/iks/r_osgi/BoxedPrimitive", "<init>", "("
 									+ args[i].getDescriptor() + ")V");
 					method.visitInsn(AASTORE);
 					slot += args[i].getSize();
 					needsBoxing = true;
 				}
 			}
 			method
 					.visitMethodInsn(INVOKEINTERFACE, ENDPOINT_I,
 							"invokeMethod",
 							"(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;");
 
 			// unboxing of primitive type return values.
 			final Type returnType = Type.getReturnType(desc);
 			final int sort = returnType.getSort();
 			switch (sort) {
 			case Type.VOID:
 				method.visitInsn(POP);
 				method.visitInsn(RETURN);
 				break;
 			case Type.BOOLEAN:
 			case Type.CHAR:
 			case Type.SHORT:
 			case Type.INT:
 			case Type.LONG:
 			case Type.DOUBLE:
 			case Type.FLOAT:
 			case Type.BYTE:
 				method.visitTypeInsn(CHECKCAST, BOXED_TYPE[sort]);
 				method.visitMethodInsn(INVOKEVIRTUAL, BOXED_TYPE[sort],
 						UNBOX_METHOD[sort], "()" + returnType.getDescriptor());
 				method.visitInsn(returnType.getOpcode(IRETURN));
 				break;
 			case Type.ARRAY:
 				method.visitTypeInsn(CHECKCAST, "["
 						+ returnType.getInternalName() + ";");
 				method.visitInsn(ARETURN);
 				break;
 			default:
 				method.visitTypeInsn(CHECKCAST, returnType.getInternalName());
 				method.visitInsn(ARETURN);
 				break;
 			}
 			method.visitMaxs(
 					(args.length == 0 ? 4 : 7) + (needsBoxing ? 2 : 0),
 					1 + args.length);
 			method.visitEnd();
 			return null;
 
 		} else {
 			// proxy method, contains code so just rewrite the code ...
 			MethodVisitor method = writer.visitMethod(access, name, desc,
 					signature, exceptions);
 			return new MethodRewriter(method);
 		}
 	}
 
 	/**
 	 * 
 	 * @see org.objectweb.asm.ClassVisitor#visitEnd()
 	 */
 	public void visitEnd() {
 		writer.visitEnd();
 	}
 
 	/**
 	 * 
 	 * @author Jan S. Rellermeyer, ETH Zurich
 	 */
 	private final class MethodRewriter implements MethodVisitor {
 		/**
 		 * 
 		 */
 		private MethodVisitor methodWriter;
 
 		/**
 		 * @param methodWriter
 		 *            methodWriter
 		 */
 		private MethodRewriter(final MethodVisitor methodWriter) {
 			this.methodWriter = methodWriter;
 		}
 
 		/**
 		 * @return AnnotationVisitor
 		 * @see org.objectweb.asm.MethodVisitor#visitAnnotationDefault()
 		 */
 		public AnnotationVisitor visitAnnotationDefault() {
 			methodWriter.visitAnnotationDefault();
 			return null;
 		}
 
 		/**
 		 * @param parameter
 		 *            parameter
 		 * @param desc
 		 *            desc
 		 * @param visible
 		 *            visible
 		 * @return AnnotationVisitor
 		 * @see org.objectweb.asm.MethodVisitor#visitParameterAnnotation(int,
 		 *      java.lang.String, boolean)
 		 */
 		public AnnotationVisitor visitParameterAnnotation(final int parameter,
 				final String desc, final boolean visible) {
 			methodWriter.visitParameterAnnotation(parameter, desc, visible);
 			return null;
 		}
 
 		/**
 		 * @see org.objectweb.asm.MethodVisitor#visitCode()
 		 */
 		public void visitCode() {
 			methodWriter.visitCode();
 		}
 
 		/**
 		 * @param opcode
 		 *            opcode
 		 * @see org.objectweb.asm.MethodVisitor#visitInsn(int)
 		 */
 		public void visitInsn(final int opcode) {
 			methodWriter.visitInsn(opcode);
 		}
 
 		/**
 		 * @param opcode
 		 *            opcode
 		 * @param operand
 		 *            operand
 		 * @see org.objectweb.asm.MethodVisitor#visitIntInsn(int, int)
 		 */
 		public void visitIntInsn(final int opcode, final int operand) {
 			methodWriter.visitIntInsn(opcode, operand);
 		}
 
 		/**
 		 * @param opcode
 		 *            opcode
 		 * @param var
 		 *            var
 		 * @see org.objectweb.asm.MethodVisitor#visitVarInsn(int, int)
 		 */
 		public void visitVarInsn(final int opcode, final int var) {
 			methodWriter.visitVarInsn(opcode, var);
 		}
 
 		/**
 		 * @param opcode
 		 *            opcode
 		 * @param desc
 		 *            desc
 		 * @see org.objectweb.asm.MethodVisitor#visitTypeInsn(int,
 		 *      java.lang.String)
 		 */
 		public void visitTypeInsn(final int opcode, final String desc) {
 			methodWriter.visitTypeInsn(opcode, desc);
 		}
 
 		/**
 		 * @param opcode
 		 *            opcode
 		 * @param owner
 		 *            owner
 		 * @param name
 		 *            name
 		 * @param desc
 		 *            desc
 		 * @see org.objectweb.asm.MethodVisitor#visitFieldInsn(int,
 		 *      java.lang.String, java.lang.String, java.lang.String)
 		 */
 		public void visitFieldInsn(final int opcode, final String owner,
 				final String name, final String desc) {
 			methodWriter.visitFieldInsn(opcode, owner, name, desc);
 		}
 
 		/**
 		 * @param opcode
 		 *            opcode
 		 * @param owner
 		 *            owner
 		 * @param name
 		 *            name
 		 * @param desc
 		 *            desc
 		 * @see org.objectweb.asm.MethodVisitor#visitMethodInsn(int,
 		 *      java.lang.String, java.lang.String, java.lang.String)
 		 */
 		public void visitMethodInsn(final int opcode, final String owner,
 				final String name, final String desc) {
 			if (opcode == INVOKEVIRTUAL
 					&& owner.replace('/', '.').equals(smartProxyClassName)) {
 				methodWriter.visitMethodInsn(opcode, implName, name, desc);
 				return;
 			}
 			methodWriter.visitMethodInsn(opcode, owner, name, desc);
 		}
 
 		/**
 		 * @param opcode
 		 *            opcode
 		 * @param label
 		 *            label
 		 * @see org.objectweb.asm.MethodVisitor#visitJumpInsn(int,
 		 *      org.objectweb.asm.Label)
 		 */
 		public void visitJumpInsn(final int opcode, final Label label) {
 			methodWriter.visitJumpInsn(opcode, label);
 		}
 
 		/**
 		 * @param label
 		 *            label
 		 * @see org.objectweb.asm.MethodVisitor
 		 *      #visitLabel(org.objectweb.asm.Label)
 		 */
 		public void visitLabel(final Label label) {
 			methodWriter.visitLabel(label);
 		}
 
 		/**
 		 * @param cst
 		 *            cst
 		 * @see org.objectweb.asm.MethodVisitor#visitLdcInsn(java.lang.Object)
 		 */
 		public void visitLdcInsn(final Object cst) {
 			methodWriter.visitLdcInsn(cst);
 		}
 
 		/**
 		 * @param var
 		 *            var
 		 * @param increment
 		 *            increment
 		 * @see org.objectweb.asm.MethodVisitor#visitIincInsn(int, int)
 		 */
 		public void visitIincInsn(final int var, final int increment) {
 			methodWriter.visitIincInsn(var, increment);
 		}
 
 		/**
 		 * @param min
 		 *            min
 		 * @param max
 		 *            max
 		 * @param dflt
 		 *            dflt
 		 * @param labels
 		 *            labels
 		 * @see org.objectweb.asm.MethodVisitor#visitTableSwitchInsn(int, int,
 		 *      org.objectweb.asm.Label, org.objectweb.asm.Label[])
 		 */
 		public void visitTableSwitchInsn(final int min, final int max,
 				final Label dflt, final Label[] labels) {
 			methodWriter.visitTableSwitchInsn(min, max, dflt, labels);
 		}
 
 		/**
 		 * @param dflt
 		 *            dflt
 		 * @param keys
 		 *            keys
 		 * @param labels
 		 *            labels
 		 * @see org.objectweb.asm.MethodVisitor
 		 *      #visitLookupSwitchInsn(org.objectweb.asm.Label, int[],
 		 *      org.objectweb.asm.Label[])
 		 */
 		public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
 				final Label[] labels) {
 			methodWriter.visitLookupSwitchInsn(dflt, keys, labels);
 		}
 
 		/**
 		 * @param desc
 		 *            desc
 		 * @param dims
 		 *            dims
 		 * @see org.objectweb.asm.MethodVisitor
 		 *      #visitMultiANewArrayInsn(java.lang.String, int)
 		 */
 		public void visitMultiANewArrayInsn(final String desc, final int dims) {
 			methodWriter.visitMultiANewArrayInsn(desc, dims);
 		}
 
 		/**
 		 * @param start
 		 *            start
 		 * @param end
 		 *            end
 		 * @param handler
 		 *            handler
 		 * @param type
 		 *            type
 		 * @see org.objectweb.asm.MethodVisitor
 		 *      #visitTryCatchBlock(org.objectweb.asm.Label,
 		 *      org.objectweb.asm.Label, org.objectweb.asm.Label,
 		 *      java.lang.String)
 		 */
 		public void visitTryCatchBlock(final Label start, final Label end,
 				final Label handler, final String type) {
 			methodWriter.visitTryCatchBlock(start, end, handler, type);
 		}
 
 		/**
 		 * @param name
 		 *            name
 		 * @param desc
 		 *            desc
 		 * @param signature
 		 *            signature
 		 * @param start
 		 *            start
 		 * @param end
 		 *            end
 		 * @param index
 		 *            index
 		 * @see org.objectweb.asm.MethodVisitor
 		 *      #visitLocalVariable(java.lang.String, java.lang.String,
 		 *      java.lang.String, org.objectweb.asm.Label,
 		 *      org.objectweb.asm.Label, int)
 		 */
 		public void visitLocalVariable(final String name, final String desc,
 				final String signature, final Label start, final Label end,
 				final int index) {
 			methodWriter.visitLocalVariable(name, desc, signature, start, end,
 					index);
 		}
 
 		/**
 		 * @param line
 		 *            line
 		 * @param start
 		 *            start
 		 * @see org.objectweb.asm.MethodVisitor#visitLineNumber(int,
 		 *      org.objectweb.asm.Label)
 		 */
 		public void visitLineNumber(final int line, final Label start) {
 			methodWriter.visitLineNumber(line, start);
 		}
 
 		/**
 		 * @param maxStack
 		 *            maxStack
 		 * @param maxLocals
 		 *            maxLocals
 		 * @see org.objectweb.asm.MethodVisitor#visitMaxs(int, int)
 		 */
 		public void visitMaxs(final int maxStack, final int maxLocals) {
 			methodWriter.visitMaxs(maxStack, maxLocals);
 		}
 
 		/**
 		 * @param desc
 		 *            desc
 		 * @param visible
 		 *            visible
 		 * @see org.objectweb.asm.MethodVisitor
 		 * @return AnnotationVisitor #visitAnnotation(java.lang.String, boolean)
 		 */
 		public AnnotationVisitor visitAnnotation(final String desc,
 				final boolean visible) {
 			methodWriter.visitAnnotation(desc, visible);
 			return null;
 		}
 
 		/**
 		 * @param attr
 		 *            attr
 		 * @see org.objectweb.asm.MethodVisitor
 		 *      #visitAttribute(org.objectweb.asm.Attribute)
 		 */
 		public void visitAttribute(final Attribute attr) {
 			methodWriter.visitAttribute(attr);
 		}
 
 		/**
 		 * @see org.objectweb.asm.MethodVisitor#visitEnd()
 		 */
 		public void visitEnd() {
 			methodWriter.visitEnd();
 		}
 	}
 
 	/**
 	 * generate a source id from IP or host name.
 	 * 
 	 * @param id
 	 *            id
 	 * @return sourceID
 	 */
 	private static String generateSourceID(final String id) {
 		char[] chars = id.toCharArray();
 		StringBuffer buffer = new StringBuffer();
 		for (int i = 0; i < chars.length; i++) {
 			if (chars[i] == '.') {
 				buffer.append('o');
 				continue;
 			}
 			if (chars[i] > 47 && chars[i] < 58) {
 				buffer.append(CHAR_TABLE[chars[i] - 48]);
 				continue;
 			}
 			buffer.append(chars[i]);
 		}
 		return buffer.toString();
 	}
 
 }
