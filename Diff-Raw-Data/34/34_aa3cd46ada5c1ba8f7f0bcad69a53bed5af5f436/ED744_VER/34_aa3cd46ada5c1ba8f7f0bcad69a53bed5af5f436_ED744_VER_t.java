 package ru.sabstest;
 
 import java.text.SimpleDateFormat;
 
 public class ED744_VER extends Holder<ED244> implements Generate<ED743_VER> {
 
 	@Override
 	public boolean generateFrom(ED743_VER source) {
 		edNo = source.edNo + 2000;
 		edDate = source.edDate;
 		edAuthor = source.edReceiver;
 		edReceiver = source.edAuthor;
 		
 		iEdNo = source.edNo;
 		iEdDate = source.edDate;
 		iEdAuthor = source.edAuthor;
 		
 		ed = new ED244();
 		
 		return ed.generateFrom(source.ed);
 	}
 
 	@Override
 	public void setFileName() {
 		filename = edAuthor + new SimpleDateFormat("yyyyMMdd").format(edDate) + String.format("%09d", edNo) + ".ED744_VER";
 	}
 
 	@Override
 	public void insertIntoDB() {
		try
		{
			DB db = new DB(Settings.server, Settings.db, Settings.user, Settings.pwd);
			db.connect();

			int idPacet = insertIntoDBPacket(db, 0, true);
			
//			String query =  "INSERT INTO [dbo].[epay_EDVer]([[ID_PACKET], [ID_DEPART], [ID_ARM],\r\n" + 
//					" [InOutMode], [EDNo], [EDDate], [EDAuthor], [EDReceiver],\r\n" + 
//					" [ini_EdNo], [ini_EdDate], [ini_EdAutor], [PacetCode], [EDQuantity], [Summa],\r\n" + 
//					" [VerificationDate], [VerificationCode], [MSGID], [Annotation], [CtrlCode],\r\n" + 
//					" [CtrlTime], [ErrorDiagnostic], [VCtrlCode])\r\n" +					 
//			"VALUES(" + DB.toString(idPacet) + ", null, " + DB.toString(edNo) + ", " + DB.toString(edDate) + ",\r\n" +
//			DB.toString(edAuthor) + ", " + DB.toString(edReceiver) + ", " + DB.toString(edDefineRequestCode) + ", null, null,\r\n" + //" + DB.toString(idED243) +  "
//			"null, " + DB.toString(iEdNo) + ", " + DB.toString(iEdDate) + ", " + DB.toString(iEdAuthor) + ", null, '43',\r\n" +
//			DB.toString(edNo) + ", " + DB.toString(edDate) + ", " + DB.toString(edAuthor) + ", null, null, null,\r\n" +
//			"null, null, null, '0')";			
//			db.st.executeUpdate(query);
			
			db.close();		

		} catch (Exception e) {
			e.printStackTrace();
			Log.msg(e);			
		}
 	}
 
 	@Override
 	ED244 createHolderObject() {
 		return new ED244();
 	}
 
 }
