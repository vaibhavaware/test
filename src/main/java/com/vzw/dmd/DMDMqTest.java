package com.vzw.dmd;

import java.io.IOException;
import java.text.Normalizer;

import javax.jms.BytesMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @version 	1.0
 * @author
 */
public class DMDMqTest extends HttpServlet
{

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
	* @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	*/
	public void defaultAction(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException
	{
		InitialContext ic = null;
		QueueConnectionFactory qcf = null;
		Queue queue = null;
		Queue rqueue = null;
		QueueConnection qconn = null;
		QueueSession qsess = null;
		QueueSender qsender = null;
		QueueReceiver qrcvr = null;
		String reqq = "java:comp/env/jms/DmdQueue";
		String resq = "java:comp/env/jms/DmdRQueue";

		String esn = req.getParameter("esn");
		String meid = req.getParameter("meid");
		if ((esn == null || esn.trim().equals(""))
			&& (meid == null || meid.trim().equals("")))
		{
			ServletOutputStream sout = res.getOutputStream();
			sout.println("Should provide esn or meid.");
			return;
		}
		String esnlock = req.getParameter("esnlock");
		System.err.println("esnlock = " + esnlock);
		String meidlock = req.getParameter("meidlock");
		System.err.println("meidlock = " + meidlock);
		String reqn = req.getParameter("reqn");
		String resn = req.getParameter("resn");
		String tran_id = req.getParameter("transaction_id");
		if (reqn != null && !reqn.trim().equals(""))
		{
			reqq += reqn.trim();
		}
		else
		{
			reqq += "1";
		}
		if (resn != null && !resn.trim().equals(""))
		{
			resq += resn.trim();
		}
		else
		{
			resq += "1";
		}

		System.err.println("reqq = " + reqq);
		System.err.println("resq = " + resq);
		try
		{
			ic = new InitialContext();
			qcf =
				(QueueConnectionFactory) ic.lookup("java:comp/env/jms/DmdQcf");
			//queue = (Queue) ic.lookup(reqq);
			//rqueue = (Queue) ic.lookup(resq);
			queue = (Queue) ic.lookup(Normalizer.normalize(reqq, Normalizer.Form.NFD));
			rqueue = (Queue) ic.lookup(Normalizer.normalize(resq, Normalizer.Form.NFD));
			qconn = qcf.createQueueConnection();
			qsess = qconn.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
			qsender = qsess.createSender(queue);
			String inp =
				"<?xml version=\"1.0\"?><ESN_Request>"
					+ "<App_Type>I2K</App_Type>";
			if (esn != null && !esn.equals(""))
			{
				inp += "<ESN>" + esn.trim() + "</ESN>";
				if (esnlock != null && !esnlock.trim().equals(""))
				{
					inp += "<ESN_LOCK>" + esnlock.trim() + "</ESN_LOCK>";
				}
			}
			else
			{
				inp += "<MEID>" + meid.trim() + "</MEID>";
				if (meidlock != null && !meidlock.trim().equals(""))
				{
					inp += "<MEID_LOCK>" + meidlock.trim() + "</MEID_LOCK>";
				}
			}
			if (tran_id != null && !tran_id.trim().equals(""))
			{
			    inp += "<TRANSACTION_ID>" + tran_id.trim() + "</TRANSACTION_ID>";
			}
			inp += "</ESN_Request>";

			TextMessage bMsg = qsess.createTextMessage();
			bMsg.setJMSReplyTo(rqueue);
			bMsg.setText(inp);
//			System.err.println("Setting Expiry as 6000");
//			bMsg.setJMSExpiration(60000);
//			System.err.println("Expiry = " + bMsg.getJMSExpiration());
//			System.err.println("Setting time to live");
//			System.err.println("Now = " + new Date());
//			qsender.setTimeToLive(60000);
			qsender.send(bMsg);
			qsess.commit();
			qrcvr = qsess.createReceiver(rqueue);
			BytesMessage brMsg = qsess.createBytesMessage();
			brMsg = (BytesMessage)qrcvr.receive();
			System.err.println("Rep Msg = " + brMsg.readUTF());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (qconn != null)
					qconn.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	/**
	* @see javax.servlet.GenericServlet#void ()
	*/
	public void init() throws ServletException
	{

		super.init();

	}

}
