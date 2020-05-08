 package com.example.wewrite;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import android.app.Activity;
 
 public class eventHandler {
   private List<events> local = new ArrayList<events>();
   private List<Integer> lastLocal = new ArrayList<Integer>();
   private List<Integer> nextLocal = new ArrayList<Integer>();
   private List<events> global = new ArrayList<events>();
   private int localPointer = -1;
  private int confirmedGlobalOrderId = 0;
   //private int localConfirmPointer = -1;
   private String username;
   private SessionActivity activity;
   
   public int getGlobalOrderId()
   {
     return confirmedGlobalOrderId;
   }
   
   public void clear()
   {
     while (!local.isEmpty())
       local.remove(0);
     while (!lastLocal.isEmpty())
       lastLocal.remove(0);
     while (!nextLocal.isEmpty())
       nextLocal.remove(0);
     while (!global.isEmpty())
       global.remove(0);
     localPointer = -1;
    confirmedGlobalOrderId = 0;
     //localConfirmPointer = -1;
   }
   
   eventHandler(String username, SessionActivity activity)
   {
     this.username = username;
     this.activity = activity;
   }
   
   private int applyAffect(events event, int originalGlobalCursor)
   {
     int newGlobalCursor = originalGlobalCursor;
     
     if (event.getRemoveLength() == 0)
     {
       if (event.getGlobalCursor() <= newGlobalCursor)
         newGlobalCursor += event.getInsertLength();
     }
     else if (event.getInsertLength() == 0)
     {
       if (event.getGlobalCursor() < newGlobalCursor)
         newGlobalCursor -= event.getRemoveLength();
       if (newGlobalCursor < 0)
         newGlobalCursor = 0;
     }
     
     return newGlobalCursor;
   }
   
   private int revertAffect(events event, int originalGlobalCursor)
   {
     int newGlobalCursor = originalGlobalCursor;
     
     if (event.getRemoveLength() == 0)
     {
       if (event.getGlobalCursor() < newGlobalCursor)
         newGlobalCursor -= event.getInsertLength();
       if (newGlobalCursor < 0)
         newGlobalCursor = 0;
     } else if (event.getInsertLength() == 0)
     {
       if (event.getGlobalCursor() <= newGlobalCursor)
         newGlobalCursor += event.getRemoveLength();
     }
     
     return newGlobalCursor;
   }
   
   private events getReverseEvent(events originalEvent)
   {
     events reverseEvent = new events(originalEvent);
     
     reverseEvent.setInsertLength(originalEvent.getRemoveLength());
     reverseEvent.setRemoveLength(originalEvent.getInsertLength());
     for (int i = originalEvent.getGlobalIndex() + 1; i < global.size(); ++i)
     {
       reverseEvent.setGlobalCursor(applyAffect(global.get(i), reverseEvent.getGlobalCursor()));
     }
     reverseEvent.setCharacters(originalEvent.getRemovedCharacters());
     reverseEvent.setRemovedCharacters(originalEvent.getCharacters());
     
     reverseEvent.setGlobalOrderId(-1);
     reverseEvent.setAfterGlobalOrderId(confirmedGlobalOrderId);
     
     return reverseEvent;
   }
   
   private String applyEvent(String originalText, events event)
   {
     StringBuffer newText = new StringBuffer(originalText);
     
     if (event.getGlobalCursor() + event.getRemoveLength() <= newText.length())
     {
       newText.delete(event.getGlobalCursor(), event.getGlobalCursor() + event.getRemoveLength());
     }
     else if (event.getRemoveLength() > newText.length())
     {
       newText.delete(0, newText.length());
     }
     else
     {
       newText.delete(event.getGlobalCursor(), newText.length());
     }
     
     if (event.getGlobalCursor() > newText.length())
     {
       newText.insert(newText.length(), event.getCharacters());
     }
     else
     {
       newText.insert(event.getGlobalCursor(), event.getCharacters());
     }
     
     return newText.toString();
   }
   
   private String revertEvent(String originalText, events event)
   {
     StringBuffer newText = new StringBuffer(originalText);
 
     if (event.getGlobalCursor() + event.getInsertLength() <= newText.length())
     {
       newText.delete(event.getGlobalCursor(), event.getGlobalCursor() + event.getInsertLength());
     }
     else if (event.getInsertLength() > newText.length())
     {
       newText.delete(0, newText.length());
     }
     else
     {
       newText.delete(event.getGlobalCursor(), newText.length());
     }
     
     if (event.getGlobalCursor() > newText.length())
     {
       newText.insert(newText.length(), event.getRemovedCharacters());
     }
     else
     {
       newText.insert(event.getGlobalCursor(), event.getRemovedCharacters());
     }
     
     return newText.toString();
   }
   
   public void receiveLocal(events event)
   {
     if (localPointer != -1)
       nextLocal.set(localPointer, Integer.valueOf(local.size()));
     lastLocal.add(Integer.valueOf(localPointer));
     nextLocal.add(Integer.valueOf(-1));
 //    while (local.size() > localPointer + 1)
 //      local.remove(local.size() - 1);
     event.setAfterGlobalOrderId(confirmedGlobalOrderId);
     System.out.println("receiveLocal: " + confirmedGlobalOrderId);
     event.setGlobalIndex(global.size());
     event.setGlobalOrderId(-1);
     event.setLocalIndex(local.size());
     local.add(event);
     localPointer = local.size() - 1;
     global.add(event);
   }
   
   public void receiveGlobal(events event, boolean isLast)
   {
     // get text
     String currentText = activity.editText.getText().toString();
     int i;
     
     System.out.println(event.getUsername().equals(username) + " " + event.getGlobalCursor() + " " + event.getInsertLength() + " " + event.getAfterGlobalOrderId() + " " + event.getGlobalOrderId());
     
     // undo unconfirmed local events, modify the global event,
     // add the event and apply affects and redo local operation
     if (!event.getUsername().equals(username))
     {
       // undo unconfirmed local events
       i = global.size() - 1;
       List<events> tempStack = new ArrayList<events>();
       while ((i >= 0) && (global.get(i).getGlobalOrderId() == -1))
       {
         tempStack.add(global.remove(i));
         currentText = revertEvent(currentText, tempStack.get(tempStack.size() - 1));
         --i;
       }
       
       i = global.size() - 1;
       while ((i >= 0) && (global.get(i).getGlobalOrderId() > event.getAfterGlobalOrderId()))
       {
         System.out.println("Global Order: event: " + i + " " + global.get(i).getGlobalOrderId() + " " + event.getAfterGlobalOrderId());
         --i;
       }
       ++i;
       //modify the global event
       while (i < global.size())
       {
         if (!global.get(i).getUsername().equals(event.getUsername()))
           event.setGlobalCursor(applyAffect(global.get(i), event.getGlobalCursor()));
         System.out.println("Modify event: " + event.getGlobalCursor() + " " + global.get(i).getGlobalCursor() + " " + global.get(i).getInsertLength());
         ++i;
       }
       // add the event
       event.setGlobalIndex(global.size());
       System.out.println(event.getGlobalCursor());
       global.add(event);
       currentText = applyEvent(currentText, event);
       // apply affects and redo local operation
       i = tempStack.size() - 1;
       while (i >= 0)
       {
         tempStack.get(i).setGlobalCursor(applyAffect(event, tempStack.get(i).getGlobalCursor()));
         local.get(tempStack.get(i).getLocalIndex()).setGlobalIndex(global.size());
         local.get(tempStack.get(i).getLocalIndex()).setGlobalCursor(tempStack.get(i).getGlobalCursor());
         global.add(tempStack.remove(i));
         currentText = applyEvent(currentText, global.get(global.size() - 1));
         --i;
       }
     }
     else
     // find itseld on the global stack and add the global order id
     {
       i = global.size() - 1;
       while ((i >= 0) && (global.get(i).getGlobalOrderId() == -1))
       {
         --i;
       }
       ++i;
       global.get(i).setGlobalOrderId(event.getGlobalOrderId());
     }
 
     confirmedGlobalOrderId = (int) event.getGlobalOrderId(); 
     System.out.println("receiveGlobal: " + confirmedGlobalOrderId);
 
     int cursor = activity.editText.getSelectionStart();
     activity.editText.removeTextChangedListener(activity.textWatcher);
     // set text 
     activity.editText.setText(currentText);
     activity.editText.addTextChangedListener(activity.textWatcher);
     if ( cursor >=0 && cursor <= activity.editText.getText().length())
       activity.editText.setSelection(cursor);
     else activity.editText.setSelection(activity.editText.getText().length());
   }
   
   // how to get and set text???
   public events undo()
   {
     events newEvent = null;
     if (localPointer != -1)
     {
       //get text;
       String text = activity.editText.getText().toString();
       
       newEvent = new events(getReverseEvent(local.get(localPointer)));
       newEvent.setAfterGlobalOrderId(confirmedGlobalOrderId);
       newEvent.setGlobalIndex(global.size());
       newEvent.setGlobalOrderId(-1);
       newEvent.setLocalIndex(local.size());
       local.add(newEvent);
       global.add(newEvent);
       nextLocal.add(nextLocal.get(localPointer));
       localPointer = lastLocal.get(localPointer);
       if (localPointer != -1)
         nextLocal.set(localPointer, local.size()-1);
       lastLocal.add(Integer.valueOf(localPointer));
       text = applyEvent(text, newEvent);
       
       //set text;
       activity.editText.setText(text);
     }
     //return event
     return newEvent;
   }
   
   public events redo()
   {
     events newEvent = null;
     if (((localPointer != -1) && (nextLocal.get(localPointer).intValue() != -1))
         || ((localPointer == -1) && (local.size() > 0)))
     {
       //get text;
       String text = activity.editText.getText().toString();
       
       int redoPointer = -1;
       if ((localPointer == -1) && (local.size() > 0))
         redoPointer = local.size() - 1;
       else if ((localPointer != -1) && (nextLocal.get(localPointer).intValue() != -1))
         redoPointer = nextLocal.get(localPointer).intValue();
       else
         System.out.println("This should not happen!");
       
       newEvent = new events(getReverseEvent(local.get(redoPointer)));
       newEvent.setAfterGlobalOrderId(confirmedGlobalOrderId);
       newEvent.setGlobalIndex(global.size());
       newEvent.setGlobalOrderId(-1);
       newEvent.setLocalIndex(local.size());
       local.add(newEvent);
       global.add(newEvent);
       if (localPointer != -1)
         nextLocal.set(localPointer, local.size() - 1);
       nextLocal.add(nextLocal.get(redoPointer));
       lastLocal.add(Integer.valueOf(localPointer));
       localPointer = local.size() - 1;
       text = applyEvent(text, newEvent);
         
       //set text;
       activity.editText.setText(text);
     }
     //return event
     return newEvent;
   }
 }
