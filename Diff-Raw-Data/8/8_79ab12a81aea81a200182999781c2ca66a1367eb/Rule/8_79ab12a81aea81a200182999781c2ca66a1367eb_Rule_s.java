 package enzet.moire.core;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Rule
  *
  * @author Sergey Vartanov (me@enzet.ru)
  */
 public class Rule
 {
     private String name;
     private int parameters;
     
     private Object[] elements;
 	
 	class Parameter
 	{
 		int number;
 		boolean isClear;
 
 		public Parameter(int number, boolean isClear)
 		{
 			this.number = number;
 			this.isClear = isClear;
 		}
 	}
 	
 	class Function
 	{
 		String text;
 		boolean isReturn;
 
 		public Function(String text, boolean isReturn)
 		{
 			this.text = text.replaceAll("\\\\", "arg");
 			this.isReturn = isReturn;
 		}
 	}
     
     public Rule(String key, String value)
     {
         name = key.substring(0, key.indexOf(" "));
         parameters = Integer.parseInt(key.substring(key.indexOf(" ")).trim());
         elements = parseElements(value);
     }
     
     public String convert(Word word, Format format)
     {
         StringBuilder returned = new StringBuilder();
         
 		int methods = 0;
 		
 		String[] param = new String[parameters];
 		
 		for (int i = 0; i < parameters; i++)
 		{
 			param[i] = word.getParameter(i, format, false);
 		}
 		for (Object element : elements)
 		{
 			if (element instanceof String)
 			{
 				returned.append(element);
 			}
 			else if (element instanceof Parameter)
 			{
 				if (((Parameter) element).isClear)
 				{
 					returned.append(word.getParameter(((Parameter) element).number - 1, format, true));
 				}
 				else
 				{
 					returned.append(param[((Parameter) element).number - 1]);
 				}
 			}
 			else if (element instanceof Function)
 			{
 				methods++;
 				
 				try
 				{
 					Class c = Class.forName("enzet.moire.Inner$" + format.name.toUpperCase());
 					Class s = String.class;
 					Class[] p = new Class[parameters];
 
 					for (int i = 0; i < parameters; i++)
 					{
 						p[i] = s;
 					}
 					Method m = c.getMethod("method_" + name + "_" + parameters + "_" + methods, p);
 					
 					if (((Function) element).isReturn)
 					{
 						returned.append(m.invoke(null, (Object[]) param));
 					}
 					else
 					{
 						m.invoke(null, (Object[]) param);
 					}
 				}
 				catch (Exception ex)
 				{
 					System.err.println("Inner class error.");
 					ex.printStackTrace();
 				}
 			}
 		}
		
        for (int i = 0; i < parameters; i++)
        {
        }
         return returned.toString();
     }
 	
 	private Object[] parseElements(String text)
 	{
 		List<Object> elements = new ArrayList<Object>();
 		
 		String l = "";
 		
 		for (int i = 0; i < text.length(); i++)
 		{
 			char c = text.charAt(i);
 			
 			switch (c)
 			{
 				case '\\':
 				{
 					if (l.length() > 0)
 					{
 						elements.add(l);
 						l = "";
 					}
 					if (i == text.length() - 1)
 					{
 						System.err.println("Elements parse error.");
 						break;
 					}
 					char c2 = text.charAt(i + 1);
 					if (c2 >= '0' && c2 <= '9')
 					{
 						elements.add(new Parameter(c2 - '0', false));
 						i++;
 						break;
 					}
 					if (c2 == 'c')
 					{
 						elements.add(new Parameter(text.charAt(i + 2) - '0', true));
 						i += 2;
 						break;
 					}
 					
 					break;
 				}
 				case '{':
 					if (l.length() > 0)
 					{
 						elements.add(l);
 						l = "";
 					}
 					i++;
 					while ((c = text.charAt(i)) != '}')
 					{
 						l += c;
 						i++;
 					}
 					elements.add(new Function(l, false));
 					break;
 				case '[':
 					if (l.length() > 0)
 					{
 						elements.add(l);
 						l = "";
 					}
 					i++;
 					while ((c = text.charAt(i)) != ']')
 					{
 						l += c;
 						i++;
 					}
 					elements.add(new Function(l, true));
 					break;
 				default:
 					l += c;
 					break;
 			}
 		}
 		if (l.length() > 0)
 		{
 			elements.add(l);
 		}
 		
 		return elements.toArray(new Object[elements.size()]);
 	}
 	
 	public String generateMethods()
 	{
 		String returned = "";
 		int methods = 0;
 		
 		for (Object element : elements)
 		{
 			if (element instanceof Function)
 			{
 				Function f = (Function) element;
 
 				returned += "\t\tpublic static ";
 				returned += f.isReturn ? "String" : "void";
 				returned += " method_" + name + "_" + parameters + "_" + ++methods + "(";
 				
 				for (int i = 0; i < parameters; i++)
 				{
 					returned += "String arg" + (i + 1);
 					if (i != parameters - 1)
 					{
 						returned += ", ";
 					}
 				}
 				returned += ")\n\t\t{\n\t\t\t";
 				
 				if (f.isReturn)
 				{
 					returned += "return " + f.text + ";";
 				}
 				else
 				{
 					returned += f.text;
 				}
 				returned += "\n\t\t}\n\n";
 			}
 		}
 		return returned;
 	}
 
     // Getters and setters
     
     public String getName()
     {
         return name;
     }
 
     public void setName(String name)
     {
         this.name = name;
     }
 
     public int getParameters()
     {
         return parameters;
     }
 
     public void setParameters(int parameters)
     {
         this.parameters = parameters;
     }
 }
