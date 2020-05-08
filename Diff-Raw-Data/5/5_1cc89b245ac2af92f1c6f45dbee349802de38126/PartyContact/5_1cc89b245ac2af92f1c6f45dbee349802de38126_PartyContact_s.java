 package module.contacts.domain;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import module.organization.domain.Party;
 import module.organization.domain.Person;
 import myorg.applicationTier.Authenticate.UserView;
 import myorg.domain.User;
 import myorg.domain.exceptions.DomainException;
 import myorg.domain.groups.PersistentGroup;
 import myorg.domain.groups.Role;
 
 import org.apache.commons.lang.StringUtils;
 
 import pt.ist.fenixWebFramework.services.Service;
 import pt.ist.fenixframework.plugins.luceneIndexing.IndexableField;
 import pt.ist.fenixframework.plugins.luceneIndexing.domain.IndexDocument;
 import pt.ist.fenixframework.plugins.luceneIndexing.domain.interfaces.Indexable;
 import pt.ist.fenixframework.plugins.luceneIndexing.domain.interfaces.Searchable;
 import dml.runtime.Relation;
 import dml.runtime.RelationListener;
 
 public abstract class PartyContact extends PartyContact_Base implements Indexable, Searchable, IndexableField {
 
     public PartyContact() {
 	super();
 	ContactsConfigurator.getInstance().addPartyContact(this);
 	this.PersistentGroupPartyContact.addListener(new ValidVisibilityGroupsEnforcer());
     }
 
     static protected void validateUser(User userCreatingTheContact, Party partyThatWillOwnTheContact, PartyContactType type) {
 	if (isOwner(userCreatingTheContact, partyThatWillOwnTheContact) && !type.equals(PartyContactType.IMMUTABLE))
 	    // if he is the owner and the contact isn't immutable, then it can
 	    // edit it
 	    return;
 	if (Role.getRole(ContactsRoles.MODULE_CONTACTS_DOMAIN_CONTACTSEDITOR).isMember(userCreatingTheContact))
 	    return;
 	throw new DomainException("manage.contacts.edit.denied.nouser");
 
     }
 
     static protected void validateVisibilityGroups(List<PersistentGroup> visibilityGroups) {
 	if (!ContactsConfigurator.getInstance().getVisibilityGroups().containsAll(visibilityGroups)) {
 	    throw new DomainException("manage.contacts.wrong.visibility.groups.defined");
 	}
 
     }
 
     /**
      * Class that makes sure that when you add a visibility group to a contact,
      * that it exists on the list of visibility groups defined in the
      * ContactsConfigurator {@link ContactsConfigurator};
      * 
      * @author João André Pereira Antunes (joao.antunes@tagus.ist.utl.pt)
      * 
      */
     final static class ValidVisibilityGroupsEnforcer implements RelationListener<PartyContact, PersistentGroup> {
 
 	@Override
 	public void beforeAdd(Relation<PartyContact, PersistentGroup> relation, PartyContact partyContact,
 		PersistentGroup persistentGroup) {
 	    if (!ContactsConfigurator.getInstance().hasVisibilityGroups(persistentGroup))
 		throw new DomainException("error.adding.contact.invalid.visibility.group",
 			DomainException.getResourceFor("resources/ContactsResources"));
 	}
 
 	@Override
 	public void afterAdd(Relation<PartyContact, PersistentGroup> rel, PartyContact o1, PersistentGroup o2) {
 	    // nothing needs to be done after as we don't have to worry about
 	    // concurrency issues
 	}
 
 	@Override
 	public void beforeRemove(Relation<PartyContact, PersistentGroup> rel, PartyContact o1, PersistentGroup o2) {
 	}
 
 	@Override
 	public void afterRemove(Relation<PartyContact, PersistentGroup> rel, PartyContact o1, PersistentGroup o2) {
 	}
 
     }
 
     @Override
     public String getFieldName() {
 	return this.getClass().getName();
     }
 
     @Override
     public Set<Indexable> getObjectsToIndex() {
 	return Collections.singleton((Indexable) this);
     }
 
     @Override
     public IndexDocument getDocumentToIndex() {
 	IndexDocument document = new IndexDocument(this);
 	document.indexField(this, getValue());
 	return document;
     }
 
     public Person getPerson() {
 	if (this.getParty() instanceof Person)
 	    return (Person) this.getParty();
 	else
 	    return null;
     }
 
     /**
      * Sets the contact value making sure that the user that called this method
      * has permissions to do it
      * 
      * @param value the value to set
      */
     @Service
     public void setContactValue(String value) {
	if (UserView.getCurrentUser() == null || !isEditableBy(UserView.getCurrentUser())) {
	    if (UserView.getCurrentUser() == null)
		throw new DomainException("manage.contacts.edit.denied.nouser", "resources.ContactsResources");
	    else
 		throw new DomainException("manage.contacts.edit.denied", "resources.ContactsResources", UserView.getCurrentUser()
 			.getUsername());
 	}
 	setValue(value);
     }
 
     public boolean isEditableBy(User user) {
 	if (isOwner(user) && !getType().equals(PartyContactType.IMMUTABLE))
 	    // if he is the owner and the contact isn't immutable, then it can
 	    // edit it
 	    return true;
 	if (Role.getRole(ContactsRoles.MODULE_CONTACTS_DOMAIN_CONTACTSEDITOR).isMember(user))
 	    return true;
 	return false;
     }
 
     /**
      * @param value the value to set on the contact.
      */
     protected abstract void setValue(String value);
 
     public String getValue() {
 	return getDescription();
     }
 
     /**
      * 
      * @return the String representation of the contact
      */
     public abstract String getDescription();
 
     /**
      * Sets the contact visible to the given groups if they are all contained in
      * the list of visibility groups of the ContactsConfigurator
      * {@link ContactsConfigurator}, otherwise it throws an exception
      * automaticly due to the listener
      * 
      * @param groups the groups to which this PartyContact will be visibile to
      */
     @Service
     public void setVisibleTo(List<PersistentGroup> groups) {
 	// add all of the groups that are on the groups but not on the current
 	// list of visibility groups
 	if (groups != null) {
 	    for (PersistentGroup persistentGroup : groups) {
 		if (!getVisibilityGroups().contains(persistentGroup)) {
 		    addVisibilityGroups(persistentGroup);
 		}
 	    }
 	}
 	List<PersistentGroup> currentSurplusGroups = new ArrayList<PersistentGroup>(getVisibilityGroups());
 	if (groups != null) {
 	    currentSurplusGroups.removeAll(groups);
 	}
 	if (!currentSurplusGroups.isEmpty()) {
 	    for (PersistentGroup persistentGroup : currentSurplusGroups) {
 		removeVisibilityGroups(persistentGroup);
 	    }
 	}
 
     }
 
     public boolean isVisibleTo(User currentUser) {
 	if (isOwner(currentUser)) {
 	    return true;
 	}
 	for (PersistentGroup group : getVisibilityGroups()) {
 	    if (group.isMember(currentUser))
 		return true;
 	}
 	return false;
     }
 
     public boolean isVisibleTo(PersistentGroup group) {
 	return (getVisibilityGroups().contains(group));
     }
 
     // FIXME (?) dependency of the structure of the Organization!!
     // DEPENDENCY
     /**
      * 
      * @param currentUser the User to assert if it is the owner of this
      *            partycontact or not
      * @return true if the currentUser is the owner of this PartyContact, false
      *         otherwise
      */
     private boolean isOwner(User currentUser) {
 	Party correspondingParty = getParty();
 	if (correspondingParty instanceof Person) {
 	    return (((Person) correspondingParty).getUser().equals(currentUser));
 	}
 	return false;
     }
 
     /**
      * 
      * @param currentUser the User to assert if it is the owner of this
      *            partycontact or not
      * @param partyFutureContactOwner the {@link Party} that will have the
      *            contact
      * @return true if the currentUser is the owner of this PartyContact, false
      *         otherwise
      */
     static private boolean isOwner(User currentUser, Party partyFutureContactOwner) {
 	Party correspondingParty = partyFutureContactOwner;
 	if (correspondingParty instanceof Person) {
 	    return (((Person) correspondingParty).getUser().equals(currentUser));
 	}
 	return false;
     }
 
     /**
      * 
      * @return the Person that owns this PartyContact or null if it doesn't
      *         exist
      */
     public Person getOwner() {
 	Party correspondingParty = getParty();
 	if (correspondingParty instanceof Person) {
 	    return (Person) correspondingParty;
 	}
 	return null;
     }
 
     @Override
     public String toString() {
 	return getDescription();
     }
 
     public void delete() {
 	removeParty();
 	removeContactsConfigurator();
 	for (PersistentGroup group : getVisibilityGroups()) {
 	    removeVisibilityGroups(group);
 	}
 	deleteDomainObject();
     }
 
     @Override
     public void setDefaultContact(Boolean defaultContact) {
 	if (defaultContact != null && defaultContact.booleanValue()) {
 	    // remove the other default contacts of this type so that there is
 	    // only one for each type
 	    for (PartyContact partyContact : getParty().getPartyContacts()) {
 		if (partyContact.getClass().isInstance(this.getClass()) && partyContact.getDefaultContact().booleanValue()) {
 		    partyContact.setDefaultContact(Boolean.FALSE);
 		}
 	    }
 	}
 	super.setDefaultContact(defaultContact);
     }
 
     @Service
     public void deleteByUser(User currentUser) {
 	if (!isEditableBy(currentUser))
 	    throw new DomainException("manage.contacts.edit.denied", UserView.getCurrentUser().getUsername());
 	delete();
     }
 
     public static EmailAddress getEmailAddressForSendingEmails(Party party) {
 	for (PartyContact contact : party.getPartyContactsSet()) {
 	    if (contact instanceof EmailAddress) {
 		EmailAddress email = (EmailAddress) contact;
 		if (email.getType().equals(PartyContactType.IMMUTABLE)) {
 		    return email;
 		}
 	    }
 	}
 	return null;
     }
 
     public static String getEmailForSendingEmails(Party party) {
 	EmailAddress email = getEmailAddressForSendingEmails(party);
 	return email != null ? email.getValue() : StringUtils.EMPTY;
     }
 }
