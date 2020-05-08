 package pt.traincompany.search;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import pt.traincompany.account.AccountManager;
 import pt.traincompany.account.Card;
 import pt.traincompany.account.CardAdapter;
 import pt.traincompany.main.R;
 import pt.traincompany.tickets.Ticket;
 import pt.traincompany.tickets.TicketActivity;
 import pt.traincompany.utility.Configurations;
 import pt.traincompany.utility.Connection;
 import pt.traincompany.utility.Utility;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TableRow.LayoutParams;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class SearchResultExtended extends Activity {
 
 	private ArrayList<Integer> route_ids = new ArrayList<Integer>();
 	private ArrayList<String> stop_starts = new ArrayList<String>();
 	private ArrayList<String> stop_ends = new ArrayList<String>();
 	ProgressDialog dialog;
 	String type_route;
 	double price_double;
 	private String strFrom;
 	private String strTo;
 	private String strDepartureTime;
 	private String strArrivalTime;
 	private String strDuration;
 	private int ticket_id;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Bundle bundle = getIntent().getExtras();
 		setContentView(R.layout.activity_search_result);
 		strFrom = bundle.getString("from");
 		strTo = bundle.getString("to");
 		setTitle(bundle.getString("from") + " > " + bundle.getString("to"));
 		
 		try {
 			int pos = bundle.getInt("position");
 
 			JSONArray info = (JSONArray) Utility.search_results.get(pos);
 
 			TextView from = (TextView) findViewById(R.id.search_result2_from);
 			TextView to = (TextView) findViewById(R.id.search_result2_to);
 			TextView duration = (TextView) findViewById(R.id.search_result2_duration);
 			TextView price = (TextView) findViewById(R.id.search_result2_price);
 
 			type_route = info.getString(0);
 			price_double = info.getDouble(4);
 			
 			strDepartureTime = info.getString(1);
 			from.setText(info.getString(1));
 			
 			strArrivalTime = info.getString(2);
 			to.setText(info.getString(2));
 			
 			strDuration = info.getString(3);
 			duration.setText(info.getString(3));
 			
 			price.setText(info.getDouble(4) + "€");
 
 			JSONArray stations = (JSONArray) info.get(5);
 			TableLayout tl = (TableLayout) findViewById(R.id.search_results2_stations);
 
 			for (int i = 0; i < stations.length(); i += 3) {
 				route_ids.add(stations.getInt(i));
 				JSONArray stations_temp = stations.getJSONArray(i + 2);
 
 				for (int j = 0; j < stations_temp.length(); j++) {
 					if (j == 0)
 						stop_starts.add(((JSONArray) stations_temp.get(j))
 								.getString(0));
 					else if (j == stations_temp.length() - 1)
 						stop_ends.add(((JSONArray) stations_temp.get(j))
 								.getString(0));
 
 					TableRow tr = new TableRow(this);
 					tr.setLayoutParams(new LayoutParams(
 							LayoutParams.WRAP_CONTENT,
 							LayoutParams.WRAP_CONTENT));
 					tr.setGravity(Gravity.CENTER_HORIZONTAL);
 
 					TextView b = new TextView(this);
 					b.setText(((JSONArray) stations_temp.get(j)).getString(0));
 					b.setLayoutParams(new LayoutParams(
 							LayoutParams.WRAP_CONTENT,
 							LayoutParams.WRAP_CONTENT));
 
 					tr.addView(b);
 
 					TextView b2 = new TextView(this);
 					b2.setText(((JSONArray) stations_temp.get(j)).getString(1));
 					b2.setLayoutParams(new LayoutParams(
 							LayoutParams.WRAP_CONTENT,
 							LayoutParams.WRAP_CONTENT));
 
 					tr.addView(b2);
 
 					tl.addView(tr, new TableLayout.LayoutParams(
 							LayoutParams.WRAP_CONTENT,
 							LayoutParams.WRAP_CONTENT));
 				}
 
 				if (stations.length() > 3 && i + 3 != stations.length()) {
 					TableRow tr = new TableRow(this);
 					tr.setLayoutParams(new LayoutParams(
 							LayoutParams.WRAP_CONTENT,
 							LayoutParams.WRAP_CONTENT));
 					tr.setGravity(Gravity.CENTER_HORIZONTAL);
 					
 					TableRow tr2 = new TableRow(this);
 					tr2.setLayoutParams(new LayoutParams(
 							LayoutParams.WRAP_CONTENT,
 							LayoutParams.WRAP_CONTENT));
 					tr2.setGravity(Gravity.CENTER_HORIZONTAL);
 
 					TextView b = new TextView(this);
 					b.setText("Transição entre comboios...");
 					b.setLayoutParams(new LayoutParams(
 							LayoutParams.WRAP_CONTENT,
 							LayoutParams.WRAP_CONTENT));
 					
 					TextView b2 = new TextView(this);
 					b2.setText("Tempo de espera: "+ info.getString(6));
 					b2.setLayoutParams(new LayoutParams(
 							LayoutParams.WRAP_CONTENT,
 							LayoutParams.WRAP_CONTENT));
 
 					tr.addView(b);
 					tr2.addView(b2);
 
 					tl.addView(tr, new TableLayout.LayoutParams(
 							LayoutParams.WRAP_CONTENT,
 							LayoutParams.WRAP_CONTENT));
 					tl.addView(tr2, new TableLayout.LayoutParams(
 							LayoutParams.WRAP_CONTENT,
 							LayoutParams.WRAP_CONTENT));
 				}
 
 			}
 
 		} catch (JSONException e) {
 			Toast.makeText(SearchResultExtended.this,
 					"Alguma coisa correu mal, tente novamente...",
 					Toast.LENGTH_LONG).show();
 			this.finish();
 		}
 
 		Button buyTicket = (Button) findViewById(R.id.btnBuyTicket);
 		buyTicket.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				if(Configurations.userId == 0) {
 					AlertDialog.Builder builder = new AlertDialog.Builder(
 							SearchResultExtended.this);
 					builder.setMessage("Pretende fazer login?")
 							.setTitle("Login")
 							.setPositiveButton("Sim",
 									new DialogInterface.OnClickListener() {
 										public void onClick(
 												DialogInterface dialog2, int id) {
 											Intent myIntent = new Intent(SearchResultExtended.this, AccountManager.class);
 											SearchResultExtended.this.startActivity(myIntent);
 										}
 									}).setNegativeButton("Não", null);
 					builder.show();
 				} else {
 					
 					dialog = ProgressDialog.show(SearchResultExtended.this, "",
 							"A comunicar com o servidor...", true);
 					dialog.setCancelable(true);
 					final Calendar c = Calendar.getInstance();
 					int mYear = c.get(Calendar.YEAR);
 					int mMonth = c.get(Calendar.MONTH);
 					int mDay = c.get(Calendar.DAY_OF_MONTH);
 					c.set(mYear, mMonth, mDay);
 					SimpleDateFormat tf = new SimpleDateFormat("HH:mm");
 					
 					try {
 						Date d = tf.parse(strDepartureTime);
 						c.set(Calendar.HOUR_OF_DAY, d.getHours());
 						c.set(Calendar.MINUTE, d.getMinutes());
 					} catch (ParseException e) {}
 					
 					DatePicker datePicker = (DatePicker) findViewById(R.id.datePickerTicket);
 	
 					final Calendar c2 = Calendar.getInstance();
 					c2.set(datePicker.getYear(), datePicker.getMonth(),
 							datePicker.getDayOfMonth());
 	
 					if (c2.after(c) || c2.compareTo(c) == 0) {
 						CheckTicket checkTicket = new CheckTicket(c2);
 						new Thread(checkTicket).start();
 					} else {
 						makeToast("Não pode comprar bilhete para essa data...");
 					}
 				}
 			}
 		});
 	}
 
 	class CheckTicket implements Runnable {
 		Calendar c2;
 		Calendar c1;
 		private int free_spots;
 
 		public CheckTicket(Calendar c2) {
 			this.c2 = c2;
 			this.c1 = c2;
 		}
 
 		public void run() {
 
 			for (int i = 0; i < route_ids.size(); i++) {
 				if (type_route.equals(Configurations.DUAL_ROUTE_OTHER_DAY)
 						&& i > 0)
 					c2.add(Calendar.DAY_OF_MONTH, 1);
 				Uri.Builder uri = Uri.parse(
 						"http://" + Configurations.AUTHORITY).buildUpon();
 				SimpleDateFormat tf = new SimpleDateFormat("dd-MM-yyyy");
 				uri.path(Configurations.VERIFYTICKET)
 						.appendQueryParameter("route_id", route_ids.get(i) + "")
 						.appendQueryParameter("stop_start",
 								stop_starts.get(i) + "")
 						.appendQueryParameter("stop_end", stop_ends.get(i) + "")
 						.appendQueryParameter(
 								"date",
 								tf.format(c2.getTime()))
 						.appendQueryParameter("format", Configurations.FORMAT)
 						.build();
 
 				String response = null;
 				try {
 					response = Connection.getJSONLine(uri.build());
 					JSONArray response_array = new JSONArray(response);
 					if (response_array.getString(0).equals("no")) {
 						makeToast("Já não há bilhetes para esta viagem...");
 						return;
 					}
 					free_spots = response_array.getInt(1);
 				} catch (Exception e) {
 					makeToast("A comunicação com o servidor falhou...");
 					return;
 				}
 			}
 
 			runOnUiThread(new Runnable() {
 				public void run() {
 					AlertDialog.Builder builder = new AlertDialog.Builder(
 							SearchResultExtended.this);
 					builder.setMessage("Pretende reservar o bilhete? Ainda há " + free_spots + " lugares livres.")
 							.setTitle("Confirmação")
 							.setPositiveButton("Sim",
 									new DialogInterface.OnClickListener() {
 										public void onClick(
 												DialogInterface dialog2, int id) {
 											dialog.dismiss();
 											dialog = ProgressDialog.show(
 													SearchResultExtended.this,
 													"",
 													"A efetuar a reserva...",
 													true);
 											dialog.setCancelable(true);
 											BuyTicket buyTicket = new BuyTicket(
 													c1);
 											new Thread(buyTicket).start();
 										}
 									}).setNegativeButton("Não", new DialogInterface.OnClickListener() {
 										public void onClick(
 												DialogInterface dialog2, int id) {
 											dialog.dismiss();
 										}
 									});
 					builder.show();
 				}
 			});
 
 		}
 	}
 
 	class BuyTicket implements Runnable {
 		Calendar c2;
 		
 
 		public BuyTicket(Calendar c1) {
 			c2 = c1;
 		}
 
 		public void run() {
 			Uri.Builder uri = Uri.parse("http://" + Configurations.AUTHORITY)
 					.buildUpon();
 			SimpleDateFormat tf = new SimpleDateFormat("dd-MM-yyyy");
 			uri.path(Configurations.BUYTICKET)
 					.appendQueryParameter("user_id", Configurations.userId +"")
 					.appendQueryParameter("price", price_double + "")
 					.appendQueryParameter("departureTime", strDepartureTime)
 					.appendQueryParameter("arrivalTime", strArrivalTime)
 					.appendQueryParameter("duration", strDuration + "")
 					.appendQueryParameter("departure", strFrom + "")
 					.appendQueryParameter("arrival", strTo + "")
 					.appendQueryParameter("date", tf.format(c2.getTime()))
 					.appendQueryParameter("format", Configurations.FORMAT)
 					.build();
 
 			String response = null;
 			try {
 				response = Connection.getJSONLine(uri.build());
 				JSONArray response_array = new JSONArray(response);
 				ticket_id = response_array.getInt(0);
 				
 				for (int i = 0; i < route_ids.size(); i++) {
 					if (type_route.equals(Configurations.DUAL_ROUTE_OTHER_DAY)
 							&& i > 0)
 						c2.add(Calendar.DAY_OF_MONTH, 1);
 					
 					uri = Uri.parse("http://" + Configurations.AUTHORITY)
 							.buildUpon();
 					uri.path(Configurations.ADDTICKETROUTES)
 							.appendQueryParameter("route_id",
 									route_ids.get(i) + "")
 							.appendQueryParameter("stop_start",
 									stop_starts.get(i) + "")
 							.appendQueryParameter("stop_end",
 									stop_ends.get(i) + "")
 							.appendQueryParameter(
 									"date",
 									tf.format(c2.getTime()))
 							.appendQueryParameter("user_id", Configurations.userId + "")
 							.appendQueryParameter("ticket_id", ticket_id + "")
 							.build();
 
 					response = null;
 					try {
 						response = Connection.getJSONLine(uri.build());
 					} catch (Exception e) {
 						makeToast("A comunicação com o servidor falhou...");
 					}
 				}
 				
 				dialog.dismiss();
 				runOnUiThread(new Runnable() {
 					public void run() {
 						Calendar c = Calendar.getInstance();
 						c.add(Calendar.DAY_OF_MONTH, 1);
 						
 						if(c2.after(c))
 							reservationDone();
						else 
 							reservationDonePayNow();
 					}
 					
 					
 					private void reservationDonePayNow() {
 						AlertDialog.Builder builder = new AlertDialog.Builder(
 								SearchResultExtended.this);
 						builder.setMessage(
 								"Como estamos próximos da data, tem de pagar o bilhete agora. Pretende fazê-lo?")
 								.setTitle("Reserva efetuada")
 								.setPositiveButton("Sim",
 										new DialogInterface.OnClickListener() {
 											public void onClick(
 													DialogInterface dialog2,
 													int id2) {
 												dialog = ProgressDialog.show(SearchResultExtended.this, "",
 														"A comunicar com o servidor...", true);
 												dialog.setCancelable(true);
 												GetCardsByUserId gc = new GetCardsByUserId();
 												new Thread(gc).start();
 											}
 										}).setNegativeButton("Não", new DialogInterface.OnClickListener() {
 											public void onClick(
 													DialogInterface dialog2,
 													int id2) {
 												dialog = ProgressDialog.show(SearchResultExtended.this, "",
 														"A cancelar o bilhete...", true);
 												dialog.setCancelable(true);
 												CancelTicket search = new CancelTicket();
 												new Thread(search).start();
 												
 											}
 										});
 						
 						builder.show();
 					}
 					
 					private void reservationDone() {
 						AlertDialog.Builder builder = new AlertDialog.Builder(
 								SearchResultExtended.this);
 						builder.setMessage(
 								"Pretende ver os detalhes do bilhete? Tem de pagá-lo até 24h antes da partida.")
 								.setTitle("Reserva efetuada")
 								.setPositiveButton("Sim",
 										new DialogInterface.OnClickListener() {
 											public void onClick(
 													DialogInterface dialog2,
 													int id2) {
 												SimpleDateFormat tf = new SimpleDateFormat("dd-MM-yyyy");
 												Ticket t = new Ticket(
 														ticket_id,
 														strDepartureTime,
 														strArrivalTime,
 														tf.format(c2.getTime()),
 														strFrom, strDuration,
 														strTo, price_double,
 														false);
 												Intent i = new Intent(
 														SearchResultExtended.this,
 														TicketActivity.class);
 												i.putExtra("ticket", (Ticket) t);
 												SearchResultExtended.this
 														.startActivity(i);
 											}
 										}).setNegativeButton("Não", null);
 						builder.show();
 					}
 				});
 
 				
 				//makeToast("Reserva efetuada com sucesso...");
 
 			} catch (Exception e) {
 				// REVERT ALL
 			}
 		}
 	}
 
 	private void makeToast(final String message) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				if (dialog != null)
 					dialog.dismiss();
 				Toast.makeText(SearchResultExtended.this, message,
 						Toast.LENGTH_SHORT).show();
 			}
 		});
 	}
 	
 	class PayTicket implements Runnable {
 
 		public void run() {
 			
 			Uri.Builder uri = Uri.parse("http://" + Configurations.AUTHORITY)
 					.buildUpon();
 			uri.path(Configurations.PAYTICKET);
 			uri.appendQueryParameter("id", ticket_id + "");
 			uri.appendQueryParameter("format", Configurations.FORMAT);
 
 			String response = null;
 
 			try {
 				response = Connection.getJSONLine(uri.build());
 				JSONArray info = new JSONArray(response);
 				if(info.getString(0).equals("paid")) {
 					makeToast("Bilhete pago com sucesso...");
 				}
 				else {
 					makeToast("Occorreu um erro com a operação...");
 					dialog = ProgressDialog.show(SearchResultExtended.this, "",
 							"A cancelar o bilhete...", true);
 					dialog.setCancelable(true);
 					CancelTicket search = new CancelTicket();
 					new Thread(search).start();
 				}
 				
 			}
 			catch(Exception e) {
 				makeToast("Occorreu um erro com a operação...");
 				dialog = ProgressDialog.show(SearchResultExtended.this, "",
 						"A cancelar o bilhete...", true);
 				dialog.setCancelable(true);
 				CancelTicket search = new CancelTicket();
 				new Thread(search).start();
 			}
 		}
 	}
 	
 	public class GetCardsByUserId implements Runnable {
 
 		public void run() {
 
 			Uri.Builder uri = Uri.parse("http://" + Configurations.AUTHORITY)
 					.buildUpon();
 			uri.path(Configurations.GETCARDSBYID);
 			uri.appendQueryParameter("format", Configurations.FORMAT);
 			uri.appendQueryParameter("userId", Configurations.userId + "");
 
 			String response = null;
 
 			try {
 				response = Connection.getJSONLine(uri.build());
 
 				JSONArray info = new JSONArray(response);
 
 				for (int i = 0; i < info.length(); i++) {
 					JSONObject card = info.getJSONObject(i);
 					String number = card.getString("number");
 					int id = card.getInt("id");
 					Card c = new Card(id, number);
 					Utility.user_cards = new ArrayList<Card>();
 					Utility.user_cards.add(c);
 				}
				
				if(Utility.user_cards.size() == 0) {
					makeToast("Adicione primeiro um cartão...");
					return;
				}
 
 				Configurations.cardsLoaded = true;
 
 				runOnUiThread(new Runnable() {
 					public void run() {
 						dialog.dismiss();
 						CardAdapter adapter = new CardAdapter(
 								SearchResultExtended.this, R.layout.creditcard_row2,
 								R.drawable.ic_launcher,
 								Utility.user_cards
 										.toArray(new Card[Utility.user_cards
 												.size()]));
 						AlertDialog.Builder builder = new AlertDialog.Builder(SearchResultExtended.this);
 					    builder.setTitle("Escolha um cartão")
 					           .setAdapter(adapter, new DialogInterface.OnClickListener() {
 					               public void onClick(DialogInterface dialog2, int which) {
 					            	   dialog = ProgressDialog.show(SearchResultExtended.this, "",
 												"A pagar o bilhete...", true);
 					            	   PayTicket pt = new PayTicket();
 					            	   new Thread(pt).start();
 					           }
 					    });
 					    builder.show();
 					    
 					}
 				});
 
 			} catch (Exception e) {
 				communicationProblem();
 			}
 		}
 	}
 	
 	private void communicationProblem() {
 		if(dialog != null) dialog.dismiss();
 		runOnUiThread(new Runnable() {
 			public void run() {
 				Toast.makeText(SearchResultExtended.this, "A comunicação com o servidor falhou...", Toast.LENGTH_LONG).show();
 		}});
 	}
 	
 	class CancelTicket implements Runnable {
 
 		public void run() {
 			
 			Uri.Builder uri = Uri.parse("http://" + Configurations.AUTHORITY).buildUpon();
 			uri.path(Configurations.CANCELTICKET)
 				.appendQueryParameter("id", ticket_id+"")
 				.build();
 			
 			try {
 				Connection.getJSONLine(uri.build());
 				makeToast("Bilhete cancelado com sucesso...");
 				
 			}
 			catch(Exception e) {
 				communicationProblem();
 			}
 		}
 
 	}
 }
 
 
 	
 
