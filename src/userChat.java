import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.*;

/**
 * klasa dziedzicząca po klasie JFrame
 * reprezentuje okno chatu
 */
public class userChat extends JFrame {
    private JButton sendButton;
    private JTextField textField;
    private JPanel chatPanel;
    private JTextPane textPane;

    private InetAddress aHost;
    private byte data[] = null;
    private String message;

    private DatagramSocket datagramSocket;
    private SimpleAttributeSet attributeSet = new SimpleAttributeSet();\


    private boolean dispose;

    /**
     * konstruktor nadający zawartość zmiennym wykorzystywanym przy połączeniu
     * UDP client - client
     * dodanie listenerów
     * @param datagramSocket socket UDP
     * @param portFrom port do wysyłania
     */
    public userChat(DatagramSocket datagramSocket, int portFrom) throws HeadlessException, UnknownHostException, SocketException {
        this.datagramSocket = datagramSocket;

        aHost = InetAddress.getLocalHost();

        init();
        dispose = false;

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // read message from user
                    message = textField.getText();
                            // split for request function
                    data = (message.trim()).getBytes();
                    appendText((message.trim()+ "\n"), Color.gray);
                    DatagramPacket packet = new DatagramPacket(data, data.length, aHost, portFrom);
                    datagramSocket.send(packet);
                    textField.setText("");
                }catch (SocketException ee) {
                } catch (IOException ex) {
                    ex.printStackTrace();
                    // parent.quit();
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                dispose = true;
            }
        });
    }

    /**
     * @return wartość logiczna zmiennej dispose
     */
    public boolean isDispose() {
        return dispose;
    }

    /**
     * @return obiekt klasy JPanel
     */
    public JPanel getChatPanel() {
        return chatPanel;
    }

    /**
     * metoda dodająca zadany tekst do obkietu JTextPane o podonym kolorze
     * @param s tekst do dodania
     * @param c kolor tekstu
     */
    private void appendText(String s, Color c) throws BadLocationException {
        attributeSet = new SimpleAttributeSet();
        StyleConstants.setItalic(attributeSet, true);
        StyleConstants.setForeground(attributeSet, c);

        Document doc = textPane.getStyledDocument();
        doc.insertString(doc.getLength(), s, attributeSet);
    }

    /**
     * inicjuje wątek nasłuchujący wiadomości
     */
    public void init(){
        textPane.setEditable(false);
        getRootPane().setDefaultButton(sendButton);
        (new Thread() {
            @Override
            public void run() {
                DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
                while (true) {
                    try {
                        datagramSocket.receive(packet);
                        String received = new String(packet.getData(), 0, packet.getLength());
                        appendText(received + "\n", Color.black);
                        //System.out.println("Message received ..."+ temp);
                    } catch (SocketException ee) {
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        //parent.quit();
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}