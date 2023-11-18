import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Socket;
import java.util.*;
import java.io.*;
import java.lang.*;
import java.util.Timer;


class Client extends Thread implements Runnable, ActionListener {

    public String username;
    String id, ip;
    int port = 0;
    InputStream is;
    OutputStream os;
    OneClientModul ocm;
    DataInputStream dis;
    DataOutputStream dos;
    Socket s;
    LoginUi ui;
    int nop;
    Vector<String> idList = new Vector<>();
    String lsnMsg, spkMsg;
    ClientUi cui;
    JFrame frame;
    JOptionPane jop = new JOptionPane();
    Thread listenTh;
    Thread chatTimeTh;



    Action enter = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            spkMsg = cui.chatTf.getText();
            spkMsg = spkMsg.trim();
            speak(spkMsg);
            cui.chatTf.setText(null);
        }
    };
    Action enter2 = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            spkMsg = cui.chatTf.getText();
            spkMsg = spkMsg.trim();
            speak(spkMsg);
            cui.chatTf.setText(null);
            cui.chatTf.setEnabled(false);
            cui.chatTf.removeActionListener(enter2);
        }
    };
    Action enter3 = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            spkMsg = cui.chatTf.getText();
            spkMsg = spkMsg.trim();
            speak(spkMsg);
            speak("liarTopic" + spkMsg);
            cui.chatTf.setText(null);
            cui.chatTf.setEnabled(false);
            cui.chatTf.removeActionListener(enter3);
        }
    };

    Client(ClientUi cui) {
        this.cui = cui;
        this.ui = cui.ui;
        this.ip = cui.ip;
        this.port = Integer.parseInt(cui.port);
        this.id = cui.id;
        this.frame = cui;
        try {

            System.out.println(id + ip + port);
            s = new Socket(ip, port);
            is = s.getInputStream();
            os = s.getOutputStream();
            dis = new DataInputStream(is);
            dos = new DataOutputStream(os);
            System.out.println("연결");
            String ent = dis.readUTF();
            System.out.println(ent);
            act();
            if (ent.equals("falsefull")) {
                System.out.println("enterfalse");
                JOptionPane.showMessageDialog(null, "해당 서버의 인원이 가득 찼습니다", "인원 초과", 0);
                dos.writeUTF("enterfalse");
                dos.flush();
                reLogin();
            } else {
                dos.writeUTF(id);
                ent = dis.readUTF();
                System.out.println(ent);
                if (ent.equals("falseid")) {
                    System.out.println("enterfalse");
                    JOptionPane.showMessageDialog(null, "중복된 아이디가 있습니다.", "ID중복", 0);
                    dos.writeUTF("enterfalse");
                    dos.flush();
                    reLogin();
                } else if (ent.equals("3초후")) {
                    System.out.println("enterfalse");
                    JOptionPane.showMessageDialog(null, "해당 서버의 게임이 시작되었습니다.", "게임 시작", 0);
                    dos.writeUTF("enterfalse");
                    dos.flush();
                    reLogin();

                } else {
                    System.out.println("낫 폴스");
                    dos.flush();
                    listenTh = new Thread(this);
                    listenTh.start();
                }

            }
        } catch (IOException ie) {
            System.out.println("Client ie: " + ie);
            JOptionPane.showMessageDialog(null, "아이피 또는 포트가 올바르지 않습니다.", "연결 오류", 0);
            reLogin();
        }
    }



    String protocol() throws IOException {
        if (lsnMsg.startsWith(id + ">>") & !lsnMsg.startsWith(id + " ")) {
            lsnMsg = lsnMsg.replaceFirst(id, "나 ");
            return lsnMsg;
        } else if (lsnMsg.startsWith(id + "님이 강퇴")) {
            s.close();
            JOptionPane.showMessageDialog(null, "관리자에 의해 강퇴당하셨습니다.", "강퇴", 0);
            cui.dispose();
            ui.reopen();
            return "exit";
        } else if (lsnMsg.startsWith("gm")) {
            System.out.println(lsnMsg + "gm메세지");
            lsnMsg = lsnMsg.substring(2);
            System.out.println(lsnMsg);
            fromGm(lsnMsg);
            return null;
        }   else {
            System.out.println(lsnMsg);
            return lsnMsg;
        }
    }

    void act() {

        cui.chatTf.addActionListener(enter);
        cui.endBtn.addActionListener(this);

    }

    void fromGm(String lsnMsg) {// 오디오 캡처 및 재생 시작

        if (lsnMsg.startsWith("liar:")) {
            cui.setTitle(id + "(으)로 게임중..(ip: " + ip + ", port: " + port + ")");
            if (lsnMsg.substring(5).equals(id)) {
                cui.topicTf.setFont(new Font("맑은 고딕", Font.BOLD, 16));
                cui.topicTf.setText("당신은 라이어입니다");
            } else {
            }
        } else if (lsnMsg.startsWith("topic:")) {
            if (cui.topicTf.getText().equals("당신은 라이어입니다")) {
            } else {
                cui.topicTf.setText(lsnMsg.substring(6));
            }
        } else if (lsnMsg.startsWith("채팅락")) {
            cui.chatTf.setEnabled(false);
        } else if (lsnMsg.startsWith("채팅언락")) {
            if (lsnMsg.substring(4).equals(id)) {
                printTimer(cui.timeTf, 10);
                cui.ta.append("상단 제한시간안에 제시어에 대해 설명해주세요.\n");
                cui.chatTf.setEnabled(true);
                chatTimeTh = new Thread(this);
                chatTimeTh.start();
                cui.chatTf.removeActionListener(enter);
                cui.chatTf.addActionListener(enter2);
            }
        } else if (lsnMsg.startsWith("votecom")) {
            if (lsnMsg.substring(7).equals(id)) {
                printTimer(cui.timeTf, 10);
                cui.ta.append("10초안에 제시어를 추리하여 입력해주세요.\n");
                cui.chatTf.setEnabled(true);
                chatTimeTh = new Thread(this);
                chatTimeTh.start();
                cui.chatTf.removeActionListener(enter2);
                cui.chatTf.addActionListener(enter3);
            }
        } else if (lsnMsg.startsWith("올언락")) {
            cui.chatTf.setEnabled(true);
            cui.chatTf.setText("");
            cui.topicTf.setText("");
            cui.timeTf.setText("");
            cui.chatTf.removeActionListener(enter2);
            cui.chatTf.removeActionListener(enter3);
            cui.chatTf.addActionListener(enter);
            idList.removeAllElements();
            cui.setTitle(id + "(으)로 채팅중..(ip: " + ip + ", port: " + port + ")");
        } else if (lsnMsg.startsWith("list")) {
            idList.add(lsnMsg.substring(4));
        } else if (lsnMsg.startsWith("vote")) {
            System.out.println("vote입장");
            String vote = new VoteDialog(this).getResult();
            System.out.println(vote);
            speak("cVote" + vote);
        } else if (lsnMsg.startsWith("result")) {
            lsnMsg = lsnMsg.substring(6);
            new Result(this, lsnMsg);
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource().equals(cui.endBtn)) {
            try {
                s.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            cui.dispose();
            ui.reopen();
        }
    }

    void reLogin() {

        cui.dispose();
        cui.ui.reopen();
        cui.ui.clientBTn.doClick();

    }

    void speak(String str) {
        try {
            if (str.startsWith("liar")) {
                dos.writeUTF(str);
                dos.flush();
            } else if (str.startsWith("cVote")) {
                dos.writeUTF(str);
                dos.flush();
            } else {
                dos.writeUTF(id + ">> " + str + "{" + username); // 사용자 이름을 서버로 붙여서 보냄
                dos.flush();
            }
        } catch (IOException ie) {
            System.out.println("speak() ie: " + ie);
        }
    }

    @Override
    public void run() {
        if (Thread.currentThread().equals(listenTh)) {
            while (true) {
                String msg = null;
                msg = listen();
                if (msg != null) {
                    if (msg.equals("exit")) {
                        System.out.println("exit");
                        closeAll();
                        break;
                    } else {
                            cui.ta.append(msg + "\n");
                        cui.sp.getVerticalScrollBar().setValue(cui.sp.getVerticalScrollBar().getMaximum());
                    }
                }
            }
        }
        if (Thread.currentThread().equals(chatTimeTh)) {
            try {
                Thread.currentThread().sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            cui.chatTf.setEnabled(false);
        }
    }

    String listen() { // 랭킹 부분때문에 수정함
        lsnMsg = "";
        try {
            lsnMsg = dis.readUTF();
            System.out.println(lsnMsg);

            if (lsnMsg.startsWith("rankings:")) {
                showRankings(lsnMsg.substring(9));
                return null;
            } else {
                return protocol();
            }
        } catch (IOException ie) {
            System.out.println(ie);
            JOptionPane.showMessageDialog(null, "서버와 연결이 끊겼습니다.");
            return "exit";
        }
    }

    void showRankings(String rankings) {
        JTextArea textArea = new JTextArea(rankings);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 500));
        JOptionPane.showMessageDialog(null, scrollPane, "랭킹", JOptionPane.INFORMATION_MESSAGE);
    }

    void printTimer(JTextField tf, int i) {

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (i * 1000);
        final String[] time = new String[1];
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {

            @Override
            public void run() {

                long currentTime = System.currentTimeMillis();
                long leftTime = endTime - currentTime;
                long leftSeconds = (leftTime / 1000) % 60;
                if (leftSeconds == 0) {
                    tf.setText("");
                    timer.cancel();
                }
                tf.setText(leftSeconds + "초");

            }

        };
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    void closeAll() {
        try {
            if (dis != null) dis.close();
            if (dos != null) dos.close();
            if (is != null) is.close();
            if (os != null) os.close();
            if (s != null) s.close();
            cui.dispose();
            cui.ui.reopen();
        } catch (IOException ie) {
        }
    }
    void login(String username, String password) {  // 로그인
        String message = "login:" + username + ":" + password + ":" + id;
        try {
            dos.writeUTF(message);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void signUp(String username, String password, String nickname) { // 회원가입
        String message = "signup:" + username + ":" + password + ":" + nickname  + ":" + id;
        try {
            dos.writeUTF(message);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void requestRankings() { // 랭킹 처리
        try {
            dos.writeUTF("rankings");
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
