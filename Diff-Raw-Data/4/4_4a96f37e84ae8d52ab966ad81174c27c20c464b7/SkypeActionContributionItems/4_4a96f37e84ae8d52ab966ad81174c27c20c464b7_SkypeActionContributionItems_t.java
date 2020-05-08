 package org.eclipse.ecf.internal.provider.skype.ui;
 
 import org.eclipse.ecf.core.IContainer;
 import org.eclipse.ecf.presence.roster.IRosterEntry;
 import org.eclipse.ecf.presence.ui.roster.AbstractRosterEntryContributionItem;
 import org.eclipse.ecf.provider.skype.SkypeContainer;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.osgi.util.NLS;
 
 public class SkypeActionContributionItems extends AbstractRosterEntryContributionItem {
 
	public SkypeActionContributionItems() {
		super();
	}

 	public SkypeActionContributionItems(String id) {
 		super(id);
 	}
 
 	protected IAction[] makeActions() {
 		final IRosterEntry entry = getSelectedRosterEntry();
 		final IContainer c = getContainerForRosterEntry(entry);
 		if (entry != null && c != null && c instanceof SkypeContainer) {
 			final IAction[] actions = new IAction[1];
 			actions[0] = new SkypeCallAction(c, entry.getUser().getID(), NLS.bind(Messages.SkypeActionContributionItems_Call_User, entry.getName()), NLS.bind(Messages.SkypeActionContributionItems_Call_User_Tooltip, entry.getName()));
 			return actions;
 		}
 		return null;
 	}
 
 }
