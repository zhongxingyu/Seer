 /*
 This file is part of the Polygraph bulk messaging framework
 Copyright (C) 2013 Wolfgang Illmeyer
 
 The Polygraph framework is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
 package com.illmeyer.polygraph.template;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 import java.io.Writer;
import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.illmeyer.polygraph.core.CoreConstants;
 import com.illmeyer.polygraph.core.MessageGunTemplateLoader;
 import com.illmeyer.polygraph.core.data.MessagePart;
 
 import freemarker.cache.TemplateLoader;
 import freemarker.core.Environment;
 import freemarker.template.TemplateDirectiveBody;
 import freemarker.template.TemplateException;
 import freemarker.template.TemplateModel;
 import freemarker.template.TemplateModelException;
 import freemarker.template.utility.DeepUnwrap;
 
 /**
  * Abstracts away the environment of the template engine and offers an interface more suited to Polygraph's needs
  * @author escitalopram
  *
  */
 public class PolygraphEnvironment {
 	private TemplateModel[] loopVars;
 	private TemplateDirectiveBody body;
 	private Environment env;
 	
 	PolygraphEnvironment(TemplateModel[] loopVars,
 			Environment env, TemplateDirectiveBody body) {
 		this.loopVars=loopVars;
 		this.body=body;
 		this.env=env;
 	}
 
 	/**
 	 * Get the value of a directive's loop variable
 	 * @param index index of the loop variable
 	 * @return loop variable
 	 */
 	public Object getLoopVar(int index) {
 		try {
 			return DeepUnwrap.unwrap(loopVars[index]) ;
 		} catch (TemplateModelException e) {
 			throw new PolygraphTemplateException(e);
 		}
 	}
 	
 	/**
 	 * Set the value of a directive's loop variable
 	 * @param index index of the loop variable
 	 * @param value new value of the loop variable
 	 */
 	public void setLoopVar(int index, Object value) {
 		try {
 			loopVars[index]=env.getObjectWrapper().wrap(value);
 		} catch (TemplateModelException e) {
 			throw new PolygraphTemplateException(e);
 		}
 	}
 	
 	/**
 	 * Return the writer the current directive writes to
 	 * @return writer
 	 */
 	public Writer getWriter() {
 		return env.getOut();
 	}
 	
 	/**
 	 * Execute the body of the current directive
 	 * @param writer where to write the body
 	 * @throws IOException
 	 */
 	public void executeBody(Writer writer) throws IOException {
 		try {
 			body.render(writer);
 		} catch (TemplateException e) {
 			throw new PolygraphTemplateException(e);
 		}
 	}
 
 	/**
 	 * Execute the body of the current directive using the current writer
 	 * @throws IOException
 	 */
 	public void executeBody() throws IOException {
 		executeBody(getWriter());
 	}
 
 	/**
 	 * Returns the current stack of tags
 	 * @return stack of tags
 	 */
 	public TagStack getTagStack() {
 		TagStack ts = (TagStack) env.getCustomAttribute(CoreConstants.ECA_TAGSTACK);
 		if (ts==null) {
 			ts=new TagStack();
 			env.setCustomAttribute(CoreConstants.ECA_TAGSTACK, ts);
 		}
 		return ts;
 	}
 
 	/**
 	 * Registers a new message part
 	 * @param name name of the new message part
 	 * @param p value of the message part
 	 * @throws PolygraphTemplateException
 	 */
 	public void registerMessagePart(String name, MessagePart p) throws PolygraphTemplateException {
 		Map<String,MessagePart> parts = getParts();
 		if (parts.containsKey(name)) throw new PolygraphTemplateException(String.format("Message part '%s' is already registered", name));
 		parts.put(name, p);
 	}
 	
 	/**
 	 * Returns a named message part
 	 * @param name name of the part to return
 	 * @return part
 	 */
 	public MessagePart getNamedPart(String name) {
 		Map<String,MessagePart> parts = getParts();
 		return parts.get(name);
 	}
 
 	/**
 	 * Return a map of message parts, create if not exists
 	 * @return map of parts
 	 */
 	private Map<String,MessagePart> getParts() {
 		@SuppressWarnings("unchecked")
 		Map<String,MessagePart> partMap = (Map<String, MessagePart>) env.getCustomAttribute(CoreConstants.ECA_PARTS); 
 		if (partMap==null) {
 			partMap = new HashMap<String, MessagePart>();
 			env.setCustomAttribute(CoreConstants.ECA_PARTS, partMap);
 		}
 		return partMap;
 	}
 	
 	/**
 	 * Ensure that there is a parent tag of a certain class. Only returns if there is.
 	 * @param tagClass desired class
 	 * @return parent tag of desired class
 	 */
 	public <A> A requireParentTag(Class<A> tagClass) {
 		// TODO decide if this should be converted to an annotation
 		TagStack ts = getTagStack();
 		if (ts.size()>1) {
 			PolygraphTag tag = ts.get(ts.size()-2);
 			if (tagClass.isInstance(tag)) {
 				@SuppressWarnings("unchecked")
 				A tag2 = (A)tag;
 				return tag2;
 			}
 		}
 		throw new PolygraphTemplateException(String.format("Parent tag of type %s expected but not found.",tagClass.getName()));
 	}
 	
 	/**
 	 * Ensure that there is an ancestor tag of a certain class. Only returns if there is.
 	 * @param tagClass desired class
 	 * @return ancestor tag of desired class
 	 */
 	public <A> A requireAncestorTag(Class<A> tagClass) {
 		// TODO decide if this should be converted to an annotation
 		TagStack ts = getTagStack();
 		if (ts.size()>1) {
 			for (int i=ts.size()-2;i>=0;--i) {
 				PolygraphTag tag=ts.get(i);
 				if (tagClass.isInstance(tag)) {
 					@SuppressWarnings("unchecked")
 					A tag2 = (A)tag;
 					return tag2;
 				}
 			}
 		}
 		throw new PolygraphTemplateException(String.format("Ancestor tag of type %s expected but not found.",tagClass.getName()));
 	}
 	
 	/**
 	 * Set a custom attribute of the environment
 	 */
 	public void setCustomAttribute(String key, Object value) {
 		// TODO fix namespace collision
 		env.setCustomAttribute(key, value);
 	}
 	
 	/**
 	 * Get a custom attribute of the environment
 	 */
 	public Object getCustomAttribute(String key) {
 		return env.getCustomAttribute(key);
 	}
 
 	/**
 	 * Determines whether the current tag has a body
 	 * @return
 	 */
 	public boolean hasBody() {
 		return body!=null;
 	}
 
 	/**
 	 * Get an InputStream for a file from the VFS
 	 * @param path path of file in the vfs
 	 * @return input stream
 	 * @throws IOException
 	 */
 	public InputStream getVfsStream(String path) throws IOException {
 		MessageGunTemplateLoader tl = (MessageGunTemplateLoader) env.getConfiguration().getTemplateLoader();
		URL fileUrl = tl.getURL(path);
		if (fileUrl==null)
			throw new PolygraphTemplateException(String.format("Requested file '%s' not found in VFS",path));
		return fileUrl.openStream();
 	}
 
 	/**
 	 * Get a Reader for a file from the VFS
 	 * @param path path of file in the vfs
 	 * @param encoding encoding of the reader
 	 * @return reader
 	 * @throws IOException
 	 */
 	public Reader getVfsReader(String path, String encoding) throws IOException {
 		TemplateLoader tl = env.getConfiguration().getTemplateLoader();
 		return tl.getReader(path, encoding);
 	}
 }
