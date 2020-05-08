 /*******************************************************************************
  * Copyright (c) CWI 2009
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
 * 
  * Contributors:
 * Jurgen Vinju (jurgenv@cwi.nl) - initial API and implementation
  *******************************************************************************/
 package org.magnolialang.terms;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.eclipse.imp.pdb.facts.IBool;
 import org.eclipse.imp.pdb.facts.IConstructor;
 import org.eclipse.imp.pdb.facts.IDateTime;
 import org.eclipse.imp.pdb.facts.IExternalValue;
 import org.eclipse.imp.pdb.facts.IInteger;
 import org.eclipse.imp.pdb.facts.IList;
 import org.eclipse.imp.pdb.facts.IMap;
 import org.eclipse.imp.pdb.facts.INode;
 import org.eclipse.imp.pdb.facts.IRational;
 import org.eclipse.imp.pdb.facts.IReal;
 import org.eclipse.imp.pdb.facts.IRelation;
 import org.eclipse.imp.pdb.facts.ISet;
 import org.eclipse.imp.pdb.facts.ISourceLocation;
 import org.eclipse.imp.pdb.facts.IString;
 import org.eclipse.imp.pdb.facts.ITuple;
 import org.eclipse.imp.pdb.facts.IValue;
 import org.eclipse.imp.pdb.facts.io.StandardTextReader;
 import org.eclipse.imp.pdb.facts.io.StandardTextWriter;
 import org.eclipse.imp.pdb.facts.type.Type;
 import org.eclipse.imp.pdb.facts.visitors.IValueVisitor;
 import org.eclipse.imp.pdb.facts.visitors.VisitorException;
 
 /**
  * This class implements the standard readable syntax for {@link IValue}'s. See
  * also {@link StandardTextReader}. This is a public copy of the exact same
  * visitor as is found inside {@link StandardTextWriter}, to allow for
  * invocation and customization.
  */
 public class ImpTermTextWriterVisitor implements IValueVisitor<IValue> {
 	private final OutputStream	stream;
 	private final int			tabSize;
 	private final boolean		indented;
 	private int					tabLevel	= 0;
 
 
 	public ImpTermTextWriterVisitor(OutputStream stream, boolean indented, int tabSize) {
 		this.stream = stream;
 		this.indented = indented;
 		this.tabSize = tabSize;
 	}
 
 
 	protected void append(String string) throws VisitorException {
 		try {
 			stream.write(string.getBytes());
 		}
 		catch(IOException e) {
 			throw new VisitorException(e);
 		}
 	}
 
 
 	private void append(char c) throws VisitorException {
 		try {
 			stream.write(c);
 		}
 		catch(IOException e) {
 			throw new VisitorException(e);
 		}
 	}
 
 
 	private void tab() {
 		this.tabLevel++;
 	}
 
 
 	private void untab() {
 		this.tabLevel--;
 	}
 
 
 	@Override
 	public IValue visitBoolean(IBool boolValue) throws VisitorException {
 		append(boolValue.getValue() ? "true" : "false");
 		return boolValue;
 	}
 
 
 	@Override
 	public IValue visitConstructor(IConstructor o) throws VisitorException {
 		return visitNode(o);
 	}
 
 
 	@Override
 	public IValue visitReal(IReal o) throws VisitorException {
 		append(o.getStringRepresentation());
 		return o;
 	}
 
 
 	@Override
 	public IValue visitInteger(IInteger o) throws VisitorException {
 		append(o.getStringRepresentation());
 		return o;
 	}
 
 
 	@Override
 	public IValue visitList(IList o) throws VisitorException {
 		append('[');
 
 		boolean indent = checkIndent(o);
 		Iterator<IValue> listIterator = o.iterator();
 		tab();
 		indent(indent);
 		if(listIterator.hasNext()) {
 			listIterator.next().accept(this);
 
 			while(listIterator.hasNext()) {
 				append(',');
 				if(indent)
 					indent();
 				listIterator.next().accept(this);
 			}
 		}
 		untab();
 		indent(indent);
 		append(']');
 
 		return o;
 	}
 
 
 	@Override
 	public IValue visitMap(IMap o) throws VisitorException {
 		append('(');
 		tab();
 		boolean indent = checkIndent(o);
 		indent(indent);
 		Iterator<IValue> mapIterator = o.iterator();
 		if(mapIterator.hasNext()) {
 			IValue key = mapIterator.next();
 			key.accept(this);
 			append(':');
 			o.get(key).accept(this);
 
 			while(mapIterator.hasNext()) {
 				append(',');
 				indent(indent);
 				key = mapIterator.next();
 				key.accept(this);
 				append(':');
 				o.get(key).accept(this);
 			}
 		}
 		untab();
 		indent(indent);
 		append(')');
 
 		return o;
 	}
 
 
 	@Override
 	public IValue visitNode(INode o) throws VisitorException {
 		String name = o.getName();
 
 		if(name.indexOf('-') != -1) {
 			append('\\');
 		}
 		append(name);
 
 		boolean indent = checkIndent(o);
 
 		append('(');
 		tab();
 		indent(indent);
 		Iterator<IValue> it = o.iterator();
 		while(it.hasNext()) {
 			it.next().accept(this);
 			if(it.hasNext()) {
 				append(',');
 				indent(indent);
 			}
 		}
 		append(')');
 		untab();
 		if(o.hasAnnotations()) {
 			append('[');
 			tab();
 			indent();
 			int i = 0;
 			Map<String, IValue> annotations = o.getAnnotations();
 			for(Entry<String, IValue> entry : annotations.entrySet()) {
 				append("@" + entry.getKey() + "=");
 				entry.getValue().accept(this);
 
 				if(++i < annotations.size()) {
 					append(",");
 					indent();
 				}
 			}
 			untab();
 			indent();
 			append(']');
 		}
 
 		return o;
 	}
 
 
 	private void indent() throws VisitorException {
 		indent(indented);
 	}
 
 
 	private void indent(boolean indent) throws VisitorException {
 		if(indent) {
 			append('\n');
 			for(int i = 0; i < tabSize * tabLevel; i++) {
 				append(' ');
 			}
 		}
 	}
 
 
 	@Override
 	public IValue visitRelation(IRelation o) throws VisitorException {
 		return visitSet(o);
 	}
 
 
 	@Override
 	public IValue visitSet(ISet o) throws VisitorException {
 		append('{');
 
 		boolean indent = checkIndent(o);
 		tab();
 		indent(indent);
 		Iterator<IValue> setIterator = o.iterator();
 		if(setIterator.hasNext()) {
 			setIterator.next().accept(this);
 
 			while(setIterator.hasNext()) {
 				append(",");
 				indent(indent);
 				setIterator.next().accept(this);
 			}
 		}
 		untab();
 		indent(indent);
 		append('}');
 		return o;
 	}
 
 
 	private boolean checkIndent(ISet o) {
 		if(indented && o.size() > 1) {
 			for(IValue x : o) {
 				Type type = x.getType();
 				if(type.isNodeType() || type.isTupleType() || type.isListType() || type.isSetType() || type.isMapType() || type.isRelationType()) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 
 	private boolean checkIndent(IList o) {
 		if(indented && o.length() > 1) {
 			for(IValue x : o) {
 				Type type = x.getType();
 				if(type.isNodeType() || type.isTupleType() || type.isListType() || type.isSetType() || type.isMapType() || type.isRelationType()) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 
 	private boolean checkIndent(INode o) {
 		if(indented && o.arity() > 1) {
 			for(IValue x : o) {
 				Type type = x.getType();
 				if(type.isNodeType() || type.isTupleType() || type.isListType() || type.isSetType() || type.isMapType() || type.isRelationType()) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 
 	private boolean checkIndent(IMap o) {
 		if(indented && o.size() > 1) {
 			for(IValue x : o) {
 				Type type = x.getType();
 				Type vType = o.get(x).getType();
 				if(type.isNodeType() || type.isTupleType() || type.isListType() || type.isSetType() || type.isMapType() || type.isRelationType()) {
 					return true;
 				}
 				if(vType.isNodeType() || vType.isTupleType() || vType.isListType() || vType.isSetType() || vType.isMapType() || vType.isRelationType()) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 
 	@Override
 	public IValue visitSourceLocation(ISourceLocation o) throws VisitorException {
 		append('|');
 		append(o.getURI().toString());
 		append('|');
 
		if(o.hasOffsetLength()) {
 			append('(');
 			append(Integer.toString(o.getOffset()));
 			append(',');
 			append(Integer.toString(o.getLength()));
 			append(',');
 			append('<');
 			append(Integer.toString(o.getBeginLine()));
 			append(',');
 			append(Integer.toString(o.getBeginColumn()));
 			append('>');
 			append(',');
 			append('<');
 			append(Integer.toString(o.getEndLine()));
 			append(',');
 			append(Integer.toString(o.getEndColumn()));
 			append('>');
 			append(')');
 		}
 		return o;
 	}
 
 
 	@Override
 	public IValue visitString(IString o) throws VisitorException {
 		append('\"');
 		for(byte ch : o.getValue().getBytes()) {
 			switch(ch) {
 			case '\"':
 				append('\\');
 				append('\"');
 				break;
 			case '>':
 				append('\\');
 				append('>');
 				break;
 			case '<':
 				append('\\');
 				append('<');
 				break;
 			case '\'':
 				append('\\');
 				append('\'');
 				break;
 			case '\\':
 				append('\\');
 				append('\\');
 				break;
 			case '\n':
 				append('\\');
 				append('n');
 				break;
 			case '\r':
 				append('\\');
 				append('r');
 				break;
 			case '\t':
 				append('\\');
 				append('t');
 				break;
 			default:
 				append((char) ch);
 			}
 		}
 		append('\"');
 		return o;
 	}
 
 
 	@Override
 	public IValue visitTuple(ITuple o) throws VisitorException {
 		append('<');
 
 		Iterator<IValue> it = o.iterator();
 
 		if(it.hasNext()) {
 			it.next().accept(this);
 		}
 
 		while(it.hasNext()) {
 			append(',');
 			it.next().accept(this);
 		}
 		append('>');
 
 		return o;
 	}
 
 
 	@Override
 	public IValue visitExternal(IExternalValue externalValue) throws VisitorException {
 		append(externalValue.toString());
 		return externalValue;
 	}
 
 
 	@Override
 	public IValue visitDateTime(IDateTime o) throws VisitorException {
 		append("$");
 		if(o.isDate()) {
 			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
 			append(df.format(new Date(o.getInstant())));
 		}
 		else if(o.isTime()) {
 			SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSSZZZ", Locale.ROOT);
 			append("T");
 			append(df.format(new Date(o.getInstant())));
 		}
 		else {
 			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ", Locale.ROOT);
 			append(df.format(new Date(o.getInstant())));
 		}
 		return o;
 	}
 
 
 	@Override
 	public IValue visitRational(IRational o) throws VisitorException {
 		append(o.getStringRepresentation());
 		return o;
 	}
 }
