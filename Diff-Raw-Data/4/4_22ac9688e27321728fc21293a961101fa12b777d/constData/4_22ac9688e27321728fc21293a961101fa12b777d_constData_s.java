 package TLTTC;
 public interface constData
 {
 	public enum Module
 	{
 		satellite, scheduler, MBO, CTC, trackController,
 		trackModel, trainModel, trainController
 	}
 	
 	public enum msg
 	{
 		verify, // temporary. Just used so that Worker.java compiles
 		MBO_TnCt_Send_Moving_Block_Authority,
 		CTC_Sch_Generate_Schedule,
 		CTC_TnMd_Request_Train_Creation,
 		CTC_TnCt_Send_Manual_MovingBlock,
 		CTC_TnCt_Send_Manual_FixedBlock,
 		CTC_TnCt_Send_Manual_Speed,
 		TcCt_TnCt_Send_Fixed_Block_Authority,
 		TcMd_TnCt_Send_Track_Speed_Limit,
 		TcMd_TnMd_Send_Yard_Node,
 		TnMd_CTC_Confirm_Train_Creation,
 		TnMd_CTC_Request_Train_Destruction,
 		TnMd_TcCt_Update_Block_Occupancy,
 		TnMd_TcMd_Request_Yard_Node,
 		TnMd_TnCt_Request_Train_Controller_Creation,
 		TnMd_TnCt_Request_Train_Controller_Destruction,
 		TnMd_TnCt_Send_Train_Velocity,
		TnMd_CTC_Confirm_Train_Creation,
		TnMd_CTC_Request_Train_Destruction,
		TnMd_CTC_Send_Block_Occupied
 		TnCt_TnMd_Send_Power,
 		TnCt_TcMd_Request_Track_Speed_Limit
 	}
 }
