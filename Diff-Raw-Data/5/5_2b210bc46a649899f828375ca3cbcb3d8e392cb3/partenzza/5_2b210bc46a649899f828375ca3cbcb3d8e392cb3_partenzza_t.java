 package it.example.storygame;
 
 
 
 
 
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 
 import android.text.method.ScrollingMovementMethod;
 import android.view.Menu;
 import android.view.View;
 
 import android.widget.Button;
 import android.widget.TextView;
 
 public class partenzza extends Activity {
 	
 	TextView textview;
 
 	Button avanti;
 	Button second;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.partenzza);
 		
 		second = (Button)findViewById(R.id.indietro);
 		//second.setVisibility(View.INVISIBLE);
 		
 		//inizio
 		textview = (TextView) findViewById(R.id.story1);
 		textview.setMovementMethod(new ScrollingMovementMethod());
 		textview.setText("Apro la borsa e dentro ci inserisco 10 corone d'oro, affero una razione alimentare dallo scafale in alto e lo apoggio" +
 				" con cura dentro la borsa poi afferro la mia spada e indosso il corpetto di pelle. Finalmente sono pronto, scendo nella " +
 				"stalla e inizia a sentirsi il rumore assordante della campana d'oro. Salgo al cavallo e parto per il campo d'adestramento." +
 				" Scendo a cavallo per il ripido sentiero che si inoltra nella foresta di Freylund, e prima di entrare nel fito bosco mi diro " +
 				" a guardare la campana d'oro inalzata sopra una tore. Una volta uscito dalla foresta procedo verso sud sulla \"Strada dei Predoni Maledetti\"" +
 				", l'unica che collega il paese con il campo d'adestramento. All'improviso vengo assalito da una banda di fuorilegge " +
 				" che vuole rapinarmi del mio oro.");
 				
 		
 		 avanti = (Button)findViewById(R.id.avanti);
 		 avanti.setText("Combati contro la banda!");
 		 avanti.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				
 				
 				//seconda schermata
 				textview = (TextView) findViewById(R.id.story1);
 				textview.setText("Pur preso alla sprovista affero la spada e colpisco il primo malvivente che mi capita a tiro stacandogli un" +
 						" braccio, e mentre il secondo 	malvivente si avvicina mi sbrigo a tirare fuori lo scudo e intanto do il colpo di " +
 						"grazia alla mia prima vitima che giaceve imobile per 	terra e senza fiatto, rendendolo inofensivo per sempre." +
 						" Intanto gli altri tre malfattori scappano terrorizzati , dopo aver'visto che ero un boccone troppo duro per loro." );
 				
 				//1 prima scelta
 				avanti.setText("Insegui i malviventi!");
 				avanti.setOnClickListener(new View.OnClickListener() {
 						
 						@Override
 						public void onClick(View v) {
 							
 							
 							
 							textview = (TextView) findViewById(R.id.story1);
 							textview.setText("Furioso mi lancio al inseguimento dei malfattori, dopo tutto sono un cavagliere e ne va della mi " +
 									" reputazione rendere giustizia, dando ai malfattori cio' che si meritano.E l'uncia pena che dovrebbero avere" +
 									" per le loro malefatte e' la morte. Arrivo alle spalle del piu' lento, il quale ignaro della mia posizione," +
 									" credeva di avermi seminato, e lo attaco alle spalle con la spada. Il malvivente cade per terra senza vita." );
 							
 							avanti.setText("Continua a seguire i malviventi!");
 							avanti.setOnClickListener(new View.OnClickListener() {
 								
 								@Override
 								public void onClick(View v) {
 									// TODO Auto-generated method stub
 									textview.setBackgroundResource(R.drawable.death);
 									textview = (TextView) findViewById(R.id.story1);
 									textview.setText("Senza fermarmi neanche per un attimo, continuo l'inseguimento dei restanti malviventi, sono a pochi passi" +
 											" da loro quando ne ho abbastanza del inseguimento, tiro fuori l'arco e con la mano destra afferro un freccia, " +
 											" prendo la mira e lancio la freccia. Manco il bersaglio e la frecia si confica nel albero, alla destra del mio bersaglio.Intanto mi acorgo che il secondo " +
 											" malvivente non era piu' sulla mia visuale, ignaro del pericolo in cui mi stavo imbatendo continuo l'inseguimento come se nulla fosse." +
 											" Al improvisso il mio bersaglio si ferma di colpo, estrago la spada e li vado in contro anche io , pensando che ne" +
 											" avesse abbastanza di scapare e che finalmente voleva arrendersi e andare in contro alla morte. Quando ero a pochi passi dal mio " +
 											" bersaglio 10 uomini armati mi circondano quatro dei quali mi tenevano sotto tiro con l'arco. Ero finito in trapola, una frecia mi " +
 											" colpisce , trapassandomi una spalla. Questa era la fine per me, intanto il malvivente che stavo inseguendo si avvicina a me e tria fuori " +
											" un mazza, si ferma davanti a me e sussura poche parole che non riesco a capire, dopo di che i da il colpo di grazia.");
 									
 									
 							
 							
 									second.setText("Fine!");
 									second.setOnClickListener(new View.OnClickListener() {
 										
 										@Override
 										public void onClick(View v) {
 											// TODO Auto-generated method stub
 											Intent finish = new Intent(partenzza.this, Read.class);
 											startActivity(finish);
 											
 										}
 									});
 									avanti.setVisibility(View.INVISIBLE);
 									
 									
 									
 								}
 							});
 							
 							avanti.setVisibility(View.VISIBLE);
 	
 							
 							
 							
 						}
 					});
 				//second.setVisibility(View.VISIBLE);
 				second.setText("Prosegui per il campo, dopo la lezione data ai malviventi.!");
 				second.setOnClickListener(new View.OnClickListener() {
 					
 					@Override
 					public void onClick(View v) {
 						// TODO an other activity
 						textview=(TextView)findViewById(R.id.story1);
 						textview.setText("Dopo una lunga galopata ecco che finalmente appare davanti ai miei occhi il campo d'adestramento.	Appena sorpasso il cancello vedo quatro soldati seduti che stano bevendo birra, e raccontano le loro imprese con impeto esagerato." +
 								" Sono tutti mezzi ubriachi e parlano a voce molto alta, ma quando mi vedono tacciono imediatamente e si alzano in piedi in segno di saluto..");
 						
 						avanti.setText("Unisciti ai soldati!");
 						avanti.setOnClickListener(new View.OnClickListener() {
 							
 							@Override
 							public void onClick(View v) {
 								// TODO Auto-generated method stub
 								textview=(TextView)findViewById(R.id.story1);
 								textview.setText("Mi siedo vicino ai quatro soldati formando un cerchio, e uno dei quatro iniza a parlare. Ora mi ricordo di lui. " +
 										" Il suo nome e caspian e ha combatuto sotto il mio comando tra le rovine della citta-fantasma di Quaren.L'uomo ho una grossa " +
 										" cicatrice in volto, fa un lungo sospiro e inizia a racontare una storia.");
 								
 									avanti.setText("Ascolta la storia!");
 									avanti.setOnClickListener(new View.OnClickListener() {
 										
 										@Override
 										public void onClick(View v) {
 											// TODO Auto-generated method stub
 											textview=(TextView)findViewById(R.id.story1);
 											textview.setText("L'uomo inizia a raccontare la storia della Pietra del potere. La pietra era stata" +
 													" forgiata dalla fusione del oro con il diamante, nell'era della Luna nera nella citta di Paressa. Centinai di anni fa fu rubata da " +
 													" un re di nome broly, che la incastono' nella sua lancia e la uso' in battagli come strumento ispiratore dei suoi fanatici seguaci. Egli credeva che la pietra l'avesse reso invulnerabile, ma le cose non stavano cosi." +
 													" Durante una batagli preso il fiume Ridhan il re fu ucciso, e la lancia dorata gli cade di mano inabissandosi nelle profondit delle acque del fiume e la pietra'andro perduita.");
 											
 											
 											avanti.setVisibility(View.INVISIBLE);
 										}
 									});
 							}
 						});
 						
 						
 						second.setText("Prosegui dopo avere ricambiato il saluto!");
 						second.setOnClickListener(new View.OnClickListener() {
 							
 							@Override
 							public void onClick(View v) {
 								// TODO Auto-generated method stub
 								textview=(TextView)findViewById(R.id.story1);
 								textview.setText("Mi inoltro nel campo e mi accorgo che un grupo di persone si sono riunite davanti a un grande cartello. Il cartello e tropo lontano e non riesco a leggere cosa c'e scritto, " +
 										" per questo decido di avvicinarmi. 	Quando arrivo vicino al cartello leggo a voce alta \n \"TORNEO DI TIRO CON L'ARCO \" \n	QUOTA D'ISCIRZIONE:2  CORONE D'ORO \n PRIMO PREMIO:L'ARCO D'ARGENTO DI BROLY.");
 								
 								avanti.setVisibility(View.VISIBLE);
 								second.setVisibility(View.INVISIBLE);
 								avanti.setText("Continua!");
 								avanti.setOnClickListener(new View.OnClickListener() {
 									
 									@Override
 									public void onClick(View v) {
 										// TODO Auto-generated method stub
 										textview=(TextView)findViewById(R.id.story1);
 										textview.setText("Continuo a inoltrarmi nel campo fino ad arrivare davanti alla \"Taverna del rospo\", e la taverna pi famosa del villaggio proprio perch e qui che servono la birra pi buona, " +
 												" ma e anche grazie a questo che smpre piena di malintenzionati e di delinquenti. Appena entro nel cortile un garzone apparso dal nulla mia da il benvenuto, prende il mio cavallo e mi mostra l'entrata " +
 												"  della taverna. All'interno , nonostante sia mattina presto, e piena di gente. In mezzo alla sala c'e un grande camino, con sopra tutta la carne che cucoce. L'odore della carne si sente in ogni angolo della taverna e fa venire l'acquolinba in bocca.");
 										
 										
 										avanti.setText("Vai al bancone per prendere una birra!");
 										avanti.setOnClickListener(new View.OnClickListener() {
 											
 											@Override
 											public void onClick(View v) {
 												// TODO Auto-generated method stub
 												textview=(TextView)findViewById(R.id.story1);
 												textview.setText("Arrivo davanti al balcone, e il proprietario della taverna, un uomo basso e cicciotello mi porgie un bocale" +
 														" e subito dopo lo riempie di bira. \"Se lo goda con calma signore\" dice \"questa e la miglior birra che potr trovare in tutto il paese\", " +
 														" li faccio un ceno di consenso con la testa e poi inizio a sorsegiare la birra. \"Mio figlio mi ha deto di aver dato da bere e da mangiare al suo" +
 														" cavallo , non si deve preoccupare ,  in buone mani , le nostre sono le stalle pi sicure di tutto il paese\". Pago due corone d'oro per la " +
 														" birra ");
 												
 												
 												
 												avanti.setText("Esci dalla taverna!");
 												avanti.setOnClickListener(new View.OnClickListener() {
 													
 													@Override
 													public void onClick(View v) {
 														// TODO Auto-generated method stub
 														
 														textview=(TextView)findViewById(R.id.story1);
 														textview.setText("Esco dalla taverna, per poi entrare nella stalla a prendere il mio cavallo." +
 																" Un rumore asordante inizia a farsi sentire da tutte le parti. Il rumore era talmente forte che rimango stordito per qualche secondo. Appena mi riprendo vedo che tutta le persone, che fino " +
 																" a qualche istante fa erano intorno a me, ora scapavno senza avere una meta precisa.Il rumore assordante veniva da tutte le campane, questo significava una cosa sola, ci stavano attacando.");
 														
 														
 														
 														avanti.setText("Torna dentro la taverna!");
 														avanti.setOnClickListener(new View.OnClickListener() {
 															
 															@Override
 															public void onClick(View v) {
 																// TODO Auto-generated method stub
 																textview.setBackgroundResource(R.drawable.death);
 																textview=(TextView)findViewById(R.id.story1);
 																textview.setText("Corro dentro la taverna in cerca di protezione, ma passato qualceh minuto al interno della " +
 																		" taverna, tra il fuggi fuggi generale, 	il fuoco inizia a inghiotire tutta la taverna. Due cavalieri nemici avevano dato fuoco alla taverna condanando tutti al suo interno alla morte.");
 																
 																second.setVisibility(View.VISIBLE);
 																second.setText("Fine!");
 																second.setOnClickListener(new View.OnClickListener() {
 																	
 																	@Override
 																	public void onClick(View v) {
 																		// TODO Auto-generated method stub
 																		Intent finish = new Intent(partenzza.this, Read.class);
 																		startActivity(finish);
 																		
 																	}
 																});
 																avanti.setVisibility(View.INVISIBLE);
 																
 															}
 														});
 														avanti.setText("Precipita nella zona dell'armamento!");
 														avanti.setOnClickListener(new View.OnClickListener() {
 															
 															@Override
 															public void onClick(View v) {
 																// TODO Auto-generated method stub
																textview.setBackgroundResource(R.drawable.death);
 																textview=(TextView)findViewById(R.id.story1);
 																textview.setText("Dopo essere salito a cavallo, mi dirigo verso la zona d'armamento, spingo il cavallo a correre come non aveva mai fato. " +
 																		" Appena arrivo davanti alle garandi sale d'armamento, in lontananza vedo l'esercito nemico che avanzava, " +
 																		" mentre il generale Ardrus aveva prepartao un piccolo esercito di cavallieri per contrattacare il nemico, " +
 																		" o per lo meno per tenerlo occupato affinche gli altri avvesero il tempo per prepararsi e dare man forte in battaglia.");
 																
 																
 																avanti.setText(" Unisciti al esercito di Ardus!");
 																avanti.setOnClickListener(new View.OnClickListener() {
 																	
 																	@Override
 																	public void onClick(View v) {
 																		// TODO Auto-generated method stub
 																		textview=(TextView)findViewById(R.id.story1);
 																		textview.setText("to be continued....");
 																		
 																		//attacarlo alla prima attivita del secondo gamestory
 																		second.setVisibility(View.VISIBLE);
 																		second.setText("Fine!");
 																		second.setOnClickListener(new View.OnClickListener() {
 																			
 																			@Override
 																			public void onClick(View v) {
 																				// TODO Auto-generated method stub
 																				Intent finish = new Intent(partenzza.this, Read.class);
 																				startActivity(finish);
 																				
 																			}
 																		});
 																		avanti.setVisibility(View.INVISIBLE);
 																		
 																	}
 																});
 																second.setVisibility(View.VISIBLE);
 																second.setText("Arrenditi ai nemici");
 																second.setOnClickListener(new View.OnClickListener() {
 																	
 																	@Override
 																	public void onClick(View v) {
 																		// TODO Auto-generated method stub
 																		textview.setBackgroundResource(R.drawable.death);
 																		textview=(TextView)findViewById(R.id.story1);
 																		textview.setText("Arrendendomi ai nemici significava andare  incontro a morte certa, ma in quel momento non vedevo altra soluzione. " +
 																				"Due cavalieri si fermano dinanzi a me.Erano grossi e impetuosi, la loro corazza color argento respingevano i raggi " +
 																				"del sole che andavano a sbaterci contro, da lontano sembravano quasi due angeli. Butto la spada e lo scudo a terra ,in " +
 																				" segno d'arresa. Uno dei due cavalieri avanza , mi gira intorno, poi alza la lancia e mi trafigge in pieno petto. Non faccio in " +
 																				" tempo ad alzare gli occhi per guardare in faccia un ultima volta \"il cavaliere dei signori delle tenebre\" che ero a terra senza forze.");
 																		
 																		//attacarlo alla prima attivita del secondo gamestory
 																		second.setText("Fine!");
 																		second.setOnClickListener(new View.OnClickListener() {
 																			
 																			@Override
 																			public void onClick(View v) {
 																				// TODO Auto-generated method stub
 																				Intent finish = new Intent(partenzza.this, Read.class);
 																				startActivity(finish);
 																				
 																			}
 																		});
 																		avanti.setVisibility(View.INVISIBLE);
 																		
 																	}
 																});
 																
 															}
 														});
 														
 														
 													}
 												});
 											}
 										});
 									}
 								});
 							}
 						});
 						
 					}
 				});
 				
 				
 	
 				
 			}
 		});
 		 
 		 
 			//second.setVisibility(View.VISIBLE);
 			second.setText("Fuggi in gropa al cavallo!");
 			second.setOnClickListener(new View.OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					// TODO an other activity
 					textview.setBackgroundResource(R.drawable.death);
 					textview=(TextView)findViewById(R.id.story1);
 					textview.setText("Senza fermarmi neanche per un attimo, continuo fugire dei restanti malviventi, sono a pochi passi da loro" +
 							" quando ne ho abbastanza del inseguimento, tiro fuori l'arco e con la mano destra afferro un freccia, prendo la mira e lancio la freccia." +
 							" Manco il bersaglio e la frecia si confica nel albero, alla destra del mio bersaglio.Intanto mi acorgo che il secondo malvivente" +
 							" non era piu' sulla mia visuale, ignaro del pericolo in cui mi stavo imbatendo continuo a fuggire come se nulla fosse. " +
 							" Al improvisso  mifermo di colpo, estrago la spada e li vado in contro , pensando di averne abbastanza di scapare e che finalmente voleva " +
 							" arrendermi e andare in contro alla morte. Quando ero a pochi passi dal mio bersaglio 10 uomini armati mi circondano quatro dei quali mi " +
 							" tenevano sotto tiro con l'arco. Ero finito in trapola, una frecia mi colpisce , trapassandomi una spalla. Questa era la fine per me, " +
 							" intanto il malvivente che stavo inseguendo si avvicina a me e tria fuori un mazza, si ferma davanti a me e sussura poche parole che non" +
 							" riesco a capire, dopo di che mi da il colpo di grazia.");
 					
 					
 					second.setText("Fine!");
 					second.setOnClickListener(new View.OnClickListener() {
 						
 						@Override
 						public void onClick(View v) {
 							// TODO Auto-generated method stub
 							Intent finish = new Intent(partenzza.this, Read.class);
 							startActivity(finish);
 							
 						}
 					});
 					avanti.setVisibility(View.INVISIBLE);
 					
 				}
 			});
 		
 		
 	}
 	
 	
 	
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
