 /**
  * 
  */
 package net.sourceforge.eclipseccase;
 
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
 import org.eclipse.core.runtime.preferences.DefaultScope;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 
 import com.sun.org.apache.bcel.internal.generic.InstructionConstants;
 
 /**
  * Class for setting default clearcase preferences.
  * 
  * @author mike
  * 
  */
 public class ClearCasePreferences extends AbstractPreferenceInitializer {
 
 	/**
 	 * Returns the preference value for <code>ADD_AUTO</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isAddAuto() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getBoolean(
 				IClearCasePreferenceConstants.ADD_AUTO);
 	}
 
 	/**
 	 * Returns the preference value for <code>ADD_AUTO</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isAddWithCheckin() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getBoolean(
 				IClearCasePreferenceConstants.ADD_WITH_CHECKIN);
 	}
 
 	/**
 	 * Returns the preference value for <code>CHECKOUT_AUTO</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isCheckoutAutoAlways() {
 		return IClearCasePreferenceConstants.ALWAYS.equals(ClearCasePlugin
 				.getDefault().getPluginPreferences().getString(
 						IClearCasePreferenceConstants.CHECKOUT_AUTO));
 	}
 
 	public static void setCheckoutAutoAlways() {
 		ClearCasePlugin.getDefault().getPluginPreferences().setValue(
 				IClearCasePreferenceConstants.CHECKOUT_AUTO,
 				IClearCasePreferenceConstants.ALWAYS);
 	}
 
 	/**
 	 * Returns the preference value for <code>CHECKOUT_AUTO</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isCheckoutAutoNever() {
 		return IClearCasePreferenceConstants.NEVER.equals(ClearCasePlugin
 				.getDefault().getPluginPreferences().getString(
 						IClearCasePreferenceConstants.CHECKOUT_AUTO));
 	}
 
 	public static void setCheckoutAutoNever() {
 		ClearCasePlugin.getDefault().getPluginPreferences().setValue(
 				IClearCasePreferenceConstants.CHECKOUT_AUTO,
 				IClearCasePreferenceConstants.NEVER);
 	}
 
 	/**
 	 * Returns the preference value for <code>CHECKOUT_LATEST</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isCheckoutLatest() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getBoolean(
 				IClearCasePreferenceConstants.CHECKOUT_LATEST);
 	}
 
 	public static boolean isFullRefreshOnAssociate() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getBoolean(
 				IClearCasePreferenceConstants.FULL_REFRESH);
 	}
 
 	/**
 	 * Returns the preference value for <code>COMMENT_ADD</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isCommentAdd() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getBoolean(
 				IClearCasePreferenceConstants.COMMENT_ADD);
 	}
 
 	/**
 	 * Returns the preference value for <code>COMMENT_ADD_NEVER_ON_AUTO</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isCommentAddNeverOnAuto() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getBoolean(
 				IClearCasePreferenceConstants.COMMENT_ADD_NEVER_ON_AUTO);
 	}
 
 	/**
 	 * Returns the preference value for <code>COMMENT_CHECKIN</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isCommentCheckin() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getBoolean(
 				IClearCasePreferenceConstants.COMMENT_CHECKIN);
 	}
 
 	/**
 	 * Returns the preference value for <code>COMMENT_CHECKOUT</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isCommentCheckout() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getBoolean(
 				IClearCasePreferenceConstants.COMMENT_CHECKOUT);
 	}
 
 	/**
 	 * Returns the preference value for
 	 * <code>COMMENT_CHECKOUT_NEVER_ON_AUTO</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isCommentCheckoutNeverOnAuto() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getBoolean(
 				IClearCasePreferenceConstants.COMMENT_CHECKOUT_NEVER_ON_AUTO);
 	}
 
 	/**
 	 * Returns the preference value for <code>COMMENT_ESCAPE</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isCommentEscape() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getBoolean(
 				IClearCasePreferenceConstants.COMMENT_ESCAPE);
 	}
 
 	/**
 	 * Returns the preference value for <code>IGNORE_NEW</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isIgnoreNew() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getBoolean(
 				IClearCasePreferenceConstants.IGNORE_NEW);
 	}
 
 	/**
 	 * Returns the preference value for <code>PRESERVE_TIMES</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isPreserveTimes() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getBoolean(
 				IClearCasePreferenceConstants.PRESERVE_TIMES);
 	}
 
 	/**
 	 * Returns the preference value for <code>RECURSIVE</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isRecursive() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getBoolean(
 				IClearCasePreferenceConstants.RECURSIVE);
 	}
 
 	/**
 	 * Returns the preference value for <code>CHECKOUT_RESERVED</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isReservedCheckoutsAlways() {
 		return IClearCasePreferenceConstants.ALWAYS.equals(ClearCasePlugin
 				.getDefault().getPluginPreferences().getString(
 						IClearCasePreferenceConstants.CHECKOUT_RESERVED));
 	}
 
 	/**
 	 * Returns the preference value for <code>CHECKOUT_RESERVED</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isReservedCheckoutsIfPossible() {
 		return IClearCasePreferenceConstants.IF_POSSIBLE.equals(ClearCasePlugin
 				.getDefault().getPluginPreferences().getString(
 						IClearCasePreferenceConstants.CHECKOUT_RESERVED));
 	}
 
 	/**
 	 * Returns the preference value for <code>CHECKOUT_RESERVED</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isReservedCheckoutsNever() {
 		return IClearCasePreferenceConstants.NEVER.equals(ClearCasePlugin
 				.getDefault().getPluginPreferences().getString(
 						IClearCasePreferenceConstants.CHECKOUT_RESERVED));
 	}
 
 	/**
 	 * Returns the preference value for <code>USE_SINGLE_PROCESS</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isUseSingleProcess() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getBoolean(
 				IClearCasePreferenceConstants.USE_SINGLE_PROCESS);
 	}
 
 	/**
 	 * Returns the preference value for <code>HIDE_REFRESH_STATE_ACTIVITY</code>
 	 * .
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isHideRefreshActivity() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getBoolean(
 				IClearCasePreferenceConstants.HIDE_REFRESH_STATE_ACTIVITY);
 	}
 
 	/**
 	 * Returns the preference value for <code>USE_CLEARDLG</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isUseClearDlg() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getBoolean(
 				IClearCasePreferenceConstants.USE_CLEARDLG);
 	}
 
 	/**
 	 * Returns the preference value for <code>PREVENT_UNNEEDED_CHILDREN_REFRESH</code>.
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isUnneededChildrenRefreshPrevented() {
 		return ClearCasePlugin
 				.getDefault()
 				.getPluginPreferences()
 				.getBoolean(
 						IClearCasePreferenceConstants.PREVENT_UNNEEDED_CHILDREN_REFRESH);
 	}
 	
 	/**
 	 * 
 	 * @return the preference value
 	 */
 	public static boolean isUCM(){
 		return ClearCasePlugin.getDefault().getPluginPreferences().getBoolean(IClearCasePreferenceConstants.USE_UCM);
 	}
 
 	/**
 	 * Gets the preference value for <code>CLEARCASE_PRIMARY_GROUP</code>.
 	 * 
 	 * @return the CLEARCASE_PRIMARY_GROUP name
 	 */
 	public static String getClearCasePrimaryGroup() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getString(
 				IClearCasePreferenceConstants.CLEARCASE_PRIMARY_GROUP);
 	}
 
 	public static String getBranchPrefix() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getString(
 				IClearCasePreferenceConstants.BRANCH_PREFIX);
 	}
 
 	public static boolean isCheckinIdenticalAllowed() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getBoolean(
 				IClearCasePreferenceConstants.CHECKIN_IDENTICAL);
 	}
 
 	/**
 	 * @return True if changes should be kept in a "keep" file after uncheckout,
 	 *         false if not.
 	 */
 	public static boolean isKeepChangesAfterUncheckout() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getBoolean(
 				IClearCasePreferenceConstants.KEEP_CHANGES_AFTER_UNCHECKOUT);
 	}
 
 	/**
 	 * @return True if refresh should traverse the link parent during refresh,
 	 *         which is an optimization for linked directories.
 	 */
 	public static boolean isTestLinkedParentInClearCase() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getBoolean(
 				IClearCasePreferenceConstants.TEST_LINKED_PARENT_IN_CLEARCASE);
 	}
 
 	public static boolean isAutoCheckinParentAfterMoveAllowed() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getBoolean(
 				IClearCasePreferenceConstants.AUTO_PARENT_CHECKIN_AFTER_MOVE);
 	}
 
 	public static int jobQueuePriority() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getInt(
 				IClearCasePreferenceConstants.JOB_QUEUE_PRIORITY);
 	}
 
 	public static boolean useGraphicalExternalUpdateView() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getBoolean(
 				IClearCasePreferenceConstants.GRAPHICAL_EXTERNAL_UPDATE_VIEW);
 	}
 
 	/**
 	 * Used to know if config spec modification has been forbidden
 	 * 
 	 * @return true if modification forbidden; false else.
 	 */
 	public static boolean isConfigSpecModificationForbidden() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getBoolean(
 				IClearCasePreferenceConstants.FORBID_CONFIG_SPEC_MODIFICATION);
 	}
 
 	/**
 	 * Used for a
 	 * 
 	 * @return
 	 */
 	public static boolean isUseMasterForAdd() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getBoolean(
 				IClearCasePreferenceConstants.ADD_WITH_MASTER);
 	}
 
 	
 	public static boolean isCompareExternal() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getBoolean(
 				IClearCasePreferenceConstants.COMPARE_EXTERNAL);
 	}
 	
 	public static String isPreventCheckOut() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getString(
 				IClearCasePreferenceConstants.PREVENT_CHECKOUT);
 	}
 	
 	public static boolean isSilentPrevent() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getBoolean(
 				IClearCasePreferenceConstants.SILENT_PREVENT);
 		
 	}
 	
 	public static void setPreventCheckOut(){
 		ClearCasePlugin.getDefault().getPluginPreferences().setValue(
 				IClearCasePreferenceConstants.PREVENT_CHECKOUT,
				value);
 	}
 	
 	public static String activityPattern() {
 		return ClearCasePlugin.getDefault().getPluginPreferences().getString(
 				IClearCasePreferenceConstants.ACTIVITY_PATTERN);
 	}
 	
 	public static String getNewActivityFormatMsg(){
 		return ClearCasePlugin.getDefault().getPluginPreferences().getString(
 				IClearCasePreferenceConstants.ACTIVITY_MSG_FORMAT);
 	}
 	
 	/**
 	 * Clients should not call this method. It will be called automatically by
 	 * the preference initializer when the appropriate default preference node
 	 * is accessed
 	 */
 	@Override
 	public void initializeDefaultPreferences() {
 		IEclipsePreferences defaults = new DefaultScope()
 				.getNode(ClearCasePlugin.PLUGIN_ID);
 		// General preferences
 		defaults.putBoolean(IClearCasePreferenceConstants.USE_SINGLE_PROCESS,
 				true);
 		defaults
 				.putBoolean(
 						IClearCasePreferenceConstants.PREVENT_UNNEEDED_CHILDREN_REFRESH,
 						true);
 		String sClearCasePrimaryGroup = System
 				.getenv("CLEARCASE_PRIMARY_GROUP");
 		if (sClearCasePrimaryGroup == null) {
 			sClearCasePrimaryGroup = "";
 		}
 		defaults.put(IClearCasePreferenceConstants.CLEARCASE_PRIMARY_GROUP,
 				sClearCasePrimaryGroup);
 		defaults
 				.put(IClearCasePreferenceConstants.TIMEOUT_GRAPHICAL_TOOLS, "2");
 		defaults.putBoolean(IClearCasePreferenceConstants.USE_CLEARDLG, false); //$NON-NLS-1$
 		defaults
 				.putBoolean(IClearCasePreferenceConstants.PRESERVE_TIMES, false);
 		defaults.putBoolean(IClearCasePreferenceConstants.IGNORE_NEW, false);
 		defaults.putBoolean(IClearCasePreferenceConstants.RECURSIVE, true); //$NON-NLS-1$
 		defaults.put(IClearCasePreferenceConstants.SAVE_DIRTY_EDITORS,
 				IClearCasePreferenceConstants.PROMPT);
 		defaults
 				.putBoolean(
 						IClearCasePreferenceConstants.HIDE_REFRESH_STATE_ACTIVITY,
 						true);
 		// source management
 		defaults.putBoolean(IClearCasePreferenceConstants.ADD_AUTO, true); //$NON-NLS-1$
 		defaults.put(IClearCasePreferenceConstants.CHECKOUT_AUTO,
 				IClearCasePreferenceConstants.PROMPT);
 		defaults.putBoolean(IClearCasePreferenceConstants.ADD_WITH_CHECKIN,
 				false);
 		defaults.put(IClearCasePreferenceConstants.CHECKOUT_RESERVED,
 				IClearCasePreferenceConstants.NEVER); //$NON-NLS-1$
 		defaults
 				.putBoolean(IClearCasePreferenceConstants.CHECKOUT_LATEST, true);
 		defaults.putBoolean(IClearCasePreferenceConstants.FULL_REFRESH, false);
 		defaults
 				.putBoolean(IClearCasePreferenceConstants.ADD_WITH_MASTER, true); //$NON-NLS-1$
 		defaults.putBoolean(IClearCasePreferenceConstants.USE_UCM, false);
 		// comments
 		defaults.putBoolean(IClearCasePreferenceConstants.COMMENT_ADD, true); //$NON-NLS-1$
 		defaults.putBoolean(
 				IClearCasePreferenceConstants.COMMENT_ADD_NEVER_ON_AUTO, true);
 		defaults
 				.putBoolean(IClearCasePreferenceConstants.COMMENT_CHECKIN, true);
 		defaults.putBoolean(IClearCasePreferenceConstants.COMMENT_CHECKOUT,
 				false); //$NON-NLS-1$
 		defaults.putBoolean(
 				IClearCasePreferenceConstants.COMMENT_CHECKOUT_NEVER_ON_AUTO,
 				true);
 		defaults
 				.putBoolean(IClearCasePreferenceConstants.COMMENT_ESCAPE, false);
 		defaults.putInt(IClearCasePreferenceConstants.JOB_QUEUE_PRIORITY,
 				Job.DECORATE); //$NON-NLS-1$
 
 		defaults.putBoolean(
 				IClearCasePreferenceConstants.TEST_LINKED_PARENT_IN_CLEARCASE,
 				false);
 		defaults.putBoolean(
 				IClearCasePreferenceConstants.KEEP_CHANGES_AFTER_UNCHECKOUT,
 				true); //$NON-NLS-1$
 		defaults.putBoolean(
 				IClearCasePreferenceConstants.AUTO_PARENT_CHECKIN_AFTER_MOVE,
 				false);
 		defaults.putBoolean(
 				IClearCasePreferenceConstants.GRAPHICAL_EXTERNAL_UPDATE_VIEW,
 				true);
 		defaults.put(IClearCasePreferenceConstants.BRANCH_PREFIX, ""); //$NON-NLS-1$
 		defaults.putBoolean(
 				IClearCasePreferenceConstants.FORBID_CONFIG_SPEC_MODIFICATION,
 				false);
 		defaults.putBoolean(IClearCasePreferenceConstants.SILENT_PREVENT,false);//$NON-NLS-1$
 
 		setGraphicalToolTimeout();
 
 	}
 
 	public static void setGraphicalToolTimeout() {
 		/* Set timeout as an environment variable */
 		System.setProperty("TIMEOUT_GRAPHICAL_TOOLS", ClearCasePlugin
 				.getDefault().getPluginPreferences().getString(
 						IClearCasePreferenceConstants.TIMEOUT_GRAPHICAL_TOOLS));
 	}
 
 }
