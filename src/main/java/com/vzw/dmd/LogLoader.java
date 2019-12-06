package com.vzw.dmd;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.*;

import com.vzw.dmd.util.DMDLogs;

import java.util.*;

/**
 * @version 	1.0
 * @author
 */
public class LogLoader extends HttpServlet implements Servlet
{
	static String propPath = null;
	static String propFile = null;
	/**
	* @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	*/
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException
	{
		defaultAction(req, resp);
	}

	/**
	* @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	*/
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException
	{
		defaultAction(req, resp);
	}

	/**
	* @see javax.servlet.http.HttpServlet#void (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	*/
	public void defaultAction(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException
	{
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();
		String act = req.getParameter("action");
		if (act != null && act.trim().equalsIgnoreCase("reload"))
		{
			DMDLogs.initLogs();
		}
		printLogs(out);
	}

	private void printLogs(PrintWriter out)
	{
		Logger rL = Logger.getRootLogger();
		out.println(
			"Root Logger = <b>"
				+ rL.getName()
				+ "</b>, Level = <b>"
				+ rL.getLevel()
				+ "</b><br>");

		out.println("<br><br>Loggers:<br>");
		Enumeration en = LogManager.getCurrentLoggers();
		while (en.hasMoreElements())
		{
			Logger L = (Logger) en.nextElement();
			out.println(
				"Logger Name = <b>"
					+ L.getName()
					+ "</b>, Level = <b>"
					+ L.getLevel()
					+ "</b><br>");
		}

	}
	/**
	* @see javax.servlet.GenericServlet#void ()
	*/
	public void init() throws ServletException
	{
		super.init();
		propPath = System.getProperty("PROPPATH");
		propFile = propPath + File.separator + "log4j.properties";
		System.out.println("Log4j Property File is : " + propFile);
		//		initLog();
	}

	//	private void initLog()
	//	{
	//		File lFile = new File(propFile);
	//		if (lFile.exists())
	//		{
	//			PropertyConfigurator.configure(propFile);
	//		}
	//		else
	//		{
	//			Properties logProperties = new Properties();
	//			logProperties.put("log4j.rootLogger", "DEBUG, A1");
	//			logProperties.put("log4j.appender.A1" , "org.apache.log4j.ConsoleAppender");
	//			logProperties.put("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
	//			logProperties.put("log4j.appender.A1.layout.ConversionPattern", "%-4r [%t] %-5p %c %x - %m%n");
	//			PropertyConfigurator.configure(logProperties);
	//		}
	//	}
}
