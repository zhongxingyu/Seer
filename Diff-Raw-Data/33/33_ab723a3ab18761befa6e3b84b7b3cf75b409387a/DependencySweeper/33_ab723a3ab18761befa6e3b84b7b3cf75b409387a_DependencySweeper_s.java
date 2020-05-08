 package com.zwitserloot.stubber.reader;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicReference;
 
 import lombok.Cleanup;
 import lombok.RequiredArgsConstructor;
 import lombok.val;
 
 import org.objectweb.asm.ClassReader;
 import org.objectweb.asm.ClassVisitor;
 import org.objectweb.asm.FieldVisitor;
 import org.objectweb.asm.MethodVisitor;
 import org.objectweb.asm.Opcodes;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.io.ByteStreams;
 import com.zwitserloot.stubber.model.ClassFile;
 import com.zwitserloot.stubber.model.FieldModel;
 import com.zwitserloot.stubber.model.MethodModel;
 
 /**
  * Starting with an initial list of types considered 'public API', scans each class
  * for any types used in public/protected members of that class, and recursively keeps looking.
  * The result is a list of types which are relevant for the API.
  */
 public class DependencySweeper {
 	private static boolean isVisible(int access) {
 		return (access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) != 0;
 	}
 	
 	private final ClassLoader cl;
 	private final List<String> exclusionPrefixes = new ArrayList<String>();
 	
 	public DependencySweeper() {
 		this(ClassLoader.getSystemClassLoader());
 	}
 	
 	public DependencySweeper(ClassLoader cl) {
 		this.cl = cl;
 		this.exclusionPrefixes.add("java/");
 	}
 	
 	/**
 	 * An exclusion prefix indicates that any class that starts with the given prefix is presumed to
 	 * be available and thus does not need to be stubbed out. Uses JVM-style naming (slashes and dollars, not dots).
 	 * By default, {@code java/} is already added. 
 	 */
 	public void addExclusionPrefix(String prefix) {
 		if (prefix == null) throw new NullPointerException("prefix");
 		this.exclusionPrefixes.add(prefix);
 	}
 	
 	private Map<String, ClassFile> map = new HashMap<String, ClassFile>();
 	
 	private Map<String, ClassFile> round(Collection<String> types, boolean skipPrivateAndPackagePrivate) throws IOException {
 		Map<String, ClassFile> map = new HashMap<String, ClassFile>();
 		
 		outer:
 		for (String t : types) {
 			for (String ex : exclusionPrefixes) if (t.startsWith(ex)) continue outer;
 			@Cleanup val in = cl.getResourceAsStream(t + ".class");
 			if (in == null) {
 				System.out.printf("WARNING: Can't find class; it will not be stubbed and it will not be scanned for further dependencies to stub: %s\n", t);
 				continue;
 			}
 			byte[] classData = ByteStreams.toByteArray(in);
 			in.close();
 			
 			val cf = make(classData, skipPrivateAndPackagePrivate);
 			
 			if (cf != null) map.put(t, cf);
 		}
 		
 		return map;
 	}
 	
 	/**
 	 * Returns all added types in the {@link #fill(Collection)} method, as well as further public API
 	 * dependencies of those types.
 	 */
 	public Collection<String> getTypeNames() {
 		return map.keySet();
 	}
 	
 	/**
 	 * Adds the given types to the list of public API, and recursively scans all public parts of all
 	 * signatures in the class for more types that are part of the API.
 	 * 
 	 * The result can be queried via {@link #getTypeNames()}.
 	 */
 	public void fill(Collection<String> initialTypes) throws IOException {
 		Map<String, ClassFile> roundResult = round(initialTypes, true);
 		
 		while (true) {
 			for (String key : map.keySet()) roundResult.remove(key);
 			map.putAll(roundResult);
 			Set<String> newTypes = new HashSet<String>();
 			for (ClassFile cf : roundResult.values()) {
 				cf.addTypeNamesInSignatures(newTypes);
 			}
 			newTypes.removeAll(map.keySet());
 			if (newTypes.isEmpty()) break;
 			roundResult = round(newTypes, false);
 		}
 	}
 	
 	private ClassFile make(byte[] classData, final boolean skipPrivateAndPackagePrivate) {
 		val fields = new ArrayList<FieldModel>();
 		val methods = new ArrayList<MethodModel>();
 		val parents = new ArrayList<String>();
 		val result = new AtomicReference<ClassFile>();
 		val skip = new AtomicBoolean();
 		
 		ClassVisitor scanner = new ClassVisitor(Opcodes.ASM4) {
 			private String name;
 			private String signature;
 			
 			@Override public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
 				if (isVisible(access)) {
 					fields.add(new FieldModel(name, desc, signature));
 				}
 				return null;
 			}
 			
 			@Override public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
 				if (isVisible(access)) {
 					methods.add(new MethodModel(name, desc, signature,
 							exceptions == null ? ImmutableList.<String>of() : ImmutableList.copyOf(exceptions)));
 				}
 				return null;
 			}
 			
 			@Override public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
 				if (!isVisible(access) && skipPrivateAndPackagePrivate) {
 					skip.set(true);
 				} else {
 					if (superName != null) parents.add(superName);
 					if (interfaces != null) for (String intf : interfaces) parents.add(intf);
 					this.name = name;
 					this.signature = signature;
 					super.visit(version, access, name, signature, superName, interfaces);
 				}
 			}
 			
 			@Override public void visitEnd() {
 				result.set(new ClassFile(parents, name, fields, methods, signature));
 			}
 		};
 		
 		new ClassReader(classData).accept(scanner, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
 		return skip.get() ? null : result.get();
 	}
 	
 	@RequiredArgsConstructor
 	private static class State {
 		final String input;
 		final Collection<String> foundTypes;
 		int pos;
 		
 		public void advancePast(char c) {
 			while (pos < input.length()) {
 				if (input.charAt(pos) == c) {
 					pos++;
 					return;
 				}
 				pos++;
 			}
 			throw new IllegalStateException();
 		}
 		
 		char c() {
 			return atEnd() ? 0 : input.charAt(pos);
 		}
 		
 		boolean atEnd() {
 			return !(pos < input.length());
 		}
 		
 		boolean parseType() {
 			if (atEnd()) return false;
 			char c = c();
 			while (c == '+' || c == '-' || c == '[') {
 				pos++;
 				c = c();
 			}
 			
 			if (c == 'I' || c == 'J' || c == 'B' || c == 'S' || c == 'C' || c == 'Z' || c == 'F' || c == 'D' || c == 'V' || c == '*') {
 				pos++;
 				return true;
 			}
 			if (c == 'T') {
 				advancePast(';');
 				return true;
 			}
 			
			if (c == 'L') {
 				pos++;
				int start = pos;
 				int end = pos;
 				
 				while (end < input.length()) {
 					char x = input.charAt(end);
 					if (x == ';') {
						foundTypes.add(input.substring(start, end));
 						pos = end + 1;
 						return true;
 					}
 					if (x == '<') {
						foundTypes.add(input.substring(start, end));
 						pos = end + 1;
 						while (c() != '>' && parseType()) ;
 						if (c() == '>') pos++;
						if (c() == ';') pos++;
 						return true;
 					}
 					end++;
 				}
 				return true;
 			}
 			
			throw new IllegalArgumentException();
 		}
 	}
 	
 	/**
 	 * Returns a set containing all non-primitive types present in any given JVM-style signature.
 	 * Supports both old-style descriptors (without generics) and new-style signatures (with generics).
 	 */
 	public static Set<String> typesInSignature(String signature) {
 		Set<String> types = new HashSet<String>();
 		typesInSignature(types, signature);
 		return types;
 	}
 	
 	/**
 	 * Adds to the supplied set all non-primitive types present in any given JVM-style signature.
 	 * Supports both old-style descriptors (without generics) and new-style signatures (with generics).
 	 */
 	public static void typesInSignature(Collection<String> types, String signature) {
 		if (signature == null) return;
 		State s = new State(signature, types);
 		if (signature.startsWith("<")) {
 			while (s.c() != '>') {
 				s.advancePast(':');
 				if (s.c() != ':') s.parseType();
 				while (s.c() == ':') {
 					s.pos++;
 					s.parseType();
 				}
 			}
 			s.pos++;
 		}
 		
 		while (!s.atEnd()) {
 			if (s.c() == '(' || s.c() == ')' || s.c() == '^') {
 				s.pos++;
 				continue;
 			}
 			s.parseType();
 		}
 	}
 }
