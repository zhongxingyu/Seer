 package game;
 
 import java.util.ArrayList;
 
 public class ChoiceStorage {
 	private int currentChoice=-1;
 	private ArrayList<Choice> choices = new ArrayList<Choice>();
 
 	/**
 	 * Builds a new instance of ChoiceStorages
 	 */
 	public ChoiceStorage(Person p) {
 		
 		
 		/////////// Constructs the Prompts ///////////
 		
 		final String pt1 = "Welcome to your first day of highschool! Today you get to decide how you want to act.\n1: Become a sports jock.\n2: Hang with the popular crowd.\n3: Study for your classes.\n4: Do nothing";
 		final String pt2 = "You get invited to a party. Everyone seems to be having fun. Drugs and alcohol are present.\n1: Get drunk and go crazy\n2: Drive your drunk friend home\n3: Socialize\n4: Stare at your phone";
 		final String pt3 = "It is the day before finals. The year is almost over and summer is within your reach. Do you: \n1: Spend your time studying for your tests.\n2: Hang out with your friends and cram the morning of the test.\n3: Don't study and try to cheat off of someone else.\n4: Just kinda chill.";
 		final String pt4 = "It is senior year and about time you made up your mind about College. Let's apply! \n1: You apply to the best engineering schools in the country. \n2: You apply to schools with Division A Lax teams. \n3: You apply to law school. \n4: You decide maybe college isn't your thing.";
		final String pt5 = "It's time to find a full-time job. n\1: Get a job. n\2: Go back to live at your parents' house.";
 		
 		/////////// Constructs the outcome string arrays ///////////
 		
 		final String[] sl1 = {
 			"You get recruited by the Lax team because of your sick Lax skills. Lax.",
 			"You meet some new friends. Everyone seems to want to sit next to you at lunch.", 
 			"You hit the books. Your grades stay steady and the teachers seem to enjoy your participation in class.",
 			"You just kinda coast along. Nobody seems to know a whole lot about you.",
 			null,
 			null,
 			null,
 			null,
 			null,
 			null,
 			"Error 808: you suck.",
 			"Error 808: you suck.",
 			"Error 808: you suck.",
 			"Error 808: you suck."
 		};
 		
 		final String[] sl2 = {
 			"You wake up on a park bench in the next town over wearing a traffic cone as hat.",
 			"The next day, your friend thanks you profusely for your wise actions of the previous night.", 
 			"You meet a girl named Maria and hit it off.",
 			"You stay in the corner and keep to yourself. No one seems to notice your presence.",
 			null,
 			null,
 			null,
 			null,
 			null,
 			null,
 			"Error 808: you suck.",
 			"Error 808: you suck.",
 			"Error 808: you suck.",
 			"Error 808: you suck."
 		};
 		
 		final String[] sl3 = {
 			"After hours of studying you feel ready for your tests. You do well on all of them.",
 			"You don't do awesome on your tests, but you feel as though your friends appreciate your carefree style.", 
 			"You do well on your tests: maybe a little too well. Nobody has proof of anything, but you lose the trust of those around you.",
 			"You do pretty average, although your not even sure your teachers know your name.",
 			null,
 			null,
 			null,
 			null,
 			null,
 			null,
 			"Error 808: you suck.",
 			"Error 808: you suck.",
 			"Error 808: you suck.",
 			"Error 808: you suck."
 		};
 		
 		final String[] sl4 = {
 			"You make it in to Engineering U. Congratulations!",
 			"You make it in to Lax U. Congratulations!", 
 			"You make it in to Lawyer U. Congratulations!",
 			"You take a job at McDoodles to make an income. Congratulations?",
 			null,
 			null,
 			null,
 			null,
 			null,
 			null,
 			"Error 808: you suck.",
 			"Error 808: you suck.",
 			"Error 808: you suck.",
 			"Error 808: you suck."
 		};
 		
 		final String[] sl5 = {
 			"Welcome to adulthood.",
 			"Your parents aren't very happy about this.",
 			null,
 			null,
 			null,
 			null,
 			null,
 			null,
 			null,
 			null,
 			"Error 808: you suck.",
 			"Error 808: you suck."
 		};
 		
 		/////////// Constructs outcomes and outcome arrays ///////////
 		
 		final Outcome o1 = new Outcome(true, 2 , -1 , 5, 0, 5, 0);
 		final Outcome o2 = new Outcome(true, 5 , -1 , 0, 2, 5, 0);
 		final Outcome o3 = new Outcome(true, -1 , 5 , 0, 5, 2, 0);
 		final Outcome o4 = new Outcome(true, 2 , 2 , 2, 2, 2, 0); 
 		
 		final Outcome o1a = new Outcome(true, -1 , -1 , -1, -1, -1, 0);
 		final Outcome o2a = new Outcome(true, -1 , -1 , -1, -1, -1, 0);
 		final Outcome o3a = new Outcome(true, -1 , -1 , -1, -1, -1, 0);
 		final Outcome o4a = new Outcome(true, 0 , 0 , 0, 0, 0, 0); 
 		
 		final Outcome[] ol1 = {
 			o1,
 			o2,
 			o3,
 			o4,
 			null,
 			null,
 			null,
 			null,
 			null,
 			null,
 			o1a,
 			o2a,
 			o3a,
 			o4a
 			};
 		
 		final Outcome o5 = new Outcome(true, 7 , -4 , 4, -2, 7, 0);
 		final Outcome o6 = new Outcome(true, 3 , 5 , 0, 0, 4, 0);
 		final Outcome o7 = new Outcome(true, 6 , 0 , 0, -1, 7, 0);
 		final Outcome o8 = new Outcome(true, 2 , 2 , 2, 2, 2, 0);
 		
 		final Outcome o5a = new Outcome(true, -1 , -1 , -1, -1, -1, 0);
 		final Outcome o6a = new Outcome(true, -1 , -1 , -1, -1, -1, 0);
 		final Outcome o7a = new Outcome(true, -1 , -1 , -1, -1, -1, 0);
 		final Outcome o8a = new Outcome(true, 0 , 0 , 0, 0, 0, 0);
 		
 		final Outcome[] ol2 = {
 			o5,
 			o6,
 			o7,
 			o8,
 			null,
 			null,
 			null,
 			null,
 			null,
 			null,
 			o5a,
 			o6a,
 			o7a,
 			o8a
 		};
 		
 		final Outcome o9 = new Outcome(true, 0 , 6 , 0, 3, 3, 0);
 		final Outcome o10 = new Outcome(true, 6 , 0 , 3, 0, 3, 0);
 		final Outcome o11 = new Outcome(true, -2 , 8 , 0, -2, 8, 0);
 		final Outcome o12 = new Outcome(true, 2 , 2 , 2, 2, 2, 0);
 		
 		final Outcome o9a = new Outcome(true, -1 , -1 , -1, -1, -1, 0);
 		final Outcome o10a = new Outcome(true, -1 , -1 , -1, -1, -1, 0);
 		final Outcome o11a = new Outcome(true, -1 , -1 , -1, -1, -1, 0);
 		final Outcome o12a = new Outcome(true, 0 , 0 , 0, 0, 0, 0);
 		
 		final Outcome[] ol3 = {
 			o9,
 			o10,
 			o11,
 			o12,
 			null,
 			null,
 			null,
 			null,
 			null,
 			null,
 			o9a,
 			o10a,
 			o11a,
 			o12a
 		};
 		
 		final Outcome o13 = new Outcome(true, 2 , -1 , 5, 0, 5, 0);
 		final Outcome o14 = new Outcome(true, 5 , -1 , 0, 2, 5, 0);
 		final Outcome o15 = new Outcome(true, -1 , 5 , 0, 5, 2, 0);
 		final Outcome o16 = new Outcome(true, 2 , 2 , 2, 2, 2, 0); 
 		
 		final Outcome o13a = new Outcome(true, -1 , -1 , -1, -1, -1, 0);
 		final Outcome o14a = new Outcome(true, -1 , -1 , -1, -1, -1, 0);
 		final Outcome o15a = new Outcome(true, -1 , -1 , -1, -1, -1, 0);
 		final Outcome o16a = new Outcome(true, 0 , 0 , 0, 0, 0, 0);
 		
 		final Outcome[] ol4 = {
 			o13,
 			o14,
 			o15,
 			o16,
 			null,
 			null,
 			null,
 			null,
 			null,
 			null,
 			o13a,
 			o14a,
 			o15a,
 			o16a
 		};
 		
 		final Outcome o17 = new Outcome(true, 2 , -1 , 5, 0, 5, 1);
 		final Outcome o18 = new Outcome(true, 5 , -1 , 0, 2, 5, 1);
 		
 		final Outcome o17a = new Outcome(true, -1 , -1 , -1, -1, -1, 0);
 		final Outcome o18a = new Outcome(true, -1 , -1 , -1, -1, -1, 0);
 		
 		final Outcome[] ol5 = {
 			o17,
 			o18,
 			null,
 			null,
 			null,
 			null,
 			null,
 			null,
 			null,
 			null,
 			o17a,
 			o18a
 		};
 		
 		/////////// Constructs requirements and requirement arrays ///////////
 		
 		final Requirements r1 = new Requirements(2, 0, 2, 0, 2);
 		final Requirements r2 = new Requirements(2, 0, 0, 2, 2);
 		final Requirements r3 = new Requirements(1, 3, 0, 1, 1);
 		final Requirements r4 = new Requirements(0, 0, 0, 0, 0);
 		
 		final Requirements[] ra1 = {
 			r1,
 			r2,
 			r3,
 			r4
 		};
 		
 		final Requirements r5 = new Requirements(6, 0, 0, 0, 6);
 		final Requirements r6 = new Requirements(2, 6, 2, 0, 2);
 		final Requirements r7 = new Requirements(6, 0, 0, 0, 6);
 		final Requirements r8 = new Requirements(0, 0, 0, 0, 0);
 		
 		final Requirements[] ra2 = {
 			r5,
 			r6,
 			r7,
 			r8
 		};
 		
 		final Requirements r9 = new Requirements(0, 3, 0, 0, 0);
 		final Requirements r10 = new Requirements(1, 0, 0, 1, 1);
 		final Requirements r11 = new Requirements(0, 3, 0, 0, 3);
 		final Requirements r12 = new Requirements(0, 0, 0, 0, 0);
 		
 		final Requirements[] ra3 = {
 			r9,
 			r10,
 			r11,
 			r12
 		};
 		
 		final Requirements r13 = new Requirements(0, 10, 0, 5, 5);
 		final Requirements r14 = new Requirements(0, 0, 10, 5, 5);
 		final Requirements r15 = new Requirements(10, 0, 0, 5, 5);
 		final Requirements r16 = new Requirements(0, 0, 0, 0, 0);
 		
 		final Requirements[] ra4 = {
 			r13,
 			r14,
 			r15,
 			r16
 		};
 		
 		final Requirements r17 = new Requirements(2, 2, 2, 2, 2);
 		final Requirements r18 = new Requirements(0, 0, 0, 0, 0);
 
 		
 		final Requirements[] ra5 = {
 			r17,
 			r18,
 		};
 		
 		/////////// Constructs the choices ///////////
 		
 		Choice c1 = new Choice(pt1, sl1, ra1, ol1, p, p.getAge(), 0, 0, 0, 0, 0);
 		Choice c2 = new Choice(pt2, sl2, ra2, ol2, p, p.getAge(), 0, 0, 0, 0, 0);
 		Choice c3 = new Choice(pt3, sl3, ra3, ol3, p, p.getAge(), 0, 0, 0, 0, 0);
 		Choice c4 = new Choice(pt4, sl4, ra4, ol4, p, p.getAge(), 0, 0, 0, 0, 0);
 		Choice c5 = new Choice(pt5, sl5, ra5, ol5, p, p.getAge(), 0, 0, 0, 0, 0);
 		
 		
 		final String[] A1Story = {
 				"You become a McDoodles worker",
 				"You become a mechanic",
 				"You become a sports star",
 				"You become an engineer",
 				"You become a CEO",
 				"You become a politician",
 				"DO NOT READ! in three days you will be kissed by the love of your life, but if you do not repost this to 5 other videos, you will be murdered in two days by a pack of wild puffins!",
 				null,
 				null,
 				null,
 				"You fail to become a McDoodles worker. You may wish to reevaluate your life.",
 				"You fail to become a mechanic. Perhaps a lower-skill job would be better for you.",
 				"You fail to become a sports star. Try to get stronger.",
 				"You fail to become an engineer. You should work at McDoodles instead.",
 				"You fail to become a CEO.",
 				"You fail to become a politician. You're not good enough at wooing voters."
 		};
 		
 		final Outcome[] A1Outcomes = {
 			new Outcome(true, 0, 0, 0, 5, 1, 0),
 			new Outcome(true, 0, 1, 1, 10, 3, 0),
 			new Outcome(true, 5, 0, 10, 20, 10, 0),
 			new Outcome(true, 0, 15, 0, 15, 10, 0),
 			new Outcome(true, 5, 10, 0, 25, 7, 0),
 			new Outcome(true, 10, 7, 0, 15, 10, 0),
 			null,
 			null,
 			null,
 			null,
 			null,
 			new Outcome(true, 0, 0, 0, 0, -20, 0),
 			new Outcome(true, 0, 0, 0, 0, -10, 0),
 			new Outcome(true, 0, 0, 0, 0, -10, 0),
 			new Outcome(true, 0, 0, 0, 0, -10, 0),
 			new Outcome(true, 0, 0, 0, 0, -10, 0),
 			new Outcome(true, 0, 0, 0, 0, -10, 0)
 			
 		};
 		
 		Requirements[] A1reqs = {
 				new Requirements(1,1,1,1,1),
 				new Requirements(3,20,15,5,5),
 				new Requirements(20,5,30,20,10),
 				new Requirements(10,30,5,15,10),
 				new Requirements(30,20,5,10,10),
 				new Requirements(20,30,5,20,15),
 				new Requirements(0,0,0,0,0)
 		};
 		
 		
 		
 		Choice choiceA1=new Choice("You decide to get a job. The choices are 'McDoodles worker', 'Mechanic', 'Sports Star', 'Engineer', 'CEO', or 'Politician'", 
 								A1Story, A1reqs, A1Outcomes, p, 2, 0, 0, 0, 0, 0);
 		
 		
 		final String A2printText = "You are bored at home. You decide to do something. You can read a book, work out, or go to a party";
 		
 		final String[] A2Story = {
 				"You decide to read a book. You are now more intelligent",
 				"You decide to work out. You are now stronger",
 				"You decide to go to a party. Charisma goes up",
 				null,
 				null,
 				null,
 				null,
 				null,
 				null,
 				null,
 				"You managed to fail at reading. Are you even literate?",
 				"You failed to work out. frynotsureifweakorstupid.jpg",
 				"You failed to go to a party. I've given up hope."
 		};
 		
 		final Requirements[] A2Reqs = {
 				new Requirements(0, 0, 0, 0, 0),
 				new Requirements(0, 0, 0, 0, 0),
 				new Requirements(0, 0, 0, 0, 0)
 		};
 		
 		
 		final Outcome[] A2Outcomes = {
 				new Outcome(true, 0, 10, 0, 0, 0, 0),
 				new Outcome(true, 0, 0, 10, 0, 0, 0),
 				new Outcome(true, 10, 0, 0, 0, 0, 0),
 				null,
 				null,
 				null,
 				null,
 				null,
 				null,
 				null,
 				new Outcome(true, -5, -5, -5, -5, -5, 0),
 				new Outcome(true, -5, -5, -5, -5, -5, 0),
 				new Outcome(true, -5, -5, -5, -5, -5, 0)
 		};
 		
 		Choice choiceA2=new Choice(A2printText, A2Story, A2Reqs, A2Outcomes, p, 2, 0, 0, 0, 0, 0);
 		
 		final String A3printText="You are very lonely. Would you like to try to get married?";
 		
 		final String[] A3Story= {
 				"Congratulations! You managed to convince someone to spend their entire life with you! You are now married.",
 				"You decided that married life is not for you.",
 				"If this text is displayed, something is horribly wrong",
 				"Seriously, if you can see this, they're coming",
 				"I'm not kidding. You'd better run",
 				"Seriously, RUN!",
 				"Well clearly you're not listening to me",
 				"I'm going to sit tight while they eat you",
 				"and I'm not going to feel any regret",
 				"Well, I guess this is a lost cause, bye",
 				"You failed to convince somebody to marry you. You must be ugly, poor, or both",
 				"How did you possibly fail at not getting married?! I'm ashamed of you."
 				
 		};
 		final Requirements[] A3reqs = {
 			new Requirements(30, 20, 15, 20, 25),
 			new Requirements(0, 0, 0, 0, 0),
 			new Requirements(0,0,0,0,0),
 			new Requirements(0,0,0,0,0),
 			new Requirements(0,0,0,0,0),
 			new Requirements(0,0,0,0,0),
 			new Requirements(0,0,0,0,0),
 			new Requirements(0,0,0,0,0),
 			new Requirements(0,0,0,0,0),
 			new Requirements(0,0,0,0,0),
 		};
 		
 		final Outcome[] A3Outcomes = {
 			new Outcome(true, 10, 0, 0, -10, 10, 0),
 			new Outcome(true, 0, 0, 0, 0, 0, 0),
 			null,
 			null,
 			null,
 			null,
 			null,
 			null,
 			null,
 			null,
 			new Outcome(true, -5, 0, 0, 0, -20, 0),
 			new Outcome(true, -10, -10, -10, -10, -40, 0),
 		};
 		Choice choiceA3=new Choice(A3printText, A3Story, A3reqs, A3Outcomes, p, 3,0,0,0,0,0);
 		
 		final String A4printText = "You really hate people. You're so angry you consider becoming a serial killer. Would you like to become a serial killer?";
 		
 		final String[] A4Story = {
 				"You succeed at becoming a serial killer, you monster",
 				"I suppose you don't have the killer instinct",
 				null,
 				null,
 				null,
 				null,
 				null,
 				null,
 				null,
 				null,
 				"At the house of your first hit, you are discovered, and the police are called. You are shot by the police and die.",
 				"I'm not sure how, but you managed to fail at not becoming a serial killer. I'm going to kill you because of your sheer incompetence. Self-control isn't even an attribute in this game!"
 		};
 		
 		final Outcome[] A4Outcomes = {
 				new Outcome(true, 5, 10, 10, -5, 10, 0),
 				new Outcome(true, 0, 0, 0, 0, 0, 0),
 				null,
 				null,
 				null,
 				null,
 				null,
 				null,
 				null,
 				null,
 				new Outcome(false, -100, -100, -100, -100, -100, 0),
 				new Outcome(false, -1000, -1000, -1000, -1000, -1000, 0)
 				
 		};
 		
 		final Requirements[] A4reqs = {
 				new Requirements(40, 30, 20, 20, 0),
 				new Requirements(0, 0, 0, 0, 0)
 		};
 		
 		Choice choiceA4=new Choice(A4printText, A4Story, A4reqs, A4Outcomes, p, 3, 30, 0, 0, 0, 0);
 		
 		final String A5PrintText = "Kids?";
 		
 		final String[] A5Story = {
 				"You've had a baby! prepare for the next 18 years well",
 				"Maybe? Well, I suppose I'll choose for you and give you a baby, you seem qualified enough. Your reluctance will not, however, go unpunished",
 				"I suppose having a kid isn't for everybody",
 				null,
 				null,
 				null,
 				null,
 				null,
 				null,
 				null,
 				"You didn't manage to have a baby. You aren't very good at this, are you?",
 				"Maybe? You seem horribly unqualified to have a kid, so I'll spare the baby and not let you have one",
 				"You failed (somehow) at not having a kid, so you'll get one anyway"
 		};
 		
 		final Requirements[] A5Reqs = {
 				new Requirements(5, 0, 5, 0, 0),
 				new Requirements(5, 15, 10, 20, 0),
 				new Requirements(0, 0, 0, 0, 0)
 		};
 		
 		final Outcome[] A5Outcomes = {
 				new Outcome(true, 5, 5, 5, -20, 5, 1),
 				new Outcome(true, 5, 5, 5, -30, -10, 1),
 				new Outcome(true, 0, 0, 0, 0, 0, 1),
 				null,
 				null,
 				null,
 				null,
 				null,
 				null,
 				null,
 				new Outcome(true, 0, 0, 0, -5, -15, 1),
 				new Outcome(true, 0, 0, 0, 0, 0, 1),
 				new Outcome(true, -5, -5, -5, -40, -5, 1)
 		};
 		
 		Choice choiceA5=new Choice(A5PrintText, A5Story, A5Reqs, A5Outcomes, p, 3, 0, 0, 0, 0, 0);
 		
 		choices.add(c1);
 		choices.add(c2);
 		choices.add(c3);
 		choices.add(c4);
 		choices.add(c5);
 		choices.add(choiceA1);
 		choices.add(choiceA2);
 		choices.add(choiceA3);
 		choices.add(choiceA4);
 		choices.add(choiceA5);
 		
 	}
 	
 	/**
 	 * returns a boolean that is true if the person's attributes satisfy all
 	 * of the requirements for the choice c 
 	 * @param p
 	 * @param c
 	 * @return whether or not the person p is qualified for choice c
 	 */
 	private boolean isQualified(Person p, Choice c){
 
 		 if(!(p.getCharisma()>=c.getCharismaReq())){
 			return false;
 		}
 		else if(!(p.getConfidence()>=c.getConfindenceReq())){
 			return false;
 		}
 		else if(!(p.getIntelligence()>=c.getIntelligenceReq())){
 			return false;
 		}
 		else if(!(p.getStrength()>=c.getStrengthReq())){
 			return false;
 		}
 		else if(!(p.getWealth()>=c.getWealthReq())){
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * @param p
 	 *            A person object
 	 * 
 	 * @return A choice that the person is qualified for
 	 */
 	/**
 	 * @param p
 	 * @return
 	 */
 	public Choice getNextChoice(Person p) {
 		Choice selectedChoice=choices.get(currentChoice+1);
 		currentChoice++;
 		boolean qualified=false;
 
 		while(!qualified){
 			if(this.isQualified(p, selectedChoice)){
 				qualified=true;
 			}
 			else{
 				selectedChoice=choices.get(currentChoice+1);
 			}
 		}
 		return selectedChoice;
 	}
 
 }
