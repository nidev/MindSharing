/**
 * 
 */
package jnu.mindsharing.hq;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.concurrent.ConcurrentMap;

import jnu.mindsharing.chainengine.ChainEngine;
import jnu.mindsharing.chainengine.MappingGraphDrawer;
import jnu.mindsharing.chainengine.ResultProcessor;
import jnu.mindsharing.chainengine.Sense;
import jnu.mindsharing.common.ApplicationInfo;
import jnu.mindsharing.common.HList;
import jnu.mindsharing.common.P;

import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.representation.FileRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * 웹 API 서비스를 위한 RESTful API 서버 (Restlet 기반)
 * 
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
	
	/**
	 * 생성자, 캐시를 위한 해시 테이블을 생성함
	 */
	public RESTServer()
	{
		rpcache = new HashMap<String, ResultProcessor>();
	}

	/**
	 * Request 카운터를 증가
	 */
	private synchronized void addReq()
	{
		P.d(TAG, "-- Current API Queue: %d/10 --", getReqs());
		current_requests += 1;
	}
	
	/**
	 * Request 카운터를 감소
	 */
	private synchronized void subReq()
	{
		P.d(TAG, "-- Current API Queue: %d/10 --", getReqs());
		current_requests -= 1;
	}
	
	/**
	 * 처리 중인 Request 수를 반환
	 */
	private synchronized int getReqs()
	{
		return current_requests;
	}
	
	/**
	 * Request 수가 제한 값에 도달했는지 확인
	 * @return 요청 제한 초과시 true, 이외에 false
	 */
	private synchronized boolean isMaxReq()
	{
		return current_requests >= max_requests;
	}
	
	/**
	 * 서버 버전 코드를 반환
	 * @return 서버 버전 코드
	 */
	@Override
	public String getVersionCode()
	{
		return versionCode;
	}

	/**
	 * 서버 버전 넘버를 반환
	 * @return 서버 버전 넘버
	 */
	@Override
	public int getVersionNumber()
	{
		return versionNumber;
	}

	/**
	 * 라이브러리 저작권 정보를 제공한다.
	 * @return 저작권 정보가 담긴 텍스트
	 */
	@Override
	public String getLicenseInfo()
	{
		return "Restlet Framework (http://restlet.org/download/legal)를 LGPL License하에 사용합니다.";
	}
	
	/**
	 * 서버 내부의 엔진 객체를 획득한다.
	 * @return ChainEngine 객체
	 */
	public ChainEngine getEngine()
	{
		return engineObject;
	}
	
	/**
	 * API 서버를 실행한다. 동작 도중 오류 발생시 Exception을 발생시킨다. 이 부분에서, 각 요청 주소에 대한 작업이 모두 정의되어있다.
	 * @param ce ChainEngine 객체
	 */
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
							String source;
							String req_id;
							
							source = body.split("text=")[1];
							req_id = String.format("r%x", req.hashCode());
							// null 이 들어간 동안은 임시 데이터이다.
							// 작업 처리 중을 의미한다. 작업이 완료되면 진짜 ResultProcessor 객체가 들어간다.
							rpcache.put(req_id, null);
							res.setEntity("{\"error\":\"success\", \"error_msg\":\"Analysis on progress\", \"data\":[\""+req_id+"\"]}", MediaType.APPLICATION_JSON);
							res.commit(); // 현재 응답을 커밋하고, 후에 처리를 함.
							try
							{
								rp = getEngine().analyze(source);
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
							res.setEntity("{\"error\":\"fail\", \"error_msg\":\"Invalid encoding. must be UTF-8.\", \"data\":[]}", MediaType.APPLICATION_JSON);
						}
						
					}
					catch (IOException e)
					{
						res.setEntity("{\"error\":\"fail\", \"error_msg\":\"Broken Text\", \"data\":[]}" , MediaType.APPLICATION_JSON);
					}
				}
				else
				{
					res.setEntity("{\"error\":\"fail\", \"error_msg\":\"Use POST method to give the server data.\", \"data\":[]}", MediaType.APPLICATION_JSON);
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
							res.setEntity("{\"error\":\"wait\", \"error_msg\":\"Processing Now.\", \"data\":[]}", MediaType.APPLICATION_JSON);
						}
						else
						{
							if (req_type.equals("json"))
							{
								res.setEntity(rpcache.get(req_id).toJSON(), MediaType.APPLICATION_JSON);
							}
							else
							{
								// XXX: 이 동작은 예측할 수 없다.
								res.setEntity(String.format("/* %s */", rpcache.get(req_id).toTXT()), MediaType.APPLICATION_JSON);
							}
						}
					}
					else
					{
						res.setEntity("{\"error\":\"fail\", \"error_msg\":\"No data found\", \"data\":[]}", MediaType.APPLICATION_JSON);
					}
				}
				else
				{
					res.setEntity("{\"error\":\"fail\", \"error_msg\":\"POST is not allowed for requesting a result.\", \"data\":[]}", MediaType.APPLICATION_JSON);
				}
			}
		};
		
		
		Restlet rtWebConsole = new Restlet(getContext()) {
			@Override
			public void handle(Request req, Response res)
			{
				if (req.getMethod() == Method.GET)
				{
					try
					{
						InputStream html = RESTServer.class.getResourceAsStream("/jnu/mindsharing/hq/webconsole.html");
						BufferedReader reader = new BufferedReader(new InputStreamReader(html, "UTF-8"));
						StringBuffer s = new StringBuffer();
						String temp;
						
						while ((temp = reader.readLine()) != null)
						{
							s.append(temp);
							s.append("\r\n");
						
						}
						res.setEntity(s.toString(), MediaType.TEXT_HTML);
					}
					catch (UnsupportedEncodingException e)
					{
						res.setEntity("Broken Encoding. Does your Java support UTF-8?", MediaType.TEXT_PLAIN);
						e.printStackTrace();
					}
					catch (IOException e)
					{
						res.setEntity("Cannot read webconsole.html from the package", MediaType.TEXT_PLAIN);
						e.printStackTrace();
					}

					
				}
			}
		};
		
		Restlet rtMachineLearningDigest = new Restlet(getContext()) {
			@Override
			public void handle(Request req, Response res)
			{
				Sense ss = new Sense();
				String digestText = ss.generateNewdexTableDigest();
				ss.closeExplicitly();

				if (digestText != null)
					res.setEntity(digestText, MediaType.TEXT_PLAIN);
				else
					res.setEntity("Fail to retrieve digest.", MediaType.TEXT_PLAIN);
			}
		};
		
		Restlet rtMachineLearningMap = new Restlet(getContext()) {
			@Override
			public void handle(Request req, Response res)
			{
				Sense ss = new Sense();
				HList hl = ss.genearteNewdexMap();
				ss.closeExplicitly();
				
				MappingGraphDrawer mgp = new MappingGraphDrawer();
				mgp.drawEmotionalWords(hl);
				mgp.writeImage();
				
				File graphjpg = new File("./graph.jpg");
				if (graphjpg.exists())
					res.setEntity(new FileRepresentation("./graph.jpg", MediaType.IMAGE_JPEG));
				else
					res.setEntity("Not available: graph.jpg", MediaType.TEXT_PLAIN);
			}
		};
		
		component.getDefaultHost().attach("/digest", rtMachineLearningDigest);
		component.getDefaultHost().attach("/map", rtMachineLearningMap);
		component.getDefaultHost().attach("/console", rtWebConsole);
		component.getDefaultHost().attach("/new", rtAnalyzer);
		component.getDefaultHost().attach("/get/{id}/{type}", rtSendResult);
		component.getDefaultHost().attach("/get/{id}", rtSendResult);
		component.getDefaultHost().attach("/test", rtTest);
		component.getDefaultHost().attach("/", RESTServer.class);
		component.start();
	}
	
	
	/**
	 * 서버 가동 상태를 반환한다.
	 * @return 가동 중이면 true, 이외에 false
	 */
	public boolean isStarted()
	{
		return srv.isStarted();
	}
	
	/**
	 * 서버에 처음 접속했을때 보여주는 텍스트 페이지 내용을 반환한다.
	 * @return 첫 화면에 나올 텍스트
	 */
	@Get
    public String toHTML()
	{
        return "Welcome to Chain Engine API Server.\r\n(Emotional data analyzer for Korean Language)\r\nHost here provides below services:\r\n=========================================\r\n"
        		+ "GET /map: Mapping Graph made by Sense module (JPEG file)\r\nGET /digest : Digest of Sense Status in text (machine learning)\r\nGET /console : Web console to test the engine(View json object, get raw analysis data)\r\nGET /test : For testing purpose\r\nPOST /new : Invoke new task of analizing. Returning id.\r\nGET /get/{id}/{type} : Get results of a certain job. Type can be either txt or json.";
    }
}
