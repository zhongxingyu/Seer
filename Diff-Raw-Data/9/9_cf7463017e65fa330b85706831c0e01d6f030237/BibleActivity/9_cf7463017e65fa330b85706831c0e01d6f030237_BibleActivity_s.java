 package com.sacred.bible;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.SAXException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.view.GestureDetector;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.google.ads.Ad;
 import com.google.ads.AdListener;
 import com.google.ads.AdRequest;
 import com.google.ads.AdRequest.ErrorCode;
 import com.google.ads.AdView;
 import com.sacred.bible.util.Util;
 
 public class BibleActivity extends Activity implements BgDataHandler,
 		AdListener {
 	/** Called when the activity is first created. */
 	private ImageView next;
 	private ImageView prev;
 	private TextView chapter_number;
 	private TextView book_name;
 	private TextView selected_book_name;
 	private TextView selected_chapter_number;
 	private ListView content;
 	private ContentAdapter contentAdapter;
 	private int chapterNumber = 0;
 	private ArrayList<ArrayList<String>> bibleData;
 	private AdView adView;
 	private String[] booksArray;
 	private String[] booksArray1;
 	private boolean testamentOld;
 	private int currentBookNumber;
 	private GestureDetector gestureDetector;
 	View.OnTouchListener gestureListener;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		next = (ImageView) findViewById(R.id.nextBook);
 		prev = (ImageView) findViewById(R.id.prevBook);
 		book_name = (TextView) findViewById(R.id.book_name);
 		chapter_number = (TextView) findViewById(R.id.chapter_number);
 		selected_book_name = (TextView) findViewById(R.id.selected_book_name);
 		selected_chapter_number = (TextView) findViewById(R.id.selected_chapter_number);
 		content = (ListView) findViewById(R.id.bibleContent);
 		Util util = new Util();
 		booksArray = util.getBookArray();
		booksArray1 = util.getBookArray1();
		if (isOnline()) {
			adView.loadAd(new AdRequest());
		}
 		adView = (AdView) BibleActivity.this.findViewById(R.id.adView);
 		LinearLayout layout = (LinearLayout) findViewById(R.id.adLayout);
 		layout.removeAllViews();
 		layout.addView(adView);
 		gestureDetector = new GestureDetector(new MyGestureDetector());
 		gestureListener = new View.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
 				if (gestureDetector.onTouchEvent(event)) {
 					return true;
 				}
 				return false;
 			}
 		};
 
 		testamentOld = true;
 		currentBookNumber = 0;
 		parseFile("testament1/book1.txt");
 		selected_book_name.setText(booksArray[currentBookNumber] + ":");
 		selected_chapter_number.setText(""
 				+ String.valueOf((chapterNumber + 1)));
 		book_name.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				View bookListView = LayoutInflater.from(BibleActivity.this)
 						.inflate(R.layout.booklist, null);
 				final AlertDialog dialog = new AlertDialog.Builder(
 						BibleActivity.this).create();
 				dialog.setView(bookListView, 0, 0, 0, 0);
 				final ListView listView = (ListView) bookListView
 						.findViewById(R.id.booklist);
 				if (testamentOld) {
 					BookListAdapter books = new BookListAdapter(
 							BibleActivity.this, booksArray);
 					listView.setAdapter(books);
 					listView.setOnItemClickListener(new OnItemClickListener() {
 						@Override
 						public void onItemClick(AdapterView<?> arg0, View arg1,
 								int arg2, long arg3) {
 							if (currentBookNumber != arg2) {
 								currentBookNumber = arg2;
 								chapterNumber = 0;
 								parseFile("testament1/book" + (arg2 + 1)
 										+ ".txt");
 								selected_book_name.setText(booksArray[arg2]
 										+ ":");
 								selected_chapter_number.setText(String
 										.valueOf((1)));
 							}
 							dialog.dismiss();
 						}
 					});
 				} else {
 					BookListAdapter books = new BookListAdapter(
 							BibleActivity.this, booksArray1);
 					listView.setAdapter(books);
 					listView.setOnItemClickListener(new OnItemClickListener() {
 						@Override
 						public void onItemClick(AdapterView<?> arg0, View arg1,
 								int arg2, long arg3) {
 							if (currentBookNumber != arg2) {
 								currentBookNumber = arg2;
 								chapterNumber = 0;
 								parseFile("testament2/book" + (arg2 + 1)
 										+ ".txt");
 								selected_book_name.setText(booksArray1[arg2]
 										+ ":");
 								selected_chapter_number.setText(String
 										.valueOf((1)));
 							}
 							dialog.dismiss();
 						}
 					});
 				}
 				dialog.show();
 			}
 		});
 
 		chapter_number.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				View bookListView = LayoutInflater.from(BibleActivity.this)
 						.inflate(R.layout.booklist, null);
 				final AlertDialog dialog = new AlertDialog.Builder(
 						BibleActivity.this).create();
 				dialog.setView(bookListView, 0, 0, 0, 0);
 				final ListView listView = (ListView) bookListView
 						.findViewById(R.id.booklist);
 				if (testamentOld) {
 					ChapterAdapter chapters = new ChapterAdapter(
 							BibleActivity.this, bibleData.size());
 					listView.setAdapter(chapters);
 					listView.setOnItemClickListener(new OnItemClickListener() {
 						@Override
 						public void onItemClick(AdapterView<?> arg0, View arg1,
 								int arg2, long arg3) {
 							chapterNumber = arg2;
 							onChapterSelection(true);
 							dialog.dismiss();
 						}
 					});
 				} else {
 					ChapterAdapter chapters = new ChapterAdapter(
 							BibleActivity.this, bibleData.size());
 					listView.setAdapter(chapters);
 					listView.setOnItemClickListener(new OnItemClickListener() {
 						@Override
 						public void onItemClick(AdapterView<?> arg0, View arg1,
 								int arg2, long arg3) {
 							chapterNumber = arg2;
 							onChapterSelection(false);
 							dialog.dismiss();
 						}
 					});
 				}
 				dialog.show();
 			}
 		});
 		next.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (chapterNumber >= bibleData.size() - 1) {
 
 				} else if (chapterNumber == bibleData.size() - 2) {
 					chapterNumber++;
 					prev.setVisibility(View.VISIBLE);
 					next.setVisibility(View.INVISIBLE);
 					changeChapter();
 				} else {
 					chapterNumber++;
 					prev.setVisibility(View.VISIBLE);
 					changeChapter();
 				}
 			}
 		});
 
 		prev.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (chapterNumber == 1) {
 					chapterNumber--;
 					prev.setVisibility(View.INVISIBLE);
 					next.setVisibility(View.VISIBLE);
 					changeChapter();
 				} else if (chapterNumber > 1) {
 					chapterNumber--;
 					prev.setVisibility(View.VISIBLE);
 					next.setVisibility(View.VISIBLE);
 					changeChapter();
 				}
 			}
 		});
 
 		content.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				Intent intent = new Intent(BibleActivity.this,
 						VerseActivity.class);
 				intent.putExtra("content", bibleData.get(chapterNumber));
 				intent.putExtra("clickedpostion", arg2);
 				intent.putExtra("currentbookposition", currentBookNumber);
 				intent.putExtra("currentchapterposition", chapterNumber);
 				intent.putExtra("testamentValue", testamentOld);
 				startActivity(intent);
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		super.onOptionsItemSelected(item);
 		switch (item.getItemId()) {
 		case R.id.testament:
 			AlertDialog.Builder builder = new AlertDialog.Builder(
 					BibleActivity.this);
 
 			builder.setMessage("Choose Testament")
 					.setCancelable(true)
 					.setPositiveButton("Old",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									if (!testamentOld) {
 										testamentOld = true;
 										currentBookNumber = 0;
 										chapterNumber = 0;
 										parseFile("testament1/book1.txt");
 										selected_book_name
 												.setText(booksArray[0] + ":");
 										selected_chapter_number.setText(String
 												.valueOf(1));
 									}
 									dialog.dismiss();
 								}
 							})
 					.setNegativeButton("New",
 							new DialogInterface.OnClickListener() {
 
 								public void onClick(DialogInterface dialog,
 										int id) {
 									if (testamentOld) {
 										testamentOld = false;
 										currentBookNumber = 0;
 										chapterNumber = 0;
 										parseFile("testament2/book1.txt");
 										selected_book_name
 												.setText(booksArray1[0] + ":");
 										selected_chapter_number.setText(String
 												.valueOf(1));
 									}
 									dialog.dismiss();
 								}
 							});
 
 			AlertDialog alert = builder.create();
 
 			alert.show();
 			break;
 		case R.id.books:
 			View bookListView = LayoutInflater.from(BibleActivity.this)
 					.inflate(R.layout.booklist, null);
 			final AlertDialog dialog = new AlertDialog.Builder(
 					BibleActivity.this).create();
 			dialog.setView(bookListView, 0, 0, 0, 0);
 			final ListView listView = (ListView) bookListView
 					.findViewById(R.id.booklist);
 			if (testamentOld) {
 				BookListAdapter books = new BookListAdapter(BibleActivity.this,
 						booksArray);
 				listView.setAdapter(books);
 				listView.setOnItemClickListener(new OnItemClickListener() {
 					@Override
 					public void onItemClick(AdapterView<?> arg0, View arg1,
 							int arg2, long arg3) {
 						if (currentBookNumber != arg2) {
 							currentBookNumber = arg2;
 							chapterNumber = 0;
 							parseFile("testament1/book" + (arg2 + 1) + ".txt");
 							selected_book_name.setText(booksArray[arg2] + ":");
 							selected_chapter_number.setText(String.valueOf(1));
 						}
 						dialog.dismiss();
 					}
 				});
 			} else {
 				BookListAdapter books = new BookListAdapter(BibleActivity.this,
 						booksArray1);
 				listView.setAdapter(books);
 				listView.setOnItemClickListener(new OnItemClickListener() {
 					@Override
 					public void onItemClick(AdapterView<?> arg0, View arg1,
 							int arg2, long arg3) {
 						if (currentBookNumber != arg2) {
 							currentBookNumber = arg2;
 							chapterNumber = 0;
 							parseFile("testament2/book" + (arg2 + 1) + ".txt");
 							selected_book_name.setText(booksArray1[arg2] + ":");
 							selected_chapter_number.setText(String.valueOf(1));
 						}
 						dialog.dismiss();
 					}
 				});
 			}
 			dialog.show();
 			break;
 		}
 		return true;
 	}
 
 	private void changeChapter() {
 		contentAdapter = new ContentAdapter(BibleActivity.this,
 				bibleData.get(chapterNumber));
 		content.setAdapter(contentAdapter);
 		contentAdapter.dataChange();
 		if (testamentOld) {
 			selected_book_name.setText(booksArray[currentBookNumber] + ":");
 			selected_chapter_number
 					.setText(String.valueOf((chapterNumber + 1)));
 		} else {
 			selected_book_name.setText(booksArray1[currentBookNumber] + ":");
 			selected_chapter_number
 					.setText(String.valueOf((chapterNumber + 1)));
 		}
 	}
 
 	private void onChapterSelection(boolean isOld) {
 		if (chapterNumber == 0) {
 			contentAdapter = new ContentAdapter(BibleActivity.this,
 					bibleData.get(chapterNumber));
 			content.setAdapter(contentAdapter);
 			contentAdapter.dataChange();
 			prev.setVisibility(View.INVISIBLE);
 			next.setVisibility(View.VISIBLE);
 			if (isOld) {
 				selected_book_name
 						.setText(booksArray1[currentBookNumber] + ":");
 			} else {
 				selected_book_name.setText(booksArray[currentBookNumber] + ":");
 			}
 			selected_chapter_number
 					.setText(String.valueOf((chapterNumber + 1)));
 		} else if (chapterNumber == bibleData.size() - 1) {
 			contentAdapter = new ContentAdapter(BibleActivity.this,
 					bibleData.get(chapterNumber));
 			content.setAdapter(contentAdapter);
 			contentAdapter.dataChange();
 			next.setVisibility(View.INVISIBLE);
 			prev.setVisibility(View.VISIBLE);
 			if (isOld) {
 				selected_book_name
 						.setText(booksArray1[currentBookNumber] + ":");
 			} else {
 				selected_book_name.setText(booksArray[currentBookNumber] + ":");
 			}
 			selected_chapter_number
 					.setText(String.valueOf((chapterNumber + 1)));
 
 		} else {
 			contentAdapter = new ContentAdapter(BibleActivity.this,
 					bibleData.get(chapterNumber));
 			content.setAdapter(contentAdapter);
 			contentAdapter.dataChange();
 			next.setVisibility(View.VISIBLE);
 			prev.setVisibility(View.VISIBLE);
 			if (isOld) {
 				selected_book_name
 						.setText(booksArray1[currentBookNumber] + ":");
 			} else {
 				selected_book_name.setText(booksArray[currentBookNumber] + ":");
 			}
 			selected_chapter_number
 					.setText(String.valueOf((chapterNumber + 1)));
 
 		}
 	}
 
 	private void parseFile(String fileName) {
 		InputStream iS = null;
 		try {
 			iS = getResources().getAssets().open(fileName);
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		SAXParserFactory factory = SAXParserFactory.newInstance();
 		try {
 			SAXParser parser = factory.newSAXParser();
 			if (iS != null)
 				parser.parse(iS, new SaxHelper(BibleActivity.this));
 
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void hanldeData(ArrayList<ArrayList<String>> bookData) {
 		if (bookData != null) {
 			bibleData = bookData;
 			contentAdapter = new ContentAdapter(BibleActivity.this,
 					bibleData.get(0));
 			content.setAdapter(contentAdapter);
 			prev.setVisibility(View.INVISIBLE);
 		}
 	}
 
 	@Override
 	public void onDismissScreen(Ad arg0) {
 
 	}
 
 	@Override
 	public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
 		adView.stopLoading();
 	}
 
 	@Override
 	public void onLeaveApplication(Ad arg0) {
 		adView.stopLoading();
 	}
 
 	@Override
 	public void onPresentScreen(Ad arg0) {
 	}
 
 	@Override
 	public void onReceiveAd(Ad arg0) {
 	}
 
 	private boolean isOnline() {
 		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		if (cm == null) {
 			return false;
 		}
 		NetworkInfo ni = cm.getActiveNetworkInfo();
 		if (ni != null) {
 			return ni.isConnectedOrConnecting();
 		} else {
 			return false;
 		}
 	}	
 
 	class MyGestureDetector extends SimpleOnGestureListener {
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 				float velocityY) {
 			try {
 				if (Math.abs(e1.getY() - e2.getY()) > 250)
 					return false;
 				// right to left swipe
 				if (e1.getX() - e2.getX() > 120 && Math.abs(velocityX) > 200) {
 					if (chapterNumber >= bibleData.size() - 1) {
 
 					} else if (chapterNumber == bibleData.size() - 2) {
 						chapterNumber++;
 						prev.setVisibility(View.VISIBLE);
 						next.setVisibility(View.INVISIBLE);
 						changeChapter();
 					} else {
 						chapterNumber++;
 						prev.setVisibility(View.VISIBLE);
 						changeChapter();
 					}
 				} else if (e2.getX() - e1.getX() > 120
 						&& Math.abs(velocityX) > 200) {
 					if (chapterNumber == 1) {
 						chapterNumber--;
 						prev.setVisibility(View.INVISIBLE);
 						next.setVisibility(View.VISIBLE);						
 					} else if (chapterNumber > 1) {
 						chapterNumber--;
 						prev.setVisibility(View.VISIBLE);
 						next.setVisibility(View.VISIBLE);						
 					}
 				}
 			} catch (Exception e) {
 				// nothing
 			}
 			return false;
 		}
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		if (gestureDetector.onTouchEvent(event))
 			return true;
 		else
 			return false;
 	}
 }
