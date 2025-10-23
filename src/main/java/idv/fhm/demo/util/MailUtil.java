package idv.fhm.demo.util;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class MailUtil {
	final static String username = "";
	final static String password = "";
//	public static void main(String[] args) {
//		// 你的 Brevo SMTP 帳密
//
//
//		// 收件人、主旨與內容
////		String toEmail = "pkugmeng@gmail.com";
//	
//
//		sendMail(toEmail, "http://localhost:8080/activate?activateStr=kxbJ%2F5GqjSDOKT3x%2B8PdBafy44GA5lrCk785aXyZe0XS4z6qdcTwL0uYNm9L9ZqQxZf%2FupFgbCrtBtsgBxUNqSc2VujTe38LVC8XLn0k7Omb67Fk9tcKusa30n1HoioHgkIFIeRNFKi3ayOp77vfEw2kt82KrSCDfcaL9Z9B97KDTrgcVfAq8b%2Fp4qjPGrPVs9Y6q%2F6dMPD%2B1OVDMU4%2FZRXdZZY6aJRgAoiKunRHCI%2BRNQ8cQ1UxleHPulLcUE02oRUpHdcQXKWS%2Fs2Afy7yDoBRFliuX7y6ZALnjl9tXcaJbDAeEy3h97RtkUws2%2BFEF%2BPWZWVnsfdRo5qJ0NHiOA%3D%3D");
//	}

	/**
	 * 傳送 HTML 格式的啟用帳號郵件
	 *
	 * @param to           收件人 email
	 * @param activateLink 啟用帳號的連結
	 */
	public static void sendMail(String to, String activateLink) {
		// SMTP 設定
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp-relay.brevo.com");
		props.put("mail.smtp.port", "587");
		props.put("mail.smtp.ssl.trust", "smtp-relay.brevo.com");

		Session session = Session.getInstance(props, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		try {
			// 建立 MimeMessage
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("pkugmeng020@gmail.com", "Support Team")); // 可加顯示名稱
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			message.setSubject("Activate Your Account");

			// HTML 郵件內容
			String htmlContent = """
					<html>
					    <body style="font-family: Arial, sans-serif; background-color: #f8f9fa; padding: 20px;">
					        <div style="max-width: 500px; margin: auto; background: #fff; border-radius: 8px; padding: 20px; text-align: center;">
					            <h2 style="color: #333;">Welcome!</h2>
					            <p style="font-size: 16px;">Thank you for registering. Please click the button below to activate your account:</p>
					            <a href="%s" style="display: inline-block; padding: 12px 25px; margin-top: 20px;
					               background-color: #007BFF; color: white; text-decoration: none;
					               border-radius: 6px; font-weight: bold;">Activate Account</a>
					            <p style="margin-top: 30px; color: #777; font-size: 14px;">
					                If the button doesn’t work, copy and paste this link into your browser:<br>
					                <a href="%s">%s</a>
					            </p>
					        </div>
					    </body>
					</html>
					"""
					.formatted(activateLink, activateLink, activateLink);

			message.setContent(htmlContent, "text/html; charset=UTF-8");

			// 寄出
			Transport.send(message);
			System.out.println("✅ HTML mail sent successfully to " + to);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendEmail(String username, String password, String toEmail, String subject, String content) {
		// 設定 SMTP 參數
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp-relay.brevo.com");
		props.put("mail.smtp.port", "587");
		props.put("mail.smtp.ssl.trust", "smtp-relay.brevo.com");

		// 建立驗證 Session
		Session session = Session.getInstance(props, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		try {
			// 建立郵件內容
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("pkugmeng020@gmail.com"));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
			message.setSubject(subject);
			message.setText(content);

			// 寄出郵件
			Transport.send(message);
			System.out.println("✅ Email sent successfully to " + toEmail);

		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
}
