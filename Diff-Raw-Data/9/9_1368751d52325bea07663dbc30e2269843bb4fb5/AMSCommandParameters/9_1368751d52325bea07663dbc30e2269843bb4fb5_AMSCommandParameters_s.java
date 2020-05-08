 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.neo.loader.ams.parameters;
 
 import org.amanzi.neo.loader.internal.NeoLoaderPluginMessages;
 
 /**
 * TODO Purpose of 
 * <p>
  *
 * </p>
  * @author Lagutko_N
  * @since 1.0.0
  */
 public enum AMSCommandParameters {
     
     BCS("BCS", ParameterType.INTEGER) {
         @Override
         public Object convert(Object rawValue) {
             Integer bcsCode = (Integer)rawValue;
             switch (bcsCode) {
             case 0:
                 return NeoLoaderPluginMessages.CBC_BCS_0;                
             case 2:
                 return NeoLoaderPluginMessages.CBC_BCS_2;
             default:            
                 return null;
             }
         }
     },
     
     BCL("BCL", ParameterType.INTEGER) {
         @Override
         public Object convert(Object rawValue) {
             return rawValue;
         }
     },
     
     MNI("MNI", ParameterType.INTEGER) {
         @Override
         public Object convert(Object rawValue) {
             return rawValue;
         }
     },
     
     RSSI("RSSI", ParameterType.INTEGER) {
         @Override
         public Object convert(Object rawValue) {
             return rawValue;
         }
     },
     
     BER("BER", ParameterType.FLOAT) {
         @Override
         public Object convert(Object rawValue) {
             return rawValue;
         }
     },
     
     LA("LA", ParameterType.INTEGER) {
         @Override
         public Object convert(Object rawValue) {
             return rawValue;
         }
     },
     
     F("F", ParameterType.FLOAT) {
         @Override
         public Object convert(Object rawValue) {
             return rawValue;
         }
     },
     
     C1("C1", ParameterType.INTEGER) {
         @Override
         public Object convert(Object rawValue) {
             return rawValue;
         }
     },
     
     BNC_LA("BNC_LA", ParameterType.INTEGER) {
         @Override
         public Object convert(Object rawValue) {
             return rawValue;
         }
     },
     
     BNC_C2("BNC_C2", ParameterType.INTEGER) {
         @Override
         public Object convert(Object rawValue) {
             return rawValue;
         }
     },
     
     BNC_RSSI("BNC_RSSI", ParameterType.INTEGER) {
         @Override
         public Object convert(Object rawValue) {
             return rawValue;
         }
     },
     
     ERROR_CODE("error_description", ParameterType.INTEGER) {
         @Override
         public Object convert(Object rawValue) {
             Integer errorCode = (Integer)rawValue;
             switch (errorCode) {
             case 2:    
                 return NeoLoaderPluginMessages.CME_Error_2;
             case 3:
                 return NeoLoaderPluginMessages.CME_Error_3;
             case 4:
                 return NeoLoaderPluginMessages.CME_Error_4;
             case 20:
                 return NeoLoaderPluginMessages.CME_Error_20;
             case 24:
                 return NeoLoaderPluginMessages.CME_Error_24;
             case 25:
                 return NeoLoaderPluginMessages.CME_Error_25;
             case 26:
                 return NeoLoaderPluginMessages.CME_Error_26;
             case 27:
                 return NeoLoaderPluginMessages.CME_Error_27;
             case 30:
                 return NeoLoaderPluginMessages.CME_Error_30;
             case 32:
                 return NeoLoaderPluginMessages.CME_Error_32;
             case 33:
                 return NeoLoaderPluginMessages.CME_Error_33;
             case 34:
                 return NeoLoaderPluginMessages.CME_Error_34;
             case 35:
                 return NeoLoaderPluginMessages.CME_Error_35;
             case 36:
                 return NeoLoaderPluginMessages.CME_Error_36;
             case 37:
                 return NeoLoaderPluginMessages.CME_Error_37;
             case 100:
                 return NeoLoaderPluginMessages.CME_Error_100;
             case 512:
                 return NeoLoaderPluginMessages.CME_Error_512;                
             case 513:
                 return NeoLoaderPluginMessages.CME_Error_513;
             case 514:
                 return NeoLoaderPluginMessages.CME_Error_514;
             case 515:
                 return NeoLoaderPluginMessages.CME_Error_515;
             case 516:
                 return NeoLoaderPluginMessages.CME_Error_516;
             case 517:
                 return NeoLoaderPluginMessages.CME_Error_517;
             case 518:
                 return NeoLoaderPluginMessages.CME_Error_518;
             default:            
                 //TODO: log error message
                 return null;
             }
         }
     },
     
     NUMBER_TYPE("number_type", ParameterType.INTEGER) {
         @Override
         public Object convert(Object rawValue) {
             Integer numberType = (Integer)rawValue;
             switch (numberType) {
             case 0:
                 return NeoLoaderPluginMessages.CNUM_Number_Type_0;
             case 1:
                 return NeoLoaderPluginMessages.CNUM_Number_Type_1;
             case 2:
                 return NeoLoaderPluginMessages.CNUM_Number_Type_2;
             case 3:
                 return NeoLoaderPluginMessages.CNUM_Number_Type_3;
             default:            
                 //TODO: log error 
                 return null;
             }
         }
     },
     
     CALLED_PARTY_IDENTITY("Called Party Identity", ParameterType.LONG) {
         @Override
         public Object convert(Object rawValue) {
             return rawValue;
         }
     },
     
     ALPHA("alpha", ParameterType.STRING) {
         @Override
         public Object convert(Object rawValue) {
             return rawValue;
         }
     },
     
     CC_INSTANCE("cc_instance", ParameterType.INTEGER) {
         @Override
         public Object convert(Object rawValue) {
             return rawValue;
         }
     },
     
     HOOK("hook", ParameterType.INTEGER) {
         @Override
         public Object convert(Object rawValue) {
             Integer hook = (Integer)rawValue;
             switch (hook) {
             case 0:            
                 return NeoLoaderPluginMessages.CTCC_Hook_0;
             case 1:
                 return NeoLoaderPluginMessages.CTCC_Hook_1;
             default:            
                 //TODO: log error message
                 return null;
             }
         }
     },
     
     SIMPLEX("simplex", ParameterType.INTEGER) {
         @Override
         public Object convert(Object rawValue) {
             Integer simplex = (Integer)rawValue;
             switch (simplex) {
             case 0:
                 return NeoLoaderPluginMessages.CTCC_Simplex_0;
             case 1:
                 return NeoLoaderPluginMessages.CTCC_Simplex_1;
             default:
                 //TODO: log error message
                 return null;
             }
         }
     },
     
     AI_SERVICE("ai_service", ParameterType.INTEGER) {
         @Override
         public Object convert(Object rawValue) {
             Integer aiService = (Integer)rawValue;
             switch (aiService) {
             case 0:
                 return NeoLoaderPluginMessages.CTCC_AI_service_0;
             case 1:
                 return NeoLoaderPluginMessages.CTCC_AI_service_1;
             case 2:
                 return NeoLoaderPluginMessages.CTCC_AI_service_2;
             case 5:
                 return NeoLoaderPluginMessages.CTCC_AI_service_5;
             default:
                 //TODO: log error message
                 return null;
             }
         }
     }, 
     
     END_TO_END_ENCRYPTION("end_to_end_encryption", ParameterType.INTEGER) {
         @Override
         public Object convert(Object rawValue) {
             Integer encryption = (Integer)rawValue;
             switch (encryption) {
             case 0:            
                 return NeoLoaderPluginMessages.CTCC_End_to_end_encryption_0;                
             case 1:
                 return NeoLoaderPluginMessages.CTCC_End_to_end_encryption_1;
             default:            
                 //TODO: log error message
                 return null;
             }
         }
     },
     
     COMMS_TYPE("comms_type", ParameterType.INTEGER) {
 
         @Override
         public Object convert(Object rawValue) {
             Integer commsType = (Integer)rawValue;
             switch (commsType) {
             case 0:
                 return NeoLoaderPluginMessages.CTCC_Comms_Type_0;
             case 1:
                 return NeoLoaderPluginMessages.CTCC_Comms_Type_1;
             case 3:
                 return NeoLoaderPluginMessages.CTCC_Comms_Type_3;
             default:
                 //TODO: log error message
                 return null;
             }
         }     
     },
     
     SLOTS_CODEC("slots_codec", ParameterType.INTEGER) {
         @Override
         public Object convert(Object rawValue) {
             Integer slotsCodec = (Integer)rawValue;
             switch (slotsCodec) {
             case 0:            
                 return NeoLoaderPluginMessages.CTCC_Slots_Codec_0;
             case 1:
                 return NeoLoaderPluginMessages.CTCC_Slots_Codec_1;
             default:            
                 //TODO: log error message
                 return null;
             }
         }
     },
     
     PROPRIETARY("proprietary", ParameterType.STRING) {
         @Override
         public Object convert(Object rawValue) {
             return rawValue;
         }
     },
     
     DISCONNECT_CAUSE("disconnect_cause", ParameterType.INTEGER) {
         @Override
         public Object convert(Object rawValue) {
             Integer disconnectCause = (Integer)rawValue;
             switch (disconnectCause) {
             case 0:            
                 return NeoLoaderPluginMessages.CTCR_Disconnect_Cause_0;
             case 1:            
                 return NeoLoaderPluginMessages.CTCR_Disconnect_Cause_1;
             case 2:            
                 return NeoLoaderPluginMessages.CTCR_Disconnect_Cause_2;
             case 3:            
                 return NeoLoaderPluginMessages.CTCR_Disconnect_Cause_3;
             case 4:            
                 return NeoLoaderPluginMessages.CTCR_Disconnect_Cause_4;
             case 5:            
                 return NeoLoaderPluginMessages.CTCR_Disconnect_Cause_5;
             case 6:            
                 return NeoLoaderPluginMessages.CTCR_Disconnect_Cause_6;
             case 7:            
                 return NeoLoaderPluginMessages.CTCR_Disconnect_Cause_7;
             case 8:            
                 return NeoLoaderPluginMessages.CTCR_Disconnect_Cause_8;
             case 9:            
                 return NeoLoaderPluginMessages.CTCR_Disconnect_Cause_9;
             case 10:            
                 return NeoLoaderPluginMessages.CTCR_Disconnect_Cause_10;
             case 11:            
                 return NeoLoaderPluginMessages.CTCR_Disconnect_Cause_11;
             case 12:            
                 return NeoLoaderPluginMessages.CTCR_Disconnect_Cause_12;
             case 13:            
                 return NeoLoaderPluginMessages.CTCR_Disconnect_Cause_13;
             case 14:            
                 return NeoLoaderPluginMessages.CTCR_Disconnect_Cause_14;
             case 15:            
                 return NeoLoaderPluginMessages.CTCR_Disconnect_Cause_15;
             case 16:            
                 return NeoLoaderPluginMessages.CTCR_Disconnect_Cause_16;
             case 17:            
                 return NeoLoaderPluginMessages.CTCR_Disconnect_Cause_17;
             case 18:            
                 return NeoLoaderPluginMessages.CTCR_Disconnect_Cause_18;
             case 19:            
                 return NeoLoaderPluginMessages.CTCR_Disconnect_Cause_19;
             case 20:            
                 return NeoLoaderPluginMessages.CTCR_Disconnect_Cause_20;
             default:            
                 //TODO: log error message
                 return null;
             }
         }
     },
     
     CALL_STATUS("call_status", ParameterType.INTEGER) {
         @Override
         public Object convert(Object rawValue) {
             Integer callStatus = (Integer)rawValue;
             
             switch (callStatus) {
             case 0:
                 return NeoLoaderPluginMessages.CTICN_Call_Status_0;
             default:
                 //TODO: log error message
                 return null;
             }
         }
     },
     
     CALLING_PARTY_IDENT_TYPE("calling_party_ident_type", ParameterType.INTEGER) {
         @Override
         public Object convert(Object rawValue) {
             Integer identType = (Integer)rawValue;
             
             switch (identType) {
             case 0:
                 return NeoLoaderPluginMessages.CTICN_Calling_Party_Ident_Type_0;
             case 1:
                 return NeoLoaderPluginMessages.CTICN_Calling_Party_Ident_Type_1;
             default:
                 //TODO: log error message
                 return null;
             }
         }
     },
     
     CALLING_PARTY_IDENT("calling_party_ident", ParameterType.LONG) {
         @Override
         public Object convert(Object rawValue) {
             return rawValue;
         }
     },
     
     AREA("area", ParameterType.INTEGER) {
         @Override
         public Object convert(Object rawValue) {
             Integer area = (Integer)rawValue;
             
             switch (area) {
             case 0:
                 return NeoLoaderPluginMessages.CTSDC_Area_0;
             default:
                 //TODO: log error message
                 return null;
             }
         }
     },
     
     RX_TX("rx_tx", ParameterType.INTEGER) {
         @Override
         public Object convert(Object rawValue) {
             Integer rxTx = (Integer)rawValue;
             
             switch (rxTx) {
             case 0:
                 return NeoLoaderPluginMessages.CTSDC_RxTx_0;
             case 1:
                 return NeoLoaderPluginMessages.CTSDC_RxTx_0;
             default:
                 //TODO: log error message
                 return null;
             }
         }
     },
     
     PRIORITY("priority", ParameterType.INTEGER) {
         @Override
         public Object convert(Object rawValue) {
             Integer priority = (Integer)rawValue;
             
             if (priority == 0) {
                 return NeoLoaderPluginMessages.CTSDC_Priority_0;
             }
             else if ((priority > 0) && (priority <= 15)) {
                 return NeoLoaderPluginMessages.getFormattedString(NeoLoaderPluginMessages.CTSDC_Priority_n, priority);                
             }
             else {
                 //TODO: log error message
                 return null;
             }
         }
     },
     
     PESQ_LISTENING_QUALITIY("pesq_listening_quality", ParameterType.FLOAT) {
         @Override
         public Object convert(Object rawValue) {
             return rawValue;
         }
     },
     
     PESQ_THRESHOLD("pesq_threshold", ParameterType.FLOAT) {
         @Override
         public Object convert(Object rawValue) {
             return rawValue;
         }
     },
     
     AVERAGE_SYMMETRICAL_DISTURBANCE("average_symmetrical_disturbance", ParameterType.FLOAT) {
         @Override
         public Object convert(Object rawValue) {
             return rawValue;
         }
     },
     
     AVERAGE_ASYMMETRICAL_DISTURBANCE("average_asymmetrical_disturbance", ParameterType.FLOAT) {
         @Override
         public Object convert(Object rawValue) {
             return rawValue;
         }
     },
     
     ESTIMATED_DELAY("estimated_delay", ParameterType.FLOAT) {
         @Override
         public Object convert(Object rawValue) {
             return rawValue;
         }
     };
     
     /**
      * Type of parameter
      * 
      * @author Lagutko_N
      * @since 1.0.0
      */
     public enum ParameterType {
         INTEGER,
         FLOAT,
         STRING,
         LONG;
     }
     
     private String name;
     
     private ParameterType type;
     
     private AMSCommandParameters(String parameterName, ParameterType type) {
         this.name = parameterName;
         this.type = type;
     }
     
     public ParameterType getType() {
         return type;
     }
     
     public String getName() {
         return name;
     }
     
     protected abstract Object convert(Object rawValue);
     
     /**
      * Parses string to get a value of this parameter
      *
      * @param parameter line with value
      * @return parsed value
      */
     public Object parseString(String parameter) {
         if (parameter.equals("OK")) {
             return null;
         }
         Object result = null;
         
         switch (type) {
         case LONG:
             result = Long.parseLong(parameter);
             break;
         case INTEGER: 
             result = Integer.parseInt(parameter);
             break;
         case FLOAT:
             result = Float.parseFloat(parameter);
             break;
         case STRING:
             result = parameter;
             break;
         default:
             result = parameter;                        
         }
         
         return convert(result);
     }
 
 }
