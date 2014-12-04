package jnu.mindsharing.testsuite;

import jnu.mindsharing.chainengine.MappingGraphDrawer;
import jnu.mindsharing.chainengine.Sense;
import jnu.mindsharing.common.HList;
import jnu.mindsharing.common.P;

/**
 * MappingGraphDrawer.java 를 테스트하기 위한 모듈
 * java -jar mindsharing.jar jnu.mindsharing.testsuite.MappingGraphDrawerTest 로 호출가능
 * 
 * 결과물로 현재 폴더에서 graph.jpg 파일을 만들어냅니다.
 * @author nidev
 *
 */
public class MappingGraphDrawerTest
{
	static String TAG = "MGDTest";

	public static void main(String[] args)
	{
		P.d(TAG, "감정어휘 매핑 그래프 도구를 테스트합니다. Sense 모듈의 데이터가 필요합니다.");
		Sense ss_mgtest = new Sense();
		P.d(TAG, "데이터 수신 중....");
		HList results = ss_mgtest.genearteNewdexMap();
		ss_mgtest.closeExplicitly();
		
		P.d(TAG, "총 %d건이 수신됨. 그래프를 작성합니다.", results.size());
		
		MappingGraphDrawer mgd = new MappingGraphDrawer();
		mgd.drawXYcoordinates();
		mgd.drawEmotionalWords(results);
		mgd.writeImage();
		P.d(TAG, "테스트 종료");
	}
}
