 package com.jakeapp.gui.swing.actions.users;
 
 import com.jakeapp.core.domain.TrustState;
 import com.jakeapp.gui.swing.JakeMainView;
import com.jakeapp.gui.swing.helpers.UserHelper;
 import com.jakeapp.gui.swing.actions.abstracts.UserAction;
 import org.apache.log4j.Logger;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 
 /**
  * The Invite people action.
  * Opens a Dialog that let you add people to the project.
  * They get an invitation and can join/refuse the project.
  */
// fixme: make abstact trust user class
 public class TrustUsersAction extends UserAction {
 	private static final Logger log = Logger.getLogger(TrustUsersAction.class);
 	private static final TrustState actionTrustState = TrustState.TRUST;
 
 	public TrustUsersAction(JList list) {
 		super(list);
 
 		String actionStr = JakeMainView.getMainView().getResourceMap().
 						getString("trustedPeopleMenuItem.text");
 
 		putValue(Action.NAME, actionStr);
 
 		// update state
 		updateAction();
 	}
 
 
 	public void actionPerformed(ActionEvent actionEvent) {
 		log.info("Trust ProjectMember " + getList() + " from" + getProject());
 		setUserTrustState(actionTrustState);
 	}
 
 	@Override
 	public void updateAction() {
 		super.updateAction();
		setEnabled(this.isEnabled() && !UserHelper.isCurrentProjectMember(getSelectedUser().getUser()));
 
 		// update state
 		putValue(Action.SELECTED_KEY, checkUserStatus(actionTrustState));
 	}
 }
