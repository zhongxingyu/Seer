 package ru.devg.dem.translating;
 
 import ru.devg.dem.quanta.Event;
import ru.devg.dem.quanta.Handler;
 
 /**
  * @author Devgru &lt;java@devg.ru&gt;
  * @version 0.175
  */
 public final class NoopTranslator<TO extends Event, FROM extends TO>
        extends Translator<TO, FROM> {

    public NoopTranslator(Handler<? super TO> target, Class<FROM> bound) {
        super(target, bound);
    }

     public TO translate(FROM event) {
         return event;
     }
 }
