 package org.cotrix.application.impl;
 
 import static org.cotrix.common.Utils.*;
 
 import java.util.Collection;
 
 import javax.inject.Inject;
 
 import org.cotrix.action.Action;
 import org.cotrix.application.DelegationPolicy;
 import org.cotrix.application.PermissionDelegationService;
 import org.cotrix.common.cdi.Current;
 import org.cotrix.domain.dsl.Users;
 import org.cotrix.domain.user.Role;
 import org.cotrix.domain.user.User;
 import org.cotrix.repository.user.UserRepository;
 
 public class DefaultDelegationService implements PermissionDelegationService {
 
 	private final User currentUser;
 	
 	private final DelegationPolicy policy;
 	
 	private final UserRepository repository;
 	
 	@Inject
 	public DefaultDelegationService(@Current User user, UserRepository repository, DelegationPolicy policy) {
 		
 		notNull("current user", user);
 		notNull("user repository", repository);
 		notNull("delegation policy",policy);
 		
 		this.currentUser=user;
 		this.repository = repository;
 		this.policy=policy;
 	}
 	
 	@Override
 	public DelegateClause delegate(final Action... actions) {
 		
 		return new DelegateClause() {
 			
 			@Override
 			public void to(User user) {
 
 				validate(user);
 				
 				policy.validateDelegation(currentUser, user, actions);
 
 				User changeset = Users.user(user).can(actions).build();
 				
 				repository.update(changeset);
 				
 			}
 		};
 		
 	}
 	
 	@Override
 	public DelegateClause delegate(Collection<Action> actions) {
 		
 		
 		return delegate(actions.toArray(new Action[0]));
 	}
 	
 	
 	@Override
 	public DelegateClause delegate(final Role... roles) {
 		
 		return new DelegateClause() {
 			
 			@Override
 			public void to(User user) {
 
 				validate(user);
 				
				for (Role role : roles)
					policy.validateDelegation(currentUser, user, toArray(role.permissions()));
 				
 				User changeset = Users.user(user).is(roles).build();
 				
 				repository.update(changeset);
 				
 			}
 		};
 	}
 	
 	
 	@Override
 	public RevokeClause revoke(final Action... actions) {
 		
 		return new RevokeClause() {
 			
 			@Override
 			public void from(User user) {
 	
 				validate(user);
 				
 				for (Action p : actions)
 					if (!user.permissions().contains(p))
 						throw new IllegalStateException(user.name()+" does not have permission "+p);
 				
 				policy.validateRevocation(currentUser, user, actions);
 				
 				User changeset = Users.user(user).cannot(actions).build();
 				
 				repository.update(changeset);
 				
 			}
 		};
 	}
 	
 	@Override
 	public RevokeClause revoke(final Role... roles) {
 		
 		return new RevokeClause() {
 			
 			@Override
 			public void from(User user) {
 				
 				validate(user);
 	
				for (Role r : roles)
					if (user.is(r))
						policy.validateRevocation(currentUser, user, toArray(r.permissions()));
 				
 				User changeset = Users.user(user).isNot(roles).build();
 				
 				repository.update(changeset);
 			}
 		};
 		
 	}
 	
 	
 	@Override
 	public RevokeClause revoke(Collection<Action> actions) {
 		return revoke(actions.toArray(new Action[0]));
 	}
 
 	
 	//helper
 	
 	
 	private void validate(User u) {
 		
 		if (u.id()==null)
 			throw new IllegalArgumentException("cannot delegate roles to unidentified user "+u);
 	}
 	
 	Action[] toArray(Collection<Action> actions) {
 		return actions.toArray(new Action[0]);
 	}
 }
