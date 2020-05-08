 package com.redvex.byteme.ui;
 
 import java.util.ArrayList;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.redvex.byteme.GameActivity;
 import com.redvex.byteme.HandsetDeviceGameActivity;
 import com.redvex.byteme.R;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.KeyguardManager;
 import android.content.Context;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.graphics.Typeface;
 import android.inputmethodservice.Keyboard;
 import android.inputmethodservice.KeyboardView;
 import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnKeyListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.view.ViewTreeObserver;
 import android.widget.TextView;
 import android.widget.LinearLayout;
 
 /**
  * A fragment representing the Game field while the game is running. This
  * fragment is either contained in a {@link GameActivity} in two-pane mode (on
  * tablets) or a {@link HandsetDeviceGameActivity} on handsets.
  * <p>
  * Activities containing this fragment MUST implement the {@link GameLogic}
  * interface.
  */
 public class InGameFragment extends SherlockFragment implements OnKeyboardActionListener,
 		OnKeyListener {
 	/**
 	 * The fragment argument representing the game type that this fragment
 	 * represents.
 	 */
 	public static final String GAME_TYPE = "game_id";
 
 	/**
 	 * Arrays containing the current rows of the UI.
 	 */
 	private ArrayList<LinearLayout> sBinaryRow = null;
 	private ArrayList<TextView> sDecimalRow = null;
 	private ArrayList<TextView> sHexadecimalRow = null;
 
 	/**
 	 * The keyboard for the decimal and hexadecimal rows.
 	 */
 	private KeyboardView mKeyboardView = null;
 	private Keyboard mKeyboardDec = null;
 	private Keyboard mKeyboardHex = null;
 
 	/**
 	 * TextView which has currently the keyboard focus
 	 */
 	private TextView mCurrentTextView = null;
 
 	private boolean mActionbarPaused = false;
 	private String mActionbarLevel = "";
 	private String mActionbarScore = "";
 	private String mActionbarLinesLeft = "";
 
 	/**
 	 * The fragment's current GameLogic object.
 	 */
 	private GameLogic mGameLogic = sGameLogicDummy;
 
 	public interface GameLogic {
 		public void startGameLogic(String gameType);
 
 		public void pauseGameLogic();
 
 		public void updateRow(int index, UIRow row);
 	}
 
 	/**
 	 * A dummy implementation of the {@link GameLogic} interface that does
 	 * nothing. Used only when this fragment is not attached to an activity.
 	 */
 	private static GameLogic sGameLogicDummy = new GameLogic() {
 		@Override
 		public void startGameLogic(String gameType) {
 		}
 
 		@Override
 		public void pauseGameLogic() {
 		}
 
 		@Override
 		public void updateRow(int index, UIRow row) {
 		}
 	};
 
 	/**
 	 * Mandatory empty constructor for the fragment manager to instantiate the
 	 * fragment (e.g. upon screen orientation changes).
 	 */
 	public InGameFragment() {
 	}
 
 	/**
 	 * Initializes the decimal and hexadecimal keyboards.
 	 */
 	private void initiateKeyboards() {
 		mKeyboardView = (KeyboardView) getActivity().findViewById(R.id.keyboard);
 
 		mKeyboardView.setEnabled(true);
 		mKeyboardView.setPreviewEnabled(false);
 		mKeyboardView.setOnKeyListener(this);
 		mKeyboardView.setOnKeyboardActionListener(this);
 
 		mKeyboardDec = new Keyboard(getActivity(), R.xml.dec);
 		mKeyboardHex = new Keyboard(getActivity(), R.xml.hex);
 	}
 
 	public void setKeyboardsInvisible() {
 		mKeyboardView.setVisibility(View.INVISIBLE);
 
 		if (mCurrentTextView != null) {
 			// The update of the row is sent to the GameLogic.
 			if (mKeyboardView.getKeyboard().equals(mKeyboardDec)) {
 				updateRow(sDecimalRow.indexOf(mCurrentTextView));
 			} else if (mKeyboardView.getKeyboard().equals(mKeyboardHex)) {
 				updateRow(sHexadecimalRow.indexOf(mCurrentTextView));
 			}
 		}
 	}
 
 	/**
 	 * Initializes each row with new ArrayList<row type> objects.
 	 */
 	private void initiateUIRows() {
 		sBinaryRow = new ArrayList<LinearLayout>();
 		sDecimalRow = new ArrayList<TextView>();
 		sHexadecimalRow = new ArrayList<TextView>();
 		getActivity().findViewById(R.id.game_field).setVisibility(View.VISIBLE);
 	}
 
 	/**
 	 * @param binField
 	 *            The LinearLayout the new binary row should be displayed in.
 	 * @param binText
 	 *            An ArrayList<String> containing the Strings for the Text of
 	 *            the row.
 	 * @param fixedBinRow
 	 *            A boolean determining if the row should be enabled or
 	 *            disabled. Set to false if the row should be enabled.
 	 */
 	private void addUIBinaryRow(LinearLayout binField, ArrayList<String> binText,
 			boolean fixedBinRow) {
 		if (sBinaryRow.size() < 10) {
 			LinearLayout newBinRow = new LinearLayout(getActivity());
 
 			newBinRow.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0,
 					1.0f));
 
 			// Each digit in the binary row is represented by a single TextView.
 			for (int i = 0; i != 8; i++) {
 				TextView newBinTextView = new TextView(getActivity());
 
 				newBinTextView.setLayoutParams(new LinearLayout.LayoutParams(0,
 						LayoutParams.MATCH_PARENT, 1.0f));
 				newBinTextView.setGravity(Gravity.CENTER);
 				// Currently just the binary row uses the encom-webfont as an
 				// example implementation. I'd suggest, when the graphics are
 				// done, the best solution would be a custom TextView/Button
 				// which uses the font directly.
 				// http://www.tron-sector.com/forums/default.aspx?a=top&id=427914
 				// http://disneydigitalbooks.go.com/tron/fonts/encom-webfont.ttf
 				newBinTextView.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),
 						"fonts/encom-webfont.ttf"));
 
 				try {
 					newBinTextView.setText(binText.get(i));
 				} catch (IndexOutOfBoundsException e) {
 					throw new IndexOutOfBoundsException(e.getMessage());
 				}
 				newBinTextView.setId(i);
 
 				newBinTextView.setOnClickListener(new View.OnClickListener() {
 					public void onClick(View view) {
 						// If the keyboard of a decimal or hexadecimal row is
 						// visible it's set to invisible and the updates made at
 						// the row are send to the GameLogic.
 						if (mKeyboardView.getVisibility() == View.VISIBLE) {
 							setKeyboardsInvisible();
 						}
 
 						TextView clickedBinTextView = (TextView) view;
 
 						// The current bit is swapped.
 						if (clickedBinTextView.getText().toString().equals("0")) {
 							clickedBinTextView.setText("1");
 						} else {
 							clickedBinTextView.setText("0");
 						}
 
 						// The update of the row is send to the GameLogic.
 						updateRow(sBinaryRow.indexOf(clickedBinTextView.getParent()));
 					}
 				});
 
 				newBinTextView.setEnabled(!fixedBinRow);
 				newBinRow.addView(newBinTextView);
 			}
 
 			try {
 				sBinaryRow.add(newBinRow);
 				binField.addView(newBinRow);
 			} catch (NullPointerException e) {
 				throw new NullPointerException(e.getMessage());
 			}
 		} else {
 			throw new IllegalStateException("Binary game field is full. No more rows can be added.");
 		}
 	}
 
 	/**
 	 * @param decField
 	 *            The LinearLayout the new decimal row should be displayed in.
 	 * @param decText
 	 *            A String containing the Text of the row.
 	 * @param fixedDecRow
 	 *            A boolean determining if the row should be enabled or
 	 *            disabled. Set to false if the row should be enabled.
 	 */
 	private void addUIDecimalRow(LinearLayout decField, String decText, boolean fixedDecRow) {
 		if (sDecimalRow.size() < 10) {
 			TextView newDecRow = new TextView(getActivity());
 
 			if (fixedDecRow) {
 				newDecRow.setText(decText);
 			} else {
 				newDecRow.setText("");
 			}
 			newDecRow.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0,
 					1.0f));
 			newDecRow.setGravity(Gravity.CENTER);
 
 			newDecRow.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View view) {
 					// If the keyboard of a hexadecimal row is visible it's set
 					// to invisible and the updates made at the row are send to
 					// the GameLogic.
 					if (mKeyboardView.getVisibility() == View.VISIBLE) {
 						setKeyboardsInvisible();
 					}
 
 					// Getting the TextView
 					mCurrentTextView = (TextView) view;
 
 					try {
 						mKeyboardView.setKeyboard(mKeyboardDec);
 					} catch (NullPointerException e) {
 						throw new NullPointerException(e.getMessage());
 					}
 
 					mKeyboardView.setVisibility(View.VISIBLE);
 					moveKeyboard();
 				}
 			});
 
 			newDecRow.setEnabled(!fixedDecRow);
 
 			try {
 				sDecimalRow.add(newDecRow);
 				decField.addView(newDecRow);
 			} catch (NullPointerException e) {
 				throw new NullPointerException(e.getMessage());
 			}
 		} else {
 			throw new IllegalStateException(
 					"Decimal game field is full. No more rows can be added.");
 		}
 	}
 
 	/**
 	 * @param hexField
 	 *            The LinearLayout the new hexadecimal row should be displayed
 	 *            in.
 	 * @param hexText
 	 *            A String containing the Text of the row.
 	 * @param fixedHexRow
 	 *            A boolean determining if the row should be enabled or
 	 *            disabled. Set to false if the row should be enabled.
 	 */
 	private void addUIHexadecimalRow(LinearLayout hexField, String hexText, boolean fixedHexRow) {
 		if (sHexadecimalRow.size() < 10) {
 			TextView newHexRow = new TextView(getActivity());
 
 			if (fixedHexRow) {
 				newHexRow.setText(hexText);
 			} else {
 				newHexRow.setText("");
 			}
 			newHexRow.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0,
 					1.0f));
 			newHexRow.setGravity(Gravity.CENTER);
 
 			newHexRow.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View view) {
 					// If the keyboard of a decimal row is visible it's set to
 					// invisible and the updates made at the row are send to the
 					// GameLogic.
 					if (mKeyboardView.getVisibility() == View.VISIBLE) {
 						setKeyboardsInvisible();
 					}
 
 					// Getting the TextView
 					mCurrentTextView = (TextView) view;
 
 					try {
 						mKeyboardView.setKeyboard(mKeyboardHex);
 					} catch (NullPointerException e) {
 						throw new NullPointerException(e.getMessage());
 					}
 
 					mKeyboardView.setVisibility(View.VISIBLE);
 					moveKeyboard();
 				}
 			});
 
 			newHexRow.setEnabled(!fixedHexRow);
 
 			try {
 				sHexadecimalRow.add(newHexRow);
 				hexField.addView(newHexRow);
 			} catch (NullPointerException e) {
 				throw new NullPointerException(e.getMessage());
 			}
 		} else {
 			throw new IllegalStateException(
 					"Hexadecimal game field is full. No more rows can be added.");
 		}
 	}
 
 	/**
 	 * Adds a new row to the UI corresponding to the current GAME_TYPE.
 	 * 
 	 * @param row
 	 *            A UIRow object containing all Strings for the binary,
 	 *            hexadecimal and decimal row.
 	 */
 	public void addUIRow(UIRow row) {
 		if (getArguments().containsKey(GAME_TYPE)) {
 			LinearLayout binField;
 			LinearLayout decField;
 			LinearLayout hexField;
 
 			switch (Integer.parseInt(getArguments().getString(GAME_TYPE))) {
 			case 1:
 				binField = (LinearLayout) getActivity().findViewById(R.id.in_game_1_binary);
 				addUIBinaryRow(binField, row.getBinRow(), row.isFixedBinRow());
 
 				decField = (LinearLayout) getActivity().findViewById(R.id.in_game_1_decimal);
 				addUIDecimalRow(decField, row.getDecRow(), row.isFixedDecRow());
 
 				break;
 			case 2:
 				binField = (LinearLayout) getActivity().findViewById(R.id.in_game_2_binary);
 				addUIBinaryRow(binField, row.getBinRow(), row.isFixedBinRow());
 
 				hexField = (LinearLayout) getActivity().findViewById(R.id.in_game_2_hexadecimal);
 				addUIHexadecimalRow(hexField, row.getHexRow(), row.isFixedHexRow());
 
 				break;
 			case 3:
 				decField = (LinearLayout) getActivity().findViewById(R.id.in_game_3_decimal);
 				addUIDecimalRow(decField, row.getDecRow(), row.isFixedDecRow());
 
 				hexField = (LinearLayout) getActivity().findViewById(R.id.in_game_3_hexadecimal);
 				addUIHexadecimalRow(hexField, row.getHexRow(), row.isFixedHexRow());
 
 				break;
 			case 4:
 				binField = (LinearLayout) getActivity().findViewById(R.id.in_game_4_binary);
 				addUIBinaryRow(binField, row.getBinRow(), row.isFixedBinRow());
 
 				decField = (LinearLayout) getActivity().findViewById(R.id.in_game_4_decimal);
 				addUIDecimalRow(decField, row.getDecRow(), row.isFixedDecRow());
 
 				hexField = (LinearLayout) getActivity().findViewById(R.id.in_game_4_hexadecimal);
 				addUIHexadecimalRow(hexField, row.getHexRow(), row.isFixedHexRow());
 
 				break;
 			default:
 				throw new IllegalStateException("No valid game type in GAME_TYPE.");
 			}
 
 			if (mCurrentTextView != null) {
 				mCurrentTextView.getViewTreeObserver().addOnGlobalLayoutListener(
 						new ViewTreeObserver.OnGlobalLayoutListener() {
 							@SuppressLint("NewApi")
 							@Override
 							public void onGlobalLayout() {
 								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
 									mCurrentTextView.getViewTreeObserver()
 											.removeOnGlobalLayoutListener(this);
 								} else {
 									mCurrentTextView.getViewTreeObserver()
 											.removeGlobalOnLayoutListener(this);
 								}
 
 								// The keyboard gets adjusted since the row
 								// receiving the keyboard input may be displayed
 								// at a new position.
 								if (mKeyboardView.getVisibility() == View.VISIBLE) {
 									moveKeyboard();
 								}
 							}
 						});
 			}
 		}
 	}
 
 	/**
 	 * Deletes a single row from the UI.
 	 * 
 	 * @param index
 	 *            The index of the row, which should be deleted from the UI.
 	 */
 	public void removeUIRow(int index) {
 		try {
 			if (getArguments().containsKey(GAME_TYPE)) {
 				if (mCurrentTextView != null) {
 					mCurrentTextView.getViewTreeObserver().addOnGlobalLayoutListener(
 							new ViewTreeObserver.OnGlobalLayoutListener() {
 								@SuppressLint("NewApi")
 								@Override
 								public void onGlobalLayout() {
 									if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
 										mCurrentTextView.getViewTreeObserver()
 												.removeOnGlobalLayoutListener(this);
 									} else {
 										mCurrentTextView.getViewTreeObserver()
 												.removeGlobalOnLayoutListener(this);
 									}
 
 									// The keyboard gets adjusted since the row
 									// receiving the keyboard input may be
 									// displayed at a new position.
 									if (mKeyboardView.getVisibility() == View.VISIBLE) {
 										moveKeyboard();
 									}
 								}
 							});
 				}
 
 				LinearLayout binField;
 				LinearLayout decField;
 				LinearLayout hexField;
 
 				switch (Integer.parseInt(getArguments().getString(GAME_TYPE))) {
 				case 1:
 					binField = (LinearLayout) getActivity().findViewById(R.id.in_game_1_binary);
 					binField.removeView(sBinaryRow.get(index));
 					sBinaryRow.remove(index);
 
 					decField = (LinearLayout) getActivity().findViewById(R.id.in_game_1_decimal);
 					decField.removeView(sDecimalRow.get(index));
 					sDecimalRow.remove(index);
 
 					break;
 				case 2:
 					binField = (LinearLayout) getActivity().findViewById(R.id.in_game_2_binary);
 					binField.removeView(sBinaryRow.get(index));
 					sBinaryRow.remove(index);
 
 					hexField = (LinearLayout) getActivity()
 							.findViewById(R.id.in_game_2_hexadecimal);
 					hexField.removeView(sHexadecimalRow.get(index));
 					sHexadecimalRow.remove(index);
 
 					break;
 				case 3:
 					hexField = (LinearLayout) getActivity()
 							.findViewById(R.id.in_game_3_hexadecimal);
 					hexField.removeView(sHexadecimalRow.get(index));
 					sHexadecimalRow.remove(index);
 
 					decField = (LinearLayout) getActivity().findViewById(R.id.in_game_3_decimal);
 					decField.removeView(sDecimalRow.get(index));
 					sDecimalRow.remove(index);
 
 					break;
 				case 4:
 					binField = (LinearLayout) getActivity().findViewById(R.id.in_game_4_binary);
 					binField.removeView(sBinaryRow.get(index));
 					sBinaryRow.remove(index);
 
 					decField = (LinearLayout) getActivity().findViewById(R.id.in_game_4_decimal);
 					decField.removeView(sDecimalRow.get(index));
 					sDecimalRow.remove(index);
 
 					hexField = (LinearLayout) getActivity()
 							.findViewById(R.id.in_game_4_hexadecimal);
 					hexField.removeView(sHexadecimalRow.get(index));
 					sHexadecimalRow.remove(index);
 
 					break;
 				default:
 					throw new IllegalStateException("No valid game type in GAME_TYPE.");
 				}
 			}
 		} catch (IndexOutOfBoundsException e) {
 			throw new IndexOutOfBoundsException(e.getMessage());
 		}
 	}
 
 	/**
 	 * When the fragment was send to the background ,by e.g. pressing the home
 	 * TextView, and it returns there are rows in the game fields which aren't
 	 * stored in sBinaryRow, sDecimalRow, sHexadecimalRow. Therefore the rows
 	 * aren't deleted by just calling removeUIRow, but by deleting all views
 	 * contained in the game fields.
 	 */
 	public void clearUIBoard() {
 		if (getArguments().containsKey(GAME_TYPE)) {
 			LinearLayout binField;
 			LinearLayout decField;
 			LinearLayout hexField;
 
 			switch (Integer.parseInt(getArguments().getString(GAME_TYPE))) {
 			case 1:
 				binField = (LinearLayout) getActivity().findViewById(R.id.in_game_1_binary);
 				binField.removeAllViews();
 				sBinaryRow.clear();
 
 				decField = (LinearLayout) getActivity().findViewById(R.id.in_game_1_decimal);
 				decField.removeAllViews();
 				sDecimalRow.clear();
 
 				break;
 			case 2:
 				binField = (LinearLayout) getActivity().findViewById(R.id.in_game_2_binary);
 				binField.removeAllViews();
 				sBinaryRow.clear();
 
 				hexField = (LinearLayout) getActivity().findViewById(R.id.in_game_2_hexadecimal);
 				hexField.removeAllViews();
 				sHexadecimalRow.clear();
 
 				break;
 			case 3:
 				hexField = (LinearLayout) getActivity().findViewById(R.id.in_game_3_hexadecimal);
 				hexField.removeAllViews();
 				sHexadecimalRow.clear();
 
 				decField = (LinearLayout) getActivity().findViewById(R.id.in_game_3_decimal);
 				decField.removeAllViews();
 				sDecimalRow.clear();
 
 				break;
 			case 4:
 				binField = (LinearLayout) getActivity().findViewById(R.id.in_game_4_binary);
 				binField.removeAllViews();
 				sBinaryRow.clear();
 
 				decField = (LinearLayout) getActivity().findViewById(R.id.in_game_4_decimal);
 				decField.removeAllViews();
 				sDecimalRow.clear();
 
 				hexField = (LinearLayout) getActivity().findViewById(R.id.in_game_4_hexadecimal);
 				hexField.removeAllViews();
 				sHexadecimalRow.clear();
 
 				break;
 			default:
 				throw new IllegalStateException("No valid game type in GAME_TYPE.");
 			}
 		}
 	}
 
 	/**
 	 * updateRow is designed to update the game logic. The current state of a
 	 * row in the UI is read and sent to the game logic.
 	 * 
 	 * @param index
 	 *            The index of the row, which should be updated.
 	 */
 	private void updateRow(int index) {
 		if (index != -1) {
 			ArrayList<String> binRowStrings = new ArrayList<String>();
 			boolean fixedBinRow = true;
 			String decRowString;
 			boolean fixedDecRow = true;
 			String hexRowString;
 			boolean fixedHexRow = true;
 
 			for (int i = 0; i != 8; i++) {
 				if (index < sBinaryRow.size()) {
 					// Reading the single bits from the UI and storing them in
 					// an ArrayList<String> to add them to an UIRow object.
 					TextView tempTextView = (TextView) sBinaryRow.get(index).getChildAt(i);
 					binRowStrings.add(tempTextView.getText().toString());
 					// If the UI bit field is enabled the binary row is not a
 					// fixed value.
 					fixedBinRow = !tempTextView.isEnabled();
 				} else {
 					binRowStrings.add("0");
 				}
 			}
 
 			if (index < sDecimalRow.size()) {
 				// Gets the UI value of the decimal row and stores it in a
 				// string.
 				decRowString = sDecimalRow.get(index).getText().toString();
 				// If the UI decimal row is enabled it's not a fixed value.
 				fixedDecRow = !sDecimalRow.get(index).isEnabled();
 			} else {
 				decRowString = "0";
 			}
 			if (index < sHexadecimalRow.size()) {
 				hexRowString = sHexadecimalRow.get(index).getText().toString();
 				fixedHexRow = !sHexadecimalRow.get(index).isEnabled();
 			} else {
 				hexRowString = "0";
 			}
 
 			// The values read from the UI are stored in a UIRow object and sent
 			// to the game logic.
 			UIRow row = new UIRow(binRowStrings, decRowString, hexRowString, fixedBinRow,
 					fixedDecRow, fixedHexRow);
 			mGameLogic.updateRow(index, row);
 		}
 	}
 
 	/**
 	 * moveKeyboard moves the keyboard to the currently active and receiving
 	 * view.
 	 */
 	@SuppressLint("NewApi")
 	private void moveKeyboard() {
 		if (mCurrentTextView != null) {
 			float widthTextView = (float) mCurrentTextView.getWidth();
 			float heightTextView = (float) mCurrentTextView.getHeight();
 
 			float widthKeyboard;
 			float heightKeyboard;
 
 			if (mKeyboardView.getKeyboard().equals(mKeyboardDec)) {
 				// Since the width and height of the two keyboards is different,
 				// it's checked whether it's an decimal or hexadecimal keyboard.
 				widthKeyboard = (float) mKeyboardDec.getMinWidth();
 				heightKeyboard = (float) mKeyboardDec.getHeight();
 			} else {
 				widthKeyboard = (float) mKeyboardHex.getMinWidth();
 				heightKeyboard = (float) mKeyboardHex.getHeight();
 			}
 
 			// Default is to expand the keyboard to the right top. The x value
 			// is made up of the x position of the mCurrentTextView. Since this
 			// TextView is embedded in a LinearLayout, the x position of the
 			// LinearLayout has to be added to the x value of the
 			// mCurrentTextView. Now x is at the beginning of the
 			// mCurrentTextView. To position the keyboard in the middle of the
 			// TextView half the width has to be added. To position the bottom
 			// of the keyboard at the top of mCurrentTextView the height of the
 			// keyboard has to be subtracted from the y position of
 			// mCurrentTextView.
 			float x = 0;
 			float y = 0;
 
 			try {
 				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 					x = mCurrentTextView.getX()
 							+ ((LinearLayout) mCurrentTextView.getParent()).getX()
 							+ (widthTextView / 2);
 					y = mCurrentTextView.getY() - heightKeyboard;
 				} else {
 					x = (float) mCurrentTextView.getLeft()
 							+ (float) ((LinearLayout) mCurrentTextView.getParent()).getLeft()
 							+ (widthTextView / 2);
 					y = (float) mCurrentTextView.getTop() - heightKeyboard;
 				}
 			} catch (NullPointerException e) {
 				setKeyboardsInvisible();
 			}
 
 			// Checks if the left/right expansion of the keyboard has to be
 			// changed
 			// from right to left.
 			if (x > (float) getView().getWidth() - widthKeyboard) {
 				x -= widthKeyboard;
 
 				// Checks if the top/bottom expansion of the keyboard has to be
 				// changed from top to bottom.
 				if (y < 0) {
 					y += heightKeyboard + heightTextView;
 				}
 			} else if (y < 0) {
 				y += heightKeyboard + heightTextView;
 			}
 
 			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 				mKeyboardView.setX(x);
 				mKeyboardView.setY(y);
 			} else {
 				int left = (int) x;
 				int top = (int) y;
 				int right = left + (int) widthKeyboard;
 				int bottom = top + (int) heightKeyboard;
 				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
 						(int) widthKeyboard, (int) heightKeyboard);
 				layoutParams.setMargins(left, top, right, bottom);
 				mKeyboardView.setLayoutParams(layoutParams);
 			}
 		}
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.actionbar, menu);
 	}
 
 	@SuppressLint("NewApi")
 	public void updateActionbarLevel(int level) {
 		mActionbarLevel = getString(R.string.actionbar_level) + Integer.toString(level);
 		getSherlockActivity().invalidateOptionsMenu();
 	}
 
 	@SuppressLint("NewApi")
 	public void updateActionbarScore(int score) {
 		mActionbarScore = getString(R.string.actionbar_score) + Integer.toString(score);
 		getSherlockActivity().invalidateOptionsMenu();
 	}
 
 	@SuppressLint("NewApi")
 	public void updateActionbarLinesLeft(int linesLeft) {
 		mActionbarLinesLeft = getString(R.string.actionbar_lines_left)
 				+ Integer.toString(linesLeft);
 		getSherlockActivity().invalidateOptionsMenu();
 	}
 
 	@Override
 	public void onPrepareOptionsMenu(Menu menu) {
 		super.onPrepareOptionsMenu(menu);
 		// The values of the action bar are updated.
 		MenuItem item = menu.findItem(R.id.actionbar_pause_resume_no_split);
 		if (item != null) {
 			// This item is just shown if the action bar isn't split.
 			if (mActionbarPaused) {
 				// If the game is paused the "Resume" title is shown.
 				item.setTitle(getString(R.string.actionbar_resume));
 			} else {
 				item.setTitle(getString(R.string.actionbar_pause));
 			}
 		}
 		menu.findItem(R.id.actionbar_level).setTitle(mActionbarLevel);
 		menu.findItem(R.id.actionbar_score).setTitle(mActionbarScore);
 		menu.findItem(R.id.actionbar_lines_left).setTitle(mActionbarLinesLeft);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.actionbar_pause_resume_no_split:
 			if (item.getTitle().toString().equals(getString(R.string.actionbar_pause))) {
 				// Pause game.
 				mActionbarPaused = true;
 				item.setTitle(getString(R.string.actionbar_resume));
 				mGameLogic.pauseGameLogic();
 				setKeyboardsInvisible();
 				getActivity().findViewById(R.id.game_field).setVisibility(View.INVISIBLE);
 			} else {
 				// Resume game.
 				mActionbarPaused = false;
 				item.setTitle(getString(R.string.actionbar_pause));
 				mGameLogic.startGameLogic(getArguments().getString(GAME_TYPE));
 				getActivity().findViewById(R.id.game_field).setVisibility(View.VISIBLE);
 			}
 			break;
 		}
 
 		return true;
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		// Activities containing this fragment must implement its GameLogic.
 		if (!(activity instanceof GameLogic)) {
 			throw new IllegalStateException("Activity must implement fragment's callbacks.");
 		}
 
 		mGameLogic = (GameLogic) activity;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setHasOptionsMenu(true);
 
 		if (savedInstanceState != null) {
 			mActionbarPaused = savedInstanceState.getBoolean("mActionbarPaused", false);
 		}
 	}
 
 	/**
 	 * The game field for the corresponding GAME_TYPE gets initiated. To change
 	 * the layout of the game field edit fragment_in_game_detail_x.xml, where x
 	 * represents the number for the GAME_TYPE.
 	 */
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		// Both views are just permitted on tablets.
 		int currentScreenSize = getResources().getConfiguration().screenLayout
 				& Configuration.SCREENLAYOUT_SIZE_MASK;
 
 		if (currentScreenSize < Configuration.SCREENLAYOUT_SIZE_XLARGE) {
 			getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		}
 
 		View rootView;
 		if (getArguments().containsKey(GAME_TYPE)) {
 			switch (Integer.parseInt(getArguments().getString(GAME_TYPE))) {
 			case 1:
 				rootView = inflater.inflate(R.layout.fragment_in_game_1, container, false);
 				break;
 			case 2:
 				rootView = inflater.inflate(R.layout.fragment_in_game_2, container, false);
 				break;
 			case 3:
 				rootView = inflater.inflate(R.layout.fragment_in_game_3, container, false);
 				break;
 			case 4:
 				rootView = inflater.inflate(R.layout.fragment_in_game_4, container, false);
 				break;
 			default:
 				throw new IllegalStateException("No valid game type in GAME_TYPE.");
 			}
 		} else {
 			throw new IllegalStateException(
 					"The Arguments of InGameDetailFragment don't contain GAME_TYPE.");
 		}
 
 		return rootView;
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putBoolean("mActionbarPaused", mActionbarPaused);
 	}
 
 	/**
 	 * The UI row objects and the keyboards are initiated and the game logic is
 	 * started.
 	 */
 	@SuppressLint("NewApi")
 	@Override
 	public void onStart() {
 		super.onStart();
 		KeyguardManager keyguardManager = (KeyguardManager) getActivity().getApplication()
 				.getSystemService(Context.KEYGUARD_SERVICE);
 
 		if (keyguardManager.inKeyguardRestrictedInputMode() || mActionbarPaused) {
 			// The user has to resume the game manually.
 			getActivity().findViewById(R.id.game_field).setVisibility(View.INVISIBLE);
 			setKeyboardsInvisible();
 			mActionbarPaused = true;
 			getSherlockActivity().invalidateOptionsMenu();
 		} else {
 			// Just if the screen is unlocked the game is started.
 			initiateKeyboards();
 			initiateUIRows();
 			mGameLogic.startGameLogic(getArguments().getString(GAME_TYPE));
 		}
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		mGameLogic.pauseGameLogic();
 	}
 
 	@Override
 	public void onDetach() {
 		super.onDetach();
 		// Reset the active GameLogic interface to the dummy implementation.
 		mGameLogic = sGameLogicDummy;
 	}
 
 	@Override
 	public void onKey(int arg0, int[] arg1) {
 		switch (arg0) {
 		case -5:
 			// If the Text field of mCurrentTextView contains characters the
 			// last one gets deleted.
 			if (mCurrentTextView.getText().toString().length() > 0) {
 				mCurrentTextView.setText(mCurrentTextView.getText().toString()
 						.substring(0, mCurrentTextView.getText().toString().length() - 1));
 			}
 			break;
 		case 10:
 			// The keyboard is set to invisible and the updates of the row are
 			// send to the GameLogic if the user presses 'Enter'.
 			setKeyboardsInvisible();
 			break;
 		default:
 			if (mKeyboardView.getKeyboard().equals(mKeyboardDec)) {
 				// The maximum number represented by one byte in decimal
 				// notation is 255. Therefore just three digits are allowed at a
 				// decimal row.
 				if (mCurrentTextView.getText().length() < 3) {
 					mCurrentTextView.append(Character.toString((char) arg0));
 				}
 			} else if (mKeyboardView.getKeyboard().equals(mKeyboardHex)) {
 				// In hexadecimal notation it's 0xFF. Therefore 2 digits are
 				// allowed.
 				if (mCurrentTextView.getText().length() < 2) {
 					mCurrentTextView.append(Character.toString((char) arg0));
 				}
 			}
 		}
 	}
 
 	@Override
 	public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
 		return false;
 	}
 
 	@Override
 	public void onPress(int arg0) {
 	}
 
 	@Override
 	public void onRelease(int arg0) {
 	}
 
 	@Override
 	public void onText(CharSequence arg0) {
 	}
 
 	@Override
 	public void swipeDown() {
 	}
 
 	@Override
 	public void swipeLeft() {
 	}
 
 	@Override
 	public void swipeRight() {
 	}
 
 	@Override
 	public void swipeUp() {
 	}
 }
