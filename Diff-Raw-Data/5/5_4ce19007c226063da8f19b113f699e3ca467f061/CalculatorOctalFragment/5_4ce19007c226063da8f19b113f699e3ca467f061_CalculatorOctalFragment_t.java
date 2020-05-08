 package com.ACM.binarycalculator;
 
 import java.math.BigInteger;
 import java.util.StringTokenizer;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 
 /**
  * 
  * @author James Van Gaasbeck, ACM at UCF <jjvg@knights.ucf.edu>
  * 
  * 
  */
 public class CalculatorOctalFragment extends SherlockFragment {
 	// this is a tag used for debugging purposes
 	private static final String TAG = "CalculatorOctalFragment";
 
 	// string constant for saving our workingTextViewText
 	private static final String KEY_WORKINGTEXTVIEW_STRING = "workingTextString";
 	private static final int VIEW_NUMBER = 3;
 	// the radix number (base-number) to be used when parsing the string.
 	private static final int VIEWS_RADIX = 8;
 
 	// these are our variables
 	// TextView mComputeTextView;
 	TextView mWorkingTextView;
 	/*
 	 * The mCurrentWorkingText string variable is the current expression, not
 	 * the entire list.
 	 */
 	private String mCurrentWorkingText;
 	/*
 	 * mExpressins is the list of all the expressions
 	 */
 	private ExpressionHouse mExpressions;
 	String mDataFromActivity;
 	FragmentDataPasser mCallback;
 	public static int numberOfOpenParenthesis;
 	public static int numberOfClosedParenthesis;
 	public static int numberOfOperators;
 
 	@Override
 	// we need to inflate our View so let's grab all the View IDs and inflate
 	// them.
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		// we need to make a view instance from our layout.
 		View v = inflater.inflate(R.layout.fragment_calculator_octal,
 				container, false);
 
 		// get the textViews by id, notice we have to reference them via the
 		// view instance we just created.
 		mWorkingTextView = (TextView) v
 				.findViewById(R.id.fragment_calculator_octal_workingTextView);
 
 		// initialize variables that need to be
 		mCurrentWorkingText = new String("");
 		mExpressions = new ExpressionHouse();
 
 		// if the we saved something away, grab it!
 		if (savedInstanceState != null) {
 			mExpressions = (ExpressionHouse) savedInstanceState
 					.getStringArrayList(KEY_WORKINGTEXTVIEW_STRING);
 			// We need to check that we aren't accessing null data or else it
 			// will crash upon turning the screen.
 			// if (mSavedStateString == null) {
 			// mSavedStateString = new String("");
 			// }
 			// set the text to be what we saved away and just now retrieved.
 			mWorkingTextView.setText(mExpressions.printAllExpressions());
 			mCurrentWorkingText = mExpressions.getCurrentExpression();
 		}
 
 		View.OnClickListener genericNumberButtonListener = new View.OnClickListener() {
 			// when someone clicks a button that isn't "special" we are going to
 			// add it to the workingTextView
 			@Override
 			public void onClick(View v) {
 				TextView textView = (TextView) v;
 				// mCurrentWorkingText = mWorkingTextView.getText().toString();
 				String textFromButton = textView.getText().toString();
 
 				if (mCurrentWorkingText.length() == 0) {
 					mWorkingTextView.setText(mWorkingTextView.getText()
 							.toString().concat(textFromButton));
 					mCurrentWorkingText = textFromButton;
 				} else {
 
 					if (mCurrentWorkingText.length() <= 47) {
 
 						StringTokenizer toke = new StringTokenizer(
 								mCurrentWorkingText.concat(textFromButton),
 								"-+/x)( ");
 						String numberLengthTest = null;
 						while (toke.hasMoreTokens()) {
 							numberLengthTest = (String) toke.nextToken();
 						}
 						if (numberLengthTest.length() > 11) {
 							return;
 						}
 
 						// if the last thing inputed was a closedParenthesis
 						// add an implicit 'x' behind the scenes.
 						if (mCurrentWorkingText.endsWith(") ")) {
 
 							mWorkingTextView.setText(mWorkingTextView.getText()
 									.toString().concat(textFromButton));
 							mCurrentWorkingText = mCurrentWorkingText.concat(""
 									+ textFromButton);
 
 							// CalculatorDecimalFragment.numberOfOperators++;
 							// CalculatorOctalFragment.numberOfOperators++;
 							// CalculatorBinaryFragment.numberOfOperators++;
 							// CalculatorHexFragment.numberOfOperators++;
 						} else {
 							// if the working TextView isn't zero we need to
 							// append
 							// the
 							// textFromButton to what is already there.
 							mWorkingTextView.setText(mWorkingTextView.getText()
 									.toString().concat(textFromButton));
 							mCurrentWorkingText = mCurrentWorkingText
 									.concat(textFromButton);
 						}
 					}
 				}
 				Log.d(TAG, "**Number, number of operators: "
 						+ numberOfOperators);
 				mExpressions.updateExpressions(mCurrentWorkingText);
 
 				onPassData(mCurrentWorkingText);
 			}
 		};
 
 		View.OnClickListener genericOperatorButtonListener = new View.OnClickListener() {
 			// when someone clicks an operator "/x+" NOT "-", "-" is special so
 			// it gets it's own listener. We can't have expressions with
 			// adjacent operators "/+x" nor can we start with them.
 			// We also cannot have a "." followed by an operator "+/x"
 			// Nor can we have a "-" followed by an operator.
 			@Override
 			public void onClick(View v) {
 				TextView textView = (TextView) v;
 				// mCurrentWorkingText = mWorkingTextView.getText().toString();
 				String textFromButton = textView.getText().toString();
 				// see if the workingTextView is empty, if so DON'T add the
 				// operator
 				if (mCurrentWorkingText.length() == 0) {
 					// do NOTHING because we can't start an expression with
 					// "+/x" but we can with "-" which is why we are going to
 					// give the minus/negative sign it's own listener.
 				} else {
 
 					if (mCurrentWorkingText.length() <= 47) {
 						// we can't have adjacent "+/x" nor can we have a "."
 						// followed by "+/x"
 						if (mCurrentWorkingText.endsWith("+ ")
 								|| mCurrentWorkingText.endsWith("x ")
 								|| mCurrentWorkingText.endsWith("/ ")
 								|| mCurrentWorkingText.endsWith(".")
 								|| mCurrentWorkingText.endsWith("- ")
 								|| mCurrentWorkingText.endsWith("-")
 								|| mCurrentWorkingText.endsWith("( ")) {
 							// do nothing because we can't have multiple
 							// adjacent
 							// operators
 
 						} else {
 							// we're safe to add the operator to the expression
 
 							if (mCurrentWorkingText.endsWith(" ")) {
 								// if the last char in the currentExpression was
 								// a space then don't add the space at the
 								// beginning, because there will be an extra
 								// space there making it look weird and mess up
 								// the calculations.
 								mWorkingTextView.setText(mWorkingTextView
 										.getText().toString()
 										.concat(textFromButton + " "));
 								mCurrentWorkingText = mCurrentWorkingText
 										.concat(textFromButton + " ");
 
 								// CalculatorDecimalFragment.numberOfOperators++;
 								// CalculatorOctalFragment.numberOfOperators++;
 								// CalculatorBinaryFragment.numberOfOperators++;
 								// CalculatorHexFragment.numberOfOperators++;
 
 							} else {
 								mWorkingTextView.setText(mWorkingTextView
 										.getText().toString()
 										.concat(" " + textFromButton + " "));
 								mCurrentWorkingText = mCurrentWorkingText
 										.concat(" " + textFromButton + " ");
 
 								// CalculatorDecimalFragment.numberOfOperators++;
 								// CalculatorOctalFragment.numberOfOperators++;
 								// CalculatorBinaryFragment.numberOfOperators++;
 								// CalculatorHexFragment.numberOfOperators++;
 							}
 						}
 					}
 				}
 				Log.d(TAG, "**Operator, number of operators: "
 						+ numberOfOperators);
 				mExpressions.updateExpressions(mCurrentWorkingText);
 
 				onPassData(mCurrentWorkingText);
 			}
 		};
 
 		View.OnClickListener openParenthesisButtonListener = new View.OnClickListener() {
 			// We can't have a "." followed by a "("
 			// We also can't have something like this "6)"
 			@Override
 			public void onClick(View v) {
 				TextView textView = (TextView) v;
 				// mCurrentWorkingText = mWorkingTextView.getText().toString();
 				String textFromButton = textView.getText().toString();
 
 				if (mCurrentWorkingText.length() == 0) {
 					// if the first thing is a "(" then don't add the
 					// unnecessary space at the front of it.
 					mWorkingTextView.setText(mWorkingTextView.getText()
 							.toString().concat(textFromButton + " "));
 					mCurrentWorkingText = mCurrentWorkingText
 							.concat(textFromButton + " ");
 
 					CalculatorDecimalFragment.numberOfOpenParenthesis++;
 					CalculatorBinaryFragment.numberOfOpenParenthesis++;
 					CalculatorHexFragment.numberOfOpenParenthesis++;
 					CalculatorOctalFragment.numberOfOpenParenthesis++;
 				} else {
 
 					if (mCurrentWorkingText.endsWith(".")) {
 						// do nothing
 					} else {
 						if (mCurrentWorkingText.length() <= 47) {
 
 							// add an implied 'x' behind the scenes for cases
 							// like this "4 ( 4 )"
 							if (mCurrentWorkingText.length() > 0) {
 								Character isAnumberTest = mCurrentWorkingText
 										.charAt(mCurrentWorkingText.length() - 1);
 								if (isOperand(isAnumberTest.toString())
 										|| mCurrentWorkingText.endsWith(") ")) {
 
 									if (mCurrentWorkingText.endsWith(") ")) {
 										mWorkingTextView
 												.setText(mWorkingTextView
 														.getText()
 														.toString()
 														.concat(" "
 																+ textFromButton
 																+ " "));
 										mCurrentWorkingText = mCurrentWorkingText
 												.concat(" " + textFromButton
 														+ " ");
 									} else {
 										mWorkingTextView
 												.setText(mWorkingTextView
 														.getText()
 														.toString()
 														.concat(" "
 																+ textFromButton
 																+ " "));
 										mCurrentWorkingText = mCurrentWorkingText
 												.concat(" " + textFromButton
 														+ " ");
 									}
 
 									// CalculatorDecimalFragment.numberOfOperators++;
 									// CalculatorBinaryFragment.numberOfOperators++;
 									// CalculatorHexFragment.numberOfOperators++;
 									// CalculatorOctalFragment.numberOfOperators++;
 								} else {
 									mWorkingTextView.setText(mWorkingTextView
 											.getText().toString()
 											.concat(textFromButton + " "));
 									mCurrentWorkingText = mCurrentWorkingText
 											.concat(textFromButton + " ");
 								}
 							} else {
 								mWorkingTextView.setText(mWorkingTextView
 										.getText().toString()
 										.concat(textFromButton + " "));
 								mCurrentWorkingText = mCurrentWorkingText
 										.concat(textFromButton + " ");
 							}
 
 							CalculatorDecimalFragment.numberOfOpenParenthesis++;
 							CalculatorBinaryFragment.numberOfOpenParenthesis++;
 							CalculatorHexFragment.numberOfOpenParenthesis++;
 							CalculatorOctalFragment.numberOfOpenParenthesis++;
 						}
 					}
 
 				}
 				Log.d(TAG, "**OpenParenthesis, number of operators: "
 						+ numberOfOperators);
 				mExpressions.updateExpressions(mCurrentWorkingText);
 
 				onPassData(mCurrentWorkingText);
 			}
 
 		};
 
 		View.OnClickListener closeParenthesisButtonListener = new View.OnClickListener() {
 			// We can't have any of these "./+-x" followed by a ")" nor can we
 			// have something like this "()"
 			// We also can't have something like this "6)" nor something like
 			// "(4x4)9)"
 			@Override
 			public void onClick(View v) {
 				TextView textView = (TextView) v;
 				// mCurrentWorkingText = mWorkingTextView.getText().toString();
 				String textFromButton = textView.getText().toString();
 
 				if (mCurrentWorkingText.length() == 0) {
 					// do nothing we can't start with ")"
 				} else {
 
 					if (mCurrentWorkingText.length() <= 47) {
 						if (((mCurrentWorkingText.endsWith(".")
 								|| mCurrentWorkingText.endsWith("/ ")
 								|| mCurrentWorkingText.endsWith("x ")
 								|| mCurrentWorkingText.endsWith("+ ")
 								|| mCurrentWorkingText.endsWith("- ")
 								|| mCurrentWorkingText.endsWith("-") || mCurrentWorkingText
 									.endsWith("( ")))
 								|| numberOfClosedParenthesis >= numberOfOpenParenthesis) {
 							// do nothing
 						} else {
 
 							mWorkingTextView.setText(mWorkingTextView.getText()
 									.toString()
 									.concat(" " + textFromButton + " "));
 							mCurrentWorkingText = mCurrentWorkingText
 									.concat(" " + textFromButton + " ");
 
 							CalculatorBinaryFragment.numberOfClosedParenthesis++;
 							CalculatorDecimalFragment.numberOfClosedParenthesis++;
 							CalculatorOctalFragment.numberOfClosedParenthesis++;
 							CalculatorHexFragment.numberOfClosedParenthesis++;
 						}
 					}
 				}
 				Log.d(TAG, "**ClosedParenthesis, number of operators: "
 						+ numberOfOperators);
 				mExpressions.updateExpressions(mCurrentWorkingText);
 
 				onPassData(mCurrentWorkingText);
 			}
 		};
 
 		View.OnClickListener genericMinusButtonListener = new View.OnClickListener() {

 			@Override
 			public void onClick(View v) {
 				TextView textView = (TextView) v;
 				// mCurrentWorkingText = mWorkingTextView.getText().toString();
 				String textFromButton = textView.getText().toString();
 				// see if the workingTextView is empty
 				if (mCurrentWorkingText.length() == 0) {
 					mWorkingTextView.setText(mWorkingTextView.getText()
 							.toString().concat(textFromButton));
 					mCurrentWorkingText = textFromButton;
 				} else if (mCurrentWorkingText.length() == 1
 						&& mCurrentWorkingText.endsWith("-")) {
 					// do nothing so we don't start out with something like this
 					// "--2"
 				} else {
 
 					if (mCurrentWorkingText.length() <= 47) {
 						// we can't have more than 2 adjacent '-'. So get the
 						// last
 						// two char's and check if it's "--"
 						if (mCurrentWorkingText.endsWith(".")
 								|| mCurrentWorkingText.endsWith("--")
 								|| mCurrentWorkingText.endsWith("(-")) {
 							// do nothing because we can't have more than 2
 							// adjacent minus's
 						} else {
 							// otherwise, add it to the view
 							if (mCurrentWorkingText.endsWith("0")
 									|| mCurrentWorkingText.endsWith("1")
 									|| mCurrentWorkingText.endsWith("2")
 									|| mCurrentWorkingText.endsWith("3")
 									|| mCurrentWorkingText.endsWith("4")
 									|| mCurrentWorkingText.endsWith("5")
 									|| mCurrentWorkingText.endsWith("6")
 									|| mCurrentWorkingText.endsWith("7")
 									|| mCurrentWorkingText.endsWith("8")
 									|| mCurrentWorkingText.endsWith("9")
 									|| mCurrentWorkingText.endsWith(") ")) {
 
 								// if the last thing was a parenthesis make sure
 								// that we don't add in an extraneous space.
 								if (mCurrentWorkingText.endsWith(") ")) {
 									mWorkingTextView.setText(mWorkingTextView
 											.getText().toString()
 											.concat(textFromButton + " "));
 									mCurrentWorkingText = mCurrentWorkingText
 											.concat(textFromButton + " ");
 								} else {
 									mWorkingTextView
 											.setText(mWorkingTextView
 													.getText()
 													.toString()
 													.concat(" "
 															+ textFromButton
 															+ " "));
 									mCurrentWorkingText = mCurrentWorkingText
 											.concat(" " + textFromButton + " ");
 								}
 							} else {
 								// this represents a negative sign, not a minus
 								// sign
 								mWorkingTextView.setText(mWorkingTextView
 										.getText().toString()
 										.concat(textFromButton));
 								mCurrentWorkingText = mCurrentWorkingText
 										.concat(textFromButton);
 							}
 						}
 					}
 				}
 				// need to pass data to our call back so all fragments can
 				// be
 				// updated with the new workingTextView
 				Log.d(TAG, "**Negative/Minus, number of operators: "
 						+ numberOfOperators);
 				mExpressions.updateExpressions(mCurrentWorkingText);
 
 				onPassData(mCurrentWorkingText);
 			}
 		};
 
 		// logic in here is pretty messy because there are a lot of cases to
 		// check for
 		View.OnClickListener backspaceButtonListener = new View.OnClickListener() {
 			// remove the last thing to be inputed into the workingTextView,
 			// also update the post fix stacks accordingly?
 			@Override
 			public void onClick(View v) {
 				// need to check if the view has anything in it, because if it
 				// doesn't the app will crash when trying to change a null
 				// string.
 				if (mCurrentWorkingText != null) {
 					if (mCurrentWorkingText.length() != 0) {
 
 						if (mCurrentWorkingText.endsWith(") ")) {
 							CalculatorDecimalFragment.numberOfClosedParenthesis--;
 							CalculatorBinaryFragment.numberOfClosedParenthesis--;
 							CalculatorHexFragment.numberOfClosedParenthesis--;
 							CalculatorOctalFragment.numberOfClosedParenthesis--;
 						} else if (mCurrentWorkingText.endsWith("(")
 								|| mCurrentWorkingText.endsWith("( ")
 								|| mCurrentWorkingText.endsWith(" ( ")) {
 							CalculatorDecimalFragment.numberOfOpenParenthesis--;
 							CalculatorBinaryFragment.numberOfOpenParenthesis--;
 							CalculatorHexFragment.numberOfOpenParenthesis--;
 							CalculatorOctalFragment.numberOfOpenParenthesis--;
 						}
 
 						if (mCurrentWorkingText.endsWith(" ( ")) {
 
 							// this deletes the last 2 char's
 							mCurrentWorkingText = mCurrentWorkingText
 									.substring(0,
 											mCurrentWorkingText.length() - 3);
 
 							mWorkingTextView
 									.setText(mWorkingTextView
 											.getText()
 											.toString()
 											.substring(
 													0,
 													mWorkingTextView.length() - 3));
 						} else if (mCurrentWorkingText.endsWith(" + ( ")
 								|| mCurrentWorkingText.endsWith(" - ( ")
 								|| mCurrentWorkingText.endsWith(" x ( ")
 								|| mCurrentWorkingText.endsWith(" / ( ")) {
 
 							// this deletes the last 2 char's
 							mCurrentWorkingText = mCurrentWorkingText
 									.substring(0,
 											mCurrentWorkingText.length() - 2);
 
 							mWorkingTextView
 									.setText(mWorkingTextView
 											.getText()
 											.toString()
 											.substring(
 													0,
 													mWorkingTextView.length() - 2));
 						}
 						// we need to delete the spaces around the operators
 						// also, not just the last char added to the
 						// workingTextView
 						else if (mCurrentWorkingText.endsWith(" + ")
 								|| mCurrentWorkingText.endsWith(" - ")
 								|| mCurrentWorkingText.endsWith(" x ")
 								|| mCurrentWorkingText.endsWith(" / ")
 								|| mCurrentWorkingText.endsWith(") ")
 								|| mCurrentWorkingText.endsWith(" ( ")) {
 							if (mCurrentWorkingText.endsWith(") x ")
 									|| mCurrentWorkingText.endsWith(") + ")
 									|| mCurrentWorkingText.endsWith(") - ")
 									|| mCurrentWorkingText.endsWith(") / ")) {
 
 								mCurrentWorkingText = mCurrentWorkingText
 										.substring(
 												0,
 												mCurrentWorkingText.length() - 2);
 
 								mWorkingTextView.setText(mWorkingTextView
 										.getText()
 										.toString()
 										.substring(0,
 												mWorkingTextView.length() - 2));
 							} else {
 
 								// this deletes the last three char's
 								mCurrentWorkingText = mCurrentWorkingText
 										.substring(
 												0,
 												mCurrentWorkingText.length() - 3);
 
 								mWorkingTextView.setText(mWorkingTextView
 										.getText()
 										.toString()
 										.substring(0,
 												mWorkingTextView.length() - 3));
 							}
 
 							mWorkingTextView.setText(mCurrentWorkingText);
 						} else if (mCurrentWorkingText.endsWith("( ")) {
 							// only delete two chars if the user started
 							// with an
 							// open parenthesis
 							mCurrentWorkingText = mCurrentWorkingText
 									.substring(0,
 											mCurrentWorkingText.length() - 2);
 
 							mWorkingTextView
 									.setText(mWorkingTextView
 											.getText()
 											.toString()
 											.substring(
 													0,
 													mWorkingTextView.length() - 2));
 
 						} else {
 							mCurrentWorkingText = mCurrentWorkingText
 									.substring(0,
 											mCurrentWorkingText.length() - 1);
 
 							mWorkingTextView
 									.setText(mWorkingTextView
 											.getText()
 											.toString()
 											.substring(
 													0,
 													mWorkingTextView.length() - 1));
 						}
 					} else {
 						return;
 					}
 				}
 				Log.d(TAG, "**Backspace, number of operators: "
 						+ numberOfOperators);
 				mExpressions.updateExpressions(mCurrentWorkingText);
 
 				onPassData(mCurrentWorkingText);
 			}
 		};
 
 		// get a reference to our TableLayout XML
 		TableLayout tableLayout = (TableLayout) v
 				.findViewById(R.id.fragment_calculator_octal_tableLayout);
 
 		// get a reference to the first (topmost) row so we can set the clear
 		// all button manually, because it was annoying trying to work it in to
 		// the for loop
 		TableRow firstRow = (TableRow) tableLayout.getChildAt(0);
 		// the clear all button was decided to be the third button in the
 		// topmost row
 
 		Button openParBut = (Button) firstRow.getChildAt(0);
 		openParBut.setText("(");
 		openParBut.setOnClickListener(openParenthesisButtonListener);
 
 		Button closeParBut = (Button) firstRow.getChildAt(1);
 		closeParBut.setText(")");
 		closeParBut.setOnClickListener(closeParenthesisButtonListener);
 
 		Button clearAllButton = (Button) firstRow.getChildAt(2);
 		clearAllButton.setText("AC");
 		clearAllButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// clear all the text in the working textView, AND maybe the
 				// computed textView as well?
 				// Also, might want to clear out the post fix expression stack
 				mWorkingTextView.setText("");
 				mCurrentWorkingText = new String("");
 				mExpressions.clearAllExpressions();
 				// update the Static variable in our activity so we can use it
 				// as a fragment argument
 				// mComputeTextView.setText("");
 
 				CalculatorDecimalFragment.numberOfOpenParenthesis = 0;
 				CalculatorBinaryFragment.numberOfOpenParenthesis = 0;
 				CalculatorHexFragment.numberOfOpenParenthesis = 0;
 				CalculatorOctalFragment.numberOfOpenParenthesis = 0;
 
 				CalculatorDecimalFragment.numberOfClosedParenthesis = 0;
 				CalculatorBinaryFragment.numberOfClosedParenthesis = 0;
 				CalculatorHexFragment.numberOfClosedParenthesis = 0;
 				CalculatorOctalFragment.numberOfClosedParenthesis = 0;
 
 				CalculatorDecimalFragment.numberOfOperators = 0;
 				CalculatorBinaryFragment.numberOfOperators = 0;
 				CalculatorHexFragment.numberOfOperators = 0;
 				CalculatorOctalFragment.numberOfOperators = 0;
 
 				onPassData(mCurrentWorkingText);
 			}
 		});
 
 		ImageButton backspaceButton = (ImageButton) firstRow.getChildAt(3);
 		backspaceButton.setOnClickListener(backspaceButtonListener);
 
 		// get a reference to the second row of the table (AND, OR, NAND)
 		TableRow secondRow = (TableRow) tableLayout.getChildAt(1);
 
 		Button sevenButton = (Button) secondRow.getChildAt(0);
 		sevenButton.setText("7");
 		sevenButton.setOnClickListener(genericNumberButtonListener);
 
 		Button blankButton2 = (Button) secondRow.getChildAt(1);
 		blankButton2.setText(null);
 		blankButton2.setOnClickListener(null);
 		// blankButton2.setClickable(false); //I tried to make the button not
 		// even clickable but when i would click the unclickable button it would
 		// put garbage in the textView.... so I left it in the code as a warner
 		// not to do this.
 
 		Button blankButton = (Button) secondRow.getChildAt(2);
 		blankButton.setText(null);
 		blankButton.setOnClickListener(null);
 		// blankButton.setClickable(false); //I tried to make the button not
 		// even clickable but when i would click the unclickable button it would
 		// put garbage in the textView.... so I left it in the code as a warner
 		// not to do this.
 
 		Button divideButton = (Button) secondRow.getChildAt(3);
 		divideButton.setText("/");
 		divideButton.setOnClickListener(genericOperatorButtonListener);
 
 		// get a reference to the third row (NOR, XOR, XNOR)
 		TableRow thirdRow = (TableRow) tableLayout.getChildAt(2);
 		// the NOR button
 		Button fourButton = (Button) thirdRow.getChildAt(0);
 		fourButton.setText("4");
 		fourButton.setOnClickListener(genericNumberButtonListener);
 		// XOR button
 		Button fiveButton = (Button) thirdRow.getChildAt(1);
 		fiveButton.setText("5");
 		fiveButton.setOnClickListener(genericNumberButtonListener);
 		// XNOR button
 		Button sixButton = (Button) thirdRow.getChildAt(2);
 		sixButton.setText("6");
 		sixButton.setOnClickListener(genericNumberButtonListener);
 
 		Button multButton = (Button) thirdRow.getChildAt(3);
 		multButton.setText("x");
 		multButton.setOnClickListener(genericOperatorButtonListener);
 
 		// fourth row (1, <<, >>)
 		TableRow fourthRow = (TableRow) tableLayout.getChildAt(3);
 		// button '1'
 		Button oneButton = (Button) fourthRow.getChildAt(0);
 		oneButton.setText("1");
 		oneButton.setOnClickListener(genericNumberButtonListener);
 		// bitwise shift Left button
 		Button twoButton = (Button) fourthRow.getChildAt(1);
 		twoButton.setText("2");
 		twoButton.setOnClickListener(genericNumberButtonListener);
 		// bitwise shift Right button
 		Button threeButton = (Button) fourthRow.getChildAt(2);
 		threeButton.setText("3");
 		threeButton.setOnClickListener(genericNumberButtonListener);
 
 		Button minusButt = (Button) fourthRow.getChildAt(3);
 		minusButt.setText("-");
 		minusButt.setOnClickListener(genericMinusButtonListener);
 
 		// now we need to get the last row of buttons and get them to the
 		// screen.
 		TableRow lastRow = (TableRow) tableLayout.getChildAt(tableLayout
 				.getChildCount() - 1);
 		// set the decimal button
 		Button zeroButton = (Button) lastRow.getChildAt(2);
 		zeroButton.setText(".");
 		zeroButton.setOnClickListener(new OnClickListener() {
 			// we can't put a "." up there if there has already been one in
 			// the current token (number)
 			@Override
 			public void onClick(View v) {
 				TextView textView = (TextView) v;
 				// mCurrentWorkingText = mWorkingTextView.getText().toString();
 				String textFromButton = textView.getText().toString();
 
 				// see if the workingTextView is empty, if so just add the '.'
 				if (mCurrentWorkingText.length() == 0) {
 
 					mWorkingTextView.setText(mWorkingTextView.getText()
 							.toString().concat(textFromButton));
 					mCurrentWorkingText = textFromButton;
 
 				} else {
 
 					if (mCurrentWorkingText.length() <= 47) {
 						StringTokenizer toke = new StringTokenizer(
 								mCurrentWorkingText, "+-/x)(", true);
 						String currentElement = null;
 						// get the current(last) token(number) so we can test if
 						// it
 						// has a '.' in it.
 						while (toke.hasMoreTokens()) {
 							currentElement = toke.nextElement().toString();
 						}
 						// if the working TextView isn't zero we need to append
 						// the
 						// textFromButton to what is already there. AND we need
 						// to
 						// check if the current token already has a '.' in it
 						// because we can't have something like '2..2' or
 						// 2.2.33'
 						if (mCurrentWorkingText.endsWith(".")
 								|| currentElement.contains(".")) {
 							// do nothing here so we don't end up with
 							// expressions
 							// like "2..2" or "2.3.22"
 						} else {
 							// otherwise we're all good and just add the ".' up
 							// there.
 							mWorkingTextView.setText(mWorkingTextView.getText()
 									.toString().concat(textFromButton));
 							mCurrentWorkingText = mCurrentWorkingText
 									.concat(textFromButton);
 						}
 					}
 				}
 				// need to pass data to our call back so all fragments can
 				// be
 				// updated with the new workingTextView
 				mExpressions.updateExpressions(mCurrentWorkingText);
 
 				onPassData(mCurrentWorkingText);
 			}
 		});
 		// set the zero button
 		Button decimalPointButton = (Button) lastRow.getChildAt(1);
 		decimalPointButton.setText("0");
 		decimalPointButton.setOnClickListener(genericNumberButtonListener);
 		// set the plus button
 		Button plusButton = (Button) lastRow.getChildAt(3);
 		plusButton.setText("+");
 		plusButton.setOnClickListener(genericOperatorButtonListener);
 		// set the equals button, it will have it's own separate listener to
 		// compute the inputed value
 		Button equalsButton = (Button) lastRow.getChildAt(0);
 		equalsButton.setText("=");
 		equalsButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				if (mCurrentWorkingText.endsWith("-")) {
 					Toast.makeText(getSherlockActivity(),
 							"That is not a valid expression.",
 							Toast.LENGTH_SHORT).show();
 					return;
 				}
 
 				// need to convert the mCurrentWorkingText (the current
 				// expression) to base10 before we do any evaluations.
 				StringTokenizer toke = new StringTokenizer(mCurrentWorkingText,
 						"x+-/)( \n", true);
 				StringBuilder builder = new StringBuilder();
 
 				while (toke.hasMoreElements()) {
 					String aToken = (String) toke.nextElement().toString();
 					if (aToken.equals("+") || aToken.equals("x")
 							|| aToken.equals("-") || aToken.equals("/")
 							|| aToken.equals("(") || aToken.equals(")")
 							|| aToken.equals(" ") || aToken.equals("\n")) {
 
 						builder.append(aToken);
 
 					}
 					// if our token contains a "." in it then that means that we
 					// need to do some conversion trickery
 					else if (aToken.contains(".")) {
 						if (aToken.endsWith(".")) {
 							// don't do anything if a token ends with "." we
 							// don't want cases like ".5 + 5."
 							return;
 						}
 						// split the string around the "." delimiter.
 						String[] parts = aToken.split("\\.");
 						StringBuilder tempBuilder = new StringBuilder();
 
 						if (aToken.charAt(0) == '.') {
 							// so it doesn't break on cases like ".5"
 						} else {
 							// add the portion of the number to the left of the
 							// "."
 							// to our string, this doesn't need any conversion
 							// nonsense because it is a whole number.
 							tempBuilder.append(Integer.toString(Integer
 									.parseInt(parts[0], VIEWS_RADIX)));
 						}
 						// convert the fraction portion
 						String getRidOfZeroBeforePoint = null;
 
 						// convert just the fraction portion of the number to
 						// base10. This method doesn't take in the "." with the
 						// fraction.
 						getRidOfZeroBeforePoint = Fractions
 								.convertFractionPortionToDecimal(parts[1],
 										VIEWS_RADIX);
 
 						// the conversion returns just the fraction
 						// portion
 						// with
 						// a "0" to the left of the ".", so let's get
 						// rid of
 						// that extra zero.
 						getRidOfZeroBeforePoint = getRidOfZeroBeforePoint
 								.substring(1, getRidOfZeroBeforePoint.length());
 
 						tempBuilder.append(getRidOfZeroBeforePoint);
 
 						builder.append(tempBuilder.toString());
 					}// closes the "." case
 					else {
 						// if it's just a regular good ol' fashioned whole
 						// number, use java's parseInt method to convert to
 						// base10
 						builder.append(Integer.parseInt(aToken, VIEWS_RADIX));
 					}
 				} // closes while() loop
 
 				// /Now convert the base10 expression into post-fix
 				String postfix = InfixToPostfix.convertToPostfix(builder
 						.toString());
 				Log.d(TAG, "**Infix: " + builder.toString() + " Postfix: "
 						+ postfix);
 
 				// tokenize to see if the expression is in fact a valid
 				// expression, i.e contains an operator, contains the correct
 				// operand to operator ratio
 				StringTokenizer toker = new StringTokenizer(
 						mCurrentWorkingText, "+-/x )(");
 				Log.d(TAG, "Number of operands: " + toker.countTokens()
 						+ " NumberOfOperators: " + numberOfOperators);
 				// the number of operators should be one less than the number of
 				// operands/tokens
 				if ((numberOfOperators != toker.countTokens() - 1)
 						|| numberOfOperators == 0) {
 					Toast.makeText(getSherlockActivity(),
 							"That is not a valid expression.",
 							Toast.LENGTH_SHORT).show();
 					return;
 				}
 
 				String theAnswerInDecimal = null;
 				if (postfix != null && postfix.length() > 0) {
 					if (!(postfix.contains("+") || postfix.contains("-")
 							|| postfix.contains("x") || postfix.contains("/"))) {
 						// don't evaluate if there is an expression with no
 						// operators
 						Toast.makeText(getSherlockActivity(),
 								"There are no operators in the expression.",
 								Toast.LENGTH_LONG).show();
 						return;
 					} else if (numberOfOpenParenthesis != numberOfClosedParenthesis) {
 						// don't evaluate if the number of closed and open
 						// parenthesis aren't equal.
 						Toast.makeText(
 								getSherlockActivity(),
 								"The number of close parentheses is not equal to the number of open parentheses.",
 								Toast.LENGTH_LONG).show();
 						return;
 					}
 					// Do the evaluation if it's safe to.
 					theAnswerInDecimal = PostfixEvaluator.evaluate(postfix);
 				} else {
 					// don't evaluate if the expression is null or empty
 					Toast.makeText(getSherlockActivity(),
 							"The expression is empty.", Toast.LENGTH_LONG)
 							.show();
 					return;
 				}
 
 				Log.d(TAG, "**Postfix: " + postfix + " AnswerInDecimal: "
 						+ theAnswerInDecimal);
 
 				String[] answerParts = theAnswerInDecimal.split("\\.");
 				StringBuilder answerInCorrectBase = null;
 				if (answerParts[0].contains("-")) {
 					String[] parseOutNegativeSign = answerParts[0].split("-");
 					answerInCorrectBase = new StringBuilder(Integer
 							.toOctalString(Integer
 									.parseInt(parseOutNegativeSign[1])));
 
 					answerInCorrectBase.insert(0, "-");
 
 				} else {
 					answerInCorrectBase = new StringBuilder(Integer
 							.toOctalString(Integer.parseInt(answerParts[0])));
 				}
 
 				String fractionPart = null;
 
 				fractionPart = Fractions.convertFractionPortionFromDecimal("."
 						+ answerParts[1], VIEWS_RADIX);
 
 				if (!fractionPart.equals("")) {
 					answerInCorrectBase.append("." + fractionPart);
 				}
 
 				String answer = "\n" + answerInCorrectBase.toString() + "\n";
 
 				mWorkingTextView.setText(mWorkingTextView.getText().toString()
 						.concat(answer));
 
 				mExpressions.updateExpressions(answer);
 				onPassData(answer);
 
 				mCurrentWorkingText = new String("");
 
 				CalculatorDecimalFragment.numberOfOpenParenthesis = 0;
 				CalculatorBinaryFragment.numberOfOpenParenthesis = 0;
 				CalculatorHexFragment.numberOfOpenParenthesis = 0;
 				CalculatorOctalFragment.numberOfOpenParenthesis = 0;
 
 				CalculatorDecimalFragment.numberOfClosedParenthesis = 0;
 				CalculatorBinaryFragment.numberOfClosedParenthesis = 0;
 				CalculatorHexFragment.numberOfClosedParenthesis = 0;
 				CalculatorOctalFragment.numberOfClosedParenthesis = 0;
 
 				CalculatorDecimalFragment.numberOfOperators = 0;
 				CalculatorBinaryFragment.numberOfOperators = 0;
 				CalculatorHexFragment.numberOfOperators = 0;
 				CalculatorOctalFragment.numberOfOperators = 0;
 			}
 		});
 
 		return v;
 	}
 
 	public static SherlockFragment newInstance() {
 		CalculatorOctalFragment binFrag = new CalculatorOctalFragment();
 		return binFrag;
 	}
 
 	// method to save the state of the application during the activity life
 	// cycle. This is so we can preserve the values in the textViews upon screen
 	// rotation.
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		// Log.i(TAG, "onSaveInstanceState");
 		// outState.putString(KEY_WORKINGTEXTVIEW_STRING, mSavedStateString);
 		outState.putStringArrayList(KEY_WORKINGTEXTVIEW_STRING, mExpressions);
 	}
 
 	// need to make sure the fragment life cycle complies with the
 	// actionBarSherlock support library
 	@Override
 	public void onAttach(Activity activity) {
 		if (!(activity instanceof SherlockFragmentActivity)) {
 			throw new IllegalStateException(getClass().getSimpleName()
 					+ " must be attached to a SherlockFragmentActivity.");
 		}
 		super.onAttach(activity);
 		// set our dataPasser interface up when the fragment is on the activity
 		try {
 			// hook the call back up to the activity it is attached to, should
 			// do this in a try/catch because the parent activity must implement
 			// the interface.
 			mCallback = (FragmentDataPasser) activity;
 		} catch (ClassCastException e) {
 			throw new ClassCastException(
 					activity.toString()
 							+ " must implement the FragmentDataPasser interface so we can pass data between the fragments.");
 		}
 	}
 
 	// callback method to send data to the activity so we can then update all
 	// the fragments
 	public void onPassData(String dataToBePassed) {
 		mCallback.onDataPassed(dataToBePassed, VIEW_NUMBER, VIEWS_RADIX);
 	}
 
 	// method to receive the data from the activity/other-fragments and update
 	// the textViews accordingly
 	public void updateWorkingTextView(String dataToBePassed, int base) {
 		if (dataToBePassed.length() != 0) {
 
 			if (dataToBePassed.contains("A") || dataToBePassed.contains("O")) {
 
 				return;
 			}
 
 			StringTokenizer toke = new StringTokenizer(dataToBePassed,
 					"x+-/)( \n", true);
 			StringBuilder builder = new StringBuilder();
 
 			while (toke.hasMoreElements()) {
 				String aToken = (String) toke.nextElement().toString();
 				if (aToken.equals("+") || aToken.equals("x")
 						|| aToken.equals("-") || aToken.equals("/")
 						|| aToken.equals("(") || aToken.equals(")")
 						|| aToken.equals(" ") || aToken.equals("\n")) {
 
 					builder.append(aToken);
 
 				}
 				// if our token contains a "." in it then that means that we
 				// need to do some conversion trickery
 				else if (aToken.contains(".")) {
 					if (aToken.endsWith(".")) {
 						// don't do any conversions when the number is still
 						// being
 						// inputed and in the current state of something like
 						// this
 						// "5."
 						return;
 					}
 					// split the string around the "." delimiter.
 					String[] parts = aToken.split("\\.");
 					StringBuilder tempBuilder = new StringBuilder();
 
 					if (aToken.charAt(0) == '.') {
 
 					} else {
 
 						// add the portion of the number to the left of the
 						// "."
 						// to our string this doesn't need any conversion
 						// nonsense.
 						tempBuilder.append(Integer.toOctalString(Integer
 								.parseInt(parts[0], base)));
 					}
 					// convert the fraction portion
 					String getRidOfZeroBeforePoint = null;
 
 					if (base == 10) {
 						String fractionWithRadixPoint = "." + parts[1];
 						String converted = Fractions
 								.convertFractionPortionFromDecimal(
 										fractionWithRadixPoint, VIEWS_RADIX);
 						parts = converted.split("\\.");
 						tempBuilder.append(".").append(parts[0]);
 					} else {
 
 						getRidOfZeroBeforePoint = Fractions
 								.convertFractionPortionToDecimal(parts[1], base);
 
 						// the conversion returns just the fraction
 						// portion
 						// with
 						// a "0" to the left of the ".", so let's get
 						// rid of
 						// that extra zero.
 						getRidOfZeroBeforePoint = getRidOfZeroBeforePoint
 								.substring(1, getRidOfZeroBeforePoint.length());
 						String partsAgain[] = getRidOfZeroBeforePoint
 								.split("\\.");
 
 						String converted = Fractions
 								.convertFractionPortionFromDecimal(
 										getRidOfZeroBeforePoint, VIEWS_RADIX);
 						partsAgain = converted.split("\\.");
 						tempBuilder.append(".").append(partsAgain[0]);
 					}
 
 					// add that to the string that gets put on the textView
 					// (this may be excessive) (I wrote this late at night
 					// so stuff probably got a little weird)
 					builder.append(tempBuilder.toString());
 
 				} else {
 					BigInteger sizeTestBigInt = new BigInteger(aToken, base);
 					if (sizeTestBigInt.bitLength() < 64) {
 						mCurrentWorkingText = Long.toOctalString(Long
 								.parseLong(aToken, base));
 						builder.append(mCurrentWorkingText);
 					}
 				}
 				mCurrentWorkingText = builder.toString();
 			}
 			mExpressions.updateExpressions(mCurrentWorkingText);
 			if (mCurrentWorkingText.contains("\n")) {
 				mCurrentWorkingText = new String("");
 			}
 			mWorkingTextView.setText(mExpressions.printAllExpressions());
 		} else {
 			mExpressions.clearAllExpressions();
 			mCurrentWorkingText = "";
 			mWorkingTextView.setText(mCurrentWorkingText);
 		}
 	}
 
 	// method to tell us if a string is a number or not
 	public static boolean isOperand(String s) {
 		double a = 0;
 		try {
 			a = Double.parseDouble(s);
 		} catch (Exception ignore) {
 			return false;
 		}
 		return true;
 	}
 }
