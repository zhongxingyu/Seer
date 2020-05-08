 import java.util.Iterator;
 
 
 public class Baeckerei {
 
 	private Doppelkeksmaschine dkm;
 	private Formmaschine fm;
 	
 	public Baeckerei() {
 		dkm = new Doppelkeksmaschine();
 		fm = null;
 	}
 	
 	private void set(Formmaschine fm){
 		this.fm = fm;
 	}
 	
 	private Keks einzelBacken(String form, String teig){		
 		setMaschine(form);
 		return fm.backe(teig);
 	}
 	
 	private Keks doppelBacken(String form, String teig, String fuellung){
 		return dkm.backe(einzelBacken(form, teig), fuellung);
 	}
 	
 	private void setMaschine(String form){
 		if(form.equals("rund")){
 			set(new Rundmaschine());
 		}else if( form.equals("mond")){
 			set(new Mondmaschine());
 		}else if( form.equals("weihnachtsmann")){
 			set(new Weihnachtsmannmaschine());
 		}
 	}
 	
 	private boolean validate(Position p){
 		if(p.getForm() == null || p.getTeig() == null || p.getAnzahl()<1) {
 			return false;
 		}
 		return ((p.getForm().equals("rund") || p.getForm().equals("mond") ||
 				p.getForm().equals("weihnachtsmann")) && (p.getTeig().equals("muerb")||
 				p.getTeig().equals("zimtstern")|| p.getTeig().equals("schokolade")) &&
 				(p.getFuellung() == null || p.getFuellung().equals("marmelade") || 
 				p.getFuellung().equals("schokolade")));
 	}
 	
 	public Keksdose bestellungBearbeiten(Bestellung bestellung){
 		Keksdose kd = new Keksdose();
 		Iterator<Position> i = bestellung.iterator();
 		while(i.hasNext()){
 			Position p = i.next();
 			if(!validate(p)){
 				System.out.println("Ungültige Eingabe in der letzten Bestellung!");
 			}else{
 				if(p.getFuellung() != null){
					for(int j = 0; j<p.getAnzahl(); j++){
						kd.add(doppelBacken(p.getForm(),p.getTeig(),p.getFuellung()));
					}
 				}else{
					for(int j = 0; j<p.getAnzahl(); j++){
						kd.add(einzelBacken(p.getForm(),p.getTeig()));
					}
 				}
 			}
 		}
 		return kd;
 	}
 }
