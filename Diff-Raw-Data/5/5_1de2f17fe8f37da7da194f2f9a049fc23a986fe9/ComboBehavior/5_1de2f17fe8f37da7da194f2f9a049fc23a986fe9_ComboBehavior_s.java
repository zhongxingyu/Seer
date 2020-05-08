 package de.jwic.controls.combo;
 
 import java.io.Serializable;
 
 /**
  * This class defines the behavior specification for the combo control. It is
  * used by the subclasses to specify the combo for their needs.
  * @author lippisch
  */
 public class ComboBehavior implements Serializable {
 	private static final long serialVersionUID = 1L;
 	protected boolean openContentOnTextboxFocus = false;
 	protected boolean selectTextOnFocus = false;
 	protected boolean textEditable = true;
 	protected String dataLoaderJSClass = "JWic.controls.Combo.BeanLoader";
 	protected String contentRendererJSClass = "JWic.controls.Combo.ComboElementListRenderer";
 	protected String labelProviderJSClass = "JWic.controls.Combo.ComboElementLabelProvider";
 	protected int maxFetchRows = -1; // -1 = all
 	protected int keyDelayTime = 100;
 	protected int minSearchKeyLength = 0;
 	protected boolean showOpenBoxIcon = true;
 	protected boolean cacheData = true;
 	protected boolean clientSideFilter = true;
 	protected boolean autoPickFirstHit = true;
 	protected boolean transferFullObject = false;
	protected String dataFilter = "JWic.controls.Combo.StringDataFilter;";
 	
 	
 	/**
 	 * @return the openContentOnTextboxFocus
 	 */
 	public boolean isOpenContentOnTextboxFocus() {
 		return openContentOnTextboxFocus;
 	}
 	/**
 	 * Set to true if the content box should get opened when the input-field 
 	 * gets the focus.
 	 * @param openContentOnTextboxFocus the openContentOnTextboxFocus to set
 	 */
 	public void setOpenContentOnTextboxFocus(boolean openContentOnTextboxFocus) {
 		this.openContentOnTextboxFocus = openContentOnTextboxFocus;
 	}
 	/**
 	 * The javascript class that handles the data load.
 	 * @return the dataLoaderJSClass
 	 */
 	public String getDataLoaderJSClass() {
 		return dataLoaderJSClass;
 	}
 	/**
 	 * The javascript class that handles the data load.
 	 * @param dataLoaderJSClass the dataLoaderJSClass to set
 	 */
 	public void setDataLoaderJSClass(String dataLoaderJSClass) {
 		this.dataLoaderJSClass = dataLoaderJSClass;
 	}
 	/**
 	 * The javascript class that renderes the data into the content box.
 	 * @return the contentRendererJSClass
 	 */
 	public String getContentRendererJSClass() {
 		return contentRendererJSClass;
 	}
 	/**
 	 * The javascript class that renderes the data into the content box.
 	 * @param contentRendererJSClass the contentRendererJSClass to set
 	 */
 	public void setContentRendererJSClass(String contentRendererJSClass) {
 		this.contentRendererJSClass = contentRendererJSClass;
 	}
 	/**
 	 * True if the text in the control should be selected on focus.
 	 * @return the selectTextOnFocus
 	 */
 	public boolean isSelectTextOnFocus() {
 		return selectTextOnFocus;
 	}
 	/**
 	 * True if the text in the control should be selected on focus. 
 	 * @param selectTextOnFocus the selectTextOnFocus to set
 	 */
 	public void setSelectTextOnFocus(boolean selectTextOnFocus) {
 		this.selectTextOnFocus = selectTextOnFocus;
 	}
 	/**
 	 * @return the textEditable
 	 */
 	public boolean isTextEditable() {
 		return textEditable;
 	}
 	/**
 	 * @param textEditable the textEditable to set
 	 */
 	public void setTextEditable(boolean textEditable) {
 		this.textEditable = textEditable;
 	}
 	/**
 	 * @return the maxFetchRows
 	 */
 	public int getMaxFetchRows() {
 		return maxFetchRows;
 	}
 	/**
 	 * @param maxFetchRows the maxFetchRows to set
 	 */
 	public void setMaxFetchRows(int maxFetchRows) {
 		this.maxFetchRows = maxFetchRows;
 	}
 	/**
 	 * @return the keyDelayTime
 	 */
 	public int getKeyDelayTime() {
 		return keyDelayTime;
 	}
 	/**
 	 * Number of milliseconds after keyboard input before the search starts. 
 	 * @param keyDelayTime the keyDelayTime to set
 	 */
 	public void setKeyDelayTime(int keyDelayTime) {
 		this.keyDelayTime = keyDelayTime;
 	}
 	/**
 	 * @return the minSearchKeyLength
 	 */
 	public int getMinSearchKeyLength() {
 		return minSearchKeyLength;
 	}
 	/**
 	 * Sets the minium text-length before the combo is starting a data-load/refresh.
 	 * @param minSearchKeyLength the minSearchKeyLength to set
 	 */
 	public void setMinSearchKeyLength(int minSearchKeyLength) {
 		this.minSearchKeyLength = minSearchKeyLength;
 	}
 	/**
 	 * @return the showOpenBoxIcon
 	 */
 	public boolean isShowOpenBoxIcon() {
 		return showOpenBoxIcon;
 	}
 	/**
 	 * If true, the icon that opens the content box is displayed.
 	 * @param showOpenBoxIcon the showOpenBoxIcon to set
 	 */
 	public void setShowOpenBoxIcon(boolean showOpenBoxIcon) {
 		this.showOpenBoxIcon = showOpenBoxIcon;
 	}
 	/**
 	 * @return the cacheData
 	 */
 	public boolean isCacheData() {
 		return cacheData;
 	}
 	/**
 	 * If true, the first response from the server is cached. 
 	 * @param cacheData the cacheData to set
 	 */
 	public void setCacheData(boolean cacheData) {
 		this.cacheData = cacheData;
 	}
 	/**
 	 * @return the clientSideFilter
 	 */
 	public boolean isClientSideFilter() {
 		return clientSideFilter;
 	}
 	/**
 	 * Filters the result on the client side, if enabled.
 	 * @param clientSideFilter the clientSideFilter to set
 	 */
 	public void setClientSideFilter(boolean clientSideFilter) {
 		this.clientSideFilter = clientSideFilter;
 	}
 	/**
 	 * @return the autoPickFirstHit
 	 */
 	public boolean isAutoPickFirstHit() {
 		return autoPickFirstHit;
 	}
 	/**
 	 * @param autoPickFirstHit the autoPickFirstHit to set
 	 */
 	public void setAutoPickFirstHit(boolean autoPickFirstHit) {
 		this.autoPickFirstHit = autoPickFirstHit;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("autoPickFirstHit=").append(autoPickFirstHit).append("; ");
 		sb.append("cacheData=").append(cacheData).append("; ");
 		sb.append("clientSideFilter=").append(clientSideFilter).append("; ");
 		sb.append("dataLoaderJSClass=").append(dataLoaderJSClass).append("; ");
 		sb.append("keyDelayTime=").append(keyDelayTime).append("; ");
 		sb.append("contentRendererJSClass=").append(contentRendererJSClass).append("; ");
 		sb.append("showOpenBoxIcon=").append(showOpenBoxIcon).append("; ");
 		sb.append("minSearchKeyLength=").append(minSearchKeyLength).append("; ");
 		sb.append("maxFetchRows=").append(maxFetchRows).append("; ");
 		sb.append("textEditable=").append(textEditable).append("; ");
 		sb.append("selectTextOnFocus=").append(selectTextOnFocus).append("; ");
 		sb.append("openContentOnTextboxFocus=").append(openContentOnTextboxFocus).append("; ");
 		return sb.toString();
 	}
 	/**
 	 * @return the labelProviderJSClass
 	 */
 	public String getLabelProviderJSClass() {
 		return labelProviderJSClass;
 	}
 	/**
 	 * @param labelProviderJSClass the labelProviderJSClass to set
 	 */
 	public void setLabelProviderJSClass(String labelProviderJSClass) {
 		this.labelProviderJSClass = labelProviderJSClass;
 	}
 	/**
 	 * @return the transferFullObject
 	 */
 	public boolean isTransferFullObject() {
 		return transferFullObject;
 	}
 	/**
 	 * Set to true to send the objects via the ObjectToJSON serializer to the client
 	 * control. This is often useful if custom label providers are used. Default
 	 * is false.
 	 * @param transferFullObject the transferFullObject to set
 	 */
 	public void setTransferFullObject(boolean transferFullObject) {
 		this.transferFullObject = transferFullObject;
 	}
 	
 	/**
 	 */
 	public void setDataFilter(String dataFilter){
 		this.dataFilter = dataFilter;
 	}
 	/**
 	 * @return dataFilter
 	 */
	public void getDataFilter(){
 		return this.dataFilter;
 	}
 	
 }
