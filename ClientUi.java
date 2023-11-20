import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

class ClientUi extends JFrame {

    LoginUi ui;
    String id, ip, port;
    ////////////////////////////////////// 클라유아이 멤버↓
    JTextArea ta = new JTextArea() {
        public void paintComponent(final Graphics g) {
            ImageIcon imageIcon = new ImageIcon("taBack.png");
            Rectangle rect = getVisibleRect();
            g.drawImage(imageIcon.getImage(), rect.x, rect.y, rect.width, rect.height, null);
            setOpaque(false);
            super.paintComponent(g);
        }
    };
    JTextArea idTa;
    JTextField topicTf, timeTf, chatTf;
    JScrollPane sp;
    JPanel tfP, taP, northP, endP, chatP;
    JPanel p1, p2;
    RoundedButton endBtn = new RoundedButton("서버 나가기");
    Container cp;
    Font f = new Font("맑은 고딕", Font.BOLD, 20);
    Font f2 = new Font("맑은 고딕", Font.PLAIN, 20);
    GridBagLayout g = new GridBagLayout();
    GridBagConstraints gc, gc2;
    Color c1, c2;
    Dimension d1;
    boolean isGameStarted = false; // 게임이 시작되었는지 나타내는 변수

    Client client;  // Client 인스턴스를 멤버 변수로 선언


    Vector<PanelUi> pv = new Vector<>();
    ClientUi(LoginUi ui) {
        try {
            this.ui = ui;
            this.id = ui.id;
            this.ip = ui.ip;
            this.port = String.valueOf(ui.port);
            System.out.println("Cui의: " + ip + port + id);
            init();
            setUi();
            client = new Client(this);                            //액션리스너 삽입
        } catch (Exception e) {

        }
    }


    void setUi() {
        setVisible(true);
        setTitle(id + "(으)로 채팅중..(ip: " + ip + ", port: " + port + ")");
        setSize(900, 750);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // 바로 창이 닫히지 않도록 설정
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showOptionDialog(
                        null,
                        "게임 중 나가면 패널티가 있습니다. 정말로 나가시겠습니까?",
                        "Exit Confirmation",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        null,
                        null
                );
                if (confirm == 0) {
                    System.exit(0);
                }
            }
        });

    }


    void init() {
        cp = getContentPane();
        cp.setLayout(new BorderLayout());                                             //cp

        northP = new JPanel(new BorderLayout());
        tfP = new ImagePanel("pBack.png");
        topicTf = new JTextField(10);
        topicTf.setEnabled(false);
        topicTf.setFont(f);
        topicTf.setDisabledTextColor(Color.black);

        JPanel topicP = new JPanel(new BorderLayout());
        topicP.add(topicTf, BorderLayout.EAST);
        timeTf = new JTextField(10);
        timeTf.setEnabled(false);
        timeTf.setFont(f);
        timeTf.setDisabledTextColor(Color.black);
        JPanel selectP = new JPanel(new BorderLayout());
        selectP.add(timeTf, BorderLayout.WEST);
        tfP.add(topicP);
        tfP.add(selectP);
        northP.add(tfP, BorderLayout.SOUTH);
        cp.add(northP, BorderLayout.NORTH);                                      //northP

        chatP = new ImagePanel("pBack.png");
        chatTf = new JTextField(20);
        chatTf.setFont(f2);
        chatTf.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        chatP.add(chatTf);
        endP = new JPanel();
        chatP.add(endBtn);
        cp.add(chatP, BorderLayout.SOUTH);                                               //chatTf

        // 버튼들 추가
        JButton loginBtn = new JButton("로그인");
        JButton signUpBtn = new JButton("회원가입");
        JButton rankingBtn = new JButton("랭킹");
        JButton recordBtn = new JButton("음성 녹음");

        // 음성 녹음 버튼
        recordBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                byte[] audioData = client.captureAudio(); // 녹음된 오디오 데이터
                client.sendAudio(audioData); // 녹음된 오디오 데이터를 서버에 전송
            }
        });


        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = JOptionPane.showInputDialog("아이디를 입력하세요.");
                String password = JOptionPane.showInputDialog("비밀번호를 입력하세요.");
                client.login(username, password); // client는 Client 인스턴스를 참조하는 변수입니다.
                client.username = username;
            }
        });


        signUpBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = JOptionPane.showInputDialog("사용하실 아이디를 입력하세요.");
                String password = JOptionPane.showInputDialog("비밀번호를 입력하세요.");
                String nickname = JOptionPane.showInputDialog("닉네임을 입력하세요.");
                client.signUp(username, password, nickname);
            }
        });

        rankingBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isGameStarted) {
                    // 게임이 진행 중일 때 "랭킹" 버튼을 누르면 경고 메시지 표시
                    JOptionPane.showMessageDialog(null, "게임이 끝나고 눌러주세요");
                } else {
                    // 게임이 끝났을 때 "랭킹" 버튼을 누르면 서버에 랭킹 정보 요청
                    client.requestRankings();
                }
            }
        });

        // 버튼들을 패널에 추가
        chatP.add(loginBtn);
        chatP.add(signUpBtn);
        chatP.add(rankingBtn);
        chatP.add(recordBtn); // 채팅 패널에 "음성 녹음" 버튼 추가


        p1 = new JPanel(new BorderLayout());
        JLabel lb =new JLabel(new ImageIcon(getClass().getResource("p1.jpg")));
        //사이드패널
        p1.add(lb);
        p1.setPreferredSize(new Dimension(150, 600));
        p2 = new JPanel(new BorderLayout());
        p2.setPreferredSize(new Dimension(150, 600));
        lb =new JLabel(new ImageIcon(getClass().getResource("p2.jpg")));
        p2.add(lb);
        //사이드패널 끝

        taP = new JPanel(new BorderLayout());
        taP.add(ta, BorderLayout.CENTER);
        sp = new JScrollPane(ta, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //cp
        sp.getVerticalScrollBar().setValue(sp.getVerticalScrollBar().getMaximum());
        taP.add(sp);                                                                            //ta패널


        ta.setLineWrap(true);
        ta.setEnabled(false);
        ta.setDisabledTextColor(new Color(200,150,100));
        ta.setFont(new Font("맑은 고딕",Font.BOLD,20));
        cp.add(taP, BorderLayout.CENTER);
        cp.add(p1, BorderLayout.WEST);
        cp.add(p2, BorderLayout.EAST);

    }



}

class PanelUi {
    JPanel panel;
    JLabel imgLb;
    JLabel idLb;
    Font f = new Font("맑은 고딕", Font.BOLD, 20);
    ClientUi cui;

    PanelUi(ClientUi cui) {
        this.cui = cui;
        for (int i = 0; i < 8; i++) {
            panel = new JPanel(new BorderLayout());
            imgLb = new JLabel(new ImageIcon("buddy.jpg"));
            imgLb.setName(String.valueOf(i) + "imgLb");
            idLb = new JLabel("아이디");
            idLb.setName(String.valueOf(i) + "idLb");
            idLb.setFont(f);
            idLb.setHorizontalAlignment(0);
            idLb.setForeground(Color.black);
            idLb.setBackground(Color.gray);
            idLb.setOpaque(true);
            panel.add(imgLb);
            panel.add(idLb, BorderLayout.SOUTH);

            if (cui.pv.size() < 4) {
                cui.p1.add(panel);
            } else if (cui.pv.size() >= 4) {
                cui.p2.add(panel);
            }
            cui.pv.add(this);
        }
    }


}