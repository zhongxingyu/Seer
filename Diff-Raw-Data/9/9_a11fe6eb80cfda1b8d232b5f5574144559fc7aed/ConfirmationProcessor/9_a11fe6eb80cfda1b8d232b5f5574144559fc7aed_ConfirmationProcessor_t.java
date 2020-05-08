 package com.ultrashare.component.business;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.ultrashare.component.facilities.MailSender;
 import com.ultrashare.component.vo.ConfirmationVO;
 import com.ultrashare.component.vo.MailVO;
 import com.ultrashare.model.Share;
 
 public class ConfirmationProcessor extends AbstractProcessor<ConfirmationVO> {
 
 	private static String SHARE_EMAIL_MESSAGE_PATTERN = "Greetings!\n\n<senderName> (<senderMail>) has shared \"<fileName>\" with you!\n\nTo download this file please click the link below:\n\n<confirmationLink>\n\nUltraSHARE.com is a place where you can share anything, with anyone.\nVisit us at http://ultrashare.valvezon.com\n\nPlease do not reply this email!\n\nIf you have any questions or problems please send us an email:\nsupport@valvezon.com\n\nBest Regards,\nThe UltraSHARE Team";
 	private static String SHARE_EMAIL_SUBJECT_PATTERN = "<senderName> has shared \"<fileName>\" with you in UltraSHARE.com!";
 	private static String SHARE_EMAIL_LINK_PATTERN = "http://ultrashare.valvezon.com/download/request/<id>/<confirmationCode>";
 
 	@Override
 	protected void execute(ConfirmationVO processItem) {
 		List<MailVO> mailsVO = new ArrayList<MailVO>(processItem.getUnmodifiableShareList().size());
 		for (Share share : processItem.getUnmodifiableShareList()) {
			mailsVO.add(new MailVO(new String[] { share.getRecipientEmail() }, SHARE_EMAIL_SUBJECT_PATTERN.replaceAll("<senderName>",
					share.getSharedUpload().getSenderName()).replaceAll("<fileName>", share.getSharedUpload().getFileName()), getShareMailMessage(share
					.getSharedUpload().getSenderName(), share.getSharedUpload().getSenderEmail(), share.getSharedUpload().getFileName(), getShareLink(share))));
 		}
 		submitShareEmail(mailsVO);
 	}
 
 	private static void submitShareEmail(List<MailVO> mailsVO) {
 		MailSender.sendMails(mailsVO);
 	}
 
 	private static String getShareMailMessage(String senderName, String senderMail, String fileName, String confirmationLink) {
 		return SHARE_EMAIL_MESSAGE_PATTERN.replaceAll("<senderName>", senderName).replaceAll("<senderMail>", senderMail).replaceAll("<fileName>", fileName)
 				.replaceAll("<confirmationLink>", confirmationLink);
 	}
 
 	private static String getShareLink(Share share) {
		return SHARE_EMAIL_LINK_PATTERN.replaceAll("<id>", share.getId().toString()).replaceAll("<confirmationCode>", share.getConfirmationCode().toString());
 	}
 }
