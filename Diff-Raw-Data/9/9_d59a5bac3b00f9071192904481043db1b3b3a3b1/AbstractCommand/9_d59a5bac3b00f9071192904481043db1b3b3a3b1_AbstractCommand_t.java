 /**
  * Copyright 2013 Simon Curd <simoncurd@gmail.com>
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.hula.lang.commands;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import bsh.Primitive;
 
 import com.hula.lang.HulaConstants;
 import com.hula.lang.model.Command;
 import com.hula.lang.parser.exception.ParseError;
 import com.hula.lang.parser.model.BeanShellScript;
 import com.hula.lang.parser.model.CommandModel;
 import com.hula.lang.parser.util.ParserUtil;
 import com.hula.lang.runtime.RuntimeConnector;
 import com.hula.lang.util.CommandUtil;
 import com.hula.lang.util.DotNotationUtil;
 
 /**
  * This is an abstract implementation of the {@link Command} instance intended for
  * extension by concrete {@link Command} implementations.
  */
 public abstract class AbstractCommand implements Command
 {
 	private static String QT = "\"";
 
 	// parameters specified in the command line
 	protected Map<String, String> signatureParameters = new HashMap<String, String>();
 
 	// the name of the return parameter
 	protected String returnParameter;
 
 	// nested commands
 	protected List<Command> commands = new ArrayList<Command>();
 
 	// the original Hula script line number
 	protected int lineNumber;
 
 	// the original Hula script command line this Command was generated from
 	protected String commandLine;
 
 	// the identifier for this instance of the command
 	protected String instanceId;
 
 	@Override
 	public List<Command> getCommands()
 	{
 		return commands;
 	}
 
 	@Override
 	public void setCommands(List<Command> commands)
 	{
 		this.commands = commands;
 	}
 
 	/**
 	 * Returns a String-based description of this {@link Command}
 	 */
 	@Override
 	public String toString()
 	{
 		return this.getClass().getSimpleName() + " (" + signatureParameters + ") as " + returnParameter;
 	}
 
 	@Override
 	public void setSignatureParameters(Map<String, String> parameters)
 	{
 		this.signatureParameters = parameters;
 	}
 
 	@Override
 	public String getSignatureParameter(String key)
 	{
 		return signatureParameters.get(key);
 	}
 
 	@Override
 	public Map<String, String> getSignatureParameters()
 	{
 		return signatureParameters;
 	}
 
 	@Override
 	public void setSignatureParameter(String name, String value)
 	{
 		this.signatureParameters.put(name, value);
 	}
 
 	@Override
 	public void setReturnParameter(String returnParameter)
 	{
 		this.returnParameter = returnParameter;
 	}
 
 	@Override
 	public String getReturnParameter()
 	{
 		return returnParameter;
 	}
 
 	/**
 	 * Helper method which looks up the parameter value from the command signature.
 	 * When the parameter value is a reference (prepended with $), it will
 	 * lookup the variable value from the runtime variables.<br/>
 	 * <br/>
 	 * For example: <br/>
 	 * <br/>
 	 * CommandName name="MusicProduct" // returns a string object with value "MusicProduct"<br/>
 	 * CommandName name=$typeName // returns the object referenced as 'typeName' in the runtime variables
 	 * 
 	 */
 	public Object getVariableValue(String parameterKey, RuntimeConnector connector)
 	{
		String parameterValue = getSignatureParameter(parameterKey); 
		if (CommandUtil.containsVariableReference(parameterValue))
 		{
 			return CommandUtil.replaceReferences(parameterValue, connector);
 		}
 		else
 		{
 			return parameterValue;
 		}
 
 	}
 
 	/**
 	 * Helper method which sets a variable. If the value is a reference
 	 * (prepended with $) it will lookup the referenced variable.<br/>
 	 * <br/>
 	 * 
 	 * For example:<br/>
 	 * <br/>
 	 * 
 	 * name="John" sets a variable with name=John<br/>
 	 * name=$realname sets name to the value of variable $realname
 	 * 
 	 * @param name The name of the variable to set
 	 * @param value The value of the variable to set
 	 * @param connector The {@link RuntimeConnector} to use
 	 */
 	public void assignVariable(String name, Object value, RuntimeConnector connector)
 	{
 
 		if (value instanceof String && value.toString().startsWith("$"))
 		{
 			value = getVariableValue(value.toString(), connector);
 		}
 
 		// Set: mapping [$script.description] to [Saves a script]
 
 		if (CommandUtil.isVariableReference(name))
 		{
 			// strip off the dollar notation
 			name = name.substring(1);
 
 			if (DotNotationUtil.isPath(name))
 			{
 				// first path item will be context parameter identifier
 				String contextId = DotNotationUtil.getFirstItem(name);
 				Object object = connector.getVariable(contextId);
 
 				DotNotationUtil.setProperty(name, object, value, 1);
 			}
 			else
 			{
 				connector.setVariable(name, value);
 			}
 
 		}
 		else
 		{
 			connector.setVariable(name, value);
 		}
 
 	}
 
 	/**
 	 * Helper method to return a variable value as a {@link String}.
 	 * 
 	 * @param name The name of the variable to find
 	 * @param connector The {@link RuntimeConnector} to use
 	 * @return The value of the variable as a {@link String}
 	 */
 	public String getVariableValueAsString(String name, RuntimeConnector connector)
 	{
 		Object value = getVariableValue(name, connector);
 		if (value instanceof Primitive)
 		{
 			Primitive primitive = (Primitive) value;
 
 			if (primitive.toString().equals("null"))
 			{
 				return null;
 			}
 
 			if (primitive == null || primitive.getType().equals(Void.TYPE))
 			{
 				return null;
 			}
 			return primitive.toString();
 		}
 		if (value != null)
 		{
 			return value.toString();
 		}
 		return null;
 	}
 
 	@Override
 	public int getLineNumber()
 	{
 		return lineNumber;
 	}
 
 	@Override
 	public void setLineNumber(int lineNumber)
 	{
 		this.lineNumber = lineNumber;
 	}
 
 	/**
 	 * Override this to run validation which is specific to the {@link Command} type
 	 * on the configuration of this {@link Command} during parsing
 	 * 
 	 * @param errors A list of errors to update based on the validation
 	 */
 	public void validate(List<ParseError> errors)
 	{
 
 	}
 
 	public void setInstanceId(String instanceId)
 	{
 		this.instanceId = instanceId;
 	}
 
 	public void updateCommandModel(CommandModel cm) throws ParseError
 	{
 		cm.addCommand(this);
 	}
 
 	public void updateBeanShellScript(BeanShellScript script)
 	{
 		addBSHLineNumber(script);
 		String fullClassName = this.getClass().getName();
 		script.add(fullClassName + " " + instanceId + " = new " + fullClassName + "();");
 
 		addBSHSignatureParameters(script);
 
 		script.add(instanceId + ".execute(" + HulaConstants.runtimeConnector + ");");
 		script.add("");
 	}
 
 	protected void addBSHLineNumber(BeanShellScript script)
 	{
 		script.add(HulaConstants.lineNumber + " = " + quoted("" + lineNumber) + ";");
 	}
 
 	protected void addBSHSignatureParameters(BeanShellScript script)
 	{
 		// get parameters
 		// Map<String, String> parameters = ParserUtil.getParametersFromCommandLine(commandLine);
 		for (String key : signatureParameters.keySet())
 		{
 			script.add(instanceId + ".setSignatureParameter(" + quoted(key) + ", " + quoted(signatureParameters.get(key)) + ");");
 		}
 
 		String returnValue = ParserUtil.getReturnParameterFromCommandLine(commandLine);
 
 		if (returnValue != null)
 		{
 			script.add(instanceId + ".setReturnParameter(" + quoted(returnValue) + ");");
 		}
 
 	}
 
 	protected String quoted(String value)
 	{
 		return QT + value + QT;
 	}
 
 	public String getCommandLine()
 	{
 		return commandLine;
 	}
 
 	public void setCommandLine(String commandLine)
 	{
 		this.commandLine = commandLine;
 	}
 
 }
