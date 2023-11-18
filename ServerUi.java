import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Socket;



class ServerUi extends JFrame implements ActionListener {


    String port;
    LoginUi ui;
    Socket s;
    /////////////////////////////////////////////서버 ui멤버
    JTextArea ta = new JTextArea();
    JTextField  chatTf;
    JScrollPane sp;
    JPanel  endP, chatP, btnP, taP;
    Container cp;
    JButton startBtn, banBtn, endBtn, clearBtn;
    JComboBox idBox;

    ServerUi(LoginUi ui) {
        this.ui = ui;
        this.port=ui.port;
        init();
        setUi();
        new LiarServer(this);
    }

    void init() {
        cp = getContentPane();
        cp.setLayout(new BorderLayout());
        endP = new JPanel(new BorderLayout());
        endBtn = new JButton("종료");
        endP.add(endBtn, BorderLayout.WEST);
        cp.add(endP, BorderLayout.NORTH);                                                                //endP

        taP = new JPanel(new BorderLayout());
        taP.add(ta, BorderLayout.CENTER);
        sp = new JScrollPane(ta, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        ta.setLineWrap(true);
        taP.add(sp);
        ta.setEditable(false);
        cp.add(taP, BorderLayout.CENTER);                                                            //taP

        chatP = new JPanel(new BorderLayout());
        btnP = new JPanel(new GridLayout(1, 4));
        startBtn = new JButton("게임 시작!");
        banBtn = new JButton("강퇴");
        idBox = new JComboBox();
        clearBtn = new JButton("채팅 지우기");
        btnP.add(startBtn);
        btnP.add(banBtn);
        btnP.add(idBox);
        btnP.add(clearBtn);
        chatTf = new JTextField("");
        ta.setEnabled(false);
        chatP.add(chatTf, BorderLayout.CENTER);
        chatP.add(btnP, BorderLayout.SOUTH);
        cp.add(chatP, BorderLayout.SOUTH);                                                            //chatP
        ta.setFont(new Font("맑은 고딕",Font.BOLD,20));
        ta.setDisabledTextColor(Color.black);
        setUi();
        act();
    }

    void setUi() {
        setVisible(true);
        setTitle( port + "에서 채팅중..");
        setSize(700, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }


    /*public void sendmessage() {
        try {
            string text = chattf.gettext();
            ta.append(text + "\n");


            //프로그램 종료
            system.exit(0);
        else{
                //입력된 메세지가 "/exit"가 아닐 경우( 전송할 메세지인 경우)
                //클라이언트에게 메세지 전송
                dos.writeutf(text);

                //초기화 및 커서요청
                chattf.settext("");
                chattf.requestfocus();
            }
        } catch (ioexception e) {
        }
    }*/
    void act() {
        endBtn.addActionListener(this);
        clearBtn.addActionListener(this);
        startBtn.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {

        if (e.getSource().equals(clearBtn)) {
            ta.setText(null);
        }

    }


}