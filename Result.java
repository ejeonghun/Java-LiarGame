import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class Result extends JDialog implements ActionListener,Runnable {
    RoundedButton okBtn;
    JPanel Nliar, Tliar;
    JTextArea jta = new JTextArea();
    JScrollPane scr = new JScrollPane(jta);
    Thread timeTh;
    String result="";
    Result(Client c ,String str) {
        result = str;
        if(result.equals("liarWin")){
            liarWin();
        }else if(result.equals("liarLose")){
            liarLose();
        }


    }

    @Override
    public void run() {
        try {
            if (result.equals("liarLose")) {
                Thread.sleep(3100);
            }else if (result.equals("liarWin")) {
                Thread.sleep(4500);
            }
            okBtn.setVisible(true);
            okBtn.addActionListener(this);
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void liarWin(){
        Nliar = new ImagePanel("라이어아님.gif");
        Nliar.setLayout(null);
        setContentPane(Nliar);
        okBtn = new RoundedButton("확인");
        okBtn.setVisible(false);
        Nliar.add(okBtn);
        okBtn.setBounds(150, 110, 70, 30);
        timeTh=new Thread(this);
        timeTh.start();
        setUi();






    }
    void liarLose(){
        Tliar = new ImagePanel("라이어잡음.gif");
        Tliar.setLayout(null);
        setContentPane(Tliar);
        okBtn = new RoundedButton("확인");
        okBtn.setVisible(false);
        Tliar.add(okBtn);
        okBtn.setBounds(150, 110, 70, 30);
        timeTh=new Thread(this);
        timeTh.start();
        setUi();
    }
    void setUi() {
        setTitle("결과!");
        setVisible(true);
        setSize(380, 185);
        setResizable(false);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    public void actionPerformed(ActionEvent e){
        dispose();
    }

}