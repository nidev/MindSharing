package jnu.mindsharing.hq;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import jnu.mindsharing.chainengine.MappingGraphDrawer;
import jnu.mindsharing.chainengine.Sense;
import jnu.mindsharing.common.HList;
import jnu.mindsharing.common.P;

public class ServiceThread extends Thread
{
	public final static String graphFile = "graph.jpg";
	public final static String digestFile = "digest.txt";
	
	final long everyseconds = 60;
	@Override
	public void run()
	{
		Sense ss = new Sense();
		
		try
		{
			while (true)
			{
				P.d("ServiceThread", "그래프와 요약 파일을 갱신합니다...");
				// 학습 모듈 연결 초기화
				
				// _graph.jpg 에 기록하고 완료되면 graph.jpg를 대체하는 방법으로
				
				// --- 이미지
				
				HList wl = ss.genearteNewdexMap();
				
				MappingGraphDrawer mgp = new MappingGraphDrawer();
				mgp.drawEmotionalWords(wl);
				mgp.writeImage(graphFile);
				// --- 문자
				// 문자 요약에는 작성 시간을 삽입.
				
				String digestText = ss.generateNewdexTableDigest(wl);
				File digest = new File(digestFile);
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(digest), "UTF-8"));
				bw.write("<!doctype html><html lang='ko'><head><meta charset='utf-8'/></head><body><pre>");
				bw.write(digestText);
				bw.write("</pre></body></html>");
				bw.close();
				
				P.d("ServiceThread", "그래프와 요약 파일을 갱신하였습니다. %d초 후에 재갱신합니다.", everyseconds);
				sleep(1000*everyseconds);
			}
		}
		catch (InterruptedException | IOException e)
		{
			P.e("ServiceThread", "서비스 쓰레드 오류 발생: %s", e.toString());
		}
		finally
		{
			ss.closeExplicitly();
		}
	}

}
