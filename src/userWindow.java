import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * klasa dziedzicząca po klasie JFrame
 * reprezentuje okno użytkownika
 */
public class userWindow extends JFrame {

    private JTable usersTable;
    private JPanel userPanel;
    private JButton connectButton;
    private JLabel usernameLabel;
    private JButton logoutButton;
    private JScrollPane scrollPane;
    private JLabel textLabel;

    private int index;
    private TableModel model;
    private String nick;

    private boolean clicked;
    private boolean clicked2;
    private boolean clicked3;
    private boolean requset;

    /**
     * konstruktor nadający zawartość kolumn tabeli
     * dodanie listenerów
     */
    public userWindow() {
        usersTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{

                },
                new String[]{
                        "Nick", "Zalogowany"
                }
        ) {
            boolean[] canEdit = new boolean[]{
                    false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        usersTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                table1MouseClicked(evt);
            }
        });

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clicked = true;
            }
        });
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clicked3 = true;
            }
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                clicked3 = true;
            }
        });

    }

    /**
     * pobiera wartości zaznaczonego wiersza
     */
    private void table1MouseClicked(java.awt.event.MouseEvent evt) {
        this.index = usersTable.getSelectedRow();
        this.model = usersTable.getModel();
        this.nick = model.getValueAt(index, 0).toString();
    }

    /**
     * @param alert utworzenie okna JOptionPane z podanym tekstem
     */
    public void alert(String alert){
        JOptionPane.showMessageDialog(null, alert);
    }

    /**
     * wyświetla powiadomienie z opcją wyboru o próbie nawiązania połączenia
     * @param source nazwa użytkownika próbującego nawiązać połączenie
     * @param destination nazwa użytkownika odbierający próbuję nawiązania połączenia
     */
    public void request (String source, String destination) {
        String title = "Hej "+ destination + ", próba nawiązania połączenia!";
        String message = source + " chce nawiązać połączenie, zgadzasz się?";

        int reply = JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION);
        if (reply == JOptionPane.YES_OPTION) {
            clicked2 = true;
            requset = true;
        }
        else {
            System.out.println("kliknięto nie");
            clicked2=true;
            requset = false;
        }
    }

    /**
     * dodaje do tabeli wiersz z podanym tekstem
     * @param users tekst do dodania w tabeli
     */
    public void addUsers(String[] users) {
        DefaultTableModel model = (DefaultTableModel) usersTable.getModel();
        Object rowData[] = new Object[3];

        rowData[0] = users[0];
        rowData[1] = users[2];
        model.addRow(rowData);
        System.out.println("dodano");
    }

    /**
     * usunięcie z tabeli wiersza który zawiera podany tekst
     * @param users tekst porównawczy
     */
    public void removeUser(String users){
        DefaultTableModel model = (DefaultTableModel) usersTable.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (((String)model.getValueAt(i, 0)).equals(users)) {
                model.removeRow(i);
            }//end of if block
        }//end of for block
    }

    /**
     * @return zwraca obiekt klasy JPanel
     */
    public JPanel getUserPanel() {
        return userPanel;
    }

    /**
     * ustawia wartość logiczną zmiennej clicket
     * @param boo wartość zmiennej do przypisania
     */
    public void setClicked(boolean boo) {
        this.clicked = boo;
    }

    /**
     * @return wartość logiczna zmiennej clicked
     */
    public boolean isClicked() {
        return clicked;
    }

    /**
     * @param username tekst do przypisania do obketu JLabel
     */
    public void setUsernameLabel(String username) {
        this.usernameLabel.setText(username);
    }

    /**
     * @return wartość zmiennej nick
     */
    public String getUserName() {
        return nick;
    }

    /**
     * @return wartość logiczna zmiennej clicked2
     */
    public boolean isClicked2() {
        return clicked2;
    }

    /**
     * @return wartość logiczna zmiennej clicked3
     */
    public boolean isClicked3() {
        return clicked3;
    }

    /**
     * @return wartość logiczna zmiennej request
     */
    public boolean isRequset() {
        return requset;
    }

    /**
     * @param clicked2 wartość logiczna do przypisania zmiennej clicked2
     */
    public void setClicked2(boolean clicked2) {
        this.clicked2 = clicked2;
    }

    /**
     * @param clicked3 wartość logiczna do przypisania zmiennej clicked3
     */
    public void setClicked3(boolean clicked3) {
        this.clicked3 = clicked3;
    }

    /**
     * @param requset wartość logiczna do przypisania zmiennej request
     */
    public void setRequset(boolean requset) {
        this.requset = requset;
    }
}
