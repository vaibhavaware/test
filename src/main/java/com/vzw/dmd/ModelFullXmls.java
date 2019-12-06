/*
 * Created on May 21, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.vzw.dmd;

import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;

import javax.naming.InitialContext;

import oracle.panama.core.xml.XML;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.vzw.dmd.ejb.FullXmlLookupLocal;
import com.vzw.dmd.ejb.FullXmlLookupLocalHome;
import com.vzw.dmd.util.DMDLogs;
import com.vzw.dmd.util.DMDProps;
import com.vzw.dmd.util.DMDRefData;
import com.vzw.dmd.valueobject.EsnLookupRequestVO;

/**
 * @author c0edhab
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ModelFullXmls
{
    private static Logger L = Logger.getLogger(DMDLogs.getLogName(ModelFullXmls.class));
    HashMap finalMap = null;
    HashMap backupMap = null;
    
    boolean loaded = false;
    boolean backupLoaded = false;
    boolean backupStarted = false;
    
    Date lastCache = null;
    
    private static ModelFullXmls _inst = new ModelFullXmls();
	FullXmlLookupLocalHome ejbHome = null;
	InitialContext ic = null;
	FullXmlLookupLocal ejb = null;
	
	private static final String LOCKSTR = "LOCK";

    
    
    private ModelFullXmls()
    {
		try
		{
		    ic = new InitialContext();
			ejbHome =
				(FullXmlLookupLocalHome) ic.lookup(
					"java:comp/env/ejb/FullXmlLookup");
			ejb = ejbHome.create();
		}
		catch (Exception e)
		{
		    L.error ("Loading of full xml lookup bean failed.", e);
		}
    }
    
    public static String getModelXml(String model)
    {
        String retXml = null;
        L.debug("getModelXml(): model = " + model + "; loaded = " + _inst.loaded);
        //System.err.println("loaded = " + _inst.loaded + "; backupStarted = " + _inst.backupStarted + "; backupLoaded = " + _inst.backupLoaded);
        //System.err.println("lastCache = " + _inst.lastCache);
        
        if (_inst.loaded)
        {
            Date now = new Date();
            //System.err.println("Model = " + model + "; now = " + now);
            long cacheDiff = now.getTime() - _inst.lastCache.getTime();
            //System.err.println("Model = " + model + "; cacheDiff = " + cacheDiff);
            
            // Get the reload diff from props
            
            // Check if the cacheDiff is greater than the one in props
            // if so, start a thread to reload the cache.
            if (cacheDiff > DMDProps.fullModelCache)
            {
                L.info ("Cache expired at , " + now);
                synchronized(LOCKSTR)
                {
                    if (!_inst.backupStarted)
                    {
                        // Calculate the cache again due to thread race cond.
                        long newDiff = now.getTime() - _inst.lastCache.getTime();
                        //System.err.println("Model = " + model + "; newDiff = " + newDiff);
                        if (newDiff == cacheDiff)
                        {
                            L.info("Starting loader at, " + now);
                            Thread loader = new Thread(_inst.new ModelLoader());
                            loader.setDaemon(true);
                            _inst.backupStarted = true;
                            _inst.backupLoaded = false;
                            loader.start();
                        }
                    }
                    else if (_inst.backupLoaded)
                    {
                        // Calculate the cache again due to thread race cond.
                        long newDiff = now.getTime() - _inst.lastCache.getTime();
                        //System.err.println("Model = " + model + "; newDiff = " + newDiff);
                        if (newDiff == cacheDiff && _inst.backupMap != null)
                        {
	                        L.info("backup finished, now = " + now);
	                        _inst.finalMap = (HashMap)_inst.backupMap.clone();
	                        _inst.backupMap = null;
	                        _inst.lastCache = new Date();
	                        _inst.loaded = true;
	                        _inst.backupStarted = false;
	                        _inst.backupLoaded = false;
                        }
                    }
                }
            }
            if (_inst.finalMap.containsKey(model))
            {
                retXml = (String)_inst.finalMap.get(model);
            }
        }
        else
        {
            synchronized (LOCKSTR)
            {
                if (!_inst.backupStarted)
                {
                    L.info ("Starting loader Thread for first time, at " + new Date());
                    Thread loader = new Thread(_inst.new ModelLoader());
                    loader.setDaemon(true);
                    _inst.backupStarted = true;
                    _inst.backupLoaded = false;
                    loader.start();
                }
                else if (_inst.backupLoaded)
                {
                    L.info("It appear that backup Finished, at "+ new Date());
                    _inst.finalMap = (HashMap)_inst.backupMap.clone();
                    _inst.backupMap = null;
                    _inst.lastCache = new Date();
                    L.info ("Setting lastCache first time, " + _inst.lastCache);
                    _inst.loaded = true;
                    _inst.backupStarted = false;
                    _inst.backupLoaded = false;
                }
            }
        }
        return retXml;
    }
    
    class ModelLoader implements Runnable
    {
        
        public void run()
        {
            L.info ("Inside ModelLoader. run()");
            //System.err.println("Inside run(): ejb = " + ejb);
            Object [] models = DMDRefData.getModels();
            backupMap = new HashMap(models.length);
            for (int i = 0; i < models.length; i++)
            {
                String aModel = (String)models[i];
                EsnLookupRequestVO lookVO = new EsnLookupRequestVO();
                lookVO.setId(aModel);
                lookVO.setIdType("MODEL");
                try
                {
                    Document retDoc = ejb.getFullXml(lookVO);
                    Element rootEle = retDoc.getDocumentElement();
                    if (!rootEle.getTagName().equalsIgnoreCase("equipment"))
                    {
                        StringWriter outWri = new StringWriter();
                        XML.printWithFormat(outWri, retDoc, "UTF-8");
                        L.info("Putting model in back, i = " + i + "; aModel = " + aModel);
                        backupMap.put(aModel, outWri.toString());
                    }
                }
                catch (Exception e)
                {
                    L.error ("ModelLoader(): aModel = " + aModel, e);
                }
            }
            backupLoaded = true;
        }
    };
}
