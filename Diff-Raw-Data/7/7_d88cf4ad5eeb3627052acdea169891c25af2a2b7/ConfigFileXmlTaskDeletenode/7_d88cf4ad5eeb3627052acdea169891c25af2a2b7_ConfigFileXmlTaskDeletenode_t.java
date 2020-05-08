 /*
  * RapidEnv: ConfigFileXmlTaskSetnodevalue.java
  *
  * Copyright (C) 2010 Martin Bluemel
  *
  * Creation Date: 09/08/2010
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the
  * GNU Lesser General Public License as published by the Free Software Foundation;
  * either version 3 of the License, or (at your option) any later version.
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  * You should have received a copies of the GNU Lesser General Public License and the
  * GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.rapidbeans.rapidenv.config.file;
 
 import java.util.logging.Level;
 
 import org.rapidbeans.core.type.TypeRapidBean;
 import org.rapidbeans.rapidenv.RapidEnvInterpreter;
 import org.rapidbeans.rapidenv.config.Configuration;
 import org.rapidbeans.rapidenv.config.Installunit;
 import org.rapidbeans.rapidenv.config.expr.ConfigExprTopLevel;
 import org.w3c.dom.Node;
 
 /**
  * The root of all evil.
  */
 public class ConfigFileXmlTaskDeletenode extends RapidBeanBaseConfigFileXmlTaskDeletenode {
 
 	/**
 	 * Find the node value and delete it.
 	 * 
 	 * @param execute
 	 *            if false only execute the check if the configuration task is
 	 *            necessary if true execute the configuration task if necessary
 	 * @param silent
 	 *            if to execute silent
 	 * 
 	 * @return if the configuration task as been performed properly or not
 	 */
 	@Override
 	public boolean check(final boolean execute, final boolean silent) {
 		boolean ok = true;
 		if (execute) {
 			ok = false;
 		}
 		final RapidEnvInterpreter interpreter = RapidEnvInterpreter.getInstance();
 		final ConfigFileXml fileCfg = (ConfigFileXml) getParentBean();
 		final ConfigFileEditorXml editor = (ConfigFileEditorXml) fileCfg.getEditor();
 		final Node node = editor.retrieveNode(getPath());
 		if (node == null) {
 			if (execute) {
 				final String msg = "    XML node \"" + getPath() + "\"" + " is not existing in file "
 				        + fileCfg.getPathAsFile().getAbsolutePath();
 				RapidEnvInterpreter.log(Level.FINE, msg);
				fileCfg.setIssue(msg);
 				ok = false;
 			} else {
 				final String msg = "    XML node \"" + getPath() + "\" is already deleted in file "
 				        + fileCfg.getPathAsFile().getAbsolutePath();
 				RapidEnvInterpreter.log(Level.FINE, msg);
 				ok = true;
 			}
 		} else {
 			if (execute) {
 				editor.deleteNode(getPath());
 				final String msg = "Deleted XML node \"" + getPath() + "\"" + " from file "
 				        + fileCfg.getPathAsFile().getAbsolutePath();
 				if (!silent) {
 					interpreter.getOut().println(msg);
 				}
 				RapidEnvInterpreter.log(Level.FINE, msg);
 				ok = true;
 			} else {
 				final String msg = "    XML node \"" + getPath() + "\" shoud be deleted deleted in file "
 				        + fileCfg.getPathAsFile().getAbsolutePath();
 				RapidEnvInterpreter.log(Level.FINE, msg);
 				fileCfg.setIssue(msg);
 				ok = false;
 			}
 		}
 		return ok;
 	}
 
 	/**
 	 * Tweaked getter with lazy initialization and expression interpretation.
 	 */
 	public synchronized String getPath() {
 		String path = super.getPath();
 		if (path == null) {
 			return null;
 		}
 		if (super.getPath() != null) {
 			path = interpret(path);
 		}
 		return path;
 	}
 
 	/**
 	 * Interpret a configuration expression.
 	 * 
 	 * @param expression
 	 *            the configuration expression to interpret
 	 * 
 	 * @return the interpreted (expanded) configuration expression
 	 */
 	private String interpret(final String expression) {
 		return new ConfigExprTopLevel((Installunit) getParentBean().getParentBean(), null, expression,
 		        ((Configuration) getParentBean()).getExpressionliteralescaping()).interpret();
 	}
 
 	/**
 	 * default constructor.
 	 */
 	public ConfigFileXmlTaskDeletenode() {
 		super();
 	}
 
 	/**
 	 * constructor out of a string.
 	 * 
 	 * @param s
 	 *            the string
 	 */
 	public ConfigFileXmlTaskDeletenode(final String s) {
 		super(s);
 	}
 
 	/**
 	 * constructor out of a string array.
 	 * 
 	 * @param sa
 	 *            the string array
 	 */
 	public ConfigFileXmlTaskDeletenode(final String[] sa) {
 		super(sa);
 	}
 
 	/**
 	 * the bean's type (class variable).
 	 */
 	private static TypeRapidBean type = TypeRapidBean.createInstance(ConfigFileXmlTaskDeletenode.class);
 
 	/**
 	 * @return the RapidBean's type
 	 */
 	public TypeRapidBean getType() {
 		return type;
 	}
 }
