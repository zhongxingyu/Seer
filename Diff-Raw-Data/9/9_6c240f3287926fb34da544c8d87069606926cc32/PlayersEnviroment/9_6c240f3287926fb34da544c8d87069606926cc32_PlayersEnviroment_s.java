 package core.ai;
 
 
 public interface PlayersEnviroment<Type extends State> extends Enviroment<Type>{
     public boolean isFinalState(Type state);
    public boolean isIsTurnOf(Type state);
 }
