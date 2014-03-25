package jnu.mindsharing.legacy.ui;
/*
 * Swing(자바의 GUI 도구)은 버튼 클릭이나 메뉴 선택 등의 이벤트를 ActionCommand라는 시스템을 통해서
 * 주고 받는다.
 * 
 * 따라서, 어떤 버튼을 눌렀을 때 팝업을 띄우고 싶으면, 이걸 눌렀을 때 발생시킬 ActionCommand를 정의해놓고,
 * ActionCommand가 도착했을 때 실행되는 함수인 actionPerformed 같은 함수에서 처리할 수 있다.
 * 
 * 이때 처리는, actionPerformed에서 넘어온 문자열이 무엇인지 확인하는 것.
 * 
 * 참쉽죠? - 밥 로스
 */

/**
 * 여기에서는 MindsharingUI 에서 쓸 액션 커맨드들을 정의해놓는다.
 * @author nidev
 */
public class MindSharingUIActions
{
	// 버튼
	final String BUTTON_CLEAR = "button_clear";
	final String BUTTON_ANALYZE = "button_analyze";
	
	// 탭
	final String TAB_MAIN_OPEN = "tab_main_open";
	final String TAB_LOG_OPEN = "tab_log_open";
	
	// 메뉴 > 파일
	final String MENU_FILE_OPEN = "menu_file_open";
	final String MENU_FILE_SAVE_RESULTS = "menu_file_save_results";
	final String MENU_FILE_EXIT = "exit";
	
	// 메뉴 > 데이터
	final String MENU_DATA_LAUNCH_TOOL = "menu_data_launch_tool";
	
	// 메뉴 > 정보
	final String MENU_INFO_VERSION = "menu_info_version";
	final String MENU_INFO_CREDIT = "menu_info_credit";
	final String MENU_INFO_HELP = "menu_info_help";
}
