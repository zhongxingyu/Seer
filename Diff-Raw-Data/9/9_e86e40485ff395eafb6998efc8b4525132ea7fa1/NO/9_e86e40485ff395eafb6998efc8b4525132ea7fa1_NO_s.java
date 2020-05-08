 package language;
 
 public class NO extends Lang{
 	
 	// ============== SignInPage Strings ===================
 	private String login_btn_lang = "Sprk";
 	private String login_btn_learn = "Lr mer";
 	private String login_signIn = "Logg inn med:";
 	private String login_moto = "Finn mentorer og trainees med de rette kvalifikasjonene og del kunnskap, alt p en plass.";
 									
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
 	private String firsttime_label_fName = "Fornavn";
 	private String firsttime_label_lName = "Etternavn:";
 	private String firsttime_label_email = "Epost";
 	private String firsttime_label_country = "Land";
 	private String firsttime_label_city = "By";
 	private String firsttime_label_connections = "Koblinger";
 	private String firsttime_label_subject = "Emne";
 	private String firsttime_label_field = "Felt";
 	private String firsttime_radio_mentor = "Mentor";
 	private String firsttime_radio_trainee = "Trainee";
	private String firsttime_label_addInfo = "Ekstra info";
 	private String firsttime_label_experience = "Erfaring";
 	private String firsttime_drop_novice = "Nybegynner";
 	private String firsttime_drop_intermediate = "Mellomniv";
 	private String firsttime_drop_expert = "Ekspert";
 	private String firsttime_drop_loreMaster = "Guru";
 	private String firsttime_btn_add = "Legg til";
 	private String firsttime_btn_submit = "Send";
 	
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
 		private String home_welcome = "Velkommen til MentorFind";
 		private String home_please_search = "Skriv i skefeltet for  starte";
 		private String connections_welcome = "Koblinger";
 		
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
 			private String menu_home = "Hjem";
 			private String menu_fields = "Felt";
 			private String menu_profile = "Profil";
 			private String menu_connections = "Koblinger";
 			private String menu_search = "Sk";
 			private String menu_logout = "Logg ut";
 			
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
 		private String search_sResult = "Skeresultater";
 		private String search_fResult = "Filtrer resultater";
 		private String search_location = "Lokasjon";
 		private String search_user = "Brukere";
 		private String search_title = "Tittel";
 		private String search_description = "Beskrivelse";
 		private String search_error = "Du skrev ikke noe i skefeltet, prv igjen.";
 		private String search_resultStart = "Ditt sk for '";
 		private String search_returned = "' returnerte ";
 		private String search_success = " resultat(er).";
 		private String search_noResults = "' gav ingen resultater, prv igjen.";
 		private String search_name = "Navn";
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
 		private String fields_welcome = "Her er en oversikt over alle feltene i MentorFind sortert alfabetisk";
 		private String fields_header = "Felt";
 		private String fields_find = "Finn";
 		
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
 		
 		private String profile_name = "Navn";
 		private String profile_location = "Lokasjon";
 		private String profile_city = "By";
 		private String profile_country = "Land";
 		private String profile_edit = "Redigr";
 		private String profile_save = "Lagre";
 		private String profile_email = "E-Post";
 		
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
 		private String connections_noconnections = "Du har ingen koblinger for yeblikket.";
 		private String connections_add = "Legg til";
 		private String connections_remove = "Fjern";
 		private String connections_you ="Deg";
 		private String connections_nomentor = "Ingen mentor";
 		private String connections_notrainee = "Ingen trainee";
 		private String connections_view = "Vis";
 		private String connections_contact = "Kontakt";
 		
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
 				private String findList_trainee_legend = "Liste av Traineer";
 				private String findList_mentor_legend = "Liste av Mentorer";
 				private String findList_no_mentor = "Ingen mentorer tilgjengelig";
 				private String findList_no_trainee = "Ingen traineer tilgjengelig";
 				private String findList_connect = "Koble til";
 				
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
 				private String request_accept = "Godta";
 				private String request_deny = "Avsl";
 				private String request_trainee = "vil vre din trainee i";
 				private String request_mentor = "vil vre din mentor i";
 				private String request_legend = "Du har en eller flere foresprsler";
 				private String request_dinied = "Foresprsel avsltt";
 				private String request_accepted = "Foresprsel godtatt";
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
