 /*
 *	http://www.jrecruiter.org
 *
 *	Disclaimer of Warranty.
 *
 *	Unless required by applicable law or agreed to in writing, Licensor provides
 *	the Work (and each Contributor provides its Contributions) on an "AS IS" BASIS,
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied,
 *	including, without limitation, any warranties or conditions of TITLE,
 *	NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE. You are
 *	solely responsible for determining the appropriateness of using or
 *	redistributing the Work and assume any risks associated with Your exercise of
 *	permissions under this License.
 *
 */
 package org.jrecruiter.service.migration;
 
 
 /**
  * This helper service contains methods that should be helpful for migrating jRecruiter
  * data from older Database schemas to newer version. Therefore, this is probably 
  * not functionality used during production. If user have the need to migrate from
  * there special database environments, this should be the place to look for.
  *  
  * @author Gunnar Hillert
 * @version $Id$
  */
 public interface MigrationService {
 
     /**
      * Method for migrating user data.
      * 
      * @param digestPasswords Shall passwords be 'digested'?
      */
     void migrateUserData(Boolean digestPasswords);
 
     /**
      * Migrate Job Data
      */
     void migrateJobData();
     
     /**
      * Method for migrating settings.
      */
     void migrateSettings();
     
     /**
      * Method for migrating regions.
      */
     void migrateRegions();
     
     /**
      * Method for migrating industries.
      */
     void migrateIndustries();
     
 }
