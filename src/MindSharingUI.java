import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;


public class MindSharingUI extends JFrame implements ActionListener
{
	Dimension main_screen_size = new Dimension(480, 600);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
		JMenuBar menubar = new JMenuBar();
		
		// 1번 메뉴 '파일'
		JMenu m_file = new JMenu("파일");
		
		JMenuItem mi_file_opentext = new JMenuItem("열기");
		m_file.add(mi_file_opentext);
		JMenuItem mi_file_saveoutput = new JMenuItem("결과 저장");
		m_file.add(mi_file_saveoutput);
		JMenuItem mi_file_exit = new JMenuItem("종료");
		/// 종료기능 시험삼아 삽입해보는 중
		mi_file_exit.setActionCommand("exit");
		mi_file_exit.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (e.getActionCommand().equals("exit"))
				{
					MindSharing.dbg("그래픽 화면을 종료합니다.");
					dispose();
					
				}
				// TODO Auto-generated method stub
				
			}
		});
		m_file.add(mi_file_exit);
		
		menubar.add(m_file);
		
		// 2번 메뉴 '데이터관리'
		JMenu m_db = new JMenu("데이터");
		JMenuItem mi_db_launch = new JMenuItem("데이터 관리툴 실행");
		m_db.add(mi_db_launch);
		
		menubar.add(m_db);

		// 3번 메뉴 '정보'
		JMenu m_info = new JMenu("정보");
		
		JMenuItem mi_info_version = new JMenuItem("버전");
		m_info.add(mi_info_version);
		JMenuItem mi_info_credit = new JMenuItem("제작 정보");
		m_info.add(mi_info_credit);
		JMenuItem mi_info_help = new JMenuItem("도움말");
		m_info.add(mi_info_help);
		
		menubar.add(m_info);
		
		// 탭 관리자 생성
		JTabbedPane maintab = new JTabbedPane();
		
		// 상단: 입력창: 라벨, 텍스트 상자, 버튼
		JLabel l_input = new JLabel("분석 텍스트 입력:");
		JTextArea ta_input = new JTextArea(3, 70);
		ta_input.setText("분석할 텍스트는 여기에 입력");
		JButton b_input = new JButton("분석");
		
		// 하단: 출력창: 텍스트 상자만 일단
		JTextArea ta_output = new JTextArea(40, 70);
		ta_output.setText("분석된 아웃풋은 일단 여기에 출력");
		
		/*
		 * 화면 구성요소 모두 프레임에 추가
		 */
		setJMenuBar(menubar);
		
		// 탭1 상단: 입력부
		JPanel topPane = new JPanel(new BorderLayout());
		topPane.add(l_input, BorderLayout.WEST);
		topPane.add(ta_input, BorderLayout.CENTER);
		topPane.add(b_input, BorderLayout.EAST);
		
		// 탭1 하단: 분석 출력부

		//JScrollPane centerPane = new JScrollPane(ta_output, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		// add(centerPane, BorderLayout.SOUTH);
		JTable outputTable = new JTable(new AbstractTableModel() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			String[] columns = {"형태소", "단어 유형", "감정값", "긍정단어", "부정단어", "누계"};
			Object[] values[] = {{"나", "명사", "0", false, false, "0"}, {"너", "명사", "0", false, true, "0"}};
			
			
			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex)
			{
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public Object getValueAt(int rowIndex, int columnIndex)
			{
				return values[rowIndex][columnIndex];
			}
			
			@Override
			public int getRowCount()
			{
				// TODO Auto-generated method stub
				return values.length;
			}
			
			@Override
			public String getColumnName(int columnIndex)
			{
				return columns[columnIndex].toString();
			}
			
			@Override
			public int getColumnCount()
			{
				return columns.length;
			}

			@Override
			public void setValueAt(Object aValue, int rowIndex, int columnIndex)
			{
				// TODO Auto-generated method stub
				fireTableCellUpdated(rowIndex, columnIndex);
			}

			@Override
			public Class<?> getColumnClass(int columnIndex)
			{
				// TODO Auto-generated method stub
				return getValueAt(0, columnIndex).getClass();
			}
		});
		
		// 탭 화면 1: 로그 분석 및 결과 출력
		
		JScrollPane scrollPane_analyze = new JScrollPane(outputTable);
		scrollPane_analyze.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane_analyze.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		topPane.add(scrollPane_analyze, BorderLayout.SOUTH);
		maintab.addTab("분석", topPane);
		
		// 탭 화면 2: 분석 과정 출력
		JEditorPane logOutputPane = new JEditorPane();
		JScrollPane scrollPane_log = new JScrollPane(logOutputPane);
		scrollPane_log.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane_log.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		logOutputPane.setText("여기에 디버그 로그 출력");
		maintab.add("로그", scrollPane_log);
		
		
		
		add(maintab, BorderLayout.CENTER);
		// add(scrollPane, BorderLayout.CENTER);
		

		/*
		 * 패킹 후 출력
		 */
        pack();
        setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// TODO Auto-generated method stub
		
	}

}
