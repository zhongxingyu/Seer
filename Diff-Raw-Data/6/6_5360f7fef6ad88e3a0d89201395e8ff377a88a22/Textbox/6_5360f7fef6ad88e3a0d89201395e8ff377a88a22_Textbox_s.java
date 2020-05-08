 package ss.linearlogic.quizquest;
 
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 import org.lwjgl.input.Keyboard;
 import org.newdawn.slick.TrueTypeFont;
 
 public class Textbox {
 	//The question string along with the font
 	private static ArrayList<String>questionLines = new ArrayList<String>();
 	private static final int max_line_count = 4;
 	
 	private static TrueTypeFont font;
 	
 	//The position of the textbox
 	private static int x;
 	private static int y;
 	
 	//The size of the textbox
 	private static int width;
 	private static int height;
 	
 	//The flag holding whether this window is active or not
 	private static boolean active = true;
 	
 	//Holds the answers to the question
 	private static ArrayList<String> answers = new ArrayList<String>();
 	
 	//Holds the letters for the answers along with the current selection
 	private final static String letters = "ABCD";
 	private static int current_selection = 0;
 	
 	//The correct answer index
 	private static int correctIndex = -1;
 	
 	private static boolean answerCorrect = false;
 	
 	private static boolean keyLifted = true;
 	
 	public Textbox() {}
 	
 	//Initialize with using a custom font
 	public static void Initialize(String fontFile) {
 		font = Renderer.LoadFont(fontFile, 24);
 		
 		x = 0;
 		y = 380;
 		
 		width = 480;
 		height = 100;
 	}
 	
 	//Initialize without using a custom font
 	public static void InitializeWithSystemFont() {
 		font = Renderer.LoadSystemFont("SansSerif", 10);
 		
 		if (font == null) System.out.println("Font is null");
 		
 		x = 0;
 		y = 380;
 		
 		width = 480;
 		height = 100;
 	}
 	
 	//Add an answer to the array
 	public static void addAnswer(String answer) {
 		if (answers.size() >= 4)
 			return;
 		
 		answers.add(answer);
 	}
 	
 	//Remove an answer from the array (int)
 	public static void removeAnswer(int indx) {
 		answers.remove(indx);
 	}
 	
 	//Remove an answer from the array (string)
 	public static void removeAnswer(String ans) {
 		answers.remove(ans);
 	}
 	
 	//Set the correct answer index
 	public static void setCorrectIndex(int indx) {
 		correctIndex = indx;
 	}
 	
 	//Toggle whether the window is active or not
 	public static void toggleActive() {
 		active = !active;
 		
 		//Make the keys disable repeat if the window is active
 		Keyboard.enableRepeatEvents(!active);
 	}
 	
 	//Check if the window is active
 	public static boolean isActive() {
 		return active;
 	}
 	
 	//Checks the flag to see if the answer is correct
 	public static boolean isAnswerCorrect() {
 		return answerCorrect;
 	}
 	
 	//Set the current question
 	public static void setQuestion(String question) {
 		StringTokenizer tokenizer = new StringTokenizer(question, "\n");
 		int counter = 0;
 		
 		while (tokenizer.hasMoreTokens()) {
 			if (counter > max_line_count) 
 				break;
 			
 			questionLines.add(tokenizer.nextToken());
 			
 			counter++;
 		}
 	}
 	
 	//Get the current selection of the cursor
 	public static int getCurrentSelection() {
 		return current_selection;
 	}
 	
 	//Set the current selection of the cursor
 	public static void setCurrentSelection(int sel) {
 		current_selection = sel;
 	}
 	
 	public static void Update() {
 		if (Keyboard.areRepeatEventsEnabled()) Keyboard.enableRepeatEvents(false);
 		
		//TODO: fix the bug in the code which is making it so key repeats are still enabled
 		if (active && Keyboard.isKeyDown(Keyboard.KEY_LEFT) && keyLifted) { current_selection -= 1; keyLifted = false; }
 		if (active && Keyboard.isKeyDown(Keyboard.KEY_RIGHT) && keyLifted) { current_selection += 1; keyLifted = false; }
 		
		if (!keyLifted && !Keyboard.isKeyDown(Keyboard.KEY_LEFT)) keyLifted = true;
		if (!keyLifted && !Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) keyLifted = true;
 		
 		//Use the numbers to correspond for answers
 		if (active && Keyboard.isKeyDown(Keyboard.KEY_1)) current_selection = 0;
 		if (active && Keyboard.isKeyDown(Keyboard.KEY_2)) current_selection = 1;
 		if (active && Keyboard.isKeyDown(Keyboard.KEY_3)) current_selection = 2;
 		if (active && Keyboard.isKeyDown(Keyboard.KEY_4)) current_selection = 3;
 		
 		//Use the letters to correspond for answers
 		if (active && Keyboard.isKeyDown(Keyboard.KEY_A)) current_selection = 0;
 		if (active && Keyboard.isKeyDown(Keyboard.KEY_B)) current_selection = 1;
 		if (active && Keyboard.isKeyDown(Keyboard.KEY_C)) current_selection = 2;
 		if (active && Keyboard.isKeyDown(Keyboard.KEY_D)) current_selection = 3;
 		
 		//When user pressed the enter key, checks to see if the user is correct
 		if (active && Keyboard.isKeyDown(Keyboard.KEY_RETURN)) if (checkIfCorrect()) answerCorrect = true;
 				
 		//Provide wrap around
 		if (current_selection > 3) current_selection = 0;
 		if (current_selection < 0) current_selection = 3;
 	}
 	
 	//Checks to see if the current selection is equal to the correct index
 	private static boolean checkIfCorrect() {
 		return current_selection == correctIndex;
 	}
 	
 	//Resets all the variables back to the original state
 	public static void reset() {
 		current_selection = 0;
 		correctIndex = -1;
 		answerCorrect = false;
 		
 		active = false;
 		answers.clear();
 		questionLines.clear();
 	}
 	
 	//Render the text box
 	public static void render() {
 		if (!active) return;
 			
 		//Render the rectangle with the question
 		Renderer.RenderColoredRectangle(x, y, width, height, 0.0f, 0.0f, 0.7f);		
 		
 		for (int i = 0; i < questionLines.size(); ++i) {
 			Renderer.RenderString(questionLines.get(i), x + 10, y + (font.getHeight() * i), font);
 		}
 				
 		//Render the selection rectangle
 		Renderer.RenderTransparentRectangle((20 + (current_selection * 100)) - 5, 445, (font.getWidth(letters.charAt(current_selection) + ": " + answers.get(current_selection))) + 10, font.getHeight() + 10);
 		
 		//Render all of the answers
 		for (int i = 0; i < 4; ++i) {
 			Renderer.RenderString(letters.charAt(i) + ": " + answers.get(i), 20 + (i * 100), 450, font);
 		}
 	}
 }
