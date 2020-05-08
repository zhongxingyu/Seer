 /*
  * Copyright (C) 2006-2010 Thomas Chemineau
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package net.aepik.casl.core.ldap.parser;
 
 import net.aepik.casl.core.ldap.Schema;
 import net.aepik.casl.core.ldap.SchemaObject;
 import net.aepik.casl.core.ldap.SchemaSyntax;
 import net.aepik.casl.core.ldap.SchemaValue;
 import java.io.IOException;
 
 /**
  * Write ldap definitions in a Openldap compliant format.
  */
 public class OpenldapWriter extends RFCWriter
 {
 
 	/**
 	 * Build a new OpenldapWriter object.
 	 */
 	public OpenldapWriter ()
 	{
 		super();
 	}
 
 	/**
 	 * Write contents onto output flow.
 	 */
 	public void write ( Schema schema ) throws IOException
 	{
 		if (output == null)
 		{
 			return;
 		}
 		if (schema == null)
 		{
 			return;
 		}
 		if (schema.getSyntax() == null)
 		{
 			return;
 		}
 		output.newLine();
 		super.write(schema);
 	}
 
 	/**
 	 * Return the string representation of a SchemaObject object.
 	 * @param object A SchemaObject object.
 	 * @return String Its String representation.
 	 */
 	public String valueOf (SchemaObject object)
 	{
 		if (object == null)
 		{
 			return "";
 		}
 		String str = "";
                 String eol = System.getProperty("line.separator");
                 String type = object.getType();
                 SchemaSyntax syntax = object.getSyntax();
                 if (type.equals(syntax.getObjectIdentifierType()))
                 {
                         String[] keys = object.getKeys();
                         SchemaValue value = object.getValue(keys[0]);
                         str = keys[0] + " " + value.toString();
                 }
 		else
 		{
 	                String[] params_name = syntax.getParameters(type);
                 	str = object.getId() + eol;
 	                for (int i = 0; i < params_name.length; i++)
 	                {
 	                        if (object.isKeyExists(params_name[i]))
 	                        {
 	                                str += "\t" + params_name[i] + " " + object.getValue(params_name[i]) + eol;
 	                        }
 	                }
 	                str = str.trim();
 			str = "\t" + str;
 		}
 		if (type.equals(syntax.getObjectClassType()))
 		{
			str = syntax.getObjectClassHeader() + " (" + eol + str + eol + "\t)";
 		}
 		if (type.equals(syntax.getAttributeType()))
 		{
			str = syntax.getAttributeHeader() + " (" + eol + str + eol + "\t)";
 		}
 		if (type.equals(syntax.getObjectIdentifierType()))
 		{
 			str = syntax.getObjectIdentifierHeader() + " " + str;
 		}
 		str = "# " + object.getId() + eol + str + eol;
 		return str;
 	}
 
 }
