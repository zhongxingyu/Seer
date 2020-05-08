 package com.sri.straylight.fmuWrapper.Controller;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 import org.bushe.swing.event.annotation.AnnotationProcessor;
 
 import com.sri.straylight.fmuWrapper.JNAfmuWrapper;
 import com.sri.straylight.fmuWrapper.JNAfmuWrapper.MessageCallbackInterface;
 import com.sri.straylight.fmuWrapper.JNAfmuWrapper.ResultCallbackInterface;
 import com.sri.straylight.fmuWrapper.JNAfmuWrapper.StateChangeCallbackInterface;
 import com.sri.straylight.fmuWrapper.event.ConfigChangeNotify;
 import com.sri.straylight.fmuWrapper.event.MessageEvent;
 import com.sri.straylight.fmuWrapper.event.ResultEvent;
 import com.sri.straylight.fmuWrapper.event.SimStateNativeNotify;
 import com.sri.straylight.fmuWrapper.event.XMLparsedEvent;
 import com.sri.straylight.fmuWrapper.framework.AbstractController;
 import com.sri.straylight.fmuWrapper.model.FMUwrapperConfig;
 import com.sri.straylight.fmuWrapper.voManaged.ScalarValueResults;
 import com.sri.straylight.fmuWrapper.voManaged.ScalarVariablesAll;
 import com.sri.straylight.fmuWrapper.voManaged.XMLparsedInfo;
 import com.sri.straylight.fmuWrapper.voNative.ConfigStruct;
 import com.sri.straylight.fmuWrapper.voNative.EnumTypeMapper;
 import com.sri.straylight.fmuWrapper.voNative.MessageStruct;
 import com.sri.straylight.fmuWrapper.voNative.ScalarValueRealStruct;
 import com.sri.straylight.fmuWrapper.voNative.ScalarValueResultsStruct;
 import com.sri.straylight.fmuWrapper.voNative.ScalarVariablesAllStruct;
 import com.sri.straylight.fmuWrapper.voNative.SimStateNative;
 import com.sun.jna.Library;
 import com.sun.jna.Native;
 
 /**
  * The Class FMUcontroller.
  */
 public class FMUcontroller extends AbstractController {
 
 	private JNAfmuWrapper jnaFMUWrapper_;
 	private ConfigStruct configStruct_;
 	protected FMUwrapperConfig fmuWrapperConfig_;
 	private SimStateNative simStateNative_;
 
 	private HashMap<SimStateNative, Boolean> fireEventsForStates_ = new HashMap<SimStateNative, Boolean>();
 
 	private MessageCallback messageCallbackFunc_;
 	private ResultCallback resultCallbackFunc_;
 	private StateChangeCallback stateChangeCallbackFunc_;
 
 	private class MessageCallback implements MessageCallbackInterface {
 
 		public boolean messageCallback(MessageStruct messageStruct) {
 
 			MessageEvent event = new MessageEvent(this, messageStruct);
 			fireEvent(event);
 
 			return true;
 		}
 
 	}
 
 	private class ResultCallback implements ResultCallbackInterface {
 
 		public boolean resultCallback(
 				ScalarValueResultsStruct scalarValueResultsStruct) {
 
 			ScalarValueResults scalarValueResults = new ScalarValueResults(
 					scalarValueResultsStruct);
 			ResultEvent event = new ResultEvent(this, scalarValueResults);
 
 			fireEvent(event);
 
 			return true;
 		}
 
 	}
 
 	private class StateChangeCallback implements StateChangeCallbackInterface {
 
 		public boolean stateChangeCallback(SimStateNative simStateNative) {
 
 			simStateNative_ = simStateNative;
 
 			if (fireEventsForStates_.containsKey(simStateNative)) {
 				SimStateNativeNotify e = new SimStateNativeNotify(this, simStateNative);
 				fireEvent(e);
 			}
 
 			return true;
 		}
 
 	}
 
 	/**
 	 * The state change callback func_. private StateChangeCallbackInterface
 	 * stateChangeCallbackFunc_ =
 	 * 
 	 * new JNAfmuWrapper.StateChangeCallbackInterface() { public boolean
 	 * stateChangeCallback(SimStateNative simStateNative) {
 	 * 
 	 * simStateNative_ = simStateNative;
 	 * 
 	 * if (fireEventsForStates_.containsKey(simStateNative)) {
 	 * notifyStateChange_(simStateNative); }
 	 * 
 	 * return true; } };
 	 */
 
 	// constructor
 	public FMUcontroller(AbstractController parent) {
 		super(parent);
 		fmuWrapperConfig_ = FMUwrapperConfig.load();
 		init();
 	}
 
 	public FMUcontroller(AbstractController parent,
 			FMUwrapperConfig fmuWrapperConfig) {
 		super(parent);
 		fmuWrapperConfig_ = fmuWrapperConfig;
 		init();
 	}
 
 	public FMUcontroller() {
 		super(null);
 		fmuWrapperConfig_ = FMUwrapperConfig.load();
 		init();
 	}
 
 	public SimStateNative getSimStateNative() {
 		return simStateNative_;
 	}
 
 	protected void init() {
 
 		AnnotationProcessor.process(this);
 
 		messageCallbackFunc_ = new MessageCallback();
 		resultCallbackFunc_ = new ResultCallback();
 		stateChangeCallbackFunc_ = new StateChangeCallback();
 		simStateNative_ = SimStateNative.simStateNative_0_uninitialized;
 
 		//fireEventsForStates_.put(SimStateNative.simStateNative_0_uninitialized,
 			//	true);
 
 		fireEventsForStates_.put(
 				SimStateNative.simStateNative_1_connect_completed, true);
 
 		fireEventsForStates_.put(
 				SimStateNative.simStateNative_2_xmlParse_completed, true);
 
 		fireEventsForStates_.put(SimStateNative.simStateNative_3_ready, true);
 
 		fireEventsForStates_.put(SimStateNative.simStateNative_4_run_completed,
 				true);
 		fireEventsForStates_.put(SimStateNative.simStateNative_4_run_started,
 				true);
 
 	//	fireEventsForStates_.put(SimStateNative.simStateNative_5_step_started,
 	//			true);
 		fireEventsForStates_.put(
 				SimStateNative.simStateNative_5_step_completed, true);
 
 		fireEventsForStates_.put(
 				SimStateNative.simStateNative_7_terminate_completed, true);
 		
 		fireEventsForStates_.put(SimStateNative.simStateNative_e_error, true);
 
 	}
 
 
 	/**
 	 * Gets the meta data.
 	 * 
 	 * @return the meta data
 	 */
 	public ConfigStruct getConfigStruct() {
 		return configStruct_;
 	}
 
 	/**
 	 * Xml parse.
 	 */
 	private void xmlParse_() {
 
 		jnaFMUWrapper_.xmlParse(fmuWrapperConfig_.fmuFolderAbsolutePath);
 
 		ScalarVariablesAllStruct scalarVariablesAllStruct = jnaFMUWrapper_
 				.getAllScalarVariables();
 		ScalarVariablesAll scalarVariablesAll = new ScalarVariablesAll(
 				scalarVariablesAllStruct);
 
 		XMLparsedInfo xmlParsed = new XMLparsedInfo(scalarVariablesAll);
 		XMLparsedEvent event = new XMLparsedEvent(this, xmlParsed);
 
 		fireEvent(event);
 
 		configStruct_ = jnaFMUWrapper_.getConfig();
 		ConfigChangeNotify event2 = new ConfigChangeNotify(this, configStruct_);
 		fireEvent(event2);
 
 	}
 
 	/**
 	 * Check config.
 	 * 
 	 * @throws IOException
 	 *             Signals that an I/O exception has occurred.
 	 */
 	private void checkConfig() throws IOException {
 
 		// check to see that DLL exists
 		boolean exists = (new File(
 				fmuWrapperConfig_.nativeLibFolderAbsolutePath
 				+ "\\FMUwrapper.dll")).exists();
 
 		if (!exists) {
 			throw new IOException("FMUwrapper.dll not found in: "
 					+ fmuWrapperConfig_.nativeLibFolderAbsolutePath);
 		}
 
 	}
 
 	/**
 	 * Connect.
 	 * 
 	 * @throws Exception
 	 *             the exception
 	 */
 	private void connect_() throws IOException {
 
 		checkConfig();
 
 		System.setProperty("jna.library.path",
 				fmuWrapperConfig_.nativeLibFolderAbsolutePath);
 
 		Map<String, Object> options = new HashMap<String, Object>();
 		EnumTypeMapper mp = new EnumTypeMapper();
 		
 		if (null == jnaFMUWrapper_) {
 			
 			options.put(Library.OPTION_TYPE_MAPPER, mp);
 			jnaFMUWrapper_ = (JNAfmuWrapper) Native.loadLibrary("FMUwrapper",
 					JNAfmuWrapper.class, options);
 		}
 		
 
 
 		jnaFMUWrapper_.connect(messageCallbackFunc_, resultCallbackFunc_,
 				stateChangeCallbackFunc_);
 
 		// notifyStateChange_(SimStateNative.simStateNative_1_connect_completed);
 	}
 
 	/**
 	 * Sets the config.
 	 * 
 	 * @param configStruct
 	 *            the new config
 	 */
 	public void setConfig(ConfigStruct configStruct) {
 
 		configStruct_ = configStruct;
 		int result = jnaFMUWrapper_.setConfig(configStruct_);
 
 		if (result == 0) {
 
 			ConfigChangeNotify event = new ConfigChangeNotify(this,
 					configStruct_);
 			fireEvent(event);
 		}
 
 	}
 
 	/**
 	 * Request state change.
 	 * 
 	 * @param newState
 	 *            the new state
 	 */
 	public void requestStateChange(SimStateNative newState) {
 
 		switch (newState) {
 		case simStateNative_1_connect_requested:
 			try {
 				connect_();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			break;
 		case simStateNative_2_xmlParse_requested:
 			xmlParse_();
 			break;
 		case simStateNative_8_tearDown_requested:
 			forceCleanup();
 			break;
 		default:
 			jnaFMUWrapper_.requestStateChange(newState);
 		}
 	}
 
 	/**
 	 * Sets the scalar value real.
 	 * 
 	 * @param idx
 	 *            the idx
 	 * @param value
 	 *            the value
 	 */
 	public void setScalarValueReal(int idx, double value) {
 		jnaFMUWrapper_.setScalarValueReal(idx, value);
 	}
 
 	/**
 	 * Sets the scalar values.
 	 * 
 	 * @param scalarValueList
 	 *            the new scalar values
 	 */
 	public void setScalarValues(Vector<ScalarValueRealStruct> scalarValueList) {
 
 		int len = scalarValueList.size();
 		ScalarValueRealStruct[] ary4 = (ScalarValueRealStruct[]) new ScalarValueRealStruct()
 		.toArray(len);
 
 		for (int i = 0; i < len; i++) {
 			ScalarValueRealStruct struct = scalarValueList.get(i);
 			ary4[i].idx = struct.idx;
 			ary4[i].value = struct.value;
 		}
 
 		jnaFMUWrapper_.setScalarValues(ary4, len);
 
 	}
 
 	public void forceCleanup() {

		if (null != jnaFMUWrapper_) {
			jnaFMUWrapper_.forceCleanup();
		}
 		
 	}
 
 }
