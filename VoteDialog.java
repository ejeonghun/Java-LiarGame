import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;

class VoteDialog extends JDialog implements ActionListener,ListSelectionListener{

    JList<String> voteList;
    JFrame frame;
    ClientUi clientUi;
    Client c;
    JPanel vPanel,p1;
    RoundedButton vButton = new RoundedButton("투표하기");
    String result;


    VoteDialog(Client c) {
        super(c.cui, "투표하기", true);
        this.clientUi = c.cui;
        this.c=c;
        this.frame = c.frame;

    }

    String getResult() {

        voteList = new JList<>(c.idList);
        voteList.setVisibleRowCount(4);
        voteList.setSelectedIndex(0);
        vPanel = new JPanel();
        setContentPane(vPanel);
        vPanel.setLayout(new BorderLayout());
        vPanel.add(voteList,BorderLayout.CENTER);
        voteList.setBounds(0,0,100,100);
        p1 = new JPanel();
        p1.add(vButton);
        p1.setBackground(Color.black);
        vPanel.add(p1,BorderLayout.SOUTH);
        vButton.setPreferredSize(new Dimension(100,30));
        voteList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        voteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        voteList.addListSelectionListener(this);
        vButton.setActionCommand("투표하기");
        vButton.addActionListener(this);

        voteList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() >= 2) {
                    vButton.doClick();

                }
            }
        });

        setUi();
        return result;
    }
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(vButton)) {
            int index = voteList.getSelectedIndex();
            result = c.idList.get(index);
        }

        dispose();
    }
    public void valueChanged(ListSelectionEvent e) {
        int index = voteList.getSelectedIndex();
        result = c.idList.get(index);
    }
void setUi(){
    setSize(300, 190);
    setLocationRelativeTo(null);
    setVisible(true);
    setBackground(Color.black);
}
}
