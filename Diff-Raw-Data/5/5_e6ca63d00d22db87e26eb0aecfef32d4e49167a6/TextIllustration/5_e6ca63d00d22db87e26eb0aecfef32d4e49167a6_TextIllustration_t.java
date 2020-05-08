 /* CMPUT301F13T06-Adventure Club: A choose-your-own-adventure story platform
  * Copyright (C) 2013 Alexander Cheung, Jessica Surya, Vina Nguyen, Anthony Ou,
  * Nancy Pham-Nguyen
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package story.book.model;
 
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import story.book.view.StoryApplication;
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.graphics.Color;
 import android.os.Build;
 import android.text.InputType;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 
 /**
  * Illustration class with textual content.
  * 
  * @author Alexander Cheung
  * @author Anthony Ou
  * @author Jessica Surya
  */
 @TargetApi(Build.VERSION_CODES.JELLY_BEAN) 
 public class TextIllustration extends Illustration<String> {
 
 	/**
 	 * Initialize content in constructor.
 	 * @param content is the String containing the text of the TextIllustration
 	 */
 	String content;
 	
 	public TextIllustration(String content) {
 		super();
 		setContent(content);
 	}
 
 	/**
 	 * Sets the content of the content of the <code>TextIllustration</code> to
 	 * the specified <code>String</code>.
 	 * 
 	 * @param	content	the <code>String</code> to set as the content
 	 */
 	public void setContent(String content) {
 		this.content = content;
 	}
 	
 	public String getContent() {
 		return this.content;
 	}
 
 	/**
 	 * Returns a <code>TextView</code> object initialized with the contents of
 	 * the illustration.
 	 * XXX: Needs review; should use generic types
 	 * 
 	 * @return	the <code>TextView</code> object with text set
 	 */
 	public View getView(String path, Boolean editMode, Context C) {
 		// If editMode is True, return an Edit Text view
 		// If editMode is False, return a Text View
 		if (editMode == false) {
 			TextView textView = new TextView(C);
 			textView.setText(this.content);
 			return formatView(textView);
 		}
 		
 		else {
 			return getEditView();
 		}
 		
 	}
 
 	/**
 	 * Returns a <code>EditText</code> object initialized with the contents of
 	 * the illustration.
 	 * 
 	 * @return	the <code>EditText</code> object with text set
 	 */
 	private EditText getEditView() {
 		EditText editView = new EditText(StoryApplication.getContext());
 		editView.setText(this.content);
		editView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		editView.setRawInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		editView.setSingleLine(false);
 		return formatView(editView);
 	}
 
 	/**
 	 * formatView() (FOR TEXTVIEWS ONLY) formats illustration textViews in an array list
 	 * by changing:
 	 * 		- text size (20)
 	 * 		- text color (black)
 	 * 		- padding on the left side
 	 * @param <T>
 	 * 
 	 * @param View		illustrations displayed as Views
 	 * @return 
 	 */
 	private <T> T formatView(T v) {
 		((TextView) v).setTextSize(20);
 		((TextView) v).setTextColor(Color.BLACK);
 		((TextView) v).setPaddingRelative(7, 0, 0, 10);
 		return v;
 	}
 }
