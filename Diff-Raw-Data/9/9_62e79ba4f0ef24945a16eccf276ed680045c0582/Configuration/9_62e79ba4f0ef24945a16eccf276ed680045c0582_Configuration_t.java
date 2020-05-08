 package model;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.util.Properties;
 
 /**
  * $Id$
  * $LastChangedDate$
  * 
  * @author Fadi M. H. Asbih
  * @email fadi_asbih@yahoo.de
  * @version $Revision$
  * @copyright $Date$
  * 
  * TERMS AND CONDITIONS:
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 public class Configuration {
 
 	private boolean generateLogin; // Generate Login based on firstname.lastname
 	private boolean generateDummy; // Generate dummy data for testing.
 	private char timeLimitUnlimited;
 	
 	private String actionLabel;
 	private String titleLabel;
 	private String firstNameLabel; 
 	private String lastNameLabel;
 	private String loginLabel;
 	private String globalRoleLabel;
 	private String localRoleLabel;
 	private String matriculationLabel;
 	private String passwordLabel;
 	private String genderLabel;
 	private String emailLabel;
 	
 	private String localRoleValue;
 	private String timeLimitFrom;
 	private String timeLimitUntil;
 	private String actionValue;
 	private String password;
 	private String genderValue;
 	private String CSVSymbol;
 	
 	private Version version;
 	
 	public Configuration() {
 		new Configuration(version);
 	}
 	
 	public Configuration(Version version) {
 				
 		this.setVersion(version);
 		
 		try {
 			Properties properties = new Properties();
 
 			BufferedInputStream stream = new BufferedInputStream(new FileInputStream("properties"));
 			properties.load(stream);
 			stream.close();
 			
 			//Labels
 			setActionLabel(properties.getProperty("actionLabel"));
 			setTitleLabel(properties.getProperty("titleLabel"));
 			setLoginLabel(properties.getProperty("loginLabel"));
 			setPasswordLabel(properties.getProperty("passwordLabel"));
 			setFirstNameLabel(properties.getProperty("firstNameLabel"));
 			setLastNameLabel(properties.getProperty("lastNameLabel"));
 			setGlobalRoleLabel(properties.getProperty("globalRoleLabel"));
 			setLocalRoleLabel(properties.getProperty("localRoleLabel"));
 			setMatriculationLabel(properties.getProperty("matriculationLabel"));
 			setEmailLabel(properties.getProperty("emailLabel"));
 			setGenderLabel(properties.getProperty("genderLabel"));
 			
 			//Values
 			setActionValue(properties.getProperty("actionValue"));
 			setLocalRoleValue(properties.getProperty("localRole"));
 			setPasswordValue(properties.getProperty("password"));
 			setGenderValue(properties.getProperty("genderValue"));
 			setCSVSymbol(properties.getProperty("CSV"));
 			setTimeLimitUnlimited(Boolean.parseBoolean(properties.getProperty("timeLimitUnlimited")));
 			setTimeLimitFrom(properties.getProperty("timeLimitFrom"));
 			setTimeLimitFrom(properties.getProperty("timeLimitUntil"));
 			
 			//Boolean
 			setGenerateLogin(Boolean.parseBoolean(properties.getProperty("generateLogin")));
 			setGenerateDummy(Boolean.parseBoolean(properties.getProperty("generateDummy")));
 			
 		} catch (Exception e) {
 			System.out.println("No properties file is loaded, Standard settings will be used.");
 		}
 		
 	}
 
 	/**
 	 * @return the firstNameLabel
 	 */
 	public String getFirstNameLabel() {
 		if(firstNameLabel==null)
 			return "Firstname";
 		else
 			return firstNameLabel;
 	}
 
 	/**
 	 * @param firstNameLabel the firstNameLabel to set
 	 */
 	public void setFirstNameLabel(String firstNameLabel) {
 		this.firstNameLabel = firstNameLabel;
 	}
 
 	/**
 	 * @return the lastNameLabel
 	 */
 	public String getLastNameLabel() {
 		if(lastNameLabel==null)
 			return "Lastname";
 		else
 			return lastNameLabel;
 	}
 
 	/**
 	 * @param lastNameLabel the lastNameLabel to set
 	 */
 	public void setLastNameLabel(String lastNameLabel) {
 		this.lastNameLabel = lastNameLabel;
 	}
 
 	/**
 	 * @return the generateLogin
 	 */
 	public boolean isGenerateLogin() {
 		if(generateLogin)
 			return true;
 		else
 			return false;
 	}
 
 	/**
 	 * @param generateLogin the generateLogin to set
 	 */
 	public void setGenerateLogin(boolean generateLogin) {
 		this.generateLogin = generateLogin;
 	}
 
 	/**
 	 * @return the loginLabel
 	 */
 	public String getLoginLabel() {
 		if(loginLabel==null)
 			return "Login";
 		else
 			return loginLabel;
 	}
 
 	/**
 	 * @param loginLabel the loginLabel to set
 	 */
 	public void setLoginLabel(String loginLabel) {
 		this.loginLabel = loginLabel;
 	}
 
 	/**
 	 * @return the globalRoleLabel
 	 */
 	public String getGlobalRoleLabel() {
 		if(globalRoleLabel==null) 
 			return "role";
 		else
 			return globalRoleLabel;
 	}
 
 	/**
 	 * @param globalRoleLabel the globalRoleLabel to set
 	 */
 	public void setGlobalRoleLabel(String globalRoleLabel) {
 		this.globalRoleLabel = globalRoleLabel;
 	}
 
 	/**
 	 * @return the matriculationLabel
 	 */
 	public String getMatriculationLabel() {
 		if(matriculationLabel==null)
 			return "Matriculation";
 		else
 			return matriculationLabel;
 	}
 
 	/**
 	 * @param matriculationLabel the matriculationLabel to set
 	 */
 	public void setMatriculationLabel(String matriculationLabel) {
 		this.matriculationLabel = matriculationLabel;
 	}
 
 	/**
 	 * @return the passwordLabel
 	 */
 	public String getPasswordLabel() {
 		if(passwordLabel==null)
 			return "Password";
 		else
 			return passwordLabel;
 	}
 
 	/**
 	 * @param passwordLabel the passwordLabel to set
 	 */
 	public void setPasswordLabel(String passwordLabel) {
 		this.passwordLabel = passwordLabel;
 	}
 
 	/**
 	 * @return the genderLabel
 	 */
 	public String getGenderLabel() {
 		if(genderLabel == null)
 			return "Gender";
 		else
 			return genderLabel;
 	}
 
 	/**
 	 * @param genderLabel the genderLabel to set
 	 */
 	public void setGenderLabel(String genderLabel) {
 		this.genderLabel = genderLabel;
 	}
 
 	/**
 	 * @return the emailLabel
 	 */
 	public String getEmailLabel() {
 		if(emailLabel==null)
 			return "Email";
 		else
 			return emailLabel;
 	}
 
 	/**
 	 * @param emailLabel the emailLabel to set
 	 */
 	public void setEmailLabel(String emailLabel) {
 		this.emailLabel = emailLabel;
 	}
 
 	/**
 	 * @return the titleLabel
 	 */
 	public String getTitleLabel() {
 		if(titleLabel==null)
 			return "Title";
 		else
 			return titleLabel;
 	}
 
 	/**
 	 * @param titleLabel the titleLabel to set
 	 */
 	public void setTitleLabel(String titleLabel) {
 		this.titleLabel = titleLabel;
 	}
 
 	/**
 	 * @return the generatePassword
 	 */
 	public boolean isGeneratePassword() {
 		if(password == null)
 			return false;
 		else
 			return true;
 	}
 
 	/**
 	 * @return the localRoleLabel
 	 */
 	public String getLocalRoleLabel() {
 		if(localRoleLabel==null)
 			return "Local Role";
 		else
 			return localRoleLabel;
 	}
 
 	/**
 	 * @param localRoleLabel the localRoleLabel to set
 	 */
 	public void setLocalRoleLabel(String localRoleLabel) {
 		this.localRoleLabel = localRoleLabel;
 	}
 
 	/**
 	 * @return the localRole
 	 */
 	public boolean isLocalRole() {
 		if(localRoleValue == null) 
 			return false;
 		else
 			return true;
 	}
 
 	/**
 	 * @return the localRoleValue
 	 */
 	public String getLocalRoleValue() {
 		return localRoleValue;
 	}
 
 	/**
 	 * @param localRoleValue the localRoleValue to set
 	 */
 	public void setLocalRoleValue(String localRoleValue) {
 		this.localRoleValue = localRoleValue;
 	}
 
 	/**
 	 * @return the passwordValue
 	 */
 	public String getPasswordValue() {
 		return password;
 	}
 
 	/**
 	 * @param passwordValue the passwordValue to set
 	 */
 	public void setPasswordValue(String passwordValue) {
 		this.password = passwordValue;
 	}
 
 	/**
 	 * @return the genderValue
 	 */
 	public String getGenderValue() {
 		if(genderValue==null)
 			return "f";
 		else
 			return genderValue;
 	}
 
 	/**
 	 * @param genderValue the genderValue to set
 	 */
 	public void setGenderValue(String genderValue) {
 		this.genderValue = genderValue;
 	}
 
 	/**
 	 * @return the actionLabel
 	 */
 	public String getActionLabel() {
 		if(actionLabel==null)
 			return "Action";
 		else
 			return actionLabel;
 	}
 
 	/**
 	 * @param actionLabel the actionLabel to set
 	 */
 	public void setActionLabel(String actionLabel) {
 		this.actionLabel = actionLabel;
 	}
 
 	/**
 	 * @return the actionValue
 	 */
 	public String getActionValue() {
 		if(actionValue == null)
 			return "Insert";
 		else
 			return actionValue;
 	}
 
 	/**
 	 * @param actionValue the actionValue to set
 	 */
 	public void setActionValue(String actionValue) {
 		this.actionValue = actionValue;
 	}
 
 	/**
 	 * @return the CSVSymbol
 	 */
 	public String getCSVSymbol() {
		if(CSVSymbol == null)
			return ";";
		else
			return CSVSymbol;
 	}
 
 	/**
 	 * @param cSVSymbol the cSVSymbol to set
 	 */
 	public void setCSVSymbol(String CSVSymbol) {
 			this.CSVSymbol = CSVSymbol;
 	}
 
 	/**
 	 * @return the timeLimitFrom
 	 */
 	public String getTimeLimitFrom() {
 		return timeLimitFrom;
 	}
 
 	/**
 	 * @param timeLimitFrom the timeLimitFrom to set
 	 */
 	public void setTimeLimitFrom(String timeLimitFrom) {
 		this.timeLimitFrom = timeLimitFrom;
 	}
 
 	/**
 	 * @return the timeLimitUntil
 	 */
 	public String getTimeLimitUntil() {
 		return timeLimitUntil;
 	}
 
 	/**
 	 * @param timeLimitUntil the timeLimitUntil to set
 	 */
 	public void setTimeLimitUntil(String timeLimitUntil) {
 		this.timeLimitUntil = timeLimitUntil;
 	}
 
 	/**
 	 * @return the timeLimitUnlimited
 	 */
 	public char getTimeLimitUnlimited() {
 		return timeLimitUnlimited;
 	}
 
 	/**
 	 * @param timeLimitUnlimited the timeLimitUnlimited to set
 	 */
 	public void setTimeLimitUnlimited(boolean timeLimitUnlimited) {
 		if(timeLimitUnlimited)
 			this.timeLimitUnlimited = 0;
 		else
 			this.timeLimitUnlimited = 1;
 	}
 
 	/**
 	 * @return the generateDummy
 	 */
 	public boolean isGenerateDummy() {
 		return generateDummy;
 	}
 
 	/**
 	 * @param generateDummy the generateDummy to set
 	 */
 	public void setGenerateDummy(boolean generateDummy) {
 		if(generateDummy) {
 			this.generateDummy = generateDummy;
 		}
 		else
 			this.generateDummy = false;
 	}
 
 	/**
 	 * @return the version
 	 */
 	public Version getVersion() {
 		return version;
 	}
 
 	/**
 	 * @param version the version to set
 	 */
 	public void setVersion(Version version) {
 		this.version = version;
 	}
 
 }
