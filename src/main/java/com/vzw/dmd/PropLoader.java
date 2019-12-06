package com.vzw.dmd;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.XSSEncoder;

import java.util.*;

/**
 * @version 	1.0
 * @author
 */
public class PropLoader extends HttpServlet implements Servlet
{

	static DMDProps props = null;
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
		String act = req.getParameter("action");
		PrintWriter out = resp.getWriter();
		resp.setContentType("text/html");
		if (act == null || act.trim().equals(""))
		{
			out.print(
				"<B><font color=\"red\">action parameter is null or empty. Usage: action=PRINT/RELOAD</font></B>");
			return;
		}
		if (act.trim().equalsIgnoreCase("print"))
		{
			printProps(out);
		}
		else
			if (act.trim().equalsIgnoreCase("reload"))
			{
				try
				{
					DMDProps.load();
				}
				catch (Exception e)
				{
					throw new ServletException(
						"Unable to load properties from: " + propFile);
				}
				out.print("<b>Reloaded Properties Successfully</b><br>");
				printProps(out);
			}
			else
			{
				out.print(
					"<B><font color=\"red\">Invalid action parameter "
						+ new XSSEncoder().encodeHTMLAttribute(act)
						+ ". Usage: action=PRINT/RELOAD</font></B>");
				return;
			}
	}

	public void printProps(PrintWriter out)
	{
		Properties props = DMDProps.getPropObject();
		out.println("<B>Properties:</B><BR>");
		Set keySet = props.keySet();
		TreeSet sortedKeys = new TreeSet(keySet);
		Iterator it = sortedKeys.iterator();
		while (it.hasNext())
		{
			String name = (String) it.next();
			String val = props.getProperty(name);
			out.println(name + " = <B>" + val + "</B><BR>");
		}
	}

	/**
	* @see javax.servlet.GenericServlet#void ()
	*/
	public void init() throws ServletException
	{
		super.init();
		//		DMDProps.load();
		//		if (!DMDProps.isLoaded())
		//			throw new ServletException ("Unable to load DMD properties");
	}

}
