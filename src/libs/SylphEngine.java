package libs;

/**
 * 분석 엔진 '실프' 스케치
 * ----
 * 클래스 ContextFragment: '문단' 분석 결과 반환에 사용
 * 함수 long .getElapsed() (BaseFragment에 없음)
 * -분석에 소요된 시간 반환(밀리초)
 * 함수 String .getSolutionInHtml() (BaseFragment에 없음)
 * -분석 과정과 결과를 html 형식 스트링으로 반환
 * 
 * ----
 * 클래스 BaseFragment: 분석결과 사용하는 기본 클래스. SentenceFragment, WordFragment, ContextFragment가 모두 상속함
 * 클래스 SentenceFragment: AnalysisResults에 들어가는 문장 분석 결과물들
 * 함수 ArrayList<String> .getWords()
 * 함수 String .getRawText()
 * 함수 int .getAverageEmotionVector()
 * 함수 Hash<String, int> .getEmotionVectors()
 * 함수 WordFragment .getNextFragment() // 문맥 프래그먼트는 문장을, 문장 프래그먼트는 단어를 반환한다.
 * 함수 int .lengthFragments() // 전체 프래그먼트 갯수
 * 함수 int .lengthRemainingFragments() // getNextFragment()가 가능한 앞으로 갯수 
 * 함수 boolean .isNextFragmentOK() // 
 * 함수 void .resetNextFragmentPosition()
 * 함수 void .setIgnoreThis
 * -SentenceFragment에 분석된 결과를 
 * 
 * ----
 * 클래스 SylphEngine: 엔진 클래스
 * 
 * 함수 static void .initEngine()
 * -엔진처리에 필요한 DB파일과 메모리 상태 점검
 * 함수 ContextFragment analyze(String text, long max_time)
 * -분석 시작 함수. 반드시 new SylphEngine().analyze() 으로 실행
 * ----
 * 
 * @author nidev
 *
 */
public class SylphEngine
{
	final static boolean USE_THREAD = true; // 쓰레드 사용 여부
	final static int MAX_THREADS = 20; // 최대 쓰레드 갯수
	final static int MAX_SUB_FRAGMENTS = 50; // 한 프래그먼트가 가질 수 있는 작은 프래그먼트 갯수
	final static int ENGINE_VERSION_MAJOR = 0; // 엔진 버전 xx.yyy 중 xx
	final static int ENGINE_VERSION_MINOR = 1; // 엔진 버전 xx.yyy 중 yyy
	final static String DATABASE_FOLDER = "./database/"; // 데이터 베이스 폴더
	
	 
	

	public SylphEngine()
	{
		// TODO Auto-generated constructor stub
	}

}
