package com.vzw.dmd;

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;

public class DMDMqClient {
   
	private static Logger L =Logger.getLogger(DMDLogs.getLogName(DMDMqClient.class));

public void sendMessageToQueue(String xmlStr) { 
	L.debug("in sendMessageToQueue()");
	String queueName = DMDProps.getProperty("DMD_CASS_QUEUE_NAME");
	L.info("DMD_CASS_QUEUE_NAME-"+queueName);
	L.debug("DMD_CASS_QUEUE_NAME-"+queueName);
	InitialContext ic = null;
	QueueConnectionFactory defQcf = null;
	QueueConnection qconn = null;
	QueueSession qsess = null;
	QueueSender qsender = null;
	try{
	ic = new InitialContext();
	defQcf = (QueueConnectionFactory) ic.lookup("jms/DmdQcf");
	qconn = defQcf.createQueueConnection(); 
	qconn.start();
	L.debug("connection : "+qconn);
	qsess = qconn.createQueueSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
	
	L.debug("Queue connection factory-"+defQcf);	
	L.debug("Sending msg  -----------\n" + xmlStr + "\n-------------");
	
	Queue mqQueue = (Queue)qsess.createQueue(queueName);
	L.debug("Queue-"+mqQueue);
	
	qsender = qsess.createSender(mqQueue);
	
	String encoding = DMDProps.getProperty("encoding");
	if (encoding == null || encoding.trim().equals(""))
		encoding = "785";
	String ccsid = DMDProps.getProperty("CCSID");
	if (ccsid == null || ccsid.trim().equals(""))
		ccsid = "37";

	TextMessage tMsg = qsess.createTextMessage();
	tMsg.setText(xmlStr);
	tMsg.setStringProperty("JMS_IBM_Format", "MQSTR");
	tMsg.setIntProperty("JMS_IBM_MsgType", 2);
	qconn.start();
	
	qsender.send(tMsg);
	L.debug("Outgoing msg =" + xmlStr);
	L.debug("Outgoing msg details =" + tMsg);
	L.debug("Reply successfully sent");
    } catch (Exception e) {
    	System.out.println("ERROR:"+e);
		L.error("Exception", e);
	} finally {
		try {
			
			if(null != qsender)
				qsender.close();
			if(null != qsess)
				qsess.close();
			if (qconn != null)
				qconn.close();
		} catch (Exception e) {
			L.error("Exception", e);
		}
	} 
}
}
	

