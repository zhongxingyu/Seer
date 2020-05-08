 package com.fernando.buscaminas;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import java.util.Random;
 import com.fernando.buscaminas.R;
 import com.fernando.buscaminas.SettingsActivity;
 
 public class MainActivity extends Activity {
 	protected Button botones[][];
 	protected int rows = 0, cols = 0, minas, banderas;
 	protected Drawable fondo;
 	
 	// Rellena la pantalla con los botones
 	public void rellenarPantalla() {
 		SharedPreferences prefe = getSharedPreferences("com.fernando.buscaminas_preferences", Context.MODE_PRIVATE);
		rows = Integer.parseInt(prefe.getString("rows", ""));
		cols = Integer.parseInt(prefe.getString("cols", ""));
		String n = prefe.getString("num_minas", "");
 		int i, x, z = 0;
 		minas = 1;
 		if(Integer.parseInt(n) > rows*cols) {
 			minas = rows*cols-1;
 		} else if(Integer.parseInt(n) < 1 || n.equals(null)) {
 			minas = 1;
 		} else {
 			minas = Integer.parseInt(n);
 		}
 		banderas = minas;
 		Button btnReiniciar = (Button) findViewById(R.id.btnReiniciar);
 		btnReiniciar.setBackgroundResource(R.drawable.inicio);
 		TextView textMinas = (TextView) findViewById(R.id.textMinas);
 		textMinas.setText("Minas: " + String.valueOf(minas));
 		TextView textTiempo = (TextView) findViewById(R.id.textTiempo);
 		textTiempo.setText("00:00");
 		Button btnBanderas = (Button) findViewById(R.id.ButtonBanderas);
 		btnBanderas.setText(String.valueOf(banderas));
 		Random rand = new Random();
 		botones = new Button[rows][cols];
 		TableLayout layout = (TableLayout) findViewById(R.id.tableLayout);
 		for(i = 0; i < rows; i++) {
 			TableRow tr = new TableRow(this);
 			for(x = 0; x < cols; x++) {
 			    botones[i][x] = new Button(getApplicationContext());
 			    botones[i][x].setId(z);
 			    botones[i][x].setOnClickListener(gestor);
 			    botones[i][x].setOnLongClickListener(mantener);
 			    tr.addView(botones[i][x]);
 			    z++;
 			}
 			layout.addView(tr);
 		}
 		while(minas > 0) {
 			int f, c;
 			f = rand.nextInt(rows);
 			c = rand.nextInt(cols);
 			if(compruebaMina(f, c)) {
 				botones[f][c].setSelected(true);
 				botones[f][c].setActivated(true);
 				minas--;
 			}
 		}
 	}
 	
 	// Comprueba si hay mina en la posicion indicada
 	public boolean compruebaMina(int i, int x) {
 		if(!botones[i][x].isSelected())
 			return true;
 		else
 			return false;
 	}
 	
 	// Comprueba el numero de minas alrededor de la posicion indicada
 	public int numMinas(int f, int c) {
 		int num = 0;
 		if(f > 0 && f < rows-1 && c > 0 && c < cols-1) {
 			if(botones[f-1][c].isSelected()) {
 				num++;
 			} if(botones[f-1][c-1].isSelected()) {
 				num++;
 			} if(botones[f-1][c+1].isSelected()) {
 				num++;
 			} if(botones[f+1][c].isSelected()) {
 				num++;
 			} if(botones[f+1][c-1].isSelected()) {
 				num++;
 			} if(botones[f+1][c+1].isSelected()) {
 				num++;
 			} if(botones[f][c-1].isSelected()) {
 				num++;
 			} if(botones[f][c+1].isSelected()) {
 				num++;
 			}
 		} else if(f == 0 && c == 0) {
 			if(botones[f][c+1].isSelected()) {
 				num++;
 			} if(botones[f+1][c+1].isSelected()) {
 				num++;
 			} if(botones[f+1][c].isSelected()) {
 				num++;
 			}
 		} else if(f == rows-1 && c == 0) {
 			if(botones[f-1][c].isSelected()) {
 				num++;
 			} if(botones[f-1][c+1].isSelected()) {
 				num++;
 			} if(botones[f][c+1].isSelected()) {
 				num++;
 			}
 		} else if(f == 0 && c == cols-1) {
 			if(botones[f+1][c].isSelected()) {
 				num++;
 			} if(botones[f+1][c-1].isSelected()) {
 				num++;
 			} if(botones[f][c-1].isSelected()) {
 				num++;
 			}
 		} else if(f == rows-1 && c == cols-1) {
 			if(botones[f-1][c].isSelected()) {
 				num++;
 			} if(botones[f-1][c-1].isSelected()) {
 				num++;
 			} if(botones[f][c-1].isSelected()) {
 				num++;
 			}
 		} else if(c == 0) {
 			if(botones[f-1][c].isSelected()) {
 				num++;
 			} if(botones[f+1][c].isSelected()) {
 				num++;
 			} if(botones[f-1][c+1].isSelected()) {
 				num++;
 			} if(botones[f+1][c+1].isSelected()) {
 				num++;
 			} if(botones[f][c+1].isSelected()) {
 				num++;
 			}
 		} else if(c == cols-1) {
 			if(botones[f-1][c].isSelected()) {
 				num++;
 			} if(botones[f+1][c].isSelected()) {
 				num++;
 			} if(botones[f-1][c-1].isSelected()) {
 				num++;
 			} if(botones[f+1][c-1].isSelected()) {
 				num++;
 			} if(botones[f][c-1].isSelected()) {
 				num++;
 			}
 		} else if(f == 0) {
 			if(botones[f][c+1].isSelected()) {
 				num++;
 			} if(botones[f][c-1].isSelected()) {
 				num++;
 			} if(botones[f+1][c-1].isSelected()) {
 				num++;
 			} if(botones[f+1][c+1].isSelected()) {
 				num++;
 			} if(botones[f+1][c].isSelected()) {
 				num++;
 			}
 		} else if(f == rows-1) {
 			if(botones[f][c+1].isSelected()) {
 				num++;
 			} if(botones[f][c-1].isSelected()) {
 				num++;
 			} if(botones[f-1][c-1].isSelected()) {
 				num++;
 			} if(botones[f-1][c+1].isSelected()) {
 				num++;
 			} if(botones[f-1][c].isSelected()) {
 				num++;
 			}
 		}
 		
 		return num;
 	}
 	
 	// Al pulsar sobre una mina muestra el contenido de cada posicion y cambia el
 	// texto del boton de reinicio
 	public void perder() {
 		for(int i = 0; i <= rows*cols; i++) {
 			int c = 0, f = 0, col = 0, fila = 0;
 			for(c = 0; c < cols; c++) {
 				for(f = 0; f < rows; f++) {
 					if(botones[f][c].getId() == i) {
 						col = c;
 						fila = f;
 					}
 				}
 			}
 			if(botones[fila][col].isSelected()) {
 				botones[fila][col].setBackgroundResource(R.drawable.bomba);
 				botones[fila][col].setEnabled(false);
 			}
 			else {
 				if(numMinas(fila, col) == 0) {
 				} else if(numMinas(fila, col) == 1) {
 					botones[fila][col].setText(String.valueOf(numMinas(fila, col)));
 				} else if(numMinas(fila, col) == 2) {
 					botones[fila][col].setText(String.valueOf(numMinas(fila, col)));
 					botones[fila][col].setTextColor(Color.YELLOW);
 				} else {
 					botones[fila][col].setText(String.valueOf(numMinas(fila, col)));
 					botones[fila][col].setTextColor(Color.RED);
 				}
 				botones[fila][col].setEnabled(false);
 			}
 			Button btnReiniciar = (Button) findViewById(R.id.btnReiniciar);
 			btnReiniciar.setBackgroundResource(R.drawable.reinicio);
 		}
 	}
 	
 	// Al pulsar sobre un botón que no este tocando ninguna mina se llama a esta funcion, que muestra todos los botones
 	// adyacentes que no esten tocando ninguna mina
 	public void propagar(int f, int c) {
 		if(f > 0 && f < rows-1 && c > 0 && c < cols-1) {
 			if(!botones[f-1][c].isSelected() && botones[f-1][c].isEnabled()) {
 				botones[f-1][c].setEnabled(false);
 				botones[f-1][c].setActivated(true);
 				comprobar(f-1, c);
 			} if(!botones[f-1][c-1].isSelected() && botones[f-1][c-1].isEnabled()) {
 				botones[f-1][c-1].setEnabled(false);
 				botones[f-1][c-1].setActivated(true);
 				comprobar(f-1, c-1);
 			} if(!botones[f-1][c+1].isSelected() && botones[f-1][c+1].isEnabled()) {
 				botones[f-1][c+1].setEnabled(false);
 				botones[f-1][c+1].setActivated(true);
 				comprobar(f-1, c+1);
 			} if(!botones[f+1][c].isSelected() && botones[f+1][c].isEnabled()) {
 				botones[f+1][c].setEnabled(false);
 				botones[f+1][c].setActivated(true);
 				comprobar(f+1, c);
 			} if(!botones[f+1][c-1].isSelected() && botones[f+1][c-1].isEnabled()) {
 				botones[f+1][c-1].setEnabled(false);
 				botones[f+1][c-1].setActivated(true);
 				comprobar(f+1, c-1);
 			} if(!botones[f+1][c+1].isSelected() && botones[f+1][c+1].isEnabled()) {
 				botones[f+1][c+1].setEnabled(false);
 				botones[f+1][c+1].setActivated(true);
 				comprobar(f+1, c+1);
 			} if(!botones[f][c-1].isSelected() && botones[f][c-1].isEnabled()) {
 				botones[f][c-1].setEnabled(false);
 				botones[f][c-1].setActivated(true);
 				comprobar(f, c-1);
 			} if(!botones[f][c+1].isSelected() && botones[f][c+1].isEnabled()) {
 				botones[f][c+1].setEnabled(false);
 				botones[f][c+1].setActivated(true);
 				comprobar(f, c+1);
 			}
 		} else if(f == 0 && c == 0) {
 			if(!botones[f][c+1].isSelected() && botones[f][c+1].isEnabled()) {
 				botones[f][c+1].setEnabled(false);
 				botones[f][c+1].setActivated(true);
 				comprobar(f, c+1);
 			} if(!botones[f+1][c+1].isSelected() && botones[f+1][c+1].isEnabled()) {
 				botones[f+1][c+1].setEnabled(false);
 				botones[f+1][c+1].setActivated(true);
 				comprobar(f+1, c+1);
 			} if(!botones[f+1][c].isSelected() && botones[f+1][c].isEnabled()) {
 				botones[f+1][c].setEnabled(false);
 				botones[f+1][c].setActivated(true);
 				comprobar(f+1, c);
 			}
 		} else if(f == rows-1 && c == 0) {
 			if(!botones[f-1][c].isSelected() && botones[f-1][c].isEnabled()) {
 				botones[f-1][c].setEnabled(false);
 				botones[f-1][c].setActivated(true);
 				comprobar(f-1, c);
 			} if(!botones[f-1][c+1].isSelected() && botones[f-1][c+1].isEnabled()) {
 				botones[f-1][c+1].setEnabled(false);
 				botones[f-1][c+1].setActivated(true);
 				comprobar(f-1, c+1);
 			} if(!botones[f][c+1].isSelected() && botones[f][c+1].isEnabled()) {
 				botones[f][c+1].setEnabled(false);
 				botones[f][c+1].setActivated(true);
 				comprobar(f, c+1);
 			}
 		} else if(f == 0 && c == cols-1) {
 			if(!botones[f+1][c].isSelected() && botones[f+1][c].isEnabled()) {
 				botones[f+1][c].setEnabled(false);
 				botones[f+1][c].setActivated(true);
 				comprobar(f+1, c);
 			} if(!botones[f+1][c-1].isSelected() && botones[f+1][c-1].isEnabled()) {
 				botones[f+1][c-1].setEnabled(false);
 				botones[f+1][c-1].setActivated(true);
 				comprobar(f+1, c-1);
 			} if(!botones[f][c-1].isSelected() && botones[f][c-1].isEnabled()) {
 				botones[f][c-1].setEnabled(false);
 				botones[f][c-1].setActivated(true);
 				comprobar(f, c-1);
 			}
 		} else if(f == rows-1 && c == cols-1) {
 			if(!botones[f-1][c].isSelected() && botones[f-1][c].isEnabled()) {
 				botones[f-1][c].setEnabled(false);
 				botones[f-1][c].setActivated(true);
 				comprobar(f-1, c);
 			} if(!botones[f-1][c-1].isSelected() && botones[f-1][c-1].isEnabled()) {
 				botones[f-1][c-1].setEnabled(false);
 				botones[f-1][c-1].setActivated(true);
 				comprobar(f-1, c-1);
 			} if(!botones[f][c-1].isSelected() && botones[f][c-1].isEnabled()) {
 				botones[f][c-1].setEnabled(false);
 				botones[f][c-1].setActivated(true);
 				comprobar(f, c-1);
 			}
 		} else if(c == 0) {
 			if(!botones[f-1][c].isSelected() && botones[f-1][c].isEnabled()) {
 				botones[f-1][c].setEnabled(false);
 				botones[f-1][c].setActivated(true);
 				comprobar(f-1, c);
 			} if(!botones[f+1][c].isSelected() && botones[f+1][c].isEnabled()) {
 				botones[f+1][c].setEnabled(false);
 				botones[f+1][c].setActivated(true);
 				comprobar(f+1, c);
 			} if(!botones[f-1][c+1].isSelected() && botones[f-1][c+1].isEnabled()) {
 				botones[f-1][c+1].setEnabled(false);
 				botones[f-1][c+1].setActivated(true);
 				comprobar(f-1, c+1);
 			} if(!botones[f+1][c+1].isSelected() && botones[f+1][c+1].isEnabled()) {
 				botones[f+1][c+1].setEnabled(false);
 				botones[f+1][c+1].setActivated(true);
 				comprobar(f+1, c+1);
 			} if(!botones[f][c+1].isSelected() && botones[f][c+1].isEnabled()) {
 				botones[f][c+1].setEnabled(false);
 				botones[f][c+1].setActivated(true);
 				comprobar(f, c+1);
 			}
 		} else if(c == cols-1) {
 			if(!botones[f-1][c].isSelected() && botones[f-1][c].isEnabled()) {
 				botones[f-1][c].setEnabled(false);
 				botones[f-1][c].setActivated(true);
 				comprobar(f-1, c);
 			} if(!botones[f+1][c].isSelected() && botones[f+1][c].isEnabled()) {
 				botones[f+1][c].setEnabled(false);
 				botones[f+1][c].setActivated(true);
 				comprobar(f+1, c);
 			} if(!botones[f-1][c-1].isSelected() && botones[f-1][c-1].isEnabled()) {
 				botones[f-1][c-1].setEnabled(false);
 				botones[f-1][c-1].setActivated(true);
 				comprobar(f-1, c-1);
 			} if(!botones[f+1][c-1].isSelected() && botones[f+1][c-1].isEnabled()) {
 				botones[f+1][c-1].setEnabled(false);
 				botones[f+1][c-1].setActivated(true);
 				comprobar(f+1, c-1);
 			} if(!botones[f][c-1].isSelected() && botones[f][c-1].isEnabled()) {
 				botones[f][c-1].setEnabled(false);
 				botones[f][c-1].setActivated(true);
 				comprobar(f, c-1);
 			}
 		} else if(f == 0) {
 			if(!botones[f][c+1].isSelected() && botones[f][c+1].isEnabled()) {
 				botones[f][c+1].setEnabled(false);
 				botones[f][c+1].setActivated(true);
 				comprobar(f, c+1);
 			} if(!botones[f][c-1].isSelected() && botones[f][c-1].isEnabled()) {
 				botones[f][c-1].setEnabled(false);
 				botones[f][c-1].setActivated(true);
 				comprobar(f, c-1);
 			} if(!botones[f+1][c-1].isSelected() && botones[f+1][c-1].isEnabled()) {
 				botones[f+1][c-1].setEnabled(false);
 				botones[f+1][c-1].setActivated(true);
 				comprobar(f+1, c-1);
 			} if(!botones[f+1][c+1].isSelected() && botones[f+1][c+1].isEnabled()) {
 				botones[f+1][c+1].setEnabled(false);
 				botones[f+1][c+1].setActivated(true);
 				comprobar(f+1, c+1);
 			} if(!botones[f+1][c].isSelected() && botones[f+1][c].isEnabled()) {
 				botones[f+1][c].setEnabled(false);
 				botones[f+1][c].setActivated(true);
 				comprobar(f+1, c);
 			}
 		} else if(f == rows-1) {
 			if(!botones[f][c+1].isSelected() && botones[f][c+1].isEnabled()) {
 				botones[f][c+1].setEnabled(false);
 				botones[f][c+1].setActivated(true);
 				comprobar(f, c+1);
 			} if(!botones[f][c-1].isSelected() && botones[f][c-1].isEnabled()) {
 				botones[f][c-1].setEnabled(false);
 				botones[f][c-1].setActivated(true);
 				comprobar(f, c-1);
 			} if(!botones[f-1][c-1].isSelected() && botones[f-1][c-1].isEnabled()) {
 				botones[f-1][c-1].setEnabled(false);
 				botones[f-1][c-1].setActivated(true);
 				comprobar(f-1, c-1);
 			} if(!botones[f-1][c+1].isSelected() && botones[f-1][c+1].isEnabled()) {
 				botones[f-1][c+1].setEnabled(false);
 				botones[f-1][c+1].setActivated(true);
 				comprobar(f-1, c+1);
 			} if(!botones[f-1][c].isSelected() && botones[f-1][c].isEnabled()) {
 				botones[f-1][c].setEnabled(false);
 				botones[f-1][c].setActivated(true);
 				comprobar(f-1, c);
 			}
 		}
 	}
 	
 	// Comprueba si tiene mina o no y le asigna un valor
 	public void comprobar(int id) {
 		int c = 0, f = 0, col = 0, fila = 0;
 		for(c = 0; c < cols; c++) {
 			for(f = 0; f < rows; f++) {
 				if(botones[f][c].getId() == id) {
 					col = c;
 					fila = f;
 				}
 			}
 		}
 		if(botones[fila][col].isSelected()) {
 			perder();
 		}
 		else {
 			int num = numMinas(fila, col);
 			if(num == 0) {
 				propagar(fila, col);
 			}
 			else if(num == 1) {
 				botones[fila][col].setText(String.valueOf(numMinas(fila, col)));
 			} else if(num == 2) {
 				botones[fila][col].setText(String.valueOf(numMinas(fila, col)));
 				botones[fila][col].setTextColor(Color.YELLOW);
 			} else {
 				botones[fila][col].setText(String.valueOf(numMinas(fila, col)));
 				botones[fila][col].setTextColor(Color.RED);
 			}
 			if(botones[fila][col].getTag() == "bandera") {
 				botones[fila][col].setBackground(fondo);
 				banderas++;
 				Button btnBanderas = (Button) findViewById(R.id.ButtonBanderas);
 				btnBanderas.setText(String.valueOf(banderas));
 			}
 			botones[fila][col].setEnabled(false);
 		}
 	}
 	
 	// Comprueba si tiene mina o no y le asigna un valor	
 	public void comprobar(int f, int c) {
 		int col = c;
 		int fila = f;
 		
 		if(botones[fila][col].isSelected()) {
 			perder();
 		}
 		else {
 			int num = numMinas(fila, col);
 			if(num == 0) {
 				propagar(fila, col);
 			}
 			else if(num == 1) {
 				botones[fila][col].setText(String.valueOf(numMinas(fila, col)));
 			} else if(num == 2) {
 				botones[fila][col].setText(String.valueOf(numMinas(fila, col)));
 				botones[fila][col].setTextColor(Color.YELLOW);
 			} else {
 				botones[fila][col].setText(String.valueOf(numMinas(fila, col)));
 				botones[fila][col].setTextColor(Color.RED);
 			}
 			if(botones[fila][col].getTag() == "bandera") {
 				botones[fila][col].setBackground(fondo);
 				banderas++;
 				Button btnBanderas = (Button) findViewById(R.id.ButtonBanderas);
 				btnBanderas.setText(String.valueOf(banderas));
 			}
 			botones[fila][col].setEnabled(false);
 		}
 	}
 	
 	// Comprueba si se han pulsado todas las posiciones
 	public boolean comprobar_acabar() {
 		int pulsados = 0;
 		for(int i = 0; i < cols*rows; i++) {
 			Button btn = (Button) findViewById(i);
 			if(btn.isActivated()) {
 				pulsados++;
 			}
 		}
 		if(pulsados == rows*cols)
 			return true;
 		else
 			return false;
 	}
 	
 	// Muestra todas las posiciones y cambia el texto del boton de reinicio
 	public void ganar() {
 		perder();
 		Button btnReiniciar = (Button) findViewById(R.id.btnReiniciar);
 		btnReiniciar.setBackgroundResource(R.drawable.ganar);
 	}
 	
 	// Listener de cada boton
 	private View.OnClickListener gestor = new View.OnClickListener() {
 		public void onClick(View v) {
 			comprobar(v.getId());
 			v.setActivated(true);
 			if(comprobar_acabar())
 				ganar();
 		}
 	};
 	
 	// Listener de pulsacion larga para cada boton
 	private View.OnLongClickListener mantener = new View.OnLongClickListener() {
 		public boolean onLongClick(View v) {
 			Button btnBanderas = (Button) findViewById(R.id.ButtonBanderas);
 			if(v.getTag() != "bandera") {
 				if(banderas > 0) {
 					v.setTag("bandera");
 					fondo = v.getBackground();
 					v.setBackgroundResource(R.drawable.bandera);
 					
 					banderas--;
 					btnBanderas.setText(String.valueOf(banderas));
 				}
 			} else {
 				v.setBackground(fondo);
 				v.setTag("no");
 				banderas++;
 				btnBanderas.setText(String.valueOf(banderas));
 			}
 			if(comprobar_acabar())
 				ganar();
 			
 			return true;
 		}
 	};
 	
 	// Listener del boton de reinicio
 	private View.OnClickListener gestor_reinicio = new View.OnClickListener() {
 		public void onClick(View v) {
 			TableLayout layout = (TableLayout) findViewById(R.id.tableLayout);
 			layout.removeAllViews();
 			rellenarPantalla();
 		}
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		Button btnReiniciar = (Button) findViewById(R.id.btnReiniciar);
 		rellenarPantalla();
 		btnReiniciar.setOnClickListener(gestor_reinicio);
 		Button btnBanderas = (Button) findViewById(R.id.ButtonBanderas);
 		btnBanderas.setEnabled(false);
 		btnBanderas.setBackgroundResource(R.drawable.bandera);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 		case R.id.preferencias:
 			Intent intent2 = new Intent(this, SettingsActivity.class);
 			startActivity(intent2);
 			break;
 		default:
 			return false;
 		}
 		return true;
 	}
 }
