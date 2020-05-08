 package pt.traincompany.tickets;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 
 import pt.traincompany.main.Home;
 import pt.traincompany.main.R;
 import pt.traincompany.search.Search;
 import pt.traincompany.search.SearchResultExtended;
 import pt.traincompany.utility.Configurations;
 import pt.traincompany.utility.Connection;
 import pt.traincompany.utility.Utility;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class TicketActivity extends Activity {
 
 	Ticket ticket;
 	Bitmap bitmap;
 	ProgressDialog dialog;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_ticket);
 
 		Ticket t = (Ticket) getIntent().getExtras().get("ticket");
 		this.ticket = t;
 
 		TextView id = (TextView) findViewById(R.id.id);
 		id.setText(ticket.id + "");
 
 		TextView departure = (TextView) findViewById(R.id.departureTime);
 		departure.setText(ticket.departureTime);
 
 		TextView arrival = (TextView) findViewById(R.id.arrivalTime);
 		arrival.setText(ticket.arrivalTime);
 
 		TextView data = (TextView) findViewById(R.id.date);
 		data.setText(ticket.date);
 
 		TextView from = (TextView) findViewById(R.id.from);
 		from.setText(ticket.from);
 
 		TextView to = (TextView) findViewById(R.id.to);
 		to.setText(ticket.to);
 
 		TextView price = (TextView) findViewById(R.id.price);
 		price.setText(ticket.price + " €");
 
 		TextView duration = (TextView) findViewById(R.id.duration);
 		duration.setText(ticket.duration);
 
 		ImageView paid = (ImageView) findViewById(R.id.paid);
 		if (ticket.paid)
 			paid.setImageResource(R.drawable.pago);
 		else
 			paid.setImageResource(R.drawable.delete);
		
 		dialog = ProgressDialog.show(TicketActivity.this, "",
 				"A gerar identificador...", true);
 
 		new Thread(new Runnable() {
 			public void run() {
 
 				URL newurl;
 				try {
 					newurl = new URL(Configurations.QRCODE + ticket.id);
 					bitmap = BitmapFactory.decodeStream(newurl
 							.openConnection().getInputStream());
 				} catch (MalformedURLException e) {
 					e.printStackTrace();
 					dialog.dismiss();
 					Toast.makeText(TicketActivity.this,
 							"Ocorreu um erro ao gerar o QR Code...",
 							Toast.LENGTH_LONG).show();
 				} catch (IOException e) {
 					e.printStackTrace();
 					dialog.dismiss();
 					Toast.makeText(TicketActivity.this,
 							"Ocorreu um erro ao gerar o QR Code...",
 							Toast.LENGTH_LONG).show();
 				}
 
 				runOnUiThread(new Runnable() {
 					public void run() {
 						ImageView qrCode = (ImageView) findViewById(R.id.qrCode);
 						qrCode.setImageBitmap(bitmap);
 						dialog.dismiss();
 					}
 				});
 			}
 		}).start();
 		
 		Button cancel = (Button) findViewById(R.id.btnDeleteTicket);
 		cancel.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				AlertDialog.Builder builder = new AlertDialog.Builder(
 						TicketActivity.this);
 				builder.setMessage(
 						"Tem a certeza que pretende cancelar o bilhete?")
 						.setTitle("Cancelar bilhete")
 						.setPositiveButton("Sim",
 								new DialogInterface.OnClickListener() {
 									public void onClick(
 											DialogInterface dialog2,
 											int id2) {
 										dialog = ProgressDialog.show(TicketActivity.this, "",
 												"A cancelar o bilhete...", true);
 										CancelTicket search = new CancelTicket();
 										new Thread(search).start();
 									}
 								}).setNegativeButton("Não", null);
 				builder.show();
 			}
 		});
 		
 	}
 	
 	class CancelTicket implements Runnable {
 
 		public void run() {
 			
 			Uri.Builder uri = Uri.parse("http://" + Configurations.AUTHORITY).buildUpon();
 			uri.path(Configurations.CANCELTICKET)
 				.appendQueryParameter("id", ticket.id+"")
 				.build();
 			
 			try {
 				Connection.getJSONLine(uri.build());
 				makeToast("Bilhete cancelado com sucesso...");
 				Utility.from_cancel_ticket = true;
 				Intent myIntent = new Intent(TicketActivity.this, Home.class);
 				TicketActivity.this.startActivity(myIntent);
 			}
 			catch(Exception e) {
 				communicationProblem();
 			}
 		}
 
 	}
 	
 	private void communicationProblem() {
 		if(dialog != null) dialog.dismiss();
 		runOnUiThread(new Runnable() {
 			public void run() {
 				Toast.makeText(TicketActivity.this, "A comunicação com o servidor falhou...", Toast.LENGTH_LONG).show();
 		}});
 	}
 	
 	private void makeToast(final String message) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				if (dialog != null)
 					dialog.dismiss();
 				Toast.makeText(TicketActivity.this, message,
 						Toast.LENGTH_SHORT).show();
 			}
 		});
 	}
 }
