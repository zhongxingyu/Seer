 /*##############################################################################
 
 Copyright (C) 2011 HPCC Systems.
 
 All rights reserved. This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU Affero General Public License for more details.
 
 You should have received a copy of the GNU Affero General Public License
 along with this program. If not, see <http://www.gnu.org/licenses/>.
 ############################################################################## */
 
 package org.hpccsystems.jdbcdriver;
 
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.regex.Matcher;
 
 import org.hpccsystems.jdbcdriver.ECLFunction.FunctionType;
 
 public class SQLFragment
 {
     public enum FragmentType
     {
         UNKNOWN_TYPE,
         NUMERIC_FRAGMENT,
         LITERAL_STRING,
         PARAMETERIZED,
         FIELD,
         LIST,
         CONTENT_MODIFIER,
         FIELD_CONTENT_MODIFIER,
         AGGREGATE_FUNCTION;
     }
 
     private String fnname   =   null;
     private String parent   =   null;
     private String value    =   null;
     private FragmentType type = FragmentType.UNKNOWN_TYPE;
 
     public SQLFragment() {}
     public SQLFragment(String framentStr)
     {
         parseExpressionFragment(framentStr);
     }
 
     public boolean isParameterized()
     {
         return type == FragmentType.PARAMETERIZED;
     }
     public String getParent()
     {
         return parent;
     }
 
     public void setParent(String parent)
     {
         this.parent = parent.toUpperCase();
     }
 
     public String getValue()
     {
         if (type == FragmentType.CONTENT_MODIFIER || type == FragmentType.FIELD_CONTENT_MODIFIER)
             return fnname + "( " + value + " )";
         else
             return value;
     }
 
     public void setValue(String value)
     {
         this.value = value;
     }
 
     public FragmentType getType()
     {
         return type;
     }
 
     public void setType(FragmentType type)
     {
         this.type = type;
     }
 
     private void handleFieldType(String fragment)
     {
         String fragsplit[] = fragment.split("\\.", 2);
         if (fragsplit.length == 1)
         {
             setValue(fragsplit[0]);
         }
         else
         {
             setParent(fragsplit[0]);
             setValue(fragsplit[1]);
         }
     }
 
     public void parseExpressionFragment(String fragment)
     {
         try
         {
             this.type = determineFragmentType(fragment);
 
             switch (type)
             {
                 case LITERAL_STRING:
                     fragment = HPCCJDBCUtils.replaceSQLwithECLEscapeChar(fragment);
                 case NUMERIC_FRAGMENT:
                 case PARAMETERIZED:
                     setValue(fragment);
                     break;
                 case FIELD:
                     handleFieldType(fragment);
                     break;
                 case LIST:
 
                     if (HPCCJDBCUtils.hasPossibleEscapedQuoteLiteral(fragment))
                     {
                         StringBuilder tmp = new StringBuilder();
 
                         StringTokenizer comatokens = new StringTokenizer(HPCCJDBCUtils.getParenContents(fragment), ",");
 
                         while (comatokens.hasMoreTokens())
                         {
                             if (tmp.length() == 0)
                                 tmp.append("[");
                             else
                                 tmp.append(", ");
 
                             tmp.append(HPCCJDBCUtils.replaceSQLwithECLEscapeChar(comatokens.nextToken().trim()));
                         }
                         tmp.append("]");
 
                         setValue(tmp.toString());
                     }
                     else
                       setValue("[" + HPCCJDBCUtils.getParenContents(fragment) + "]");
 
                     break;
                 case AGGREGATE_FUNCTION:
                     Matcher matcher = HPCCJDBCUtils.AGGFUNCPATTERN.matcher(fragment);
 
                     if (matcher.matches())
                     {
                         ECLFunction func = ECLFunctions.getEclFunction(matcher.group(1));
 
                         if (func == null)
                            System.out.println("Function found in HAVING cluase might not be supported.");
                         else
                         {
                             if (func.getFunctionType() == FunctionType.CONTENT_MODIFIER)
                             {
                                 this.type = FragmentType.CONTENT_MODIFIER;
 
                                 setFnname(func.getEclFunction());
                                 String subfragment = matcher.group(3).trim();
                                 FragmentType subfragtype = determineFragmentType(subfragment);
                                 switch (subfragtype)
                                 {
                                     case FIELD:
                                         handleFieldType(subfragment);
                                         this.type = FragmentType.FIELD_CONTENT_MODIFIER;
                                         break;
                                     case LITERAL_STRING:
                                     case NUMERIC_FRAGMENT:
                                     case PARAMETERIZED:
                                         setValue(subfragment);
                                         break;
                                 }
                             }
                             else
                             {
                                 String fnname = matcher.group(1).trim();
                                 setParent(fnname);
                                 setValue(fnname+"out");
                             }
                         }
                     }
                     break;
                 default:
                     break;
             }
         }
         catch (Exception e)
         {
             System.out.println("Error while parsing SQL fragment: " + fragment);
         }
     }
 
     public static SQLFragment createExpressionFragment(String fragment)
     {
         SQLFragment frag = new SQLFragment();
 
         frag.parseExpressionFragment(fragment);
 
         return frag;
     }
 
     public static FragmentType determineFragmentType (String fragStr)
     {
         if (fragStr == null || fragStr.length() <= 0)
         {
             return FragmentType.UNKNOWN_TYPE;
         }
         else if (HPCCJDBCUtils.isParameterizedStr(fragStr))
         {
             return FragmentType.PARAMETERIZED;
         }
         else if (HPCCJDBCUtils.isLiteralString(fragStr))
         {
             return FragmentType.LITERAL_STRING;
         }
         else if (HPCCJDBCUtils.isNumeric(fragStr))
         {
             return FragmentType.NUMERIC_FRAGMENT;
         }
         else if (HPCCJDBCUtils.isInParenthesis(fragStr))
         {
             return FragmentType.LIST;
         }
         else if (HPCCJDBCUtils.isAggFunction(fragStr))
         {
             return FragmentType.AGGREGATE_FUNCTION;
         }
         else
         {
             return FragmentType.FIELD;
         }
     }
 
     public String getFullColumnName()
     {
         if (type == FragmentType.FIELD)
             return getParent() + "." + getValue();
         else
             return getValue();
     }
 
     public void updateFragmentColumParent(List<SQLTable> sqlTables) throws Exception
     {
         if (type == FragmentType.FIELD || type == FragmentType.FIELD_CONTENT_MODIFIER)
         {
             if (parent != null && parent.length() > 0)
             {
                 setParent(searchForPossibleTableName(sqlTables));
             }
             else if (sqlTables.size() == 1)
             {
                 setParent(sqlTables.get(0).getName());
             }
             else
             {
                 throw new Exception("Ambiguous field found: " + getValue());
             }
         }
     }
 
     /**
      * Returns table name if the tablename or alias match Otherwise
      * throw exception
      */
     private String searchForPossibleTableName(List<SQLTable> sqlTables) throws Exception
     {
         for (int i = 0; i < sqlTables.size(); i++)
         {
             SQLTable currTable = sqlTables.get(i);
             if (parent.equalsIgnoreCase(currTable.getAlias()) || parent.equalsIgnoreCase(currTable.getName()))
                 return currTable.getName();
         }
 
         throw new Exception("Invalid field found: " + getFullColumnName());
     }
     public String getFnname()
     {
         return fnname;
     }
     public void setFnname(String fnname)
     {
         this.fnname = fnname;
     }
 }
