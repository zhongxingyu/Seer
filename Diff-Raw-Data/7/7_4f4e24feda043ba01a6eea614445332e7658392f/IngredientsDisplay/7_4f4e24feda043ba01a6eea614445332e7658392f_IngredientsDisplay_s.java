 package org.wrowclif.recipebox.ui;
 
 import org.wrowclif.recipebox.AppData;
 import org.wrowclif.recipebox.Ingredient;
 import org.wrowclif.recipebox.Recipe;
 import org.wrowclif.recipebox.RecipeIngredient;
 import org.wrowclif.recipebox.R;
 import org.wrowclif.recipebox.impl.UtilityImpl;
 import org.wrowclif.recipebox.ui.ListAutoCompleteAdapter;
 import org.wrowclif.recipebox.ui.ListAutoCompleteAdapter.Specifics;
 
 import java.util.List;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.Button;
 import android.widget.AdapterView;
 import android.widget.AutoCompleteTextView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.ListView;
 import android.view.KeyEvent;
 import android.view.inputmethod.EditorInfo;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.ViewGroup;
 import android.text.TextWatcher;
 import android.text.Editable;
 
 
 public class IngredientsDisplay extends Activity {
 
 	private boolean edit;
 	private Recipe r;
 	private ArrayAdapter<RecipeIngredient> adapter;
 	private int moveableItem;
 
 	private static final int NEW_INGREDIENT_DIALOG = 0;
 	private static final int DELETE_INGREDIENT_DIALOG = 1;
 	private static final int EDIT_DIALOG = 2;
 	private static final int CREATE_DIALOG = 3;
 	private static final int DELETE_DIALOG = 4;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState)	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.ingredients_display);
 
 		Intent intent = getIntent();
 
 		r = ((RecipeTabs) getParent()).curRecipe;
 		edit = ((RecipeTabs) getParent()).editing;
 		moveableItem = -1;
 
 		if(r != null) {
 			setTitle(r.getName());
 
 			ListView lv = (ListView) findViewById(R.id.ingredient_list);
 			adapter = new ArrayAdapter<RecipeIngredient>(this, R.layout.ingredient_display_item, r.getIngredients()) {
 				public View getView(final int position, View convert, ViewGroup group) {
 					if(convert == null) {
 						LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 						convert = vi.inflate(R.layout.ingredient_display_item, null);
 					}
 
 					final RecipeIngredient i = getItem(position);
 
					TextView tv = (TextView) convert.findViewById(R.id.ingredient_name);
					tv.setText(i.getName());

					tv = (TextView) convert.findViewById(R.id.ingredient_amount);
					tv.setText(i.getAmount());
 
 					Button be = (Button) convert.findViewById(R.id.edit_button);
 					Button bd = (Button) convert.findViewById(R.id.delete_button);
 
 					if(edit) {
 						be.setVisibility(View.VISIBLE);
 						bd.setVisibility(View.VISIBLE);
 
 						be.setOnClickListener(new OnClickListener() {
 							public void onClick(View v) {
 								Bundle bundle = new Bundle();
 								bundle.putInt("position", position);
 
 								showDialog(NEW_INGREDIENT_DIALOG, bundle);
 							}
 						});
 						bd.setOnClickListener(new OnClickListener() {
 							public void onClick(View v) {
 								Bundle bundle = new Bundle();
 								bundle.putInt("position", position);
 
 								showDialog(DELETE_INGREDIENT_DIALOG, bundle);
 							}
 						});
 						convert.setOnLongClickListener(new OnLongClickListener() {
 							public boolean onLongClick(View v) {
 								if(moveableItem != position) {
 									moveableItem = position;
 								} else {
 									moveableItem = -1;
 								}
 								adapter.notifyDataSetChanged();
 								return true;
 							}
 						});
 					} else {
 						be.setVisibility(View.GONE);
 						bd.setVisibility(View.GONE);
 						convert.setOnLongClickListener(null);
 					}
 
 					Button mu = (Button) convert.findViewById(R.id.up_button);
 					Button md = (Button) convert.findViewById(R.id.down_button);
 
 					if(moveableItem == position) {
 						mu.setVisibility(View.VISIBLE);
 						mu.setOnClickListener(new OnClickListener() {
 							public void onClick(View v) {
 								if(position > 0) {
 									r.swapIngredientPositions(adapter.getItem(position), adapter.getItem(position -1));
 									RecipeIngredient i = adapter.getItem(position);
 									moveableItem = position - 1;
 									adapter.remove(i);
 									adapter.insert(i, position -1);
 								}
 							}
 						});
 						md.setVisibility(View.VISIBLE);
 						md.setOnClickListener(new OnClickListener() {
 							public void onClick(View v) {
 								if(position < adapter.getCount() - 1) {
 									r.swapIngredientPositions(adapter.getItem(position), adapter.getItem(position + 1));
 									RecipeIngredient i = adapter.getItem(position + 1);
 									moveableItem = position + 1;
 									adapter.remove(i);
 									adapter.insert(i, position);
 								}
 							}
 						});
 					} else {
 						mu.setVisibility(View.GONE);
 						md.setVisibility(View.GONE);
 					}
 					return convert;
 				}
 			};
 
 			lv.setAdapter(adapter);
 
 			Button add = (Button) findViewById(R.id.add_button);
 
 			add.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					Bundle bundle = new Bundle();
 
 					showDialog(NEW_INGREDIENT_DIALOG, bundle);
 				}
 			});
 
 			Button done = (Button) findViewById(R.id.done_button);
 
 			done.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					setEditing(false);
 				}
 			});
 
 			if(!edit) {
 				add.setVisibility(View.GONE);
 				done.setVisibility(View.GONE);
 			}
 		}
 
 
     }
 
 	public void onResume() {
 		super.onResume();
 		boolean editing = ((RecipeTabs) getParent()).editing;
 		if(edit != editing) {
 			setEditing(editing);
 		}
 	}
 
     protected void setEditing(boolean editing) {
 		Button add = (Button) findViewById(R.id.add_button);
 		Button done = (Button) findViewById(R.id.done_button);
 		if(editing) {
 			add.setVisibility(View.VISIBLE);
 			done.setVisibility(View.VISIBLE);
 		} else {
 			add.setVisibility(View.GONE);
 			done.setVisibility(View.GONE);
 			moveableItem = -1;
 		}
 		edit = editing;
 		adapter.notifyDataSetChanged();
 
 		((RecipeTabs) getParent()).editing = editing;
 	}
 
 	protected void onPrepareDialog(int id, Dialog d, final Bundle bundle) {
 
 		switch(id) {
 			case NEW_INGREDIENT_DIALOG : {
 				IngredientDialog iDialog = (IngredientDialog) d;
 
 				int position = bundle.getInt("position", -1);
 				if(position < 0) {
 					iDialog.prepareNew(r, adapter);
 				} else {
 					iDialog.prepareExisiting(r, adapter, adapter.getItem(position), position);
 				}
 				break;
 			}
 
 			case DELETE_INGREDIENT_DIALOG : {
 				AlertDialog dialog = (AlertDialog) d;
 				final RecipeIngredient ri = adapter.getItem(bundle.getInt("position", -1));
 				dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Delete", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						r.removeIngredient(ri);
 
 						adapter.remove(ri);
 					}
 				});
 				break;
 			}
 		}
 	}
 
 	protected Dialog onCreateDialog(int id, Bundle bundle) {
 		Dialog dialog = null;
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
 		switch(id) {
 			case NEW_INGREDIENT_DIALOG : {
 				return new IngredientDialog(this);
 			}
 			case DELETE_INGREDIENT_DIALOG : {
 				final RecipeIngredient ri = adapter.getItem(bundle.getInt("position", -1));
 				builder.setTitle("Delete Ingredient");
 				builder.setMessage("Are you sure you want to delete this ingredient?");
 				builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						r.removeIngredient(ri);
 
 						adapter.remove(ri);
 					}
 				});
 				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 
 					}
 				});
 				break;
 			}
 
 			case CREATE_DIALOG : {
 				builder.setTitle("Create New Recipe");
 				builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						Recipe r2 = UtilityImpl.singleton.newRecipe("New Recipe");
 						Intent intent = new Intent(IngredientsDisplay.this, RecipeTabs.class);
 						intent.putExtra("id", r2.getId());
 						intent.putExtra("edit", true);
 						intent.putExtra("tab", 1);
 
 						IngredientsDisplay.this.finish();
 						startActivity(intent);
 					}
 				});
 				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 
 					}
 				});
 				break;
 			}
 
 			case EDIT_DIALOG : {
 				builder.setTitle("Edit Recipe");
 				builder.setItems(new String[] {"Edit This Recipe", "Create Variant", "Cancel"},
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							switch(which) {
 								case 0 :
 									setEditing(!edit);
 									break;
 								case 1 :
 									Recipe r2 = r.branch(r.getName() + " (branch)");
 									Intent intent = new Intent(IngredientsDisplay.this, RecipeTabs.class);
 									intent.putExtra("id", r2.getId());
 									intent.putExtra("edit", true);
 									intent.putExtra("tab", 1);
 
 									IngredientsDisplay.this.finish();
 									startActivity(intent);
 									break;
 							}
 						}
 				});
 				break;
 			}
 
 			case DELETE_DIALOG : {
 				builder.setTitle("Delete Recipe");
 				builder.setMessage("Are you sure you want to delete the recipe?");
 				builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						r.delete();
 						IngredientsDisplay.this.finish();
 					}
 				});
 				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 
 					}
 				});
 				break;
 			}
 		}
 		builder.setCancelable(true);
 
 		dialog = builder.create();
 
 		return dialog;
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.recipe_menu, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 			case R.id.edit :
 				if(edit) {
 					setEditing(false);
 				} else {
 					showDialog(EDIT_DIALOG);
 				}
 				return true;
 
 			case R.id.delete :
 				showDialog(DELETE_DIALOG);
 				return true;
 
 			case R.id.create :
 				showDialog(CREATE_DIALOG);
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
     public void onStop() {
 		super.onStop();
 
 		AppData.getSingleton().close();
 	}
 }
