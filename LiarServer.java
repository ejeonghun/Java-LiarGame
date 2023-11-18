import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

class LiarServer extends Thread implements ActionListener {
    ServerSocket ss;
    Socket s;
    int port = 3000;
    String portN;
    Vector<OneClientModul> v = new Vector<OneClientModul>();
    OneClientModul ocm;
    Thread gameThread = new Thread();
    Thread serverThread;
    ServerUi sui;
    String msg;
    String liarTopic = "10초초과";
    ArrayList voteList;
    boolean isTopicPhase = false; // 주제 설명 시간인지 나타내는 필드


    LiarServer(ServerUi sui) {
        try {
            this.sui = sui;
            this.portN = sui.ui.port;
            port = Integer.parseInt(portN);
            ss = new ServerSocket(port);
            System.out.println(ss);
            sui.setTitle("ip: " + InetAddress.getLocalHost().getHostAddress() + ", port: " + port + " 서버관리자");
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverThread = new Thread(this);
        serverThread.start();
        this.sui = sui;
        this.s = sui.s;
        act();
    }

    // 주제 설명 시간을 시작하는 메소드
    void startTopicPhase() {
        isTopicPhase = true;
        for (OneClientModul ocm : v) {
            ocm.updateTopicPhase(isTopicPhase);
        }
    }

    // 주제 설명 시간을 종료하는 메소드
    void endTopicPhase() {
        isTopicPhase = false;
        for (OneClientModul ocm : v) {
            ocm.updateTopicPhase(isTopicPhase);
        }
    }

    void kick() {
        String banId = String.valueOf(sui.idBox.getSelectedItem());
        for (OneClientModul ocm : v) {
            if (ocm.chatId.equals(banId)) {
                ocm.broadcast(ocm.chatId + "님이 강퇴당했습니다..");
                v.remove(ocm);
                ocm.closeAll();
                break;
            }
        }
    }

    @Override
    public void run() {
        if (currentThread().equals(serverThread)) {
            try {
                while (true) {
                    s = ss.accept();
                    OutputStream os = s.getOutputStream();
                    DataOutputStream dos = new DataOutputStream(os);
                    if (v.size() == 8) {
                        dos.writeUTF("false");
                    } else if (gameThread.isAlive() == true) {
                        dos.writeUTF("true");
                        dos.writeUTF("3초후");
                        System.out.println("enterfalse");
                    } else if (v.size() < 8) {
                        dos.writeUTF("true");
                        ocm = new OneClientModul(this);
                        sui.idBox.addItem(ocm.chatId);
                        v.add(ocm);
                        ocm.start();
                    }
                }
            } catch (IOException ie) {
                pln(port + "번 포트 사용중.");
            } finally {
                try {
                    if (ss != null) ss.close();
                    System.out.println("서버다운");
                } catch (IOException ie) {
                }
            }
        }
        if (currentThread().equals(gameThread)) {
             // 로그인 하지 않은 사용자가 있을 때 구문 추가
            try {
                for (OneClientModul ocm : ocm.ls.v) {
                    if (!ocm.isLoggedIn) {
                        ocm.broadcast("모든 사용자가 로그인해야 게임을 시작할 수 있습니다.");
                        sui.startBtn.setEnabled(true);
                        return;
                    }
                }
                if (v.size() != 0) {
                    ocm.broadcast("3초후 게임을 시작합니다.");
                    sleep(1000);
                    ocm.broadcast("2초후 게임을 시작합니다.");
                    sleep(1000);
                    ocm.broadcast("1초후 게임을 시작합니다.");
                    sleep(1000);
                    voteList = new ArrayList();
                    System.out.println("겜메");
                    new GameManager(this);

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void sleepTh(int i) {
        try {
            currentThread().sleep(i * 1000);
        } catch (InterruptedException e) {
        }
    }


    void act() {
        Action enter = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                msg = sui.chatTf.getText();
                msg = msg.trim();
                msg = "관리자 >> " + msg;
                sui.chatTf.setText(null);
                if (v.size() != 0) {
                    ocm.broadcast(msg);
                } else {
                    sui.ta.append("서버에 인원이 없습니다.\n");
                }
            }
        };
        sui.chatTf.addActionListener(enter);
        sui.banBtn.addActionListener(this);
        sui.startBtn.addActionListener(this);
        sui.endBtn.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(sui.banBtn)) {
            kick();
        }
        if (e.getSource().equals(sui.startBtn)) {
            if (v.size() != 0) {
                sui.startBtn.setEnabled(false);
                gameThread = new Thread(this);
                gameThread.start();
            }
        }
        if (e.getSource().equals(sui.endBtn)) {

            System.exit(0);

        }
    }

    void pln(String str) {
        System.out.println(str);
    }

    void p(String str) {
        System.out.print(str);
    }

}                                                                                               //라이어서버


class OneClientModul extends Thread {
    //원클모듈
    LiarServer ls;
    Socket s;
    InputStream is;
    OutputStream os;
    DataInputStream dis;
    DataOutputStream dos;
    String chatId;
    ServerUi sui;
    boolean isLoggedIn = false; // 로그인 상태 정보 저장
    private String username;
    boolean isTopicPhase = false;

    private byte[] audioBuffer = new byte[1024]; // 음성 데이터 처리
    private int bytesRead; // 음성 데이터

    void updateTopicPhase(boolean isTopicPhase) {
        this.isTopicPhase = isTopicPhase;
    }

    OneClientModul(LiarServer ls) {
        this.ls = ls;
        this.s = ls.s;
        this.sui = ls.sui;
        try {
            is = s.getInputStream();
            os = s.getOutputStream();
            dis = new DataInputStream(is);
            dos = new DataOutputStream(os);
            System.out.println("ocm 입장");
            chatId = dis.readUTF();
            System.out.println("chatId : " + chatId);
            if (chatId.equals("enterfalse")) {
                closeAll();
            } else {
                String enterId = chatId;
                Boolean checkId = true;
                for (OneClientModul ocm : ls.v) {
                    if (ocm.chatId.equals(enterId)) {
                        System.out.println("중복 아이디 찾는중");
                        checkId = false;
                        continue;
                    }
                }
                if (checkId == false) {
                    System.out.println("중복아이디 있음");
                    dos.writeUTF("falseid");
                    System.out.println("falseid");
                    closeAll();
                } else {
                    System.out.println("주ㅡㅇ복아이디 없음");
                    dos.writeUTF("true");
                }
            }
        } catch (IOException ie) {
        }
    }

    public void run() {
        listen();
    }

    void listen() {
        String msg = "";
        int i;
        try {
            broadcast(chatId + " 님이 입장하셨습니다. (현재 인원: " + ls.v.size() + "명)");
            broadcast("먼저 로그인을 해주세요");
            while (true) {
                msg = dis.readUTF();
                if (msg.startsWith("login:")) {
                    System.out.println(msg);
                    String[] parts = msg.split(":");
                    String username = parts[1];
                    String password = parts[2];
                    String login_chatId = parts[3]; // 사용자의 chatId
                    boolean success = login(username, password, login_chatId);
                    // 로그인 시도 시 사용자 chatId를 db에 매핑
                    if (success) {
                        broadcast(username + "님이 로그인하셨습니다.");
                        isLoggedIn = true;
                    } else {
                        broadcast("로그인에 실패하셨습니다. 유저 이름 또는 비밀번호를 확인해주세요");
                    }
                } else if (msg.startsWith("signup:")) {
                    String[] parts = msg.split(":");
                    String username = parts[1];
                    String password = parts[2];
                    String nickname = parts[4];
                    boolean success = signUp(username, password, nickname);
                    if (success) {
                        broadcast(username + "님이 회원가입하셨습니다.");
                    } else {
                        broadcast("회원가입에 실패하셨습니다. 다시 시도해주세요");
                    }
                } else if (msg.startsWith("liarTopic")) {
                    if (msg != null) {
                        ls.liarTopic = msg.substring(9);
                    }
                    System.out.println(ls.liarTopic);
                } else if (msg.startsWith("cVote")) {
                    msg = msg.substring(5);
                    ls.voteList.add(msg);
                    System.out.println("투표 : " + msg);
                    if (ls.voteList.size() == ls.v.size()) {
                        ls.gameThread.interrupt();
                    }
                } else if (msg.equals("enterfalse")) {
                } else if (msg.startsWith("ranking")) { // 랭킹 요청
                    broadcast(getRankings());
                }

                else {
                    broadcast(msg);
                }
            }
        } catch (IOException ie) {
            ls.v.remove(this);
            // 회원이 로그인 상태에서 접속을 끊었을 때만 패널티를 부여합니다.
            if (isLoggedIn) {
                ban_plus(username);
                broadcast(username + " 님이 퇴장하셨습니다. (현재 인원: " + ls.v.size() + "명)");
            } else {
                broadcast(chatId + " 님이 퇴장하셨습니다. (현재 인원: " + ls.v.size() + "명)");
            }
            ls.sui.idBox.removeItem(chatId);
        } finally {
            closeAll();
        }
    }


    void broadcast(String msg) {
        try {
            // 메시지에서 사용자 ID와 실제 메시지를 분리
            int lastBraceIndex = msg.lastIndexOf("{");
            String userId, msgWithoutUsername;
            if (lastBraceIndex == -1) {  // '{'가 없는 경우
                userId = "unknown";  // username을 알 수 없으므로 unknown으로 설정
                msgWithoutUsername = msg;  // 전체 메시지를 msgWithoutUsername로 설정
            } else {  // '{'가 있는 경우
                userId = msg.substring(lastBraceIndex + 1);  // 마지막 중괄호 이후가 username
                msgWithoutUsername = msg.substring(0, lastBraceIndex);  // username을 제외한 메시지를 만듦
            }

            // 제시어 필터링 구문
            if (isTopicPhase && msgWithoutUsername.contains(ls.liarTopic)) {
                System.out.println(userId + "금지어 발설 !!!!!!!");
                // 제시어를 포함한 메시지는 서버에서만 처리하고 다른 사용자에게는 보내지 않음
                System.out.println(userId + ": " + msgWithoutUsername);  // 서버 쪽에서 메시지를 출력
                // 여기에 DB의 Ban_count를 증감시키는 메소드 호출
                boolean success = ban_plus(userId);
                if (success) {
                    broadcast("아이디 : " + userId + " 님 경고입니다.");
                } else {
                    broadcast("데이터베이스에 오류가 발생했습니다.");
                }

                // 모든 클라이언트에게 userId가 금지어를 발설했다고 알림
                for (OneClientModul ocm : ls.v) {
                    ocm.dos.writeUTF(userId + "님이 금지어를 발설했습니다.");
                    // 금지어를 "***"로 마스킹 처리
                    msgWithoutUsername = msgWithoutUsername.replace(ls.liarTopic, "***");
                    ocm.dos.flush();
                }

            }

            // 제시어를 포함하지 않은 메시지는 모든 사용자에게 전송
            for (OneClientModul ocm : ls.v) {
                ocm.dos.writeUTF(msgWithoutUsername);
                ocm.dos.flush();
            }
            if (msg.startsWith("gm")) {
                msg = msg.substring(2);
                sui.ta.append(msg + "\n");
                sui.sp.getVerticalScrollBar().setValue(sui.sp.getVerticalScrollBar().getMaximum());
            } else sui.ta.append(msg + "\n");
            sui.sp.getVerticalScrollBar().setValue(sui.sp.getVerticalScrollBar().getMaximum());
        } catch (IOException ie) {
        }
    }

    // 로그인
    private boolean login(String user_id, String user_pw, String login_chatId) {
        String url = "jdbc:mysql://localhost:3306/liargame";
        String dbUsername = "root";
        String dbPassword = "1234";

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String sql = "SELECT * FROM Member WHERE user_id = ? AND user_pw = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, user_id);
            statement.setString(2, user_pw);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                username = login_chatId;  // 로그인에 성공했을 때 username 필드 업데이트

                // chatId를 username에 매핑하는 코드
                sql = "UPDATE Member SET nickname = ? WHERE user_id = ?";
                statement = connection.prepareStatement(sql);
                statement.setString(1, username);
                statement.setString(2, user_id);
                statement.executeUpdate();

                return true;
            }
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // 회원가입
    private boolean signUp(String user_id, String user_pw, String nickname) {
        String url = "jdbc:mysql://localhost:3306/liargame";
        String dbUsername = "root";
        String dbPassword = "1234";

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String sql = "INSERT INTO member (user_id, user_pw, nickname) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, user_id);
            statement.setString(2, user_pw);
            statement.setString(3, nickname);

            int rows = statement.executeUpdate();
            System.out.println(rows+"회원 가입 완료" + user_id +" "+ nickname);
            String rank_sql = "INSERT INTO `RANK` (user_id, game_count, win_count) VALUES (?, 0, 0)";
            PreparedStatement statement2 = connection.prepareStatement(rank_sql);
            statement2.setString(1, user_id);
            int rows2 = statement2.executeUpdate();

            return rows2 > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    private boolean ban_plus(String user_id) { // 밴 카운터 처리 메소드
        String url = "jdbc:mysql://localhost:3306/liargame";
        String dbUsername = "root";
        String dbPassword = "1234";

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            // ban_count 값을 1 증가시키는 SQL 구문
            String sql = "UPDATE member SET ban_count = IF(ban_count IS NULL, 1, ban_count + 1) WHERE user_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, user_id);

            int rows = statement.executeUpdate();
            System.out.println(user_id + "Ban_count + 1 완료");
            return rows > 0; // 성공적으로 행이 업데이트되었다면 true를 반환
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false; // 오류가 발생하거나 업데이트된 행이 없다면 false를 반환
    }



    public void updateWinCountForLiar(String liar) { // 라이어 승리시 승/패 기록 처리 메소드
        String url = "jdbc:mysql://localhost:3306/liargame";
        String dbUsername = "root";
        String dbPassword = "1234";

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            // 닉네임으로 user_id 찾기
            String sql = "SELECT user_id FROM Member WHERE nickname = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, liar);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String userId = resultSet.getString("user_id");

                // user_id를 이용하여 win_count 값과 게임 카운트를 1 증가시키기
                sql = "UPDATE `Rank` SET win_count = win_count + 1, game_count = game_count+1 WHERE user_id = ?";
                statement = connection.prepareStatement(sql);
                statement.setString(1, userId);
                int rows = statement.executeUpdate();
                System.out.println(liar + "Win_count + 1 완료");

                // user_id를 이용하여 승리 횟수 가져오기
                String sql2 = "SELECT win_count FROM `RANK` WHERE user_id = ?";
                PreparedStatement st2 = connection.prepareStatement(sql2);
                st2.setString(1, userId);
                ResultSet rs2 = st2.executeQuery();
                if (rs2.next()) {
                    int winCount = rs2.getInt("win_count");
                    ls.ocm.broadcast(liar + "님의 승리 횟수는 " + winCount + "번 입니다.");
                }
            }

            // 라이어를 제외한 모든 유저들의 game_count를 1회 증가시키기
            for (OneClientModul ocm : ls.v) {
                if (!ocm.chatId.equals(liar)) {
                    // 라이어가 아닌 유저의 user_id를 가져오기
                    sql = "SELECT user_id FROM Member WHERE nickname = ?";
                    statement = connection.prepareStatement(sql);
                    statement.setString(1, ocm.chatId);
                    resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        String nonLiarUserId = resultSet.getString("user_id");

                        // user_id를 이용하여 game_count 값을 1 증가시키기
                        sql = "UPDATE `Rank` SET game_count = game_count + 1 WHERE user_id = ?";
                        statement = connection.prepareStatement(sql);
                        statement.setString(1, nonLiarUserId);
                        statement.executeUpdate();
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateWinCountForNonLiars(String liar) { // 라이어 패배 시 승/패 기록 처리 메소드
        String url = "jdbc:mysql://localhost:3306/liargame";
        String dbUsername = "root";
        String dbPassword = "1234";

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            for (OneClientModul ocm : ls.v) {
                if (!ocm.chatId.equals(liar)) { // 만약 라이어가 아닐 경우 win_count, game_count를 1씩 증가.
                    // 닉네임으로 user_id 찾기
                    String sql = "SELECT user_id FROM Member WHERE nickname = ?";
                    PreparedStatement statement = connection.prepareStatement(sql);
                    statement.setString(1, ocm.chatId);
                    ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        String userId = resultSet.getString("user_id");

                        // user_id를 이용하여 win_count 값을 1 증가시키기
                        sql = "UPDATE `Rank` SET win_count = win_count + 1, game_count = game_count + 1 WHERE user_id = ?";
                        statement = connection.prepareStatement(sql);
                        statement.setString(1, userId);
                        statement.executeUpdate();
                    }
                } else { // 만약 라이어일 경우 game_count만 1 증가
                    String liar_sql = "SELECT user_id FROM Member WHERE nickname = ?";
                    PreparedStatement liar_state = connection.prepareStatement(liar_sql);
                    liar_state.setString(1, ocm.chatId);
                    ResultSet rs_liar = liar_state.executeQuery();
                    rs_liar.next();
                    String liar_userId = rs_liar.getString("user_id");
                    liar_sql = "UPDATE `Rank` SET game_count = game_count + 1 WHERE user_id = ?";
                    liar_state = connection.prepareStatement(liar_sql);
                    liar_state.setString(1, liar_userId);
                    liar_state.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public String getRankings() { // 랭킹 처리 메소드
        String rankings = "";
        String url = "jdbc:mysql://localhost:3306/liargame";
        String dbUsername = "root";
        String dbPassword = "1234";

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            // win_count가 높은 순으로 모든 유저를 조회합니다.
            String sql = "SELECT user_id, win_count FROM `Rank` ORDER BY win_count DESC";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            // 조회된 결과를 문자열로 변환합니다.
            while (resultSet.next()) {
                rankings += "유저 : " + resultSet.getString("user_id") + " : " + resultSet.getInt("win_count") + "번\n";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rankings;
    }



    void closeAll() {
        try {
            if (dis != null) dis.close();
            if (dos != null) dos.close();
            if (is != null) is.close();
            if (os != null) os.close();
            if (s != null) s.close();
        } catch (IOException ie) {
        }
    }
}
