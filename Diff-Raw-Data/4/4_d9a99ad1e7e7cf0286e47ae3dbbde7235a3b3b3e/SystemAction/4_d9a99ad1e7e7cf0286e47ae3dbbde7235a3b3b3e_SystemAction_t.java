 package ch.hsr.waktu.domain;
 
 public enum SystemAction {
 	AddProjects(0), ReadAllProjects(1), ReadOwnProjects(2), 
 	EditAllProjects(3), EditOwnProjects(4), 
	SetAllProjectsInactive(5), SetOwnProjectInactive(6),
	SetAllProjectsActive(7), SetOwnProjectActive(8),
 	CreateAllWorkPackages(9), CreateOwnWorkPackages(10),
 	ReadAllWorkPackages(11), ReadOwnWorkPackages(12),
 	EditAllWorkPackages(13), EditOwnWorkPackages(14),
 	SetAllWorkPackagesInActive(15), SetOwnWorkPackagesInActive(16),
 	AddUser(17), AddUserToAllProjects(18), AddUserToOwnProjects(19), 
 	ReadAllUserData(20), ReadStaffMemberData(21), EditAllUserData(22), EditStaffMemberData(23),
 	SetAllUsersInactive(24), SetStaffMemberInactive(25),
 	SettAllUsersActive(26), SetStaffMembersActive(27),
 	CreateAllWorksessions(28), CreateOwnWorksession(29),
 	ListAllWorksessions(30), ListWorksessionsFromOwnProjects(31), ListOwnWorksessions(32),
 	EditAllWorksessions(33), EditOwnWorksessions(34),
 	DeleteAllWorksessions(35), DeleteOwnWorksessions(36),
 	CreateAllFavorites(37), CreateOwnFavorites(38),
 	ReadAllFavorites(39), ReadOwnFavorites(40),
 	EditAllFavorites(41), EditOwnFavorites(42),
 	DeleteAllFavorites(43), DeleteOwnFavorites(44);
 	
 	private int index;
 	
 	SystemAction(int index) {
 		this.index = index;
 	}
 	
 	public int getIndex() {
 		return index;
 	}
 }
