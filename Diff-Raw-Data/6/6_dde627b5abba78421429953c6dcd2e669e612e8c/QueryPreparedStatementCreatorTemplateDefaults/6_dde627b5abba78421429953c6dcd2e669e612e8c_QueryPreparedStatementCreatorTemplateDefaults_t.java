//;-*- mode: java -*-
 /*
                         QueryJ
 
    Copyright (C) 2002-2006  Jose San Leandro Armendariz
                             chous@acm-sl.org
 
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU General Public
     License as published by the Free Software Foundation; either
     version 2 of the License, or any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     General Public License for more details.
 
     You should have received a copy of the GNU General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
     Thanks to ACM S.L. for distributing this library under the GPL license.
     Contact info: chous@acm-sl.org
     Postal Address: c/Playa de Lagoa, 1
                     Urb. Valdecabanas
                     Boadilla del monte
                     28660 Madrid
                     Spain
 
  ******************************************************************************
  *
  * Filename: $RCSfile: $
  *
  * Author: Jose San Leandro Armendariz
  *
  * Description: Defines the default subtemplates to generate
  *              QueryPreparedStatementCreator sources.
  *
  */
 package org.acmsl.queryj.tools.templates.dao;
 
 /*
  * Importing some project-specific classes.
  */
 import org.acmsl.queryj.tools.templates.JavaTemplateDefaults;
 
 /**
  * Defines the default subtemplates to generate QueryPreparedStatementCreator
  * sources.
  * @author <a href="mailto:chous@acm-sl.org"
  *         >Jose San Leandro</a>
  */
 public interface QueryPreparedStatementCreatorTemplateDefaults
     extends  JavaTemplateDefaults
 {
     /**
      * The default header.
      */
     public static final String DEFAULT_HEADER =
           HEADER
         + " *****************************************************************"
         + "*************\n"
         + " *\n"
         + " * Filename: $" + "RCSfile: $\n"
         + " *\n"
         + " * Author: QueryJ\n"
         + " *\n"
         + " * Description: Creates the <code>PreparedStatement</code>\n"
         + " *              required to perform any <i>JDBC</i> operation.\n"
         + " *\n"
         + " */\n";
 
     /**
      * The package declaration.
      */
     public static final String DEFAULT_PACKAGE_DECLARATION =
         "package {0};\n\n"; // package
 
     /**
      * The ACM-SL imports.
      */
     public static final String DEFAULT_ACMSL_IMPORTS =
           "\n/*\n"
         + " * Importing some ACM-SL classes.\n"
         + " */\n"
         + "import org.acmsl.queryj.Query;\n\n";
 
     /**
      * Additional imports.
      */
     public static final String DEFAULT_ADDITIONAL_IMPORTS =
           "\n/*\n"
         + " * Importing Spring classes.\n"
         + " */\n"
         + "import org.springframework.jdbc.core.PreparedStatementCreator;\n\n";
     
     /**
      * The JDK imports.
      */
     public static final String DEFAULT_JDK_IMPORTS =
           "/*\n"
         + " * Importing some JDK classes.\n"
         + " */\n"
         + "import java.sql.Connection;\n"
         + "import java.sql.PreparedStatement;\n"
         + "import java.sql.SQLException;\n\n";
 
     /**
      * The default class Javadoc.
      */
     public static final String DEFAULT_JAVADOC =
           "/**\n"
         + " * Creates the <code>PreparedStatement</code> required to perform any\n"
         + " * <i>JDBC</i> operation.\n"
         + " * @author <a href=\"http://maven.acm-sl.org/queryj\">QueryJ</a>\n"
         + " */\n";
 
     /**
      * The class definition.
      */
     public static final String DEFAULT_CLASS_DEFINITION =
           "public class QueryPreparedStatementCreator\n"
         + "    implements  PreparedStatementCreator\n";
 
     /**
      * The class start.
      */
     public static final String DEFAULT_CLASS_START =
           "{\n";
 
     /**
      * The class constructor.
      */
     public static final String DEFAULT_CLASS_CONSTRUCTOR =
           "    /**\n"
         + "     * The configured query.\n"
         + "     */\n"
         + "    private Query m__Query;\n\n"
         + "    /**\n"
         + "     * The SQL query.\n"
         + "     */\n"
         + "    private String m__strSql;\n\n"
         + "    /**\n"
         + "     * Builds a <code>QueryPreparedStatementCreator</code>\n"
         + "     * for given query.\n"
         + "     * @param query the query.\n"
         + "     * @precondition query != null\n"
         + "     */\n"
         + "    public QueryPreparedStatementCreator(final Query query)\n"
         + "    {\n"
         + "        immutableSetQuery(query);\n"
         + "    }\n\n"
         + "    /**\n"
         + "     * Builds a <code>QueryPreparedStatementCreator</code>\n"
         + "     * for given query.\n"
         + "     * @param query the query.\n"
         + "     * @precondition query != null\n"
         + "     */\n"
         + "    public QueryPreparedStatementCreator(final String query)\n"
         + "    {\n"
         + "        immutableSetSql(query);\n"
         + "    }\n\n"
         + "    /**\n"
         + "     * Specifies the query.\n"
         + "     * @param query such query.\n"
         + "     */\n"
         + "    protected final void immutableSetQuery(final Query query)\n"
         + "    {\n"
         + "        m__Query = query;\n"
         + "    }\n\n"
         + "    /**\n"
         + "     * Specifies the query.\n"
         + "     * @param query such query.\n"
         + "     */\n"
         + "    protected void setQuery(final Query query)\n"
         + "    {\n"
         + "        immutableSetQuery(query);\n"
         + "    }\n\n"
         + "    /**\n"
         + "     * Retrieves the query.\n"
         + "     * @return such information.\n"
         + "     */\n"
         + "    public Query getQuery()\n"
         + "    {\n"
         + "        return m__Query;\n"
         + "    }\n\n"
         + "    /**\n"
         + "     * Specifies the SQL query.\n"
         + "     * @param query such query.\n"
         + "     */\n"
         + "    protected final void immutableSetSql(final String query)\n"
         + "    {\n"
         + "        m__strSql = query;\n"
         + "    }\n\n"
         + "    /**\n"
         + "     * Specifies the query.\n"
         + "     * @param query such query.\n"
         + "     */\n"
         + "    protected void setSql(final String query)\n"
         + "    {\n"
         + "        immutableSetSql(query);\n"
         + "    }\n\n"
         + "    /**\n"
         + "     * Retrieves the query.\n"
         + "     * @return such information.\n"
         + "     */\n"
         + "    public String getSql()\n"
         + "    {\n"
         + "        return m__strSql;\n"
         + "    }\n\n";
 
     // <helper subtemplates>
 
     /**
      * The createPreparedStatement method.
      */
     public static final String DEFAULT_CREATE_PREPARED_STATEMENT_METHOD =
           "     // <create prepared statement>\n\n"
         + "    /**\n"
         + "     * Creates the prepared statement required to accomplish the\n"
         + "     * <i>SQL</i> operation.\n"
         + "     * @param connection the connection.\n"
         + "     * @return the <code>PreparedStatement</code> instance.\n"
         + "     * @throws SQLException if something wrong occurs. <b>Spring</b> takes\n"
         + "     * care of this.\n"
         + "     * @precondition connection != null\n"
         + "     */\n"
         + "    public PreparedStatement createPreparedStatement(\n"
         + "        final Connection connection)\n"
         + "      throws SQLException\n"
         + "    {\n"
         + "        return\n"
         + "            createPreparedStatement(\n"
         + "                connection, getQuery(), getSql());\n"
         + "    }\n\n"
         + "    /**\n"
         + "     * Creates the prepared statement required to accomplish the\n"
         + "     * <i>SQL</i> operation.\n"
         + "     * @param connection the connection.\n"
         + "     * @param query the query.\n"
         + "     * @param sql the sql query.\n"
         + "     * @return the <code>PreparedStatement</code> instance.\n"
         + "     * @throws SQLException if something wrong occurs. <b>Spring</b> takes\n"
         + "     * care of this.\n"
         + "     * @precondition connection != null\n"
         + "     * @precondition (query != null) || (sql != null)\n"
         + "     */\n"
         + "    protected PreparedStatement createPreparedStatement(\n"
         + "        final Connection connection,\n"
         + "        final Query query,\n"
         + "        final String sql)\n"
         + "      throws SQLException\n"
         + "    {\n"
         + "        PreparedStatement result = null;\n\n"
         + "        if  (query != null)\n"
         + "        {\n"
         + "            result = query.prepareStatement(connection);\n"
         + "        }\n"
         + "        else if  (sql != null)\n"
         + "        {\n"
         + "            result = connection.prepareStatement(sql);\n"
         + "        }\n\n"
         + "        return result;\n"
         + "    }\n"
         + "    // </create prepared statement>\n";
 
     /**
      * The default class end.
      */
     public static final String DEFAULT_CLASS_END = "}\n";
 }
