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
    String liarTopic = "10���ʰ�";
    ArrayList voteList;
    boolean isTopicPhase = false; // ���� ���� �ð����� ��Ÿ���� �ʵ�


    LiarServer(ServerUi sui) {
        try {
            this.sui = sui;
            this.portN = sui.ui.port;
            port = Integer.parseInt(portN);
            ss = new ServerSocket(port);
            System.out.println(ss);
            sui.setTitle("ip: " + InetAddress.getLocalHost().getHostAddress() + ", port: " + port + " ����������");
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverThread = new Thread(this);
        serverThread.start();
        this.sui = sui;
        this.s = sui.s;
        act();
    }

    // ���� ���� �ð��� �����ϴ� �޼ҵ�
    void startTopicPhase() {
        isTopicPhase = true;
        for (OneClientModul ocm : v) {
            ocm.updateTopicPhase(isTopicPhase);
        }
    }

    // ���� ���� �ð��� �����ϴ� �޼ҵ�
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
                ocm.broadcast(ocm.chatId + "���� ������߽��ϴ�..");
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
                        dos.writeUTF("3����");
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
                pln(port + "�� ��Ʈ �����.");
            } finally {
                try {
                    if (ss != null) ss.close();
                    System.out.println("�����ٿ�");
                } catch (IOException ie) {
                }
            }
        }
        if (currentThread().equals(gameThread)) {
             // �α��� ���� ���� ����ڰ� ���� �� ���� �߰�
            try {
                for (OneClientModul ocm : ocm.ls.v) {
                    if (!ocm.isLoggedIn) {
                        ocm.broadcast("��� ����ڰ� �α����ؾ� ������ ������ �� �ֽ��ϴ�.");
                        sui.startBtn.setEnabled(true);
                        return;
                    }
                }
                if (v.size() != 0) {
                    ocm.broadcast("3���� ������ �����մϴ�.");
                    sleep(1000);
                    ocm.broadcast("2���� ������ �����մϴ�.");
                    sleep(1000);
                    ocm.broadcast("1���� ������ �����մϴ�.");
                    sleep(1000);
                    voteList = new ArrayList();
                    System.out.println("�׸�");
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
                msg = "������ >> " + msg;
                sui.chatTf.setText(null);
                if (v.size() != 0) {
                    ocm.broadcast(msg);
                } else {
                    sui.ta.append("������ �ο��� �����ϴ�.\n");
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

}                                                                                               //���̾��


class OneClientModul extends Thread {
    //��Ŭ���
    LiarServer ls;
    Socket s;
    InputStream is;
    OutputStream os;
    DataInputStream dis;
    DataOutputStream dos;
    String chatId;
    ServerUi sui;
    boolean isLoggedIn = false; // �α��� ���� ���� ����
    private String username;
    boolean isTopicPhase = false;

    private byte[] audioBuffer = new byte[1024]; // ���� ������ ó��
    private int bytesRead; // ���� ������

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
            System.out.println("ocm ����");
            chatId = dis.readUTF();
            System.out.println("chatId : " + chatId);
            if (chatId.equals("enterfalse")) {
                closeAll();
            } else {
                String enterId = chatId;
                Boolean checkId = true;
                for (OneClientModul ocm : ls.v) {
                    if (ocm.chatId.equals(enterId)) {
                        System.out.println("�ߺ� ���̵� ã����");
                        checkId = false;
                        continue;
                    }
                }
                if (checkId == false) {
                    System.out.println("�ߺ����̵� ����");
                    dos.writeUTF("falseid");
                    System.out.println("falseid");
                    closeAll();
                } else {
                    System.out.println("�֤Ѥ������̵� ����");
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
            broadcast(chatId + " ���� �����ϼ̽��ϴ�. (���� �ο�: " + ls.v.size() + "��)");
            broadcast("���� �α����� ���ּ���");
            while (true) {
                msg = dis.readUTF();
                if (msg.startsWith("login:")) {
                    System.out.println(msg);
                    String[] parts = msg.split(":");
                    String username = parts[1];
                    String password = parts[2];
                    String login_chatId = parts[3]; // ������� chatId
                    boolean success = login(username, password, login_chatId);
                    // �α��� �õ� �� ����� chatId�� db�� ����
                    if (success) {
                        broadcast(username + "���� �α����ϼ̽��ϴ�.");
                        isLoggedIn = true;
                    } else {
                        broadcast("�α��ο� �����ϼ̽��ϴ�. ���� �̸� �Ǵ� ��й�ȣ�� Ȯ�����ּ���");
                    }
                } else if (msg.startsWith("signup:")) {
                    String[] parts = msg.split(":");
                    String username = parts[1];
                    String password = parts[2];
                    String nickname = parts[4];
                    boolean success = signUp(username, password, nickname);
                    if (success) {
                        broadcast(username + "���� ȸ�������ϼ̽��ϴ�.");
                    } else {
                        broadcast("ȸ�����Կ� �����ϼ̽��ϴ�. �ٽ� �õ����ּ���");
                    }
                } else if (msg.startsWith("liarTopic")) {
                    if (msg != null) {
                        ls.liarTopic = msg.substring(9);
                    }
                    System.out.println(ls.liarTopic);
                } else if (msg.startsWith("cVote")) {
                    msg = msg.substring(5);
                    ls.voteList.add(msg);
                    System.out.println("��ǥ : " + msg);
                    if (ls.voteList.size() == ls.v.size()) {
                        ls.gameThread.interrupt();
                    }
                } else if (msg.equals("enterfalse")) {
                } else if (msg.startsWith("ranking")) { // ��ŷ ��û
                    broadcast(getRankings());
                }

                else {
                    broadcast(msg);
                }
            }
        } catch (IOException ie) {
            ls.v.remove(this);
            // ȸ���� �α��� ���¿��� ������ ������ ���� �г�Ƽ�� �ο��մϴ�.
            if (isLoggedIn) {
                ban_plus(username);
                broadcast(username + " ���� �����ϼ̽��ϴ�. (���� �ο�: " + ls.v.size() + "��)");
            } else {
                broadcast(chatId + " ���� �����ϼ̽��ϴ�. (���� �ο�: " + ls.v.size() + "��)");
            }
            ls.sui.idBox.removeItem(chatId);
        } finally {
            closeAll();
        }
    }


    void broadcast(String msg) {
        try {
            // �޽������� ����� ID�� ���� �޽����� �и�
            int lastBraceIndex = msg.lastIndexOf("{");
            String userId, msgWithoutUsername;
            if (lastBraceIndex == -1) {  // '{'�� ���� ���
                userId = "unknown";  // username�� �� �� �����Ƿ� unknown���� ����
                msgWithoutUsername = msg;  // ��ü �޽����� msgWithoutUsername�� ����
            } else {  // '{'�� �ִ� ���
                userId = msg.substring(lastBraceIndex + 1);  // ������ �߰�ȣ ���İ� username
                msgWithoutUsername = msg.substring(0, lastBraceIndex);  // username�� ������ �޽����� ����
            }

            // ���þ� ���͸� ����
            if (isTopicPhase && msgWithoutUsername.contains(ls.liarTopic)) {
                System.out.println(userId + "������ �߼� !!!!!!!");
                // ���þ ������ �޽����� ���������� ó���ϰ� �ٸ� ����ڿ��Դ� ������ ����
                System.out.println(userId + ": " + msgWithoutUsername);  // ���� �ʿ��� �޽����� ���
                // ���⿡ DB�� Ban_count�� ������Ű�� �޼ҵ� ȣ��
                boolean success = ban_plus(userId);
                if (success) {
                    broadcast("���̵� : " + userId + " �� ����Դϴ�.");
                } else {
                    broadcast("�����ͺ��̽��� ������ �߻��߽��ϴ�.");
                }

                // ��� Ŭ���̾�Ʈ���� userId�� ����� �߼��ߴٰ� �˸�
                for (OneClientModul ocm : ls.v) {
                    ocm.dos.writeUTF(userId + "���� ����� �߼��߽��ϴ�.");
                    // ����� "***"�� ����ŷ ó��
                    msgWithoutUsername = msgWithoutUsername.replace(ls.liarTopic, "***");
                    ocm.dos.flush();
                }

            }

            // ���þ �������� ���� �޽����� ��� ����ڿ��� ����
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

    // �α���
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
                username = login_chatId;  // �α��ο� �������� �� username �ʵ� ������Ʈ

                // chatId�� username�� �����ϴ� �ڵ�
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

    // ȸ������
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
            System.out.println(rows+"ȸ�� ���� �Ϸ�" + user_id +" "+ nickname);
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


    private boolean ban_plus(String user_id) { // �� ī���� ó�� �޼ҵ�
        String url = "jdbc:mysql://localhost:3306/liargame";
        String dbUsername = "root";
        String dbPassword = "1234";

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            // ban_count ���� 1 ������Ű�� SQL ����
            String sql = "UPDATE member SET ban_count = IF(ban_count IS NULL, 1, ban_count + 1) WHERE user_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, user_id);

            int rows = statement.executeUpdate();
            System.out.println(user_id + "Ban_count + 1 �Ϸ�");
            return rows > 0; // ���������� ���� ������Ʈ�Ǿ��ٸ� true�� ��ȯ
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false; // ������ �߻��ϰų� ������Ʈ�� ���� ���ٸ� false�� ��ȯ
    }



    public void updateWinCountForLiar(String liar) { // ���̾� �¸��� ��/�� ��� ó�� �޼ҵ�
        String url = "jdbc:mysql://localhost:3306/liargame";
        String dbUsername = "root";
        String dbPassword = "1234";

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            // �г������� user_id ã��
            String sql = "SELECT user_id FROM Member WHERE nickname = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, liar);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String userId = resultSet.getString("user_id");

                // user_id�� �̿��Ͽ� win_count ���� ���� ī��Ʈ�� 1 ������Ű��
                sql = "UPDATE `Rank` SET win_count = win_count + 1, game_count = game_count+1 WHERE user_id = ?";
                statement = connection.prepareStatement(sql);
                statement.setString(1, userId);
                int rows = statement.executeUpdate();
                System.out.println(liar + "Win_count + 1 �Ϸ�");

                // user_id�� �̿��Ͽ� �¸� Ƚ�� ��������
                String sql2 = "SELECT win_count FROM `RANK` WHERE user_id = ?";
                PreparedStatement st2 = connection.prepareStatement(sql2);
                st2.setString(1, userId);
                ResultSet rs2 = st2.executeQuery();
                if (rs2.next()) {
                    int winCount = rs2.getInt("win_count");
                    ls.ocm.broadcast(liar + "���� �¸� Ƚ���� " + winCount + "�� �Դϴ�.");
                }
            }

            // ���̾ ������ ��� �������� game_count�� 1ȸ ������Ű��
            for (OneClientModul ocm : ls.v) {
                if (!ocm.chatId.equals(liar)) {
                    // ���̾ �ƴ� ������ user_id�� ��������
                    sql = "SELECT user_id FROM Member WHERE nickname = ?";
                    statement = connection.prepareStatement(sql);
                    statement.setString(1, ocm.chatId);
                    resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        String nonLiarUserId = resultSet.getString("user_id");

                        // user_id�� �̿��Ͽ� game_count ���� 1 ������Ű��
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

    public void updateWinCountForNonLiars(String liar) { // ���̾� �й� �� ��/�� ��� ó�� �޼ҵ�
        String url = "jdbc:mysql://localhost:3306/liargame";
        String dbUsername = "root";
        String dbPassword = "1234";

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            for (OneClientModul ocm : ls.v) {
                if (!ocm.chatId.equals(liar)) { // ���� ���̾ �ƴ� ��� win_count, game_count�� 1�� ����.
                    // �г������� user_id ã��
                    String sql = "SELECT user_id FROM Member WHERE nickname = ?";
                    PreparedStatement statement = connection.prepareStatement(sql);
                    statement.setString(1, ocm.chatId);
                    ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        String userId = resultSet.getString("user_id");

                        // user_id�� �̿��Ͽ� win_count ���� 1 ������Ű��
                        sql = "UPDATE `Rank` SET win_count = win_count + 1, game_count = game_count + 1 WHERE user_id = ?";
                        statement = connection.prepareStatement(sql);
                        statement.setString(1, userId);
                        statement.executeUpdate();
                    }
                } else { // ���� ���̾��� ��� game_count�� 1 ����
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


    public String getRankings() { // ��ŷ ó�� �޼ҵ�
        String rankings = "";
        String url = "jdbc:mysql://localhost:3306/liargame";
        String dbUsername = "root";
        String dbPassword = "1234";

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            // win_count�� ���� ������ ��� ������ ��ȸ�մϴ�.
            String sql = "SELECT user_id, win_count FROM `Rank` ORDER BY win_count DESC";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            // ��ȸ�� ����� ���ڿ��� ��ȯ�մϴ�.
            while (resultSet.next()) {
                rankings += "���� : " + resultSet.getString("user_id") + " : " + resultSet.getInt("win_count") + "��\n";
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
