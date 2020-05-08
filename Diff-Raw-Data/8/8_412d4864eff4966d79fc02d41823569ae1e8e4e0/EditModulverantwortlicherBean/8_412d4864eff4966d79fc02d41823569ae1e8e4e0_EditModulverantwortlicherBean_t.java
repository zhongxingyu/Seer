 package server;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 
 import ctrl.DBModule;
 import ctrl.DBSubject;
 import data.Module;
 import data.Subject;
 
 @ManagedBean(name = "EditModulverantwortlicherBean")
 @RequestScoped
 public class EditModulverantwortlicherBean {
 	private String modTitle, modDesc, subTitle, subDesc, aim;
 	private int version, ects;
 	private boolean ack;
 
	@SuppressWarnings("unused")
 	private Module loadModul(String modTitle) {
 		return DBModule.loadModule(modTitle);
 	}
 
 	private boolean saveModule() {
 		if (modTitle.isEmpty() && modDesc.isEmpty())
 			return false;
 		Module tmp = new Module(modTitle, modDesc);
 		System.out.println(tmp.toString());
 		DBModule.saveModule(tmp);
 		return true;
 	}
 	
 	public String checkSuccessModule(){
 		if(saveModule())
 			return "createSuccess";
 		else
 			return "create";
 	}
 
	@SuppressWarnings("unused")
 	private Subject loadSubject(int version, String subTitle, String modTitle) {
 		return DBSubject.loadSubject(version, subTitle, modTitle);
 	}
 
 	private boolean saveSubject() {
 		if (version < 0 && subTitle.isEmpty() && modTitle.isEmpty())
 			return false;
 		Subject tmp = new Subject(version, subTitle, modTitle, subDesc, aim,
 				ects, ack);
 		System.out.println(tmp.toString());
 		DBSubject.saveSubject(tmp);
 		return true;
 	}
 	
 	public String checkSuccessSubject(){
 		if(saveSubject())
 			return "createSuccess";
 		else
 			return "create";
 	}
 
 	/**
 	 * @return the modTitle
 	 */
 	public String getModTitle() {
 		return modTitle;
 	}
 
 	/**
 	 * @param modTitle
 	 *            the modTitle to set
 	 */
 	public void setModTitle(String modTitle) {
 		this.modTitle = modTitle;
 	}
 
 	/**
 	 * @return the modDesc
 	 */
 	public String getModDesc() {
 		return modDesc;
 	}
 
 	/**
 	 * @param modDesc
 	 *            the modDesc to set
 	 */
 	public void setModDesc(String modDesc) {
 		this.modDesc = modDesc;
 	}
 
 	/**
 	 * @return the subTitle
 	 */
 	public String getSubTitle() {
 		return subTitle;
 	}
 
 	/**
 	 * @param subTitle
 	 *            the subTitle to set
 	 */
 	public void setSubTitle(String subTitle) {
 		this.subTitle = subTitle;
 	}
 
 	/**
 	 * @return the subDesc
 	 */
 	public String getSubDesc() {
 		return subDesc;
 	}
 
 	/**
 	 * @param subDesc
 	 *            the subDesc to set
 	 */
 	public void setSubDesc(String subDesc) {
 		this.subDesc = subDesc;
 	}
 
 	/**
 	 * @return the aim
 	 */
 	public String getAim() {
 		return aim;
 	}
 
 	/**
 	 * @param aim
 	 *            the aim to set
 	 */
 	public void setAim(String aim) {
 		this.aim = aim;
 	}
 
 	/**
 	 * @return the version
 	 */
 	public int getVersion() {
 		return version;
 	}
 
 	/**
 	 * @param version
 	 *            the version to set
 	 */
 	public void setVersion(int version) {
 		this.version = version;
 	}
 
 	/**
 	 * @return the ects
 	 */
 	public int getEcts() {
 		return ects;
 	}
 
 	/**
 	 * @param ects
 	 *            the ects to set
 	 */
 	public void setEcts(int ects) {
 		this.ects = ects;
 	}
 
 	/**
 	 * @return the ack
 	 */
 	public boolean isAck() {
 		return ack;
 	}
 
 	/**
 	 * @param ack
 	 *            the ack to set
 	 */
 	public void setAck(boolean ack) {
 		this.ack = ack;
 	}
 
 }
