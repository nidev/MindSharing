/**
 * 
 */
package jnu.mindsharing.hq;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.concurrent.ConcurrentMap;

import jnu.mindsharing.chainengine.ChainEngine;
import jnu.mindsharing.chainengine.ResultProcessor;
import jnu.mindsharing.common.ApplicationInfo;
import jnu.mindsharing.common.P;

import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
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
	private HashMap<String, ResultProcessor> rpcache;
	
	public RESTServer()
	{
		rpcache = new HashMap<String, ResultProcessor>();
	}

	private synchronized void addReq()
	{
		P.d(TAG, "-- Current API Queue: %d/10 --", getReqs());
		current_requests += 1;
	}
	
	private synchronized void subReq()
	{
		P.d(TAG, "-- Current API Queue: %d/10 --", getReqs());
		current_requests -= 1;
	}
	
	private synchronized int getReqs()
	{
		return current_requests;
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
		return "Restlet Framework (http://restlet.org/download/legal)를 LGPL License하에 사용합니다.";
	}
	
	public ChainEngine getEngine()
	{
		return engineObject;
	}
	
	public static String glueJSONPCallback(Request req, String json_data)
	{
		String callback_function_body = "%s(%s)";
		String callback_function = req.getResourceRef().getQueryAsForm().getFirstValue("callback");
		return String.format(callback_function_body, callback_function == null? "" : callback_function, json_data);
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
					String rawBody = null;
					String body = null;
					try
					{
						// TODO: 공용 서비스시 API 토큰 인식 기능
						rawBody = req.getEntityAsText();
						body = URLDecoder.decode(rawBody, "UTF-8");
						ResultProcessor rp = null;
						if (body != null)
						{
							String req_id;
							req_id = String.format("r%x", req.hashCode());
							// null 이 들어간 동안은 임시 데이터이다.
							// 작업 처리 중을 의미한다. 작업이 완료되면 진짜 ResultProcessor 객체가 들어간다.
							rpcache.put(req_id, null);
							res.setEntity(glueJSONPCallback(req, "{error:\"success\", error_msg:\"Request analysis without errors\", data:[\""+req_id+"\"]}") , MediaType.APPLICATION_JSON);
							res.commit(); // 현재 응답을 커밋하고, 후에 처리를 함.
							try
							{
								rp = getEngine().analyze(body);
								rp.addErrorInfo("success", "No error");
							}
							catch (Exception e) // 어떤 오류가 발생할지모르지만,
							{
								rp = new ResultProcessor(null);
								rp.addErrorInfo("fail", "Analyzer error.");
							}
							finally
							{
								rpcache.put(req_id, rp);
							}
						}
						else
						{
							res.setEntity(glueJSONPCallback(req, "{error:\"fail\", error_msg:\"Invalid encoding. must be UTF-8.\", data:[]}") , MediaType.APPLICATION_JSON);
						}
						
					}
					catch (IOException e)
					{
						res.setEntity(glueJSONPCallback(req, "{error:\"fail\", error_msg:\"Broken Text\", data:[]}") , MediaType.APPLICATION_JSON);
					}
				}
				else
				{
					res.setEntity(glueJSONPCallback(req, "{error:\"fail\", error_msg:\"Use POST method to give the server data.\", data:[]}") , MediaType.APPLICATION_JSON);
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
					String req_id;
					String req_type;
					req_id = attrs.get("id").toString();
					req_type = attrs.get("type").toString();
					
					P.d(TAG, "GET result requested on id=%s as type=%s", req_id, req_type);
					
					if (rpcache.containsKey(req_id))
					{
						if (rpcache.get(req_id) == null)
						{
							res.setEntity(glueJSONPCallback(req, "{error:\"wait\", error_msg:\"Processing Now.\", data:[]}") , MediaType.APPLICATION_JSON);
						}
						else
						{
							if (req_type.equals("json"))
							{
								res.setEntity(glueJSONPCallback(req, rpcache.get(req_id).toJSON()), MediaType.APPLICATION_JSON);
							}
							else
							{
								// XXX: 이 동작은 예측할 수 없다.
								res.setEntity(glueJSONPCallback(req, String.format("/* %s */", rpcache.get(req_id).toTXT())), MediaType.APPLICATION_JSON);
							}
						}
					}
					else
					{
						res.setEntity(glueJSONPCallback(req, "{error:\"fail\", error_msg:\"No data found\", data:[]}") , MediaType.APPLICATION_JSON);
					}
				}
				else
				{
					res.setEntity(glueJSONPCallback(req, "{error:\"fail\", error_msg:\"POST is not allowed for requesting a result.\", data:[]}") , MediaType.APPLICATION_JSON);
				}
			}
		};
		
		Restlet rtJSONExample = new Restlet(getContext()) {
			@Override
			public void handle(Request req, Response res)
			{
				if (req.getMethod() == Method.GET)
				{
					String json_data = "{\"name\": \"Charlie\", \"age\": 24, \"grades\": {\"1\": \"A\", \"2\":\"B\", \"3\":\"C\", \"4\":\"D\"}}";
					
					res.setEntity(glueJSONPCallback(req, json_data), MediaType.APPLICATION_JSON);
				}
			}
		};
		
		
		component.getDefaultHost().attach("/json_example", rtJSONExample);
		component.getDefaultHost().attach("/new", rtAnalyzer);
		component.getDefaultHost().attach("/get/{id}/{type}", rtSendResult);
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
        return "GET /test : For testing purpose\r\nGET /json_example : Get an example of JSON\r\nPOST /new : Invoke new task of analizing. Returning id.\r\nGET /get/id : Get results of a certain job.";
    }
}
