import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import libs.ELog;
import libs.SylphEngine;
import libs.fragments.BaseFragment;
import libs.fragments.ContextFragment;



public class MindSharingUI extends JFrame implements ActionListener, ChangeListener
{
	// 화면 크기 지정
	Dimension main_screen_size = new Dimension(480, 600);
	
	// 로그 자동 갱신용 쓰레드 작성
	private class LogUpdate extends TimerTask
	{
		@Override
		public void run()
		{
			try
			{
				Thread.sleep(200);
			}
			catch (InterruptedException e)
			{
				ELog.e(TAG, "로그 자동 갱신 타이머 실행 도중 오류가 발생했습니다.");
			}
			
			logOutputPane.setText(ELog.getFullBuffer());
		}
	}; 
	Timer t = null;
	
	// 시리얼라이징 ID
	private static final long serialVersionUID = 1L;
	
	// ELog 태그
	String TAG = "UI";
	
	// Action command 들
	MindSharingUIActions ac = new MindSharingUIActions();
	
	// 테이블 모델
	AbstractTableModel outputTableModel;
	
	// 핸들러가 필요한 UI 객체 들은 미리 전역 변수로 정의
	JTabbedPane maintab;
	JMenuBar menubar;
	JTextArea ta_input;
	JButton b_input;
	JButton b_clear;
	JTextArea ta_output;
	JPanel topPane;
	JTable outputTable;
	JScrollPane scrollPane_analyze;
	JEditorPane logOutputPane;
	
	// 테이블에 Fragment 객체에서 정보를 추출해 담음
	ArrayList<Object[]> rows = new ArrayList<Object[]>();
	
	public MindSharingUI()
	{
		/*
		 * 화면 설정
		 */
		// 화면 크기 조절 가능
		setResizable(true);
		// 최소 화면 크기 설정
		setMinimumSize(main_screen_size);
		// 종료버튼 누를 때 자동으로 종료
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// 창 제목 설정, 제목은 : MindSharing version. x.yyy
		setTitle("MindSharing " + MindSharing.getVersionString());
		// 화면 구성요소가 추가 될때, 위에서부터 아래로 분석하도록 수정
		setLocationRelativeTo(null);
		
		/*
		 * 화면 아이템 생성
		 * (아마도 JMenuBar랑 JMenu 전부 클래스 전역변수로 옮겨야할듯
		 */
		// 메뉴바 생성
		menubar = new JMenuBar();
		
		// 1번 메뉴 '파일'
		JMenu m_file = new JMenu("파일");
		
		JMenuItem mi_file_opentext = new JMenuItem("열기");
		mi_file_opentext.setActionCommand(ac.MENU_FILE_OPEN);
		mi_file_opentext.addActionListener(this);
		m_file.add(mi_file_opentext);
		
		JMenuItem mi_file_saveoutput = new JMenuItem("결과 저장");
		mi_file_saveoutput.setActionCommand(ac.MENU_FILE_SAVE_RESULTS);
		mi_file_saveoutput.addActionListener(this);
		m_file.add(mi_file_saveoutput);

		JMenuItem mi_file_exit = new JMenuItem("종료");
		mi_file_exit.setActionCommand(ac.MENU_FILE_EXIT);
		mi_file_exit.addActionListener(this);
		m_file.add(mi_file_exit);
		
		menubar.add(m_file);
		
		// 2번 메뉴 '데이터관리'
		JMenu m_db = new JMenu("데이터");
		JMenuItem mi_db_launch = new JMenuItem("데이터 관리툴 실행");
		mi_db_launch.setActionCommand(ac.MENU_DATA_LAUNCH_TOOL);
		mi_db_launch.addActionListener(this);
		m_db.add(mi_db_launch);
		
		menubar.add(m_db);

		// 3번 메뉴 '정보'
		JMenu m_info = new JMenu("정보");
		
		JMenuItem mi_info_version = new JMenuItem("버전");
		mi_info_version.setActionCommand(ac.MENU_INFO_VERSION);
		mi_info_version.addActionListener(this);
		m_info.add(mi_info_version);
		
		JMenuItem mi_info_credit = new JMenuItem("제작 정보");
		mi_info_credit.setActionCommand(ac.MENU_INFO_CREDIT);
		mi_info_credit.addActionListener(this);
		m_info.add(mi_info_credit);
		
		JMenuItem mi_info_help = new JMenuItem("도움말");
		mi_info_help.setActionCommand(ac.MENU_INFO_HELP);
		mi_info_help.addActionListener(this);
		m_info.add(mi_info_help);
		
		menubar.add(m_info);
		
		// 탭 관리자 생성
		maintab = new JTabbedPane();
		
		// 상단: 입력창: 라벨, 텍스트 상자, 버튼
		JLabel l_input = new JLabel("분석 텍스트 입력:");
		ta_input = new JTextArea(3, 40);
		ta_input.setToolTipText("분석할 텍스트는 여기에 입력");
		
		b_input = new JButton("분석");
		b_input.setActionCommand(ac.BUTTON_ANALYZE);
		b_input.addActionListener(this);
		b_clear = new JButton("클리어");
		b_clear.setActionCommand(ac.BUTTON_CLEAR);
		b_clear.addActionListener(this);
		
		// 하단: 출력창: 텍스트 상자만 일단
		ta_output = new JTextArea(40, 70);
		ta_output.setText("분석된 아웃풋은 일단 여기에 출력");
		
		/*
		 * 화면 구성요소 모두 프레임에 추가
		 */
		setJMenuBar(menubar);
		
		// 탭1 상단: 입력부
		topPane = new JPanel(new BorderLayout());
		
		topPane.add(l_input, BorderLayout.WEST);
		topPane.add(ta_input, BorderLayout.CENTER);
		
		JPanel top_buttonPane = new JPanel(new BorderLayout());
		top_buttonPane.add(b_input, BorderLayout.CENTER);
		top_buttonPane.add(b_clear, BorderLayout.SOUTH);
		topPane.add(top_buttonPane, BorderLayout.EAST);
		topPane.invalidate();
		
		// 탭1 하단: 분석 출력부
		outputTableModel = new AbstractTableModel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			String[] columns = {"단어", "감정값", "긍정/부정", "강조(x2)", "감소(x0.5) ", "누적 감정값"};
			
			@Override
			public void setValueAt(Object aValue, int rowIndex, int columnIndex)
			{
				// 구현할 필요 없음
			}
			
			@Override
			public void removeTableModelListener(TableModelListener l)
			{
				// 구현할 필요 없음
			}
			
			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex)
			{
				return false;
			}
			
			@Override
			public Object getValueAt(int rowIndex, int columnIndex)
			{
				return rows.get(rowIndex)[columnIndex];
			}
			
			@Override
			public int getRowCount()
			{
				return rows.size();
			}
			
			@Override
			public String getColumnName(int columnIndex)
			{
				return columns[columnIndex];
			}
			
			@Override
			public int getColumnCount()
			{
				return columns.length;
			}
			
			@Override
			public Class<?> getColumnClass(int columnIndex)
			{
				return rows.get(0)[columnIndex].getClass();
			}
			
			@Override
			public void addTableModelListener(TableModelListener l)
			{
				;
			}
		};
		outputTable = new JTable(outputTableModel);		
		// 탭 화면 1: 로그 분석 및 결과 출력
		
		scrollPane_analyze = new JScrollPane(outputTable);
		scrollPane_analyze.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane_analyze.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		topPane.add(scrollPane_analyze, BorderLayout.SOUTH);
		maintab.addTab("분석", topPane);
		
		
		// 탭 화면 2: 분석 과정 출력
		logOutputPane = new JEditorPane();
		JScrollPane scrollPane_log = new JScrollPane(logOutputPane);
		scrollPane_log.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane_log.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		//logOutputPane.setText("여기에 디버그 로그 출력" + ELog.getFullBuffer());
		maintab.add("로그", scrollPane_log);
		
		maintab.addChangeListener(this);
		
		
		
		add(maintab, BorderLayout.CENTER);
		// add(scrollPane, BorderLayout.CENTER);
		

		/*
		 * 패킹 후 출력
		 */
        pack();
        setVisible(true);
	}
	
	public Object[] getFragmentRow(String p_unit, int p_value, int p_signal, boolean p_amplify, boolean p_minimize, int p_accumulate)
	{
		// Fragment 정보를 테이블에 옮겨 적기 위한 ArrayList용 객체 정의
		// (형태소, 감정값, 긍정/부정, 감정 강화, 감정 축소, 누적값)
		Object[] row = {p_unit, p_value, p_signal, p_amplify, p_minimize, p_accumulate};
		return row;
	}
		
	@Override
	public void actionPerformed(ActionEvent e)
	{
		// 버튼이나 메뉴가, setActionCommand로 연결된 경우에, 발생된 이벤트가 이쪽을 통하여 나온다.
		// 다른 창을 띄우는 것도 가능하고, 호출 작업도 가능함.
		ELog.addTimelineToBuffer();
		ELog.d(TAG, "다음과 같은 이벤트가 수신되었습니다: " + e.getActionCommand());
		String cmd = e.getActionCommand();
		if (cmd == null)
		{
			// null이 리턴될 수 있는 경우는?
		}
		else
		{
			if (cmd.equals(ac.BUTTON_CLEAR))
			{
				ta_input.setText("");
			}
			else if (cmd.equals(ac.BUTTON_ANALYZE))
			{
				ELog.addTimelineToBuffer();
				ELog.d(TAG, "분석 버튼을 눌렀습니다.");
				SylphEngine engine = new SylphEngine();
				SylphEngine.initEngine();
				if (ta_input.getText().length() == 0)
				{
					JOptionPane.showMessageDialog(this, "텍스트가 입력되어있지 않습니다.");
				}
				else
				{
					rows.clear();
					ContextFragment cf = engine.analyze(ta_input.getText());
					for (BaseFragment bsf: cf.getFragments())
					{
						for (BaseFragment _bsf: bsf.getFragments())
						{
							rows.add(getFragmentRow(_bsf.sourceText, _bsf.emotionValue, _bsf.emotionValue, false, false, 0));
							ELog.e(TAG, rows.size());
						}
					}
					outputTable.invalidate();
					outputTable.repaint();
					maintab.repaint();
					
					//outputTableModel.fireTableRowsInserted(0, rows.size());;
				}
			}
			else if (cmd.equals(ac.MENU_INFO_VERSION))
			{
				;
			}
			else
			{
				ELog.e(TAG, "아직 구현되지 않은 기능입니다: " + cmd);
			}

		}
	}
	
	public void startLogViewThread()
	{
		
	}
	
	public void stopLogViewThread()
	{
		
	}

	@Override
	public void stateChanged(ChangeEvent e)
	{
		// 탭 위치 변경 이벤트를 처리하기 위한 것
		// 로그 탭의 경우, 계속 가져와 갱신하는 쓰레드(thread)가 필요하기 때문에, 여기에서 생성하고 종료할 수 있다.
		ELog.d(TAG, "현재 열려있는 탭은 " + (maintab.getSelectedIndex() == 0 ? "분석":"로그") + " 탭입니다.");
		if (maintab.getSelectedIndex() == 1)
		{
			// ELog 로그 불러오기
			ELog.d(TAG, "새 로그를 가져오는 쓰레드를 시작합니다.");
			if (t != null)
			{
				t.cancel();
			}
			Timer t = new Timer();
			t.schedule(new LogUpdate(), 0, 200);
		}
		else
		{
			ELog.d(TAG, "새 로그를 가져오는 쓰레드를 중단합니다.");
			Timer t = new Timer();
			if (t != null)
			{
				t.cancel();
				t = null;
			}
			// 쓰레드 종료 코드
		}
	}

}
