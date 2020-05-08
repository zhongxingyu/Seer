 package Automat;
 
 public class Verteilung {
 	
 //Variablen
 
 		private Auswahlklappe m_Auswahlklappe;
 		
 		private Laufband m_HinteresLaufband;
 		
 		private Sensor s_AuswahlklappeEingangsLichtschranke;
 		private Sensor s_MehrwegBehaelterLichtschranke;
 		private Sensor s_PetBehaelterLichtschranke;
 		
 		private ParallelWarteClass workerThread;
 	
 //Konstruktor
 		
 		public Verteilung(){
 
 			m_Auswahlklappe = new Auswahlklappe(Adressen.Auswahlklappe.ordinal());
 			m_HinteresLaufband = new Laufband(Adressen.LaufbandAusgang.ordinal());
 			s_AuswahlklappeEingangsLichtschranke = new Sensor(Adressen.AuswahlklappeEingangslichtschranke.ordinal());
 			s_MehrwegBehaelterLichtschranke = new Sensor(Adressen.UebergabelichtschrankeMehrweg.ordinal());
 			s_PetBehaelterLichtschranke = new Sensor(Adressen.UebergabelichtschrankePET.ordinal());
 			
 			workerThread = new ParallelWarteClass(10000);
 		}
 
 //Methoden
 		
 		protected boolean Durchlauf(Sensor s){
 				
 			workerThread.run();
 			
 			while(!s.read() && workerThread.isAlive());
 			
 			if (!workerThread.isAlive()){
 				
 				return false;
 			}
 			
 			if(workerThread.isAlive()){
 				workerThread.interrupt();			
 			}
 			
 			workerThread.run();
 			
 			while(s.read() && workerThread.isAlive());
 			
 			if (!workerThread.isAlive()){
 				
 				return false;
 			}
 			
 			return true;
 		}
 		
 		public boolean Flasche_weiterleiten(FlaschenType Flasche){
 			
 			switch (Flasche) {
 			
 				case PET:
 					
 						m_HinteresLaufband.vorwaerts();
 						m_Auswahlklappe.stellen(Flasche);
 						
 						getUebergabeLichtschrankePET();
 						
 						m_HinteresLaufband.stopp();
 						
 						return true;
 				
 				case Mehrweg:
 					
 						m_HinteresLaufband.vorwaerts();
 						m_Auswahlklappe.stellen(Flasche);	
 						
 						getUebergabeLichtschrankeMehrweg();
 						
 						m_HinteresLaufband.stopp();
 						
 						return true;
 	
 				default:
 						
 						m_HinteresLaufband.rueckwerts();
 						
 						return false;
 				
 			}
 		}
 			
 
 		public boolean getUebergabeLichtschrankeMehrweg(){
 		
 			return Durchlauf(s_MehrwegBehaelterLichtschranke);
 			
 		}
 		
 		public boolean getUebergabeLichtschrankePET(){
 		
 			return Durchlauf(s_PetBehaelterLichtschranke);
 		}
 		
 }
