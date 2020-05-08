 package ru.devg.dem.translating;
 
 import ru.devg.dem.quanta.Event;
 
 /**
  * @author Devgru &lt;java@devg.ru&gt;
  * @version 0.175
  */
 public final class NoopTranslator<TO extends Event, FROM extends TO>
        implements TranslatorStrategy<TO, FROM> {
     public TO translate(FROM event) {
         return event;
     }
 }
