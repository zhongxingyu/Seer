 /*
  * Copyright (c) 2013 Nu Echo Inc. All rights reserved.
  */
 
 package com.nuecho.rivr.samples.helloworld;
 
 import javax.json.*;
 
 import org.slf4j.*;
 
 import com.nuecho.rivr.core.dialogue.*;
 import com.nuecho.rivr.voicexml.dialogue.*;
 import com.nuecho.rivr.voicexml.rendering.voicexml.*;
 import com.nuecho.rivr.voicexml.turn.*;
 import com.nuecho.rivr.voicexml.turn.first.*;
 import com.nuecho.rivr.voicexml.turn.input.*;
 import com.nuecho.rivr.voicexml.turn.last.*;
 import com.nuecho.rivr.voicexml.turn.output.audio.*;
 import com.nuecho.rivr.voicexml.turn.output.interaction.*;
 import com.nuecho.rivr.voicexml.util.*;
 import com.nuecho.rivr.voicexml.util.json.*;
import static com.nuecho.rivr.voicexml.turn.output.interaction.InteractionBuilder.*;
 
 /**
  * @author Nu Echo Inc.
  */
 public final class HelloWorldDialogue implements VoiceXmlDialogue {
 
     private static final String DIALOG_ID_MDC_KEY = "dialogId";
     private static final String CAUSE_PROPERTY = "cause";
     private final Logger mDialogLog = LoggerFactory.getLogger("hello.world");
 
     @Override
     public VoiceXmlLastTurn run(VoiceXmlFirstTurn firstTurn, VoiceXmlDialogueContext context) throws Exception {
 
         MDC.put(DIALOG_ID_MDC_KEY, context.getDialogueId());
 
         mDialogLog.info("Starting dialogue");
 
         JsonObjectBuilder resultObjectBuilder = JsonUtils.createObjectBuilder();
         try {
            InteractionBuilder interactionBuilder = newInteractionBuilder("hello");
             interactionBuilder.addPrompt(new SynthesisText("Hello World!"));
             InteractionTurn turn = interactionBuilder.build();
             VoiceXmlInputTurn inputTurn = DialogueUtils.doTurn(context, turn);
             if (VoiceXmlEvent.hasEvent(VoiceXmlEvent.CONNECTION_DISCONNECT_HANGUP, inputTurn.getEvents()))
                 throw new HangUp();
             if (VoiceXmlEvent.hasEvent(VoiceXmlEvent.ERROR, inputTurn.getEvents()))
                 throw new PlatformError(inputTurn.getEvents().get(0).getMessage());
         } catch (InterruptedException exception) {
             Thread.currentThread().interrupt();
         } catch (Exception exception) {
             mDialogLog.error("Error during dialogue", exception);
             JsonUtils.add(resultObjectBuilder, CAUSE_PROPERTY, ResultUtils.toJson(exception));
         }
 
         VariableDeclarationList variables = VariableDeclarationList.create(resultObjectBuilder.build());
         mDialogLog.info("Ending dialogue");
 
         return new VoiceXmlExitTurn("result", variables);
     }
 }
