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
 		MBO_TnCt_Send_Moving_Block_Authority,
 		Sch_MBO_Notify_Train_Added_Removed,//Deprecated
 		Sch_CTC_Send_Schedule,
 		CTC_Sch_Generate_Schedule,
 		CTC_TcMd_Send_Track_Closing,
 		CTC_TcMd_Send_Track_Opening,
 		CTC_TnMd_Request_Train_Creation,
 		CTC_TnCt_Send_Manual_MovingBlock,
 		CTC_TnCt_Send_Manual_FixedBlock,
 		CTC_TnCt_Send_Manual_Speed,
 		TcCt_TnCt_Send_Fixed_Block_Authority,
 		TcMd_TnCt_Send_Track_Speed_Limit, //deprecated
 		TcMd_TnCt_Confirm_Occupancy_Return_Block_Stats, //deprecated
 		TcMd_TnCt_Confirm_Depopulation, //deprecated
 		TcMd_TnCt_Send_Track_Gnd_State, //deprecated
 		TcMd_TnCt_Send_Station_Name, //deprecated
 		TnMd_Sch_Notify_Yard,
 		TnMd_CTC_Confirm_Train_Creation,
 		TnMd_CTC_Request_Train_Destruction,
 		TnMd_CTC_Send_Block_Occupied,
 		TnMd_TcCt_Update_Block_Occupancy,
 		TnMd_TnCt_Request_Train_Controller_Creation, //deprecated 
 		TnMd_TnCt_Request_Train_Controller_Destruction, //deprecated
 		TnMd_TnCt_Send_Train_Velocity, //deprecated
 		TnCt_TnMd_Send_Power, //deprecated
 		verify,
 		TnCt_TnMd_Request_Train_Velocity, //deprecated
 		TnCt_TcMd_Request_Track_Speed_Limit, //deprecated
 		TnMd_TcMd_Request_Track_Speed_Limit,
 		Sat_TnCnt_Request_Traversed_Block_Stats,
 		Sat_TnCt_ReceiptConfirm_Traversed_Block_Stats,
 		TcMd_TnCt_Send_Station_State, //deprecated
 		MBO_TnMd_Request_Velocity,
 		TnMd_MBO_Send_Velocity,
 		TnMd_Sch_Station_Arrival,
 		Sch_MBO_Send_Train_Info,
 		placeHolder
 	}
 
 	public enum NodeType
 	{
 		Node,
 		Connector,
 		Crossing,
 		Switch,
 		Yard,
 		Station
 
 	}
 }
