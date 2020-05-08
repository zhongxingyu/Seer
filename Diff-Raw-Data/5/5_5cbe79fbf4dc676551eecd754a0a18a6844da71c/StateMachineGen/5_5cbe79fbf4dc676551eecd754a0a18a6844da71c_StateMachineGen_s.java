 package org.eclipse.etrice.generator.c.gen;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import java.util.List;
 import org.eclipse.etrice.core.room.ActorClass;
 import org.eclipse.etrice.core.room.State;
 import org.eclipse.etrice.generator.etricegen.ExpandedActorClass;
 import org.eclipse.etrice.generator.extensions.RoomExtensions;
 import org.eclipse.etrice.generator.generic.GenericStateMachineGenerator;
 import org.eclipse.xtext.xtend2.lib.StringConcatenation;
 
 @SuppressWarnings("all")
 @Singleton
 public class StateMachineGen extends GenericStateMachineGenerator {
   @Inject
   private RoomExtensions _roomExtensions;
   
   public StringConcatenation genDataMembers(final ExpandedActorClass xpac, final ActorClass ac) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("/* state machine variables */");
     _builder.newLine();
     _builder.append("etInt16 state;");
     _builder.newLine();
     _builder.append("etInt16 history[");
     List<State> _allLeafStates = this._roomExtensions.getAllLeafStates(xpac);
     int _size = _allLeafStates.size();
    _builder.append(_size, "");
     _builder.append("];");
     _builder.newLineIfNotEmpty();
     return _builder;
   }
   
   public StringConcatenation genInitialization(final ExpandedActorClass xpac, final ActorClass ac) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("self->state = STATE_TOP;");
     _builder.newLine();
     _builder.append("{");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("int i;");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("for (i=0; i<");
     List<State> _allLeafStates = this._roomExtensions.getAllLeafStates(xpac);
     int _size = _allLeafStates.size();
     _builder.append(_size, "	");
     _builder.append("; ++i)");
     _builder.newLineIfNotEmpty();
     _builder.append("\t\t");
     _builder.append("self->history[i] = NO_STATE;");
     _builder.newLine();
     _builder.append("}");
     _builder.newLine();
     _builder.append("executeInitTransition(self);");
     _builder.newLine();
     return _builder;
   }
   
   public StringConcatenation genExtra(final ExpandedActorClass xpac, final ActorClass ac) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.newLine();
     String _accessLevelPrivate = this.langExt.accessLevelPrivate();
     _builder.append(_accessLevelPrivate, "");
     _builder.append("void setState(");
     String _name = ac.getName();
     _builder.append(_name, "");
     _builder.append("* self, int new_state) {");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("self->state = new_state;");
     _builder.newLine();
     _builder.append("}");
     _builder.newLine();
     return _builder;
   }
 }
