 package language;
 
 public class EN extends Lang{
 	// ============== SignInPage Strings ===================
 	private String login_btn_lang = "Language";
 	private String login_btn_learn = "Learn more";
 	private String login_signIn = "Sign in with:";
 	private String login_moto = "Find mentors and trainees with the right qualifications and share knowledge, all in one place.";
 
 	public String getLogin_btn_lang() {
 		return login_btn_lang;
 	}
 	public String getLogin_btn_learn() {
 		return login_btn_learn;
 	}
 	public String getLogin_signIn() {
 		return login_signIn;
 	}
 	public String getLogin_moto() {
 		return login_moto;
 	}
 	
 	// ============== FirstTimeLogIn Strings ===================
 	private String firsttime_label_personal = "Personal";
 	private String firsttime_label_fName = "First name";
 	private String firsttime_label_lName = "Last name";
 	private String firsttime_label_email = "Email";
 	private String firsttime_label_country = "Country";
 	private String firsttime_label_city = "City";
 	private String firsttime_label_connections = "Connections";
 	private String firsttime_label_subject = "Subject";
 	private String firsttime_label_field = "Field";
 	private String firsttime_radio_mentor = "Mentor";
 	private String firsttime_radio_trainee = "Trainee";
	private String firsttime_label_addInfo = "Additional info";
 	private String firsttime_label_experience = "Experience";
 	private String firsttime_drop_novice = "Novice";
 	private String firsttime_drop_intermediate = "Intermediate";
 	private String firsttime_drop_expert = "Expert";
 	private String firsttime_drop_loreMaster = "Guru";
 	private String firsttime_btn_add = "Add";
 	private String firsttime_btn_submit = "Submit";
 	
 	public String getFirsttime_label_personal() {
 		return firsttime_label_personal;
 	}
 	public String getFirsttime_label_fName() {
 		return firsttime_label_fName;
 	}
 	public String getFirsttime_label_lName() {
 		return firsttime_label_lName;
 	}
 	public String getFirsttime_label_email() {
 		return firsttime_label_email;
 	}
 	public String getFirsttime_label_country() {
 		return firsttime_label_country;
 	}
 	public String getFirsttime_label_city() {
 		return firsttime_label_city;
 	}
 	public String getFirsttime_label_connections() {
 		return firsttime_label_connections;
 	}
 	public String getFirsttime_label_subject() {
 		return firsttime_label_subject;
 	}
 	public String getFirsttime_label_field() {
 		return firsttime_label_field;
 	}
 	public String getFirsttime_radio_mentor() {
 		return firsttime_radio_mentor;
 	}
 	public String getFirsttime_radio_trainee() {
 		return firsttime_radio_trainee;
 	}
 	public String getFirsttime_label_addInfo() {
 		return firsttime_label_addInfo;
 	}
 	public String getFirsttime_label_experience() {
 		return firsttime_label_experience;
 	}
 	public String getFirsttime_btn_add() {
 		return firsttime_btn_add;
 	}
 	public String getFirsttime_btn_submit() {
 		return firsttime_btn_submit;
 	}
 	public String getFirsttime_drop_novice() {
 		return firsttime_drop_novice;
 	}
 	public String getFirsttime_drop_intermediate() {
 		return firsttime_drop_intermediate;
 	}
 	public String getFirsttime_drop_expert() {
 		return firsttime_drop_expert;
 	}
 	public String getFirsttime_drop_loreMaster() {
 		return firsttime_drop_loreMaster;
 	}
 	
 	// ============== Home Strings ===================
 	private String home_welcome = "Welcome to MentorFind";
 	private String home_please_search = "Please type in the search box";
 	private String connections_welcome = "Connections";
 	
 	@Override
 	public String getHome_welcome() {
 		
 		return home_welcome;
 	}
 	@Override
 	public String getHome_please_search() {
 		
 		return home_please_search;
 	}
 
 	@Override
 	public String getConnections_welcome() {
 		return connections_welcome;
 	}
 	
 	// ============== Menu Strings ===================
 		private String menu_home = "Home";
 		private String menu_fields = "Fields";
 		private String menu_profile = "Profile";
 		private String menu_connections = "Connections";
 		private String menu_search = "Search";
 		private String menu_logout = "Log out";
 		
 	public String getMenu_logout() {
 		return menu_logout;
 	}
 		
 	public String getMenu_home() {
 		return menu_home;
 	}
 
 	public String getMenu_fields() {
 		return menu_fields;
 	}
 
 	public String getMenu_profile() {
 
 		return menu_profile;
 	}
 
 	public String getMenu_connections() {
 
 		return menu_connections;
 	}
 
 	public String getMenu_search() {
 		return menu_search;
 	}
 	
 	// ============== Search Results Strings ===================
 	private String search_sResult = "Search results";
 	private String search_fResult = "Filter results";
 	private String search_location = "Location";
 	private String search_user = "User";
 	private String search_title = "Title";
 	private String search_description = "Description";
 	private String search_error = "You did not enter a search query. Please try again.";
 	private String search_resultStart = "Your search for '";
 	private String search_returned = "' returned ";
 	private String search_success = " result(s).";
 	private String search_noResults = "' did not return any results. Please try again.";
 	private String search_name = "Name";
 	@Override
 	public String getSearch_sResult() {
 		
 		return search_sResult;
 	}
 	@Override
 	public String getSearch_fResult() {
 		
 		return search_fResult;
 	}
 	@Override
 	public String getSearch_location() {
 		
 		return search_location;
 	}
 	@Override
 	public String getSearch_user() {
 		
 		return search_user;
 	}
 	@Override
 	public String getSearch_title() {
 		
 		return search_title;
 	}
 	@Override
 	public String getSearch_description() {
 		
 		return search_description;
 	}
 	@Override
 	public String getSearch_error() {
 		
 		return search_error;
 	}
 	@Override
 	public String getSearch_success() {
 		
 		return search_success;
 	}
 	@Override
 	public String getSearch_resultStart() {
 		
 		return search_resultStart;
 	}
 	@Override
 	public String getSearch_noResults() {
 		
 		return search_noResults;
 	}
 	@Override
 	public String getSearch_returned() {
 		
 		return search_returned;
 	}
 	@Override
 	public String getSearch_name() {
 		
 		return search_name;
 	}
 
 	// ============== Fields Strings ===================
 	private String fields_welcome = "This is an overview of all available fields on MentorFind sorted alphabetically";
 	private String fields_header = "Fields";
 	private String fields_find = "Find";
 	
 	public String getFields_findIn() {
 		return fields_find;
 	}
 	
 	@Override
 	public String getFields_header() {
 		return fields_header;
 	}
 	
 	@Override
 	public String getFields_welcome() {
 		return fields_welcome;
 	}
 
 	// ================ Profile Strings ===============
 	
 	private String profile_name = "Name";
 	private String profile_location = "Location";
 	private String profile_city = "City";
 	private String profile_country = "Country";
 	private String profile_edit = "Edit";
 	private String profile_save = "Save";
 	private String profile_email = "E-Mail";
 	
 	@Override
 	public String getProfile_name() {
 		return profile_name;
 	}
 	@Override
 	public String getProfile_location() {
 		return profile_location;
 	}
 	@Override
 	public String getProfile_city() {
 		return profile_city;
 	}
 	@Override
 	public String getProfile_country() {
 		return profile_country;
 	}
 	@Override
 	public String getProfile_save() {
 		return profile_save;
 	}
 	@Override
 	public String getProfile_edit() {
 		return profile_edit;
 	}
 	@Override
 	public String getProfile_email() {
 		return profile_email;
 	}
 	
 	// ========== Connections Strings ========
 	private String connections_noconnections = "You have no connections at this time.";
 	private String connections_add = "Add";
 	private String connections_remove = "Remove";
 	private String connections_you ="You";
 	private String connections_nomentor = "No mentor yet";
 	private String connections_notrainee = "No trainee yet";
 	private String connections_view = "View";
 	private String connections_contact = "Contact";
 	
 	@Override
 	public String getConnections_contact() {
 		// TODO Auto-generated method stub
 		return connections_contact;
 	}
 	
 	public String getConnections_view() {
 		return connections_view;
 	}
 	
 	@Override
 	public String getConnections_noConnections() {
 		return connections_noconnections;
 	}
 	
 	@Override
 	public String getConnections_add() {
 		return connections_add;
 	}
 	
 	@Override
 	public String getConnections_remove() {
 		return connections_remove;
 	}
 	@Override
 	public String getConnections_noMentor() {
 		return connections_nomentor;
 	}
 	@Override
 	public String getConnections_noTrainee() {
 		return connections_notrainee;
 	}
 	@Override
 	public String getConnections_you() {
 		return connections_you;
 	}
 	
 	// ============== Mentor/Trainee List Strings ===================
 		private String findList_trainee_legend = "List of Trainees";
 		private String findList_mentor_legend = "List of Mentors";
 		private String findList_no_mentor = "No mentors available";
 		private String findList_no_trainee = "No trainees available";
 		private String findList_connect = "Connect";
 		
 		@Override
 		public String getFindList_connect() {
 			// TODO Auto-generated method stub
 			return findList_connect;
 		}	
 		
 		public String getFindList_trainee_legend() {
 			return findList_trainee_legend;
 		}
 		public String getFindList_mentor_legend() {
 			return findList_mentor_legend;
 		}
 		public String getFindList_no_mentor() {
 			return findList_no_mentor;
 		}
 		public String getFindList_no_trainee() {
 			return findList_no_trainee;
 		}
 		
 		// ============== Request Strings ===================
 		private String request_accept = "Accept";
 		private String request_deny = "Deny";
 		private String request_trainee = "wants to be your trainee in";
 		private String request_mentor = "wants to be your mentor in";
 		private String request_legend = "You have request(s)";
 		private String request_dinied = "Request denied";
 		private String request_accepted = "Request accpected";
 		@Override
 		public String getRequest_accept() {
 			// TODO Auto-generated method stub
 			return request_accept;
 		}
 		@Override
 		public String getRequest_deny() {
 			// TODO Auto-generated method stub
 			return request_deny;
 		}
 		@Override
 		public String getRequest_traniee() {
 			// TODO Auto-generated method stub
 			return request_trainee;
 		}
 		@Override
 		public String getRequest_mentor() {
 			// TODO Auto-generated method stub
 			return request_mentor;
 		}
 		@Override
 		public String getRequest_legend() {
 			// TODO Auto-generated method stub
 			return request_legend;
 		}
 		@Override
 		public String getRequest_dinied() {
 			// TODO Auto-generated method stub
 			return request_dinied;
 		}
 		@Override
 		public String getRequest_accepted() {
 			// TODO Auto-generated method stub
 			return request_accepted;
 		}
 			
 }
