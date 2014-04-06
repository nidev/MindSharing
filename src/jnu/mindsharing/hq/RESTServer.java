/**
 * 
 */
package jnu.mindsharing.hq;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.concurrent.ConcurrentMap;

import jnu.mindsharing.chainengine.CEResultObject;
import jnu.mindsharing.chainengine.ChainEngine;
import jnu.mindsharing.common.ApplicationInfo;

import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * @author nidev
 *
 */
public class RESTServer extends ServerResource implements ApplicationInfo
{
	final String versionCode = "happy";
	final int versionNumber = 1;
	private String TAG = "REST";
	private ChainEngine engineObject;
	private Server srv;
	private int current_requests = 0;
	private int max_requests = 5;

	private synchronized void addReq()
	{
		current_requests += 1;
	}
	
	private synchronized void subReq()
	{
		current_requests -= 1;
	}
	
	private synchronized boolean isMaxReq()
	{
		return current_requests >= max_requests;
	}
	
	@Override
	public String getVersionCode()
	{
		return versionCode;
	}

	@Override
	public int getVersionNumber()
	{
		return versionNumber;
	}

	@Override
	public String getLicenseInfo()
	{
		return "RESTServer.java utilizes Restlet Framework (http://restlet.org/download/legal) under LGPL License.";
	}
	
	public ChainEngine getEngine()
	{
		return engineObject;
	}
	
	public void run(ChainEngine ce) throws Exception
	{
		engineObject = ce;
		
		Component component = new Component();
		component.getServers().add(Protocol.HTTP, 8182);

		Restlet rtTest = new Restlet(getContext()) {
			@Override
			public void handle(Request req, Response res)
			{
				res.setEntity(req.toString(), MediaType.TEXT_PLAIN);
			}
		};
		
		Restlet rtAnalyzer = new Restlet(getContext()) {
			@Override
			public void handle(Request req, Response res)
			{
				addReq();
				
				if (req.getMethod() == Method.POST)
				{
					Representation entity = req.getEntity();
					String rawBody[] = null;
					String body = null;
					try
					{
						rawBody = entity.getText().split("=");
						body = URLDecoder.decode(rawBody[1], "UTF-8");
						if (body != null)
						{
							CEResultObject ceres = getEngine().analyze(body);
							res.setEntity(ceres.toJSON(), MediaType.TEXT_PLAIN);
						}
						else
						{
							res.setEntity("{error:1, msg:'No data'}", MediaType.TEXT_PLAIN);
						}
						
					}
					catch (IOException e)
					{
						res.setEntity("{error:1, msg:'Malformed url-encoded text'}", MediaType.TEXT_PLAIN);
					}
				}
				else
				{
					res.setEntity("{error:1, msg:'Method POST required'}", MediaType.TEXT_PLAIN);
				}
				subReq();
			}
		};
		
		Restlet rtSendResult = new Restlet(getContext()) {
			@Override
			public void handle(Request req, Response res)
			{
				if (req.getMethod() == Method.GET)
				{
					ConcurrentMap<String, Object> attrs = req.getAttributes();
					res.setEntity("Requested id is : " + attrs.get("id").toString(), MediaType.TEXT_PLAIN);
				}
			}
		};
		
		component.getDefaultHost().attach("/new", rtAnalyzer);
		component.getDefaultHost().attach("/get/{id}", rtSendResult);
		component.getDefaultHost().attach("/test", rtTest);
		component.getDefaultHost().attach("/", RESTServer.class);
		component.start();
	}
	
	public boolean isStarted()
	{
		return srv.isStarted();
	}
	
	@Get
    public String toString()
	{
        return "GET /test : For testing purpose\r\nPOST /new : Invoke new task of analizing. Returning id.\r\nGET /get/id : Get results of a certain job.";
    }
}
