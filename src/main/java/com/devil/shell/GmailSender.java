package com.devil.shell;

import java.io.File;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.devil.utils.Base64Coder;

public class GmailSender {
	private Session session;
	private String subject;
	private String from;
	private Address[] cc;
	private Address[] receivers;
	private String htmlContent;
	private String textContent;
	private File affix;//附件
	
	public GmailSender(String user,String pwd){
		this.from=user;
		this.session = this.createSession(user,pwd);
	}
	/*************************************************/
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public void setReceivers(String... receivers){
		if(receivers!=null&&receivers.length>0){
			try {
				this.receivers = this.getAddress(receivers);
			} catch (AddressException e) {
				throw new RuntimeException(e);
			}
		}
	}
	public void setCc(String... cc) throws AddressException{
		if(cc!=null&&cc.length>0){
			this.cc = this.getAddress(cc);
		}
	}
	public void setHtmlContent(String htmlContent) {
		this.htmlContent = htmlContent;
	}
	public void setTextContent(String textContent) {
		this.textContent = textContent;
	}
	public void setAffix(File affix) {
		this.affix = affix;
	}

	public void sendSSLMessage()throws MessagingException {
		if(this.from==null||this.receivers==null||this.subject==null||this.receivers==null){
			throw new RuntimeException("啥都没有，还发什么邮件啊!");			
		}
		Message msg = this.createMessage(session);
		Transport.send(msg);
	}
	
	private Session createSession(final String from, final String pwd){
//		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.auth", "true");
		props.put("mail.debug", "true");
		props.put("mail.smtp.port", "465");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.fallback", "false");

		Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(from, pwd);
			}
		});

		session.setDebug(true);//此设定为可选，决定控制台是否是冗长输出
		return session;
	}
	 private Message createMessage(Session session) throws AddressException, MessagingException {
	       MimeMessage message = new MimeMessage(session);

	       // 加载发件人地址

	       message.setFrom(new InternetAddress(from));
	       message.setSentDate(new Date());
	       message.addRecipients(Message.RecipientType.TO, this.receivers);
	       if (this.cc!=null){
	           message.addRecipients(Message.RecipientType.CC,this.cc);
	       }

	       message.setSubject(this.subject);
	       
	       Multipart multipart = new MimeMultipart();
	       MimeBodyPart contentPart = new MimeBodyPart();
	       if(this.htmlContent!=null){
	    	   contentPart.setContent(htmlContent, "text/html;charset=UTF-8");
	       }else{
	    	   contentPart.setText(textContent, "UTF-8");
	       }
	       multipart.addBodyPart(contentPart);

	       if(this.affix!=null){
	    	   BodyPart bodyPart = new MimeBodyPart();
	    	   DataSource source = new FileDataSource(affix);
	    	   bodyPart.setDataHandler(new DataHandler(source));
	    	   String fileName = "=?UTF-8?B?"+ Base64Coder.encode(affix.getName().getBytes()) + "?=";

	    	   bodyPart.setFileName(fileName);

            multipart.addBodyPart(bodyPart);
	    	   
	       }
	       message.setContent(multipart);
	       message.saveChanges();
	       return message;
	    }
	 
	    private Address[] getAddress(String[] address) throws AddressException {
	        Address[] addrs = new InternetAddress[address.length];
	        for (int i = 0; i < address.length; i++)
	            addrs[i] = new InternetAddress(address[i]);
	        return addrs;
	     }
	    /******************************************************/
		public static void main(String args[]) throws Exception {
			GmailSender sender = new GmailSender("xingqisheng@gmail.com","s200592304");
			sender.setReceivers("xingqisheng@gmail.com");
			sender.setTextContent("hello,我");
			sender.setSubject("主题");
			sender.setAffix(new File("C:\\Users\\devil\\Desktop\\icon.jpg"));
			sender.sendSSLMessage();
		}
}