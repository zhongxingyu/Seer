 package mhyhre.lightrabbit.Scenes;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import mhyhre.lightrabbit.GameState;
 import mhyhre.lightrabbit.MainActivity;
 import mhyhre.lightrabbit.MhyhreScene;
 
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.sprite.batch.SpriteBatch;
 import org.andengine.entity.text.Text;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.font.Font;
 import org.andengine.opengl.texture.region.ITextureRegion;
 
 import android.util.Log;
 
 public class SceneGameMemorize extends MhyhreScene {
 	
 	
 	
 	class WordPair{
 		public String word1, word2;
 		
 		public WordPair(String w1, String w2) {
 			this.word1 = new String(w1);
 			this.word2 = new String(w2);
 		}
 	}
 	
 	public String itemFontName = "Furore";
 
 	SceneGame sceneGame;
 	
 	private int maxItemCount;
 	private int currentItemCount;
 	private int errorsCount = 0;
 	private int errorMaxCount = 2;
 	private int selectByProgramm;
 	
 	ArrayList<WordItem> wordItems;
 	ArrayList<Text> labels;
 	ArrayList<WordPair> wordPairs;
 	SpriteBatch UIBatch;
 	Font itemFont;
 
 	final float borderSize = 100;
 	final float heightStep = 80;
 	final float centerOffset = 180;
 
 	public SceneGameMemorize(final SceneGame sceneGame, int maxItemCount) {
 
 		this.sceneGame = sceneGame;
 		
 		Background background = new Background(0.78f, 0.78f, 0.80f);
 		setBackgroundEnabled(true);
 		setBackground(background);
 
 		this.maxItemCount = maxItemCount;
 
 		// UI batch setup
 		UIBatch = new SpriteBatch(MainActivity.Res.getTextureAtlas(SceneGame.uiAtlasName), maxItemCount * 2, MainActivity.Me.getVertexBufferObjectManager());
 		attachChild(UIBatch);
 
 		// Text setup
 		itemFont = MainActivity.Res.getFont(itemFontName);
 		wordItems = new ArrayList<WordItem>(maxItemCount);
 		labels = new ArrayList<Text>(maxItemCount);
 		wordPairs = new ArrayList<WordPair>(maxItemCount/2);
 
 		// Labels setup
 		for (int i = 0; i < maxItemCount; i++) {
 			labels.add(new Text(0, 0, itemFont, "", 50, MainActivity.Me.getVertexBufferObjectManager()));
 			attachChild(labels.get(i));
 		}
 	}
 
 	public void setWordsList(final ArrayList<String> inWords) {
 
 		selectByProgramm = 0;
 		
 		if (inWords.size() > maxItemCount || inWords.size()%2 == 1) {
 			Log.e(MainActivity.DebugID, "Scene::setWordsList: strange list size");
 			return;
 		}
 
 		currentItemCount = inWords.size();
 
 		wordItems.clear();
 		wordPairs.clear();
 		
 		for (int i = 0; i < currentItemCount; i++) {
 			wordItems.add(new WordItem(new String(inWords.get(i))));
 		}
 		
 		for (int i = 0; i < currentItemCount; i+=2) {
 			wordPairs.add(new WordPair(inWords.get(i), inWords.get(i+1)));
 			Log.i(MainActivity.DebugID, "#### " + inWords.get(i) +" - "+ inWords.get(i+1));
 		}
 	}
 
 	public void update() {
 
 		ITextureRegion itemRegion = SceneGame.TextureRegions.get(0);
 		final float leftCenterOffset = MainActivity.getHalfWidth() - centerOffset;
 		final float rightCenterOffset = MainActivity.getHalfWidth() + centerOffset;
 		
 		float colorDelta = 0.01f;
 		
 		for (int i = 0; i < currentItemCount; i += 2) {
 			
 			// Left column
 			WordItem leftItem = wordItems.get(i);
 			leftItem.setX(leftCenterOffset);
 			leftItem.setY(borderSize + (i / 2) * heightStep);
 			leftItem.setWidth(itemRegion.getWidth());
 			leftItem.setHeight(itemRegion.getHeight());
 			
 			
 			// Right column
 			WordItem rightItem = wordItems.get(i + 1);
 			rightItem.setX(rightCenterOffset);
 			rightItem.setY(borderSize + (i / 2) * heightStep);
 			rightItem.setWidth(itemRegion.getWidth());
 			rightItem.setHeight(itemRegion.getHeight());
 			
 			
 			
 			
 			if(SceneGame.getMode() == GameState.Recollect){
 				if(selectByProgramm == i){
 					leftItem.setColor(0.8f, 0.8f, 1.0f);
 				} else {
 					leftItem.setColor(0.8f, 0.8f, 0.8f);
 				}
 				
 				if(rightItem.isEnabled() == true){
 
 					rightItem.desireColor(colorDelta, 1, 1, 1);
 				} else {
 					rightItem.desireColor(colorDelta, 0.8f, 0.8f, 0.8f);
 				}
 			} else {
 				leftItem.setColor(1.0f, 1.0f, 1.0f);
 				rightItem.setColor(1.0f, 1.0f, 1.0f);
 			}
 		}
 		
 		updateLabels();
 		updateUIBatch();
 	}
 
 	private void updateLabels() {
 
 		final float textBorderSize = itemFont.getLineHeight() / 2;
 
 		for (int i = 0; i < currentItemCount; i++) {
 
 			WordItem item = wordItems.get(i);
 			Text text = labels.get(i);
 			
 			text.setText(item.getWord());
 			text.setPosition(item.getX() - text.getWidth()/2, item.getY() - textBorderSize);
 			text.setVisible(true);
 		}
 
 		// Hide not used labels
 		for (int i = currentItemCount; i < maxItemCount; i++) {
 			labels.get(i).setVisible(false);
 		}
 	}
 
 	private void updateUIBatch() {
 
 		ITextureRegion itemTextureRegion = SceneGame.TextureRegions.get(0);
 
 		for (int i = 0; i < currentItemCount; i++) {
 			WordItem item = wordItems.get(i);
 			UIBatch.draw(itemTextureRegion, item.getRectX(), item.getRectY(), item.getWidth(), item.getHeight(), 0, item.getRed(), item.getGreen(), item.getBlue(), item.getAlpha());
 		}
 		
 		if(SceneGame.getMode() == GameState.Memorize){
 			
 			ITextureRegion equallRegion = SceneGame.TextureRegions.get(1);
 			int halfItemCount = currentItemCount/2;
 			float xPosition = MainActivity.getHalfWidth() - equallRegion.getWidth()/2;
 			float mBorder = borderSize - equallRegion.getHeight()/2;
 			
 			for (int i = 0; i < halfItemCount; i++) {
 				UIBatch.draw(equallRegion, xPosition, mBorder + i * heightStep, equallRegion.getWidth(), equallRegion.getHeight(), 0, 1, 1, 1, 1);
 			}
 		}
 		
 		UIBatch.submit();
 	}
 
 	boolean isTouchDown = false;
 	boolean isSlidingMove = false;
 	TouchEvent oldTouchEvent = null;
 	float lastY = 0;
 
 	@Override
 	public boolean onSceneTouchEvent(final TouchEvent event) {
 
 		float sensitivity = 10.0f;
 
 		if (event.isActionUp()) {
 			isTouchDown = false;
 			lastY = event.getY();
 
 			if (oldTouchEvent != null && isSlidingMove == false) {
 				clickEvent(oldTouchEvent.getX()-getX(),oldTouchEvent.getY()-getY());
 			}
 		}
 
 		if (event.isActionMove()) {
 			if (oldTouchEvent != null) {
 
 				// Check slide move
 				float offsetX = event.getX() - oldTouchEvent.getX();
 				float offsetY = event.getY() - oldTouchEvent.getY();
 
 				if (offsetX > sensitivity || offsetX < -sensitivity) {
 					isSlidingMove = true;
 				}
 
 				if (offsetY > sensitivity || offsetY < -sensitivity) {
 					isSlidingMove = true;
 				}
 			}
 
 			if (isTouchDown == true && isSlidingMove == true) {
 				scrollScene(event.getY() - lastY);
 				lastY = event.getY();
 			}
 		}
 
 		if (event.isActionDown()) {
 			isTouchDown = true;
 			isSlidingMove = false;
 			oldTouchEvent = event.clone();
 			lastY = event.getY();
 		}
 
 		return super.onSceneTouchEvent(event); 
 	}
 
 	private void clickEvent(float x, float y) {
 	
 		if(SceneGame.getMode() == GameState.Recollect){
 			
 			Log.i(MainActivity.DebugID, "CLICK!");
 			
 			for (int i = 1; i < currentItemCount; i+=2) {
 	
 				WordItem item = wordItems.get(i);
 				if(item.isEnabled() == true){
 					if (isPointInRect(x, y, item.getRectX(), item.getRectY(), item.getWidth(), item.getHeight())) {
 						
 						// if it's good click
 						if(wordPairs.get(selectByProgramm/2).word2.equals(item.getWord())){
 							
 							Log.i(MainActivity.DebugID, "Good click: " + wordPairs.get(selectByProgramm/2).word1 + " - " + item.getWord());
 							
 							item.setEnabled(false);
 							item.setColor(0.3f, 1.0f, 0.3f);
 							
 							selectByProgramm += 2;
 							MainActivity.vibrate(30);
 							if(selectByProgramm >= currentItemCount){
 								sceneGame.enableNextButton(true);
 								if(sceneGame.getCurrentLevel()%2 == 0){
									MainActivity.Res.getSound("yes1").play();
 								} else {
									MainActivity.Res.getSound("yes2").play();
 								}
 								
 							}
 						} else {
 							
 							Log.i(MainActivity.DebugID, "Bad click: " + wordPairs.get(selectByProgramm/2).word1 + " - " + item.getWord());
 		
 							item.setColor(1.0f, 0.3f, 0.3f);
 							errorsCount++;
 							MainActivity.vibrate(60);
 							if(errorsCount > errorMaxCount){
 								sceneGame.setGameState(GameState.Loss);
 							} else {
								MainActivity.Res.getSound("error").play();
 							}
 						}
 						break;
 					}
 				}
 			}
 		}
 
 	}
 	
 
 	
 	@Override
 	protected void onManagedUpdate(final float pSecondsElapsed) {
 
 		update();
 		super.onManagedUpdate(pSecondsElapsed);
 	}
 
 	private void scrollScene(float scrollValue) {
 
 		setX(0);
 		setY(getY() + scrollValue);
 
 		float scrollSize = MainActivity.getHeight() - ((borderSize * 2) + (heightStep * currentItemCount / 2) - SceneGame.TextureRegions.get(0).getHeight());
 
 		if (getY() < scrollSize) {
 			setY(scrollSize);
 		}
 
 		if (getY() > 0) {
 			setY(0);
 		}
 	}
 
 	private boolean isPointInRect(float pointX, float pointY, float x, float y, float w, float h) {
 
 		if (pointX < x || pointX > x + w)
 			return false;
 
 		if (pointY < y || pointY > y + h)
 			return false;
 
 		return true;
 	}
 
 	private void miniShuffle(int a, int b){
 		String word = wordItems.get(a).getWord();
 		wordItems.get(a).setWord(wordItems.get(b).getWord());
 		wordItems.get(b).setWord(word);
 	}
 	
 	public void shuffleWords(){
 		
 		Log.i(MainActivity.DebugID, "SceneGameShow::shuffleWords:");
 		printWordPairs();
 	
 		Random rand = new Random();
 
 		for(int i=1; i<currentItemCount; i+=2){
 			int b = 2*rand.nextInt((currentItemCount-1)/2) + 1;	
 			miniShuffle(i, b);
 		}
 		
 		Log.i(MainActivity.DebugID, "--------------------------------");
 		printWordPairs();
 	}
 	
 	public void printWordPairs(){
 		for(int i=0; i<currentItemCount; i+=2){
 			Log.i(MainActivity.DebugID, "[" + wordItems.get(i).getWord() + "-" + wordItems.get(i+1).getWord() + "]");
 		}
 	}
 	
 	public int getErrorsCount() {
 		return errorsCount;
 	}
 
 	public void setErrorsCount(int errorsCount) {
 		this.errorsCount = errorsCount;
 	}
 
 	public int getErrorMaxCount() {
 		return errorMaxCount;
 	}
 
 	public void setErrorMaxCount(int errorMaxCount) {
 		this.errorMaxCount = errorMaxCount;
 	}
 
 }
