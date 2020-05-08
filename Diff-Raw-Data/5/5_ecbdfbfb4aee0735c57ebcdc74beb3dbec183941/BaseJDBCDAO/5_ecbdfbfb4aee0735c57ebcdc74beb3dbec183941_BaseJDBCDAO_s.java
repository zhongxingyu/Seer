 /**
  * Copyright Notice. Copyright 2008 ScenPro, Inc ("caBIG(TM)
  * Participant").caXchange was created with NCI funding and is part of the
  * caBIG(TM) initiative. The software subject to this notice and license includes
  * both human readable source code form and machine readable, binary, object
  * code form (the "caBIG(TM) Software"). This caBIG(TM) Software License (the
  * "License") is between caBIG(TM) Participant and You. "You (or "Your") shall mean
  * a person or an entity, and all other entities that control, are controlled
  * by, or are under common control with the entity. "Control" for purposes of
  * this definition means (i) the direct or indirect power to cause the direction
  * or management of such entity, whether by contract or otherwise, or (ii)
  * ownership of fifty percent (50%) or more of the outstanding shares, or (iii)
  * beneficial ownership of such entity. License. Provided that You agree to the
  * conditions described below, caBIG(TM) Participant grants You a non-exclusive,
  * worldwide, perpetual, fully-paid-up, no-charge, irrevocable, transferable and
  * royalty-free right and license in its rights in the caBIG(TM) Software,
  * including any copyright or patent rights therein, to (i) use, install,
  * disclose, access, operate, execute, reproduce, copy, modify, translate,
  * market, publicly display, publicly perform, and prepare derivative works of
  * the caBIG(TM) Software in any manner and for any purpose, and to have or permit
  * others to do so; (ii) make, have made, use, practice, sell, and offer for
  * sale, import, and/or otherwise dispose of caBIG(TM) Software (or portions
  * thereof); (iii) distribute and have distributed to and by third parties the
  * caBIG(TM) Software and any modifications and derivative works thereof; and (iv)
  * sublicense the foregoing rights set out in (i), (ii) and (iii) to third
  * parties, including the right to license such rights to further third parties.
  * For sake of clarity, and not by way of limitation, caBIG(TM) Participant shall
  * have no right of accounting or right of payment from You or Your sublicensees
  * for the rights granted under this License. This License is granted at no
  * charge to You. Your downloading, copying, modifying, displaying, distributing
  * or use of caBIG(TM) Software constitutes acceptance of all of the terms and
  * conditions of this Agreement. If you do not agree to such terms and
  * conditions, you have no right to download, copy, modify, display, distribute
  * or use the caBIG(TM) Software. 1. Your redistributions of the source code for
  * the caBIG(TM) Software must retain the above copyright notice, this list of
  * conditions and the disclaimer and limitation of liability of Article 6 below.
  * Your redistributions in object code form must reproduce the above copyright
  * notice, this list of conditions and the disclaimer of Article 6 in the
  * documentation and/or other materials provided with the distribution, if any.
  * 2. Your end-user documentation included with the redistribution, if any, must
  * include the following acknowledgment: "This product includes software
  * developed by ScenPro, Inc." If You do not include such end-user
  * documentation, You shall include this acknowledgment in the caBIG(TM) Software
  * itself, wherever such third-party acknowledgments normally appear. 3. You may
  * not use the names "ScenPro, Inc", "The National Cancer Institute", "NCI",
  * "Cancer Bioinformatics Grid" or "caBIG(TM)" to endorse or promote products
  * derived from this caBIG(TM) Software. This License does not authorize You to use
  * any trademarks, service marks, trade names, logos or product names of either
  * caBIG(TM) Participant, NCI or caBIG(TM), except as required to comply with the
  * terms of this License. 4. For sake of clarity, and not by way of limitation,
  * You may incorporate this caBIG(TM) Software into Your proprietary programs and
  * into any third party proprietary programs. However, if You incorporate the
  * caBIG(TM) Software into third party proprietary programs, You agree that You are
  * solely responsible for obtaining any permission from such third parties
  * required to incorporate the caBIG(TM) Software into such third party proprietary
  * programs and for informing Your sublicensees, including without limitation
  * Your end-users, of their obligation to secure any required permissions from
  * such third parties before incorporating the caBIG(TM) Software into such third
  * party proprietary software programs. In the event that You fail to obtain
  * such permissions, You agree to indemnify caBIG(TM) Participant for any claims
  * against caBIG(TM) Participant by such third parties, except to the extent
  * prohibited by law, resulting from Your failure to obtain such permissions. 5.
  * For sake of clarity, and not by way of limitation, You may add Your own
  * copyright statement to Your modifications and to the derivative works, and
  * You may provide additional or different license terms and conditions in Your
  * sublicenses of modifications of the caBIG(TM) Software, or any derivative works
  * of the caBIG(TM) Software as a whole, provided Your use, reproduction, and
  * distribution of the Work otherwise complies with the conditions stated in
  * this License. 6. THIS caBIG(TM) SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED
  * OR IMPLIED WARRANTIES (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE)
  * ARE DISCLAIMED. IN NO EVENT SHALL THE ScenPro, Inc OR ITS AFFILIATES BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS caBIG(TM) SOFTWARE, EVEN IF ADVISED OF
  * THE POSSIBILITY OF SUCH DAMAGE.
  */
 package gov.nih.nci.ctom.ctlab.persistence;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 
 /**
  * @author griffinch
  */
 public abstract class BaseJDBCDAO
 {
 
 	private static final String CONFIG_FILE = "/dataConnection.properties";
 
 	// Logging File
 	private static Logger log = Logger.getLogger("client");
 
 	/**
 	 * Gets the database Connection
 	 * 
 	 * @return Connection
 	 */
 	public Connection getConnection()
 	{
 
 		Connection result = null;
 		Properties props = new Properties();
 		// Get the file input stream
 		try
 		{
 			InputStream stream = getClass().getResourceAsStream(CONFIG_FILE);
 			props.load(stream);
 			stream.close();
 		}
 		catch (FileNotFoundException e1)
 		{
 			log.error("The config file not found: " + CONFIG_FILE);
 		}
 		catch (IOException e1)
 		{
 			log.error("Error reading the config file: " + CONFIG_FILE);
 		}
 		// Read the properties from the properties file
 		String fDriverName = (String) props.getProperty("driver");
 		String fDbName = (String) props.getProperty("url");
 		String fUserName = (String) props.getProperty("user");
 		String fPassword = (String) props.getProperty("passwd");
 		try
 		{
 			Class.forName(fDriverName).newInstance();
 			log.debug("Check classpath. loaded db driver: " + fDriverName);
 		}
 		catch (Exception ex)
 		{
 			log.error("Check classpath. Cannot load db driver: " + fDriverName);
 		}
 
 		try
 		{ // get the Connection
 			result = DriverManager.getConnection(fDbName, fUserName, fPassword);
 			if (result != null)
 			{
 				log.info("Connection to db obtained");
 
 			}
 			else
 			{
 				log.error("Unable to obtain connection to the db");
 				throw (new NullPointerException("Null Connection object"));
 			}
 
 		}
 		catch (SQLException e)
 		{
 			log.error("Driver loaded, but cannot connect to db: " + fDbName);
 			log.error("Driver loaded, but cannot connect to db: " + e.getLocalizedMessage());
 		}
 		
 		return result;
 	}
 
 	/**
 	 * Retrieves the next Id value from the database. Executes the query
 	 * depending on the database.
 	 * 
 	 * @param con
 	 *            Connection
 	 * @param seq
 	 *            Sequence used to retrieve the next Id
 	 * @return Id
 	 */
 	public final Long getNextVal(Connection con, String seq) throws SQLException
 	{
 
 		Long nextValue = null;
 		Statement stmt = con.createStatement();
 		ResultSet rs = null;
 		try
 		{
 
 			DatabaseMetaData metaData = con.getMetaData();
 			String databaseName = metaData.getDatabaseProductName();
 			//retrieve the next id to insert into database table  for Oracle
			if (databaseName.indexOf("Oracle") > 0)
 			{
 
				rs = stmt.executeQuery("select" + seq + ".nextval from dual");
 				rs.next();
 				nextValue = rs.getLong(1);
 			}
 			//retrieve the next id to insert into database table  for Postgres
 			else
 			{
 				rs = stmt.executeQuery("select nextval('" + seq + "')");
 				rs.next();
 				nextValue = rs.getLong(1);
 
 			}
 		}
 		finally
 		{
 			rs = SQLHelper.closeResultSet(rs);
 			stmt = SQLHelper.closeStatement(stmt);
 		}
 		return nextValue;
 	}
 
 }
