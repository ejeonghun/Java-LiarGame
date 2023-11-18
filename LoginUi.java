import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.RoundRectangle2D;
import java.io.*;

import static java.lang.Thread.*;

class LoginUi extends JFrame implements ActionListener, Runnable {

    String id, ip, port;
    RoundedButton serverBtn, clientBTn, endBtn;
    JPanel p1;
    File file = new File("BMEuljiro10yearslater.ttf");
    Font font;
    Thread th = new Thread(this);

    {
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, file);
            File file = new File("bgm.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        if (currentThread().equals(th)) {
            try {
                sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            serverBtn.setVisible(true);
            clientBTn.setVisible(true);
            endBtn.setVisible(true);
        }

    }

    LoginUi() {
        init();
    }

    void init() { //
        serverBtn = new RoundedButton("서버 생성하기");
        clientBTn = new RoundedButton("서버 입장하기");
        endBtn = new RoundedButton("종료하기");
        p1 = new ImagePanel("back2.gif");
        setContentPane(p1);
        p1.add(serverBtn);
        p1.add(clientBTn);
        p1.add(endBtn);
        setUi();
        serverBtn.setBounds(580, 320, 150, 40);
        serverBtn.setVisible(false);
        clientBTn.setBounds(580, 400, 150, 40);
        clientBTn.setVisible(false);
        endBtn.setBounds(580, 480, 150, 40);
        endBtn.setVisible(false);
        action();
        th.start();
    }

    void setUi() {
        setTitle("라이어게임");
        setVisible(true);
        setSize(805, 630);
        setResizable(false);
        setLayout(null);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void actionPerformed(ActionEvent e) {                                      //버튼 리스너 생성
        if (e.getSource().equals(serverBtn)) {
            LoginDialog lD = new LoginDialog(this, this, "서버 생성하기");
            ip = lD.ipTf.getText();
            port = lD.portTf.getText();

        }
        if (e.getSource().equals(clientBTn)) {
            LoginDialog lD = new LoginDialog(this, this, "서버 입장하기");
            ip = lD.ipTf.getText();
            port = lD.portTf.getText();
        }
        if (e.getSource().equals(endBtn)) {
            System.exit(0);
        }
    }

    void action() {                                                                       //버튼리스너 입력
        serverBtn.addActionListener(this);
        clientBTn.addActionListener(this);
        endBtn.addActionListener(this);
    }

    void reopen() {
        setVisible(true);

    }

    public static void main(String[] args) {
        new LoginUi();
    }
}                                                                                           //LoginUi

class ImagePanel extends JPanel {

    Image image;

    public ImagePanel(String str) {                                                  //       패널에 이미지 입또歐

        image = Toolkit.getDefaultToolkit().createImage(str);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.drawImage(image, 0, 0, this);
        }
    }

}

class LoginDialog extends JDialog implements ActionListener, KeyListener { //    버튼 클릭시 생성되는 창          todo
    JTextField idTf, ipTf, portTf;
    JLabel idLb, ipLb, portLb;
    JButton okBtn, noBtn, ipBtn;
    JPanel p1;
    JFrame frame;
    LoginUi ui;
    String title;

    LoginDialog(JFrame frame, LoginUi ui, String title) {
        super(frame, title, true);
        this.frame = frame;
        this.ui = ui;
        this.title = title;
        init();
        if (getTitle().equals("서버 생성하기")) {
            addS();
            setUiS();
        } else if (getTitle().equals("서버 입장하기")) {
            addC();
            setUiC();
        }
    }

    void init() {

        idLb = new JLabel("아이디");
        idLb.setOpaque(true);
        idLb.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        idLb.setBackground(new Color(255,224,140));
        idLb.setHorizontalAlignment(JLabel.CENTER);
        idTf = new JTextField(10);

        ipLb = new JLabel("아이피");
        ipLb.setOpaque(true);
        ipLb.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        ipLb.setBackground(new Color(255,224,140));
        ipLb.setHorizontalAlignment(JLabel.CENTER);
        ipTf = new JTextField(10);

        portLb = new JLabel("포트");
        portLb.setOpaque(true);
        portLb.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        portLb.setBackground(new Color(255,224,140));
        portLb.setHorizontalAlignment(JLabel.CENTER);
        portTf = new JTextField(10);
        portTf.addKeyListener(this);

        okBtn = new JButton("확인");
        okBtn.setBackground(new Color(255,166,72));
        noBtn = new JButton("취소");
        noBtn.setBackground(new Color(255,166,72));
        okBtn.addActionListener(this);
        noBtn.addActionListener(this);
        p1 = new JPanel();
    }

    void setUiS() {
        setLayout(new GridLayout(2, 2));
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    void setUiC() {
        setLayout(new GridLayout(4, 2));
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    void addS() {
        add(portLb);
        add(portTf);
        add(okBtn);
        add(noBtn);
    }

    void addC() { //
        add(idLb);
        add(idTf);
        add(ipLb);
        add(ipTf);
        add(portLb);
        add(portTf);
        add(okBtn);
        add(noBtn);
    }


    public void actionPerformed(ActionEvent e) {                                            //todo ip,port,id null잡아주기

        if (e.getSource().equals(okBtn) & title.equals("서버 생성하기")) {
            ui.port = portTf.getText().trim();
            Boolean chk = check();
            if (chk = true) {
                dispose();
                frame.dispose();
                new ServerUi(ui);
            }
        } else if (e.getSource().equals(okBtn) & title.equals("서버 입장하기")) {
            ui.id = idTf.getText().trim();
            ui.ip = ipTf.getText().trim();
            ui.port = portTf.getText().trim();
            Boolean chk = check();
            if (chk == true) {
                dispose();
                frame.dispose();
                new ClientUi(ui);
            } else {
                portTf.setText("");
            }
        } else if (e.getSource().equals(noBtn)) {
            idTf.setText("");
            ipTf.setText("");
            portTf.setText("");
            dispose();
        }
    }

    boolean check() {
        try {


            int i = Integer.parseInt(ui.port);
            if (1 > i | i > 65535) {
                JOptionPane.showMessageDialog(null, "정확한 포트를 입력해주세요.");
                return false;
            }
            return true;
        } catch (NumberFormatException ne) {
            JOptionPane.showMessageDialog(null, "정확한 포트를 입력해주세요");
            return false;
        }
    }
    @Override
    public void keyTyped(KeyEvent e) {
    }
    @Override
    public void keyReleased(KeyEvent e) {
    }
    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER){
            okBtn.doClick();
        }
    }
} //로그인 다이얼로그

class RoundedButton extends JButton {                                                       //라운디드 버튼 클래스

    private static final long serialVersionUID = 1L;
    private Color startColor = new Color(217, 173, 29);
    private Color endColor = new Color(250, 129, 2);
    private Color rollOverColor = new Color(255, 143, 89);
    private Color pressedColor = new Color(204, 67, 0);
    private int outerRoundRectSize = 10;
    private int innerRoundRectSize = 8;
    private GradientPaint GP;

    public RoundedButton() {
        this(null, null);
    }

    public RoundedButton(String text) {
        this(text, null);
    }

    public RoundedButton(Action a) {
        this(null, null);
        setAction(a);
    }

    public RoundedButton(Icon icon) {
        this(null, icon);
    }

    public RoundedButton(String text, Icon icon) {
        super(text, icon);

        setContentAreaFilled(false);
        setBorderPainted(false);
        setFont(new Font("맑은 고딕", Font.BOLD, 16));
        setForeground(Color.WHITE);
        setFocusable(false);
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int h = getHeight();
        int w = getWidth();
        ButtonModel model = getModel();

        if (!model.isEnabled()) {
            setForeground(Color.GRAY);
            GP = new GradientPaint(0, 0, new Color(192, 192, 192), 0, h, new Color(192, 192, 192), true);
        } else {
            setForeground(Color.white);
            if (model.isRollover()) {
                GP = new GradientPaint(0, 0, rollOverColor, 0, h, rollOverColor, true);
            } else {
                GP = new GradientPaint(0, 0, startColor, 0, h, endColor, true);
            }
        }

        g2d.setPaint(GP);
        GradientPaint p1;
        GradientPaint p2;
        if (model.isPressed()) {
            GP = new GradientPaint(0, 0, pressedColor, 0, h, pressedColor, true);
            g2d.setPaint(GP);
            p1 = new GradientPaint(0, 0, new Color(0, 0, 0), 0, h - 1, new Color(100, 100, 100));
            p2 = new GradientPaint(0, 1, new Color(0, 0, 0, 50), 0, h - 3, new Color(255, 255, 255, 100));
        } else {
            p1 = new GradientPaint(0, 0, new Color(100, 100, 100), 0, h - 1, new Color(0, 0, 0));
            p2 = new GradientPaint(0, 1, new Color(255, 255, 255, 100), 0, h - 3, new Color(0, 0, 0, 50));
            GP = new GradientPaint(0, 0, startColor, 0, h, endColor, true);
        }
        RoundRectangle2D.Float r2d = new RoundRectangle2D.Float(0, 0, w - 1, h - 1, outerRoundRectSize, outerRoundRectSize);
        Shape clip = g2d.getClip();
        g2d.clip(r2d);
        g2d.fillRect(0, 0, w, h);
        g2d.setClip(clip);
        g2d.setPaint(p1);
        g2d.drawRoundRect(0, 0, w - 1, h - 1, outerRoundRectSize, outerRoundRectSize);
        g2d.setPaint(p2);
        g2d.drawRoundRect(1, 1, w - 3, h - 3, innerRoundRectSize, innerRoundRectSize);
        g2d.dispose();

        super.paintComponent(g);
    }

    /**
     * This method sets the Actual Background Color of the Button
     */
    public void setStartColor(Color color) {
        startColor = color;
    }

    /**
     * This method sets the Pressed Color of the Button
     */
    public void setEndColor(Color pressedColor) {
        endColor = pressedColor;
    }

    /**
     * @return Starting Color of the Button
     */
    public Color getStartColor() {
        return startColor;
    }

    /**
     * @return Ending Color of the Button
     */
    public Color getEndColor() {
        return endColor;
    }

    public void setRollOverColor(Color rollOverColor) {
        this.rollOverColor = rollOverColor;
    }

    public Color getRollOverColor() {
        return rollOverColor;
    }

    public void setPressedColor(Color pressedColor) {
        this.pressedColor = pressedColor;
    }

    public Color getPressedColor() {
        return pressedColor;
    }
}







