package org.javiervegas.mail

import java.util.Properties
import javax.mail._
import javax.mail.internet._
import java.io.{OutputStream,File}
import javax.activation.{DataHandler,FileDataSource}

class JavaxMailer(from: String ,to: String, subject :String, body: String, attachments: List[File]) extends Mailer {
  
  
  def send = {
      val props = new Properties
      props.put("mail.smtp.host","10.142.15.30")
      val message = new MimeMessage(Session.getDefaultInstance(props,null))
      message.setFrom(new InternetAddress(from))
      message.addRecipient(Message.RecipientType.TO,new InternetAddress(to))
      message.addRecipient(Message.RecipientType.TO,new InternetAddress("from"))
      message.setSubject(subject)
      
      val bodyPart = new MimeBodyPart
      bodyPart.setText(body)
      
      val multipart = new MimeMultipart
      multipart.addBodyPart(bodyPart)
      
      attachments.map(getPart(_)).foreach(addPart(_))
      
      def addPart(attachment: MimeBodyPart) = {
        multipart.addBodyPart(attachment)
      }
      
      message.setContent(multipart)
      Transport.send(message)
      false
  }
  
  def getPart(attachment: File) = {
        val attachedPart = new MimeBodyPart
        attachedPart.setDataHandler(new DataHandler(new FileDataSource(attachment)))
        attachedPart.setFileName(subject+".gif")
        attachedPart
  }
    
}