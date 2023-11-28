
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;



import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;

public class ChatUI extends JFrame {
    private String userID;
    private String roomID;
    private JButton backButton;
    private JButton chatButton;
    private JTextArea chat;
    private JScrollPane chatS;
    private JTextField chatT;
    private JavaSpace space;

    public ChatUI(String userID, String roomID, JavaSpace space) {
        this.userID = userID;
        this.roomID = roomID;
        this.space = space;
        initUI();
        initThread();
        backButton();
        initChat();
    }

    private void initUI() {
        this.backButton = new JButton();
        this.chat = new JTextArea();
        this.chatS = new JScrollPane();
        this.chatButton = new JButton();
        this.chatT = new JTextField();
        this.setResizable(false);
        this.setSize(450, 450);
        this.setTitle( roomID + " - Usuario: " + userID);
        this.setContentPane(new JLabel());
        JLabel chatLabel = new JLabel( roomID.toUpperCase());
        chatLabel.setFont(new Font("Arial", Font.BOLD, 14));
        chatLabel.setBounds(200, 0, 200, 60);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                removeUser(userID, roomID);
                ChatUI.this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
        });

        chat.setEditable(false);
        chat.setColumns(20);
        chat.setRows(5);
        chat.setWrapStyleWord(true);
        chat.setLineWrap(true);
        chat.setFont(chat.getFont().deriveFont(12f));
        chat.setMargin(new Insets(10, 10, 10, 10));
        DefaultCaret caret = (DefaultCaret) chat.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        chatS.setViewportView(chat);
        chatS.setBounds(20, 40, 380, 260);
        chatT.setBounds(20, 310, 280, 80);
        chatButton.setText("Enviar");
        chatButton.setBounds(320, 310, 80, 39);
        backButton.setText("Voltar");
        backButton.setBounds(320, 350, 80, 39);
        chat.append(
                  "\n* Para mensagens particular Digite '/para nome mensagem' *"
                + "\n* Para ver os nomes Digite '/nomes' *\n");
        try {
            Message msg = new Message();
            msg.userID = userID;
            msg.roomID = roomID;
            msg.type = "connected";
            msg.mesage = "\n___ " + userID + " se conectou ___";

            space.write(msg, null, Lease.FOREVER);
            Message msgTemplate = new Message();
            space.take(msgTemplate, null, Lease.FOREVER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.add(backButton);
        this.add(chatLabel);
        this.add(chatS);
        this.add(chatT);
        this.add(chatButton);
        this.setVisible(true);


    }

    private void initChat() {
        ActionListener actionListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent ae) {
                sendMsg();
            }
        };

        KeyListener keyListener = new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMsg();
                }
            }
        };

        chatButton.addActionListener(actionListener);
        chatT.addKeyListener(keyListener);
    }
    private void backMain() {
        this.removeAll();
        this.setVisible(false);
        new RoomUI(this.userID);
        removeUser(userID, roomID);
    }
    private void nameList() {
        User template = new User();
        template.roomID = roomID;
        User user;
        List<User> users = new ArrayList<User>();
        try {
            while (true) {
                user = (User) space.take(template, null, 1000);

                if (user != null) {
                    users.add(user);
                } else {
                    break;
                }
            }
            if (!users.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append("\n\n******** Nomes ********\n");
                for (User connectedUser : users) {
                    sb.append("\n" + connectedUser.userID);

                    space.write(connectedUser, null, Lease.FOREVER);
                }
                sb.append("\n\n************************\n");
                chat.append(sb.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMsg() {
        String message = chatT.getText();

        chatT.setText("");

        try {
            if (message.startsWith("/nomes")) {
                chat.append("\n" + userID + ": " + message);

                nameList();
            }
            else {
                Message msg = new Message();

                msg.userID = userID;
                msg.roomID = roomID;
                msg.type = "chat";

                if (message.startsWith("/para")) {
                    msg.part = true;
                    msg.partMSend = userID;

                    String[] sp = message.split(" ", 3);

                    try {
                        msg.partMRecieve = sp[1];
                        msg.mesage = sp[2];

                        chat.append("\n## Mensagem Particular Para " + sp[1] + ": " + sp[2] + " ##");
                    } catch (Exception e) {
                        msg.mesage = message;
                        msg.part = false;
                    }
                }
                else {
                    msg.mesage = message;
                    msg.part = false;

                    chat.append("\n" + userID + ": " + message);
                }

                space.write(msg, null, Lease.FOREVER);

                Message template = new Message();
                space.take(template, null, Lease.FOREVER);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateChatPosition();
    }
    private void backButton() {
        ActionListener al = new ActionListener()
        {
            public void actionPerformed(ActionEvent ae) {
                backMain();
            }
        };
        backButton.addActionListener(al);
    }

    private void updateChatPosition() {
        try {
            chat.setCaretPosition(chat.getLineStartOffset(chat.getLineCount() - 1));
        } catch (BadLocationException e) {
            System.out.println("BadLocationException - updateChatPosition()");
        }
        chatT.grabFocus();
    }
    public void initThread() {
        MessageReader messageListener = new MessageReader(space, chat, userID, roomID);
        messageListener.start();
    }

    private void removeUser(String userID, String roomID) {
        User template = new User();
        template.userID = userID;
        template.roomID = roomID;
        User user;
        try {
            user = (User) space.take(template, null, 1000);
            if (user != null) {
                Message msg = new Message();
                msg.userID = userID;
                msg.roomID = roomID;
                msg.type = "disconnected";
                msg.mesage = "\n----- " + userID + " se desconectou da sala! -----";
                space.write(msg, null, Lease.FOREVER);
                Message msgTemplate = new Message();
                space.take(msgTemplate, null, Lease.FOREVER);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}