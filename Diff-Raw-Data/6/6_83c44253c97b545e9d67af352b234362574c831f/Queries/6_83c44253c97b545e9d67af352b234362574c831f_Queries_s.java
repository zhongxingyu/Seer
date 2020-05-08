 /*L
  *  Copyright Ekagra Software Technologies Ltd.
  *  Copyright SAIC, SAIC-Frederick
  *
  *  Distributed under the OSI-approved BSD 3-Clause License.
  *  See http://ncip.github.com/common-security-module/LICENSE.txt for details.
  */
 
 package gov.nih.nci.security.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 
 /**
  *
  *<!-- LICENSE_TEXT_START -->
  *
  *The NCICB Common Security Module (CSM) Software License, Version 3.0 Copyright
  *2004-2005 Ekagra Software Technologies Limited ('Ekagra')
  *
  *Copyright Notice.  The software subject to this notice and license includes both
  *human readable source code form and machine readable, binary, object code form
  *(the 'CSM Software').  The CSM Software was developed in conjunction with the
  *National Cancer Institute ('NCI') by NCI employees and employees of Ekagra.  To
  *the extent government employees are authors, any rights in such works shall be
  *subject to Title 17 of the United States Code, section 105.
  *
  *This CSM Software License (the 'License') is between NCI and You.  'You (or
  *'Your') shall mean a person or an entity, and all other entities that control,
  *are controlled by, or are under common control with the entity.  'Control' for
  *purposes of this definition means (i) the direct or indirect power to cause the
  *direction or management of such entity, whether by contract or otherwise, or
  *(ii) ownership of fifty percent (50%) or more of the outstanding shares, or
  *(iii) beneficial ownership of such entity.
  *
  *This License is granted provided that You agree to the conditions described
  *below.  NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up,
  *no-charge, irrevocable, transferable and royalty-free right and license in its
  *rights in the CSM Software to (i) use, install, access, operate, execute, copy,
  *modify, translate, market, publicly display, publicly perform, and prepare
  *derivative works of the CSM Software; (ii) distribute and have distributed to
  *and by third parties the CSM Software and any modifications and derivative works
  *thereof; and (iii) sublicense the foregoing rights set out in (i) and (ii) to
  *third parties, including the right to license such rights to further third
  *parties.  For sake of clarity, and not by way of limitation, NCI shall have no
  *right of accounting or right of payment from You or Your sublicensees for the
  *rights granted under this License.  This License is granted at no charge to You.
  *
  *1.	Your redistributions of the source code for the Software must retain the
  *above copyright notice, this list of conditions and the disclaimer and
  *limitation of liability of Article 6 below.  Your redistributions in object code
  *form must reproduce the above copyright notice, this list of conditions and the
  *disclaimer of Article 6 in the documentation and/or other materials provided
  *with the distribution, if any.
  *2.	Your end-user documentation included with the redistribution, if any, must
  *include the following acknowledgment: 'This product includes software developed
  *by Ekagra and the National Cancer Institute.'  If You do not include such
  *end-user documentation, You shall include this acknowledgment in the Software
  *itself, wherever such third-party acknowledgments normally appear.
  *
  *3.	You may not use the names 'The National Cancer Institute', 'NCI' 'Ekagra
  *Software Technologies Limited' and 'Ekagra' to endorse or promote products
  *derived from this Software.  This License does not authorize You to use any
  *trademarks, service marks, trade names, logos or product names of either NCI or
  *Ekagra, except as required to comply with the terms of this License.
  *
  *4.	For sake of clarity, and not by way of limitation, You may incorporate this
  *Software into Your proprietary programs and into any third party proprietary
  *programs.  However, if You incorporate the Software into third party proprietary
  *programs, You agree that You are solely responsible for obtaining any permission
  *from such third parties required to incorporate the Software into such third
  *party proprietary programs and for informing Your sublicensees, including
  *without limitation Your end-users, of their obligation to secure any required
  *permissions from such third parties before incorporating the Software into such
  *third party proprietary software programs.  In the event that You fail to obtain
  *such permissions, You agree to indemnify NCI for any claims against NCI by such
  *third parties, except to the extent prohibited by law, resulting from Your
  *failure to obtain such permissions.
  *
  *5.	For sake of clarity, and not by way of limitation, You may add Your own
  *copyright statement to Your modifications and to the derivative works, and You
  *may provide additional or different license terms and conditions in Your
  *sublicenses of modifications of the Software, or any derivative works of the
  *Software as a whole, provided Your use, reproduction, and distribution of the
  *Work otherwise complies with the conditions stated in this License.
  *
  *6.	THIS SOFTWARE IS PROVIDED 'AS IS,' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
  *(INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,
  *NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED.  IN NO
  *EVENT SHALL THE NATIONAL CANCER INSTITUTE, EKAGRA, OR THEIR AFFILIATES BE LIABLE
  *FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  *DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  *SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  *CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
  *TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  *THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  *<!-- LICENSE_TEXT_END -->
  *
  */
 
 /**
  * This class is a helper class which provides Queries in PreparedStatement format for optimised querying.
  *
  */
 public class Queries {
 
 	protected static PreparedStatement getQueryForUserAndGroupForAttribute(String loginName,
 			                                 String objectId,
 											 String attribute,
 											 String privilegeName,
 											 int application_id, Connection cn) throws SQLException{
 
 
 		  StringBuffer stbr = new StringBuffer();
 		  stbr.append("and pe.object_id=?");
 		  stbr.append(" and pe.attribute=?");
 		  stbr.append(" and u.login_name=?");
 		  stbr.append(" and p.privilege_name=?");
 		  //stbr.append(" and pg.application_id=?");
 		  stbr.append(" and pe.application_id=?");
 
 		  StringBuffer sqlBfr = new StringBuffer();
 		  sqlBfr.append(getStaticStringForUserAndGroupForAttribute());
 		  sqlBfr.append(stbr.toString());
 		  sqlBfr.append(" union ");
 		  sqlBfr.append(getStaticStringForUserAndGroupForAttribute2());
 		  sqlBfr.append(stbr.toString());
 
 
 
 		  int i=1;
 		  PreparedStatement pstmt = cn.prepareStatement(sqlBfr.toString());
 		  pstmt.setString(i++,objectId );
 		  pstmt.setString(i++,attribute);
 		  pstmt.setString(i++,loginName);
 		  pstmt.setString(i++,privilegeName);
 		  //pstmt.setInt(i++,application_id);
 		  pstmt.setInt(i++,application_id);
 
 		  pstmt.setString(i++,objectId );
 		  pstmt.setString(i++,attribute);
 		  pstmt.setString(i++,loginName);
 		  pstmt.setString(i++,privilegeName);
 		  //pstmt.setInt(i++,application_id);
 		  pstmt.setInt(i++,application_id);
 
 		  return pstmt;
 
 	}
 
 	protected static PreparedStatement getQueryForUserAndGroupForAttributeValue(String loginName,
             String objectId,
 			 String attribute,
 			 String attributeValue,
 			 String privilegeName,
 			 int application_id, Connection cn) throws SQLException{
 
 
 		StringBuffer stbr = new StringBuffer();
 		stbr.append("and pe.object_id=?");
 		stbr.append(" and pe.attribute=?");
 		stbr.append(" and pe.attribute_value=?");
 		stbr.append(" and u.login_name=?");
 		stbr.append(" and p.privilege_name=?");
 		//stbr.append(" and pg.application_id=?");
 		stbr.append(" and pe.application_id=?");
 
 		StringBuffer sqlBfr = new StringBuffer();
 		sqlBfr.append(getStaticStringForUserAndGroupForAttribute());
 		sqlBfr.append(stbr.toString());
 		sqlBfr.append(" union ");
 		sqlBfr.append(getStaticStringForUserAndGroupForAttribute2());
 		sqlBfr.append(stbr.toString());
 
 
 
 		int i=1;
 		PreparedStatement pstmt = cn.prepareStatement(sqlBfr.toString());
 		pstmt.setString(i++,objectId );
 		pstmt.setString(i++,attribute);
 		pstmt.setString(i++,attributeValue);
 		pstmt.setString(i++,loginName);
 		pstmt.setString(i++,privilegeName);
 		//pstmt.setInt(i++,application_id);
 		pstmt.setInt(i++,application_id);
 
 		pstmt.setString(i++,objectId );
 		pstmt.setString(i++,attribute);
 		pstmt.setString(i++,attributeValue);
 		pstmt.setString(i++,loginName);
 		pstmt.setString(i++,privilegeName);
 		//pstmt.setInt(i++,application_id);
 		pstmt.setInt(i++,application_id);
 
 		return pstmt;
 
 	}
 
 	protected static PreparedStatement getQueryForCheckPermissionForUserAndGroup(String loginName,
 															            String objectId,
 																		 String privilegeName,
 																		 int application_id, Connection cn) throws SQLException{
 
 		StringBuffer stbr = new StringBuffer();
 		stbr.append("and pe.object_id='").append(objectId).append("'");
 		stbr.append(" and u.login_name='").append(loginName).append("'");
 		stbr.append(" and p.privilege_name='").append(privilegeName).append("'");
 		//stbr.append(" and pg.application_id=").append(application_id);
 		stbr.append(" and pe.application_id=").append(application_id);
 
 		StringBuffer sqlBfr = new StringBuffer();
 		sqlBfr.append(getStaticStringForUserAndGroupForAttribute());
 		sqlBfr.append(stbr.toString());
 		sqlBfr.append(" union ");
 		sqlBfr.append(getStaticStringForUserAndGroupForAttribute2());
 		sqlBfr.append(stbr.toString());
 
 
 		  PreparedStatement pstmt = cn.prepareStatement(sqlBfr.toString());
 		  /*
 		   * int i=1;
 		     pstmt.setString(i++,objectId );
 		  pstmt.setString(i++,loginName);
 		  pstmt.setString(i++,privilegeName);
 		  pstmt.setInt(i++,application_id);
 		  pstmt.setInt(i++,application_id);
 
 		  pstmt.setString(i++,objectId );
 		  pstmt.setString(i++,loginName);
 		  pstmt.setString(i++,privilegeName);
 		  pstmt.setInt(i++,application_id);
 		  pstmt.setInt(i++,application_id);*/
 
 		  return pstmt;
 
 		}
 
 
 	protected static PreparedStatement getQueryForCheckPermissionForGroup(String userName,
 			                                 String objectId,
 											 String privilegeName,
 											 int application_id, Connection cn) throws SQLException{
 		StringBuffer stbr = new StringBuffer();
 		stbr.append("select 'X'");
 		stbr.append("from csm_protection_group pg,");
 		stbr.append("csm_protection_element pe,");
 		stbr.append("csm_pg_pe pgpe,");
 		stbr.append("csm_user_group_role_pg ugrpg,");
 		stbr.append("csm_user u,");
 		stbr.append("csm_group g,");
 		stbr.append("csm_user_group ug,");
 		stbr.append("csm_role_privilege rp,");
 		stbr.append("csm_privilege p ");
 		stbr.append("where pgpe.protection_group_id = pg.protection_group_id ");
 		stbr.append(" and pgpe.protection_element_id = pe.protection_element_id");
 		stbr.append(" and pe.object_id=?");
		stbr.append(" and ugrpg.protection_group_id = ANY (select pg1.protection_group_id from csm_protection_group pg1 where pg1.protection_group_id = pg.protection_group_id or pg1.protection_group_id = (select pg2.parent_protection_group_id from csm_protection_group pg2 where pg2.protection_group_id = pg.protection_group_id))");
 		stbr.append(" and ugrpg.group_id = g.group_id ");
 		stbr.append(" and ug.user_id = u.user_id");
 		stbr.append(" and u.login_name=?");
 		stbr.append(" and ugrpg.role_id = rp.role_id ");
 		stbr.append(" and rp.privilege_id = p.privilege_id");
 		stbr.append(" and p.privilege_name=?");
 		//stbr.append(" and pg.application_id=?");
 		stbr.append(" and pe.application_id=?");
 
 		int i=1;
 		PreparedStatement pstmt = cn.prepareStatement(stbr.toString());
 		pstmt.setString(i++,objectId );
 		pstmt.setString(i++,userName);
 		pstmt.setString(i++,privilegeName);
 		//pstmt.setInt(i++,application_id);
 		pstmt.setInt(i++,application_id);
 
 		return pstmt;
 	}
 
 	protected static PreparedStatement getQueryForCheckPermissionForOnlyGroup(String groupName, String objectId, String privilegeName, int application_id, Connection cn) throws SQLException
 	{
 		StringBuffer stbr = new StringBuffer();
 		stbr.append("SELECT 'X'");
 		stbr.append(" FROM csm_protection_group pg,");
 		stbr.append(" INNER JOIN csm_pg_pe pgpe ON pgpe.protection_group_id = pg.protection_group_id");
 		stbr.append(" INNER JOIN csm_protection_element pe ON pe.protection_element_id = pgpe.protection_element_id");
 		stbr.append(" INNER JOIN csm_user_group_role_pg ugrpg ON ugrpg.protection_group_id = pgpe.protection_group_id");
 		stbr.append(" INNER JOIN csm_group g ON g.group_id = ugrpg.group_id");
 		stbr.append(" INNER JOIN csm_role_privilege rp ON rp.role_id = ugrpg.role_id");
 		stbr.append(" INNER JOIN csm_privilege p ON p.privilege_id = rp.privilege_id");
 		stbr.append("  WHERE pe.object_id=?");
 		stbr.append(" AND g.group_name=?");
 		stbr.append(" AND p.privilege_name=?");
 		stbr.append(" AND pe.application_id=?");
 
 		int i=1;
 		PreparedStatement pstmt = cn.prepareStatement(stbr.toString());
 		pstmt.setString(i++,objectId );
 		pstmt.setString(i++,groupName);
 		pstmt.setString(i++,privilegeName);
 		pstmt.setInt(i++,application_id);
 
 		return pstmt;
 	}
 
 	protected static PreparedStatement getQueryForAccessibleGroups(String objectId, String privilegeName, int applicationId, Connection cn) throws SQLException
 	{
 		StringBuffer stbr = new StringBuffer();
 		stbr.append("SELECT distinct g.group_id ");
 		stbr.append(" FROM csm_group g ");
 		stbr.append(" INNER JOIN csm_user_group_role_pg ugrpg ON ugrpg.group_id = g.group_id ");
 		stbr.append(" INNER JOIN csm_pg_pe pgpe ON pgpe.protection_group_id = ugrpg.protection_group_id ");
 		stbr.append(" INNER JOIN csm_protection_element pe ON pe.protection_element_id = pgpe.protection_element_id ");
 		stbr.append(" INNER JOIN csm_role_privilege rp ON rp.role_id = ugrpg.role_id ");
 		stbr.append(" INNER JOIN csm_privilege p ON p.privilege_id = rp.privilege_id ");
 		stbr.append("  WHERE pe.object_id=?");
 		stbr.append(" AND p.privilege_name=?");
 		stbr.append(" AND pe.application_id=?");
 
 		int i=1;
 		PreparedStatement pstmt = cn.prepareStatement(stbr.toString());
 		pstmt.setString(i++,objectId );
 		pstmt.setString(i++,privilegeName);
 		pstmt.setInt(i++,new Integer(applicationId).intValue());
 
 		return pstmt;
 	}
 
 	protected static PreparedStatement getQueryForAccessibleGroupsWithAttribute(String objectId, String attribute, String privilegeName, int applicationId, Connection cn) throws SQLException
 	{
 		StringBuffer stbr = new StringBuffer();
 		stbr.append("SELECT distinct g.group_id ");
 		stbr.append(" FROM csm_group g ");
 		stbr.append(" INNER JOIN csm_user_group_role_pg ugrpg ON ugrpg.group_id = g.group_id ");
 		stbr.append(" INNER JOIN csm_pg_pe pgpe ON pgpe.protection_group_id = ugrpg.protection_group_id ");
 		stbr.append(" INNER JOIN csm_protection_element pe ON pe.protection_element_id = pgpe.protection_element_id ");
 		stbr.append(" INNER JOIN csm_role_privilege rp ON rp.role_id = ugrpg.role_id ");
 		stbr.append(" INNER JOIN csm_privilege p ON p.privilege_id = rp.privilege_id ");
 		stbr.append("  WHERE pe.object_id=?");
 		stbr.append(" AND pe.attribute=?");
 		stbr.append(" AND p.privilege_name=?");
 		stbr.append(" AND pe.application_id=?");
 
 		int i=1;
 		PreparedStatement pstmt = cn.prepareStatement(stbr.toString());
 		pstmt.setString(i++,objectId );
 		pstmt.setString(i++,attribute );
 		pstmt.setString(i++,privilegeName);
 		pstmt.setInt(i++,new Integer(applicationId).intValue());
 
 		return pstmt;
 	}
 
 
 	/**
 	 * getQueryForCheckPermissionForOnlyGroup for Object Id, Attribute Name
 	 *
 	 * @param groupName
 	 * @param objectId
 	 * @param attributeName
 	 * @param privilegeName
 	 * @param application_id
 	 * @param cn
 	 * @return
 	 * @throws SQLException
 	 */
 	protected static PreparedStatement getQueryForCheckPermissionForOnlyGroup(String groupName, String objectId, String attributeName, String privilegeName, int application_id, Connection cn) throws SQLException
 	{
 		StringBuffer stbr = new StringBuffer();
 		stbr.append("SELECT 'X'");
 		stbr.append(" FROM csm_protection_group pg");
 		stbr.append(" INNER JOIN csm_pg_pe pgpe ON pgpe.protection_group_id = pg.protection_group_id");
 		stbr.append(" INNER JOIN csm_protection_element pe ON pe.protection_element_id = pgpe.protection_element_id");
 		stbr.append(" INNER JOIN csm_user_group_role_pg ugrpg ON ugrpg.protection_group_id = pgpe.protection_group_id");
 		stbr.append(" INNER JOIN csm_group g ON g.group_id = ugrpg.group_id");
 		stbr.append(" INNER JOIN csm_role_privilege rp ON rp.role_id = ugrpg.role_id");
 		stbr.append(" INNER JOIN csm_privilege p ON p.privilege_id = rp.privilege_id ");
 		stbr.append("  WHERE pe.object_id=?");
 		stbr.append(" AND pe.attribute=?");
 		stbr.append(" AND g.group_name=?");
 		stbr.append(" AND p.privilege_name=?");
 		stbr.append(" AND pe.application_id=?");
 
 		int i=1;
 		PreparedStatement pstmt = cn.prepareStatement(stbr.toString());
 		pstmt.setString(i++,objectId );
 		pstmt.setString(i++,attributeName );
 		pstmt.setString(i++,groupName );
 		pstmt.setString(i++,privilegeName);
 		pstmt.setInt(i++,application_id);
 
 		return pstmt;
 	}
 
 	/**
 	 * getQueryForCheckPermissionForOnlyGroup for Object Id, Attribute and AttributeValue
 	 *
 	 * @param groupName
 	 * @param objectId
 	 * @param attributeName
 	 * @param attributeValue
 	 * @param privilegeName
 	 * @param application_id
 	 * @param cn
 	 * @return
 	 * @throws SQLException
 	 */
 	protected static PreparedStatement getQueryForCheckPermissionForOnlyGroup(String groupName, String objectId, String attributeName, String attributeValue, String privilegeName, int application_id, Connection cn) throws SQLException
 	{
 		StringBuffer stbr = new StringBuffer();
 		stbr.append("SELECT 'X'");
 		stbr.append(" FROM csm_protection_group pg,");
 		stbr.append(" INNER JOIN csm_pg_pe pgpe ON pgpe.protection_group_id = pg.protection_group_id");
 		stbr.append(" INNER JOIN csm_protection_element pe ON pe.protection_element_id = pgpe.protection_element_id");
 		stbr.append(" INNER JOIN csm_user_group_role_pg ugrpg ON ugrpg.protection_group_id = pgpe.protection_group_id");
 		stbr.append(" INNER JOIN csm_group g ON g.group_id = ugrpg.group_id");
 		stbr.append(" INNER JOIN csm_role_privilege rp ON rp.role_id = ugrpg.role_id");
 		stbr.append(" INNER JOIN csm_privilege p ON p.privilege_id = rp.privilege_id ");
 		stbr.append("  WHERE pe.object_id=?");
 		stbr.append(" AND pe.attribute=?");
 		stbr.append(" AND pe.attribute_value=?");
 		stbr.append(" AND g.group_name=?");
 		stbr.append(" AND p.privilege_name=?");
 		stbr.append(" AND pe.application_id=?");
 
 		int i=1;
 		PreparedStatement pstmt = cn.prepareStatement(stbr.toString());
 		pstmt.setString(i++,objectId );
 		pstmt.setString(i++,attributeName );
 		pstmt.setString(i++,attributeValue );
 		pstmt.setString(i++,groupName );
 		pstmt.setString(i++,privilegeName);
 		pstmt.setInt(i++,application_id);
 
 		return pstmt;
 	}
 
 	protected static PreparedStatement getQueryForCheckPermissionForUser(String userName,
 			                                                  String objectId,
 															  String privilegeName,
 															  int application_id, Connection cn) throws SQLException {
 		StringBuffer stbr = new StringBuffer();
 		stbr.append("select 'X'");
 		stbr.append("from csm_protection_group pg,");
 		stbr.append("csm_protection_element pe,");
 		stbr.append("csm_pg_pe pgpe,");
 		stbr.append("csm_user_group_role_pg ugrpg,");
 		stbr.append("csm_user u,");
 		stbr.append("csm_role_privilege rp,");
 		stbr.append("csm_privilege p ");
 		stbr.append("where pgpe.protection_group_id = pg.protection_group_id ");
 		stbr.append(" and pgpe.protection_element_id = pe.protection_element_id");
 		stbr.append(" and pe.object_id=?");
 		stbr.append(" and ugrpg.protection_group_id = ANY (select pg1.protection_group_id from csm_protection_group pg1 where pg1.protection_group_id = pg.protection_group_id or pg1.protection_group_id = (select pg2.parent_protection_group_id from csm_protection_group pg2 where pg2.protection_group_id = pg.protection_group_id)) ");
 		stbr.append(" and ugrpg.user_id = u.user_id");
 		stbr.append(" and u.login_name=?");
 		stbr.append(" and ugrpg.role_id = rp.role_id ");
 		stbr.append(" and rp.privilege_id = p.privilege_id");
 		stbr.append(" and p.privilege_name=?");
 		//stbr.append(" and pg.application_id=?");
 		stbr.append(" and pe.application_id=?");
 
 		int i=1;
 		PreparedStatement pstmt = cn.prepareStatement(stbr.toString());
 		pstmt.setString(i++,objectId );
 		pstmt.setString(i++,userName );
 		pstmt.setString(i++,privilegeName);
 		//pstmt.setInt(i++,application_id);
 		pstmt.setInt(i++,application_id);
 
 		return pstmt;
 
 	}
 
 	private static String getStaticStringForUserAndGroupForAttribute(){
 		StringBuffer stbr = new StringBuffer();
 		stbr.append("SELECT 'X'");
 		stbr.append(" FROM csm_protection_group pg,");
 		stbr.append("  INNER JOIN csm_pg_pe pgpe ON pgpe.protection_group_id = pg.protection_group_id");
 		stbr.append("  INNER JOIN csm_protection_element pe ON pe.protection_element_id = pgpe.protection_element_id");
 		stbr.append("  INNER JOIN csm_user_group_role_pg ugrpg ON ugrpg.protection_group_id = pgpe.protection_group_id");
 		stbr.append("  INNER JOIN csm_user u ON u.user_id = ugrpg.user_id");
 		stbr.append("  INNER JOIN csm_role r ON r.role_id = ugrpg.role_id");
 		stbr.append("  INNER JOIN csm_role_privilege rp ON rp.role_id = r.role_id");
 		stbr.append("  INNER JOIN csm_privilege p ON p.privilege_id = rp.privilege_id");
 
 		return stbr.toString();
 	}
 
 	private static String getStaticStringForUserAndGroupForAttribute2(){
 		StringBuffer stbr = new StringBuffer();
 		stbr.append("SELECT 'X'");
 		stbr.append(" FROM csm_user_group_role_pg ugrpg");
 		stbr.append(" INNER JOIN csm_role r ON r.role_id = ugrpg.role_id");
 		stbr.append(" INNER JOIN csm_group g ON g.group_id = ugrpg.group_id");
 		stbr.append(" INNER JOIN csm_user_group ug ON ug.group_id = g.group_id");
 		stbr.append(" INNER JOIN csm_user u ON u.user_id = ug.user_id");
 		stbr.append(" INNER JOIN csm_protection_group pg ON pg.protection_group_id = ugrpg.protection_group_id");
 		stbr.append(" INNER JOIN csm_pg_pe pgpe ON pgpe.protection_group_id = pg.protection_group_id");
 		stbr.append(" INNER JOIN csm_protection_element pe ON pe.protection_element_id = pgpe.protection_element_id");
 		stbr.append(" INNER JOIN csm_role_privilege rp ON rp.role_id = r.role_id");
 		stbr.append(" INNER JOIN csm_privilege p ON p.privilege_id = rp.privilege_id");
 
 		return stbr.toString();
 	}
 
 	public static PreparedStatement getQueryForObjectMap(String loginName,String objectId,String privilegeName,int application_id, Connection cn) throws SQLException{
 		 StringBuffer stbr = new StringBuffer();
 		stbr.append("and pe.object_id=?");
 		stbr.append(" and u.login_name=?");
 		stbr.append(" and p.privilege_name=?");
 		//stbr.append(" and pg.application_id=?");
 		stbr.append(" and pe.application_id=?");
 
 		StringBuffer sqlBfr = new StringBuffer();
 		sqlBfr.append(getQueryForObjectMap_user());
 		sqlBfr.append(stbr.toString());
 		sqlBfr.append(" union ");
 		sqlBfr.append(getQueryForObjectMap_group());
 		sqlBfr.append(stbr.toString());
 
 		int i=1;
 		PreparedStatement pstmt = cn.prepareStatement(sqlBfr.toString());
 		pstmt.setString(i++,objectId );
 		pstmt.setString(i++,loginName );
 		pstmt.setString(i++,privilegeName);
 		//pstmt.setInt(i++,application_id);
 		pstmt.setInt(i++,application_id);
 
 		pstmt.setString(i++,objectId );
 		pstmt.setString(i++,loginName );
 		pstmt.setString(i++,privilegeName);
 		//pstmt.setInt(i++,application_id);
 		pstmt.setInt(i++,application_id);
 
 		return pstmt;
 	}
 
 	private static String getQueryForObjectMap_user(){
 		StringBuffer stbr = new StringBuffer();
 		stbr.append("SELECT pe.attribute ");
 		stbr.append(" FROM csm_protection_element pe");
 		stbr.append(" INNER JOIN csm_pg_pe pgpe ON pgpe.protection_element_id = pe.protection_element_id");
 		stbr.append(" INNER JOIN csm_protection_group pg ON pg.protection_group_id = pgpe.protection_group_id		");
 		stbr.append(" INNER JOIN csm_user_group_role_pg ugrpg ON ugrpg.protection_group_id = pg.protection_group_id");
 		stbr.append(" INNER JOIN csm_user u ON u.user_id = ugrpg.user_id");
 		stbr.append(" INNER JOIN csm_role r ON r.role_id = ugrpg.role_id");
 		stbr.append(" INNER JOIN csm_role_privilege rp ON rp.role_id = r.role_id");
 		stbr.append(" INNER JOIN csm_privilege p ON p.privilege_id = rp.privilege_id");
 
 		return stbr.toString();
 	}
 
 	private static String getQueryForObjectMap_group(){
 		StringBuffer stbr = new StringBuffer();
 		stbr.append("SELECT pe.attribute");
 		stbr.append(" FROM csm_protection_element pe");
 		stbr.append(" INNER JOIN csm_pg_pe pgpe ON pgpe.protection_element_id = pe.protection_element_id");
 		stbr.append(" INNER JOIN csm_protection_group pg ON pg.protection_group_id = pgpe.protection_group_id");
 		stbr.append(" INNER JOIN csm_user_group_role_pg ugrpg ON ugrpg.protection_group_id = pg.protection_group_id");
 		stbr.append(" INNER JOIN csm_role r ON r.role_id = ugrpg.role_id");
 		stbr.append(" INNER JOIN csm_group g ON g.group_id = ugrpg.group_id");
 		stbr.append(" INNER JOIN csm_user_group ug ON ug.group_id = g.group_id");
 		stbr.append(" INNER JOIN csm_user u ON u.user_id = ug.user_id");
 		stbr.append(" INNER JOIN csm_role_privilege rp ON rp.role_id = r.role_id");
 		stbr.append(" INNER JOIN csm_privilege p ON p.privilege_id = rp.privilege_id");
 
 		return stbr.toString();
 	}
 
 
 	protected static PreparedStatement getQueryforUserPEPrivilegeMap(String user_id, int application_id, Connection cn) throws SQLException
 	{
 		StringBuffer stbr = new StringBuffer();
 
 		stbr.append("SELECT DISTINCT pe.protection_element_id as pe_id, p.privilege_id as p_id");
 		stbr.append("	FROM csm_protection_element pe");
 		stbr.append("		INNER JOIN csm_pg_pe pgpe ON pgpe.protection_element_id = pe.protection_element_id");
 		stbr.append("       INNER JOIN csm_user_group_role_pg ugrpg ON ugrpg.protection_group_id = pgpe.protection_group_id");
 		stbr.append("       INNER JOIN csm_user_group ug ON ug.group_id = ugrpg.group_id");
 		stbr.append("       INNER JOIN csm_role_privilege rp ON rp.role_id = ugrpg.role_id");
 		stbr.append("       INNER JOIN csm_privilege p ON p.privilege_id = rp.privilege_id");
 		stbr.append("	WHERE pe.application_id=?");
 		stbr.append("		AND ug.user_id = ?");
 		stbr.append(" UNION ALL ");
 		stbr.append("SELECT DISTINCT pe.protection_element_id as pe_id, p.privilege_id as p_id");
 		stbr.append("	FROM csm_protection_element pe");
 		stbr.append("		INNER JOIN csm_pg_pe pgpe ON pgpe.protection_element_id = pe.protection_element_id");
 		stbr.append("		INNER JOIN csm_user_group_role_pg ugrpg ON ugrpg.protection_group_id = pgpe.protection_group_id");
 		stbr.append("		INNER JOIN csm_role_privilege rp ON rp.role_id = ugrpg.role_id");
 		stbr.append("		INNER JOIN csm_privilege p ON p.privilege_id = rp.privilege_id");
 		stbr.append("	WHERE pe.application_id=?");
 		stbr.append("		AND ugrpg.user_id = ?");
 		stbr.append(" UNION ALL ");
 		stbr.append("SELECT DISTINCT upe.protection_element_id as pe_id, 0 as p_id");
 		stbr.append("	FROM csm_protection_element cpe");
 		stbr.append("		INNER JOIN csm_user_pe upe ON upe.protection_element_id = cpe.protection_element_id");
 		stbr.append("	WHERE cpe.application_id = ?");
 		stbr.append("		AND upe.user_id = ?");
 		stbr.append(" ORDER BY pe_id, p_id");
 
 
 		int i=1;
 		PreparedStatement pstmt = cn.prepareStatement(stbr.toString());
 		//pstmt.setInt(i++,application_id);
 		pstmt.setInt(i++,application_id);
 		pstmt.setInt(i++,new Integer(user_id).intValue());
 		//pstmt.setInt(i++,application_id);
 		pstmt.setInt(i++,application_id);
 		pstmt.setInt(i++,new Integer(user_id).intValue());
 		pstmt.setInt(i++,application_id);
 		pstmt.setInt(i++,new Integer(user_id).intValue());
 		//pstmt.setInt(i++,application_id);
 		return pstmt;
 
 	}
 
 	protected static PreparedStatement getQueryforGroupPEPrivilegeMap(String group_id, int application_id, Connection cn) throws SQLException
 	{
 		StringBuffer stbr = new StringBuffer();
 
 		stbr.append("SELECT DISTINCT pe.protection_element_id as pe_id, rp.privilege_id as p_id");
 		stbr.append("	FROM csm_role_privilege rp");
 		stbr.append("		INNER JOIN csm_user_group_role_pg ugrpg ON ugrpg.role_id = rp.role_id");
 		stbr.append("		INNER JOIN csm_pg_pe pgpe ON pgpe.protection_group_id = ugrpg.protection_group_id");
 		stbr.append("		INNER JOIN csm_protection_element pe ON pe.protection_element_id = pgpe.protection_element_id");
 		stbr.append("	WHERE pe.application_id=?");
 		stbr.append("		AND ugrpg.group_id = ?");
 		stbr.append("	ORDER BY pe_id, p_id");
 
 		int i=1;
 		PreparedStatement pstmt = cn.prepareStatement(stbr.toString());
 		//pstmt.setInt(i++,application_id);
 		pstmt.setInt(i++,application_id);
 		pstmt.setInt(i++,new Integer(group_id).intValue());
 
 
 		return pstmt;
 
 	}
 
 
 	protected static PreparedStatement getQueryforUserAttributeMap(String userName, String className, String privilegeName, int application_id, Connection cn) throws SQLException
 	{
 		StringBuffer stbr = new StringBuffer();
 
 		stbr.append("SELECT DISTINCT pe.attribute");
 		stbr.append("	FROM csm_protection_element pe");
 		stbr.append("		INNER JOIN csm_pg_pe pgpe ON pgpe.protection_element_id = pe.protection_element_id");
 		stbr.append("		INNER JOIN csm_user_group_role_pg ugrpg ON ugrpg.protection_group_id = pgpe.protection_group_id");
 		stbr.append("		INNER JOIN csm_role_privilege rp ON rp.role_id = ugrpg.role_id");
 		stbr.append("		INNER JOIN csm_privilege p ON p.privilege_id = rp.privilege_id");
 		stbr.append("		INNER JOIN csm_user u ON u.user_id = ugrpg.user_id");
 		stbr.append("	WHERE (pe.attribute is not null or pe.attribute != '')");
 		stbr.append("		AND pe.object_id=?");
 		stbr.append("		AND u.login_name=?");
 		stbr.append("		AND p.privilege_name=?");
 		stbr.append("		AND pe.application_id=?");
 
 
 		PreparedStatement pstmt = cn.prepareStatement(stbr.toString());
 		int i=1;
 		pstmt.setString(i++,className);
 		pstmt.setString(i++,userName);
 		pstmt.setString(i++,privilegeName);
 		pstmt.setInt(i++,application_id);
 
 		return pstmt;
 
 	}
 
 	protected static PreparedStatement getQueryforGroupAttributeMap(String groupName, String className, String privilegeName, int application_id, Connection cn) throws SQLException
 	{
 
 		StringBuffer stbr = new StringBuffer();
 
 		stbr.append("SELECT DISTINCT pe.attribute");
 		stbr.append("	FROM csm_protection_element pe");
 		stbr.append("		INNER JOIN csm_pg_pe pgpe ON pgpe.protection_element_id = pe.protection_element_id");
 		stbr.append("		INNER JOIN csm_user_group_role_pg ugrpg ON ugrpg.protection_group_id = pgpe.protection_group_id");
 		stbr.append("		INNER JOIN csm_role_privilege rp ON rp.role_id = ugrpg.role_id");
 		stbr.append("		INNER JOIN csm_group g ON g.group_id = ugrpg.group_id");
 		stbr.append("		INNER JOIN csm_privilege p ON p.privilege_id = rp.privilege_id");
 		stbr.append("	WHERE (pe.attribute is not null or pe.attribute != '')");
 		stbr.append("		AND pe.object_id=?");
 		stbr.append("		AND g.group_name =?");
 		stbr.append("		AND p.privilege_name=?");
 		stbr.append("		AND pe.application_id=?");
 
 
 		PreparedStatement pstmt = cn.prepareStatement(stbr.toString());
 		int i=1;
 		pstmt.setString(i++,className);
 		pstmt.setString(i++,groupName);
 		pstmt.setString(i++,privilegeName);
 		pstmt.setInt(i++,application_id);
 
 		return pstmt;
 
 	}
 
 
 	protected static PreparedStatement getQueryforUptOperationPE(String peObjectId, String userLoginName, Long applicationId, Connection cn) throws SQLException
 	{
 
 		StringBuffer stbr = new StringBuffer();
 
 //		stbr.append("SELECT pe.protection_element_id");
 //		stbr.append("      FROM  csm_protection_element pe,");
 //		stbr.append("            csm_protection_group pg,");
 //		stbr.append("            csm_pg_pe pgpe,");
 //		stbr.append("            csm_application app");
 //		stbr.append("      WHERE pg.protection_group_name = ?");
 //		stbr.append("            AND pg.protection_group_id = pgpe.protection_group_id");
 //		stbr.append("            AND pgpe.protection_element_id = pe.protection_element_id");
 //		stbr.append("            AND pe.application_id=?");
 //		stbr.append("            AND pe.application_id = app.application_id");
 
 		stbr.append("SELECT pe.protection_element_id");
 		stbr.append("      FROM  csm_protection_element pe,");
 		stbr.append("            csm_user u,");
 		stbr.append("            csm_user_group_role_pg ugrp,");
 		stbr.append("            csm_protection_group pg,");
 		stbr.append("            csm_pg_pe pgpe");
 		stbr.append("      WHERE pe.object_id= ?");
 		stbr.append("        AND u.login_name= ?");
 		stbr.append("        AND pe.application_id=?");
 		stbr.append("        and ugrp.USER_ID=u.USER_ID");
 		stbr.append("        and pg.PROTECTION_GROUP_ID=ugrp.PROTECTION_GROUP_ID");
 		stbr.append("        AND pg.protection_group_id = pgpe.protection_group_id");
 		stbr.append("        AND pgpe.protection_element_id = pe.protection_element_id");
 
 
 		PreparedStatement pstmt = cn.prepareStatement(stbr.toString());
 		int i=1;
 		pstmt.setString(i++,peObjectId);
 		pstmt.setString(i++, userLoginName);
 		pstmt.setLong(i++,applicationId);
 
 		return pstmt;
 
 	}
 
 	protected static PreparedStatement getQueryforLinkPEUser(String linkName, int protectionElementId, Long userId, Long applicationId, Connection cn) throws SQLException
 	{
 
 		StringBuffer stbr = new StringBuffer();
 
 		stbr.append("SELECT pe.protection_element_id");
 		stbr.append("      FROM  csm_protection_element pe,");
 		stbr.append("            csm_protection_group pg,");
 		stbr.append("            csm_pg_pe pgpe,");
 		stbr.append("            csm_user_pe upe,");
 		stbr.append("            csm_user usr,");
 		stbr.append("            csm_application app");
 		stbr.append("      WHERE upe.protection_element_id = ?");
 		stbr.append("            AND upe.user_id =  ?");
 		stbr.append("            AND pe.protection_element_id = upe.protection_element_id");
 		stbr.append("            AND upe.user_id = usr.user_id");
 		stbr.append("            AND pe.protection_element_id = pgpe.protection_element_id");
 		stbr.append("            AND pg.protection_group_id = pgpe.protection_group_id");
 		stbr.append("            AND pg.protection_group_name =  ?");
 		stbr.append("            AND pe.application_id = app.application_id");
 		stbr.append("            AND pe.application_id =  ?");
 
 
 		PreparedStatement pstmt = cn.prepareStatement(stbr.toString());
 		int i=1;
 		pstmt.setInt(i++,protectionElementId);
 		pstmt.setLong(i++,userId);
 		pstmt.setString(i++,linkName);
 		pstmt.setLong(i++,applicationId);
 
 		return pstmt;
 
 	}
 
 	/*
 	protected static PreparedStatement getQueryforGroupsAttributeMap(String[] groupNames, String className, String privilegeName, int application_id, Connection cn) throws SQLException
 	{
 		//TODO Today
 
 		StringBuffer stbr = new StringBuffer();
 
 		stbr.append("SELECT DISTINCT pe.attribute");
 		stbr.append("      FROM  csm_protection_element pe,");
 		stbr.append("            csm_protection_group pg,");
 		stbr.append("            csm_privilege p,");
 		stbr.append("            csm_group g,");
 		stbr.append("            csm_pg_pe pgpe,");
 		stbr.append("            csm_role r,");
 		stbr.append("            csm_role_privilege rp,");
 		stbr.append("            csm_user_group_role_pg ugrpg");
 		stbr.append("      WHERE pgpe.protection_group_id = ANY (SELECT pg1.protection_group_id FROM csm_protection_group pg1 WHERE pg1.parent_protection_group_id = ugrpg.protection_group_id OR pg1.protection_group_id = ugrpg.protection_group_id)");
 		stbr.append("            AND ugrpg.role_id = rp.role_id");
 		stbr.append("            AND rp.privilege_id = p.privilege_id");
 		stbr.append("            AND pg.protection_group_id = pgpe.protection_group_id");
 		stbr.append("            AND pgpe.protection_element_id = pe.protection_element_id");
 		stbr.append("            AND ugrpg.group_id = g.group_id");
 		stbr.append("            AND (pe.attribute is not null or pe.attribute <> '')");
 		stbr.append("            AND pe.object_id=?");
 		stbr.append("            AND g.group_name ");
 		int count = 0;
 		if(groupNames.length>0){
 			if(groupNames.length==1){
 				stbr.append(" = '"+groupNames[count]+"' ");
 			}else{
 				stbr.append(" IN (");
 				if(count==0){
 					for(;count<groupNames.length;count++){
 						stbr.append(" ? ");
 						if(groupNames.length-count == 1){
 							stbr.append(" )");
 						}else{
 							stbr.append(" ,");
 						}
 					}
 				}
 			}
 		}
 		stbr.append("            AND p.privilege_name=?");
 		stbr.append("            AND pe.application_id=?");
 
 
 		PreparedStatement pstmt = cn.prepareStatement(stbr.toString());
 		int i=1;
 		pstmt.setString(i++,className);
 		count = 0;
 		for(;count<groupNames.length;count++){
 			pstmt.setString(i++,groupNames[count]);
 		}
 
 		pstmt.setString(i++,privilegeName);
 		pstmt.setInt(i++,application_id);
 
 		return pstmt;
 
 	}*/
 
 
 
 }
