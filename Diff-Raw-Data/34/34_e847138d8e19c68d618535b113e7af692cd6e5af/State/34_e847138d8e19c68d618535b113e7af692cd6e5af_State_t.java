 package eu.ist.fears.client.common;
 
 public enum State {
 
 	Novo("Novo"),
     Planeado("Planeado"),
     Implementacao("Em implementa&ccedil;&atilde;o"),
    Completo("Completo"),
	Rejeitado("Rejeitado");
     
     protected String _html;
     
     
     private State(String html){
 		_html=html;
 	}
 	
     public String getHTML(){
     	return _html;
     }
     
     public String getListBoxHTML(){
     	if(!this.toString().equals(State.Implementacao.toString()))
     		return _html;
     	else return State.Implementacao.toString();
     }
 	
 }
