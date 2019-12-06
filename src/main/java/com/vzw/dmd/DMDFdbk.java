package com.vzw.dmd;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.vzw.dmd.util.DMDHtmlUtils;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;

/**
 * @version 	1.0
 * @author
 */
public class DMDFdbk extends HttpServlet
{
	private static Logger L =
		Logger.getLogger(DMDLogs.getLogName(DMDFdbk.class));
	/**
	* @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	*/
	public void doGet(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException
	{
		defaultAction(req, res);
	}

	/**
	* @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	*/
	public void doPost(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException
	{
		defaultAction(req, res);
	}

	/**
	* @see javax.servlet.GenericServlet#void ()
	*/
	public void init() throws ServletException
	{
		super.init();
	}

	/**
	* @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	*/
	public void defaultAction(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException
	{
		String action = req.getParameter("action");
		ServletOutputStream sout = res.getOutputStream();
		if (action == null || action.trim().equals(""))
		{
			// Show feedback form.
			DMDHtmlUtils.print_header(sout);
			DMDHtmlUtils.print_left_bar(req, sout);
			DMDHtmlUtils.print_feedback_form(req, sout);
		}
		else
		{
			// process submit.
			String message = "Message: " + "\n" + req.getParameter("message");
			String needToBeContacted = req.getParameter("contact_flag");
			String name = req.getParameter("name");
			String email = req.getParameter("email");
			String phone = req.getParameter("phone");
			String ext = req.getParameter("extension");
			String location = req.getParameter("location");

			message += "\n\n\nWould like to be contacted: "
				+ needToBeContacted
				+ "\nName: "
				+ name
				+ "\nEmail: "
				+ email
				+ "\nphone: "
				+ phone
				+ "\next: "
				+ ext
				+ "\nlocation: "
				+ location;

			sendMail("DMD Feedback", message);

			//getServletContext().log("message: " + message);

			String msg =
				" Your feedback has been sent to DMD support team. Thank you!";
			DMDHtmlUtils.print_header(sout);
			DMDHtmlUtils.print_left_bar(req, sout);
			DMDHtmlUtils.printRightAreaWithMsg(sout, msg);
		}
	}
	private void sendMail(String subject, String mailMsg)
	{
		//String host = "HQBEDIGW1.corp.bam.com";	  
		String host = DMDProps.getProperty("mail_host");
		if (host == null || host.trim().equals(""))
			host = "mre.odc.vzwcorp.com";
		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		Session session = Session.getDefaultInstance(props, null);
		try
		{
			Message msg = new MimeMessage(session);
			//String mailaddress = "lian.zhou@verizonwireless.com";
			// String mailaddress = "9732191001@msg.myvzw.com";
			//String mailaddress = "david.lee1@verizonwireless.com";		  
			msg.setFrom(new InternetAddress("DMDFeedback"));
			Object[] fdbk_list = DMDProps.getFdbkList();
			InternetAddress address[] = new InternetAddress[fdbk_list.length];
			for (int i = 0; i < fdbk_list.length; i++)
			{
				address[i] = new InternetAddress((String) fdbk_list[i]);
			}
			msg.setRecipients(javax.mail.Message.RecipientType.TO, address);
			msg.setSubject(subject);
			msg.setSentDate(new java.util.Date());
			msg.setText(mailMsg);
			Transport.send(msg);
		}
		catch (MessagingException mex)
		{
			L.error("Error during send mail", mex);
			getServletContext().log(mex.toString());
		}
	}

}
