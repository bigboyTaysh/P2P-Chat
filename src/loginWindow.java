import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class loginWindow extends JFrame{
    private JButton loginButton;
    private JTextField loginTextField;
    private JLabel loginLabel;
    private JLabel statusLogin;
    private JPanel loginPanel;

    private String userName;
    private boolean clicked;


    public loginWindow() {
        getRootPane().setDefaultButton(loginButton);
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userName = loginTextField.getText();
                clicked = true;
            }
        });
    }

    public String getUserName() {
        return userName;
    }

    public boolean isClicked() {
        return clicked;
    }

    public void setClicked(boolean boo) {
        this.clicked = boo;
    }

    public JPanel getLoginPanel() {
        return loginPanel;
    }

    public void alert(String alert){
        JOptionPane.showMessageDialog(null, alert);
    }
}
