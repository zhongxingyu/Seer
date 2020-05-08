 package edu.uwg.jamestwyford.connect64;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.util.SparseIntArray;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TableLayout;
 import android.widget.Toast;
 
 /**
  * Activity for the 8x8 game grid.
  * 
  * @author jtwyford
  * @version assignment3
  */
 public class Connect64 extends Activity {
 	private static final String LOG_TAG = "C64";
 	private static final int NO_INPUT = -1;
 	private TableLayout grid;
 	private TableLayout inputGrid;
 	private Spinner rangeSpinner;
 	private int input;
 	private int[] initialPositions;
 	private int[] initialValues;
 	private SparseIntArray gridState;
 	private int range;
 
 	/**
 	 * Input handler for the 64 game grid buttons.
 	 * 
 	 * @param view
 	 *            the button clicked
 	 */
 	public void gameButtonClick(final View view) {
 		final Button button = (Button) view;
 		Log.d(LOG_TAG, "" + button.getTag());
 
 		if (this.input > 0) {
 			setText(button);
 		} else {
 			clearText(button);
 		}
 		configureInputButtons();
 		if (this.gridState.size() == 64) {
 			checkWinCondition();
 		}
 	}
 
 	/**
 	 * Input handler for the 16 input buttons.
 	 * 
 	 * @param view
 	 *            the button clicked
 	 */
 	public void inputButtonClick(final View view) {
 		final Button button = (Button) view;
 		this.input = Integer.valueOf(button.getText().toString());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(final Menu menu) {
 		getMenuInflater().inflate(R.menu.connect64, menu);
 		return true;
 	}
 
 	@Override
 	protected void onCreate(final Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_connect64);
 		this.grid = (TableLayout) findViewById(R.id.connect64);
 		this.gridState = new SparseIntArray(64);
 		this.input = NO_INPUT;
 
 		setupRangeSpinner();
 
 		// final int[] testPositions = new int[] { 11, 18, 88, 81, 27, 33, 66,
 		// 54 };
 		// final int[] testValues = new int[] { 1, 8, 15, 22, 34, 49, 55, 64 };
 
 		final int[] testPositions = new int[] { 12, 13, 14, 15, 16, 17, 18, 28,
 				27, 26, 25, 24, 23, 22, 21, 31, 32, 33, 34, 35, 36, 37, 38, 48,
 				47, 46, 45, 44, 43, 42, 41, 51, 52, 53, 54, 55, 56, 57, 58, 68,
 				67, 66, 65, 64, 63, 62, 61, 71, 72, 73, 74, 75, 76, 77, 78, 88,
 				87, 86, 85, 84, 83, 82 };
 
 		final int[] testValues = new int[] { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
 				12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27,
 				28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43,
 				44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59,
 				60, 61, 62, 63 };
 
 		resetAndInitializePuzzle(testPositions, testValues);
 	}
 
 	/**
 	 * overall board win condition check. Board must be full and all 64 button
 	 * neighbors must be valid.
 	 */
 	private void checkWinCondition() {
 		final Toast toast;
 
 		if (isBoardCorrect()) {
 			toast = Toast.makeText(this, "Winner!", Toast.LENGTH_LONG);
 		} else {
 			toast = Toast.makeText(this, "Loser!", Toast.LENGTH_LONG);
 		}
 		toast.show();
 	}
 
 	private void clearText(final Button button) {
 		final int pos = getTag(button);
 		this.gridState.delete(pos);
 		button.setText("");
 	}
 
 	/**
 	 * Sets the text on the input buttons to the current range, and sets the
 	 * enabled state for each button as appropriate. Buttons whose freshly-set
 	 * text appears on the game grid will be disabled.
 	 */
 	private void configureInputButtons() {
 		this.inputGrid = (TableLayout) findViewById(R.id.inputButtons);
 		int buttonVal;
 		Button inputButton;
 		for (int i = 1; i <= 16; i++) {
 			buttonVal = 16 * this.range + i;
 			inputButton = (Button) this.inputGrid.findViewWithTag("in" + i);
 			inputButton.setText("" + buttonVal);
 			if (this.gridState.indexOfValue(buttonVal) >= 0) {
 				inputButton.setEnabled(false);
 			} else {
 				inputButton.setEnabled(true);
 			}
 		}
 	}
 
 	private Button getButton(final String tag) {
 		return (Button) this.grid.findViewWithTag(tag);
 	}
 
 	private Integer getTag(final Button button) {
 		return Integer.valueOf(button.getTag().toString());
 	}
 
 	/**
 	 * returns the value as an integer of the button or -2 if empty/null
 	 * 
 	 * @param button
 	 *            the button to get the value of
 	 * @return the integer value of the button or -2 if empty/null
 	 */
 	private int getValue(final Button button) {
 		final int invalidValue = -2;
 		try {
 			return Integer.valueOf(button.getText().toString());
 		} catch (final NumberFormatException ex) {
 			return invalidValue;
 		}
 	}
 
 	/**
 	 * Win-condition checking at the button level. Looks for a neighbor with
 	 * value+1 and a neighbor with value-1.
 	 * 
	 * @param x
	 *            the column of the desired button [1-8]
	 * @param y
	 *            the row of the desired button [1-8]
 	 * @return true if one neighbor has value+1 AND another neighbor has value-1
 	 */
 	private boolean hasCorrectNeighbors(final Button button) {
 		final int tag = getTag(button);
 		final int x = tag / 10;
 		final int y = tag % 10;
 		final int value = getValue(button);
 
 		// TODO come up with more efficient solution
 		final Button left = getButton("" + (x - 1) + y);
 		final Button right = getButton("" + (x + 1) + y);
 		final Button up = getButton("" + x + (y - 1));
 		final Button down = getButton("" + x + (y + 1));
 
 		final boolean hasNext = (value == 64) || hasNext(value, left)
 				|| hasNext(value, right) || hasNext(value, up)
 				|| hasNext(value, down);
 		final boolean hasPrev = (value == 1) || hasPrev(value, left)
 				|| hasPrev(value, right) || hasPrev(value, up)
 				|| hasPrev(value, down);
 
 		return hasNext && hasPrev;
 	}
 
 	private boolean hasNext(final int thisValue, final Button otherButton) {
 		if (otherButton == null) {
 			return false;
 		}
 		return getValue(otherButton) == thisValue + 1;
 	}
 
 	private boolean hasPrev(final int thisValue, final Button otherButton) {
 		if (otherButton == null) {
 			return false;
 		}
 		return getValue(otherButton) == thisValue - 1;
 	}
 
 	private boolean isBoardCorrect() {
 		for (int i = 1; i <= 8; i++) {
 			for (int j = 1; j <= 8; j++) {
 				final Button button = getButton("" + i + j);
 				if (this.isEmpty(button) || !this.hasCorrectNeighbors(button)) {
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	private boolean isEmpty(final Button button) {
 		if (button == null || button.getText().equals("")) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Resets the grid and loads values into the specified positions.
 	 * <p>
 	 * <b>NOTE:</b> The 8x8 game grid uses subscripts starting at 11 in the
 	 * top-left corner and ending at 88 in the bottom-right. So, row 3 column 4
 	 * is position 34.
 	 * 
 	 * @param positions
 	 *            an integer array with values from 11-88 (but no 0s or 9s).
 	 *            Does not need to be sorted.
 	 * @param values
 	 *            an equal-length integer array with values from 1-64.
 	 */
 	private void resetAndInitializePuzzle(final int[] positions,
 			final int[] values) {
 		if (positions.length != values.length) {
 			throw new IllegalArgumentException(
 					"positions.length != values.length");
 		}
 		this.initialPositions = positions;
 		this.initialValues = values;
 
 		resetGrid();
 		storeInitialValues();
 	}
 
 	private void resetGrid() {
 		this.rangeSpinner.setSelection(0);
 
 		for (int i = 1; i <= 8; i++) {
 			for (int j = 1; j <= 8; j++) {
 				final Button button = getButton("" + i + j);
 				button.setText("");
 				button.setEnabled(true);
 			}
 		}
 		this.gridState.clear();
 	}
 
 	private void setRange(final int pos) {
 		this.range = pos;
 	}
 
 	private void setText(final Button button) {
 		if (this.input < 1) {
 			return;
 		}
 		if (getValue(button) > 0) {
 			clearText(button);
 		}
 		button.setText("" + this.input);
 		final int pos = getTag(button);
 		this.gridState.put(pos, this.input);
 		this.input = NO_INPUT;
 	}
 
 	private void setupRangeSpinner() {
 		this.rangeSpinner = (Spinner) findViewById(R.id.rangeSpinner);
 		final ArrayAdapter<CharSequence> adapter = ArrayAdapter
 				.createFromResource(this, R.array.ranges,
 						android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		this.rangeSpinner.setAdapter(adapter);
 		this.rangeSpinner
 				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 					@Override
 					public void onItemSelected(final AdapterView<?> parent,
 							final View view, final int pos, final long id) {
 						setRange(pos);
 						configureInputButtons();
 					}
 
 					@Override
 					public void onNothingSelected(final AdapterView<?> parent) {
 					}
 				});
 	}
 
 	private void storeInitialValues() {
 		for (int i = 0; i < this.initialPositions.length; i++) {
 			final Button button = getButton("" + this.initialPositions[i]);
 			button.setText("" + this.initialValues[i]);
 			button.setEnabled(false);
 			this.gridState.put(this.initialPositions[i], this.initialValues[i]);
 		}
 	}
 }
