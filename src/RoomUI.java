
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;

public class RoomUI extends JFrame  {

    private JavaSpace space;
    private String userID;
    private JPanel chatList;
    private JScrollPane chatScrollPane;
    private ButtonGroup roomButtonGroup;
    private JButton choseButton;
    private JButton newChatRoomButton;
    private JButton updateButton;

    User user;
    Room room;

    public RoomUI(String userID) {
        this.userID = userID;
        System.out.println("lookup");
        Lookup finder = new Lookup(JavaSpace.class);
        this.space = (JavaSpace) finder.getService();

        initSpace();
        initUI();
        createUser();
        creteRoom();
        choseRoomButton();
        attButton();
    }

    public void initSpace() {
        try {
            System.out.println("Procurando pelo servico JavaSpace...");

            if (space == null) {
                System.out.println("O servico JavaSpace nao foi encontrado. Encerrando...");
                System.exit(-1);
            }

            System.out.println("O servico JavaSpace foi encontrado.");
            System.out.println(space);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initUI() {
        this.chatList = new JPanel();
        this.chatScrollPane = new JScrollPane();
        this.updateButton = new JButton();
        this.choseButton = new JButton();
        this.newChatRoomButton = new JButton();
        this.roomButtonGroup = new ButtonGroup();
        this.setResizable(false);
        this.setTitle("Salas");
        this.setSize(400, 500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(new JLabel());
        JLabel sensorLabel = new JLabel("Salas");
        sensorLabel.setFont(new Font("Arial", Font.BOLD, 18));
        sensorLabel.setBounds(170, 100, 200, 60);
        chatList.setBackground(Color.WHITE);
        chatList.setLayout(new BoxLayout(chatList, BoxLayout.Y_AXIS));
        chatScrollPane.setViewportView(chatList);
        chatScrollPane.setBounds(40, 150, 310, 250);
        updateButton.setText("Atualizar");
        updateButton.setBounds(50, 60, 100, 30);
        newChatRoomButton.setText("Criar Sala");
        newChatRoomButton.setBounds(250, 60, 100, 30);
        choseButton.setText("Escolher Sala");
        choseButton.setBounds(90, 410, 200, 30);
        choseButton.setFont(new Font("Arial", Font.BOLD, 18));

        this.add(sensorLabel);
        this.add(chatScrollPane);
        this.add(updateButton);
        this.add(choseButton);
        this.add(newChatRoomButton);

    }
    private void choseRoomButton( ){
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!validatedRoom()) {
                    return;
                }
                String roomID = roomButtonGroup.getSelection().getActionCommand();
                System.out.println(userID);
                System.out.println(roomID);
                RoomUI.this.removeAll();
                RoomUI.this.setVisible(false);
                new ChatUI(userID, roomID, space);
            }
        };
        choseButton.addActionListener(al);
    }

    private boolean validatedRoom() {
        boolean isValid = true;

        if (!validateButtonGroup(roomButtonGroup)) {
            isValid = false;

            JOptionPane.showMessageDialog(null, "Você deve escolher uma sala para cotinuar", "Escolha uma Sala !", JOptionPane.ERROR_MESSAGE);
        }
        return isValid;
    }

    private boolean validateButtonGroup(ButtonGroup buttonGroup) {
        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();

            if (button.isSelected()) {
                return true;
            }
        }

        return false;
    }
    private void createUser(){
        String userID;
        JLabel userIDLabel = new JLabel("Usuário: " + this.userID);
        userIDLabel.setFont(new Font("Arial", Font.BOLD, 16));
        userIDLabel.setBounds(40, 0, 200, 60);
        if (!verifyIfUserExists(this.userID)) {
            while (true) {
                userID = JOptionPane.showInputDialog(getParent(), "Digite o nome do Usuario:");

                if (userID == null) {
                    break;
                }
                if (userID.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Você deve escolher um nome de Usuario", "Escolha seu Usuario", JOptionPane.ERROR_MESSAGE);
                }
                if (verifyIfUserExists(userID)) {
                    JOptionPane.showMessageDialog(null, "Já existe um usuário com esse nome por favor escolhar outro nome " + userID, "Usuario já existe", JOptionPane.ERROR_MESSAGE);
                }
                if (!verifyIfUserExists(userID) && !userID.isEmpty()) {

                    User user = new User();
                    user.userID = userID;
                    this.userID = userID;
                    System.out.println(this.userID);

                    userIDLabel = new JLabel("Usuário: " + this.userID);
                    userIDLabel.setFont(new Font("Arial", Font.BOLD, 16));
                    userIDLabel.setBounds(40, 0, 200, 60);

                    try {
                        space.write(user, null, Lease.FOREVER);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        this.add(userIDLabel);
        RoomUI.this.setVisible(true);
    }

    private void creteRoom() {
        ActionListener al = new ActionListener()
        {
            public void actionPerformed(ActionEvent ae) {
                String roomID;
                while (true) {
                    roomID = JOptionPane.showInputDialog(getParent(), "Digite o nome da sala:");
                    if (roomID == null) {
                        break;
                    }
                    if (roomID.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "O nome da sala não pode ser vazio", "Erro",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    if(verifyIfRoomExists(roomID)){
                        JOptionPane.showMessageDialog(null, "Já existe uma sala com o nome: " + roomID, "Erro",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    if (!roomID.isEmpty() && !verifyIfRoomExists(roomID) ){
                            Room chatRoom = new Room();
                            chatRoom.name = roomID;
                            try {
                                space.write(chatRoom, null, Lease.FOREVER);
                            } catch (Exception e) {}
                            break;
                    }
                }
            }
        };
        newChatRoomButton.addActionListener(al);
    }
    private boolean verifyIfRoomExists(String roomID) {
        Room template = new Room();
        template.name = roomID;
        try {
            room = (Room) space.read(template, null, 1000);
            if (room != null) {
                return true;
            }
        } catch (Exception e) {}
        return false;
    }

    private boolean verifyIfUserExists(String userID) {
        User template = new User();
        template.userID = userID;
        try {
            user = (User) space.read(template, null, 1000);
            if (user != null) {
                return true;
            }
        } catch (Exception e) {}
        return false;
    }

    private void attButton() {
        ActionListener al = new ActionListener()
        {
            public void actionPerformed(ActionEvent ae) {
                Room template = new Room();
                Room chatRoom;
                List<Room> rooms = new ArrayList<Room>();
                roomButtonGroup = new ButtonGroup();
                chatList.removeAll();
                try {
                    while (true) {
                        chatRoom = (Room) space.take(template, null, 3 * 1000);
                        if (chatRoom != null) {
                            rooms.add(chatRoom);
                        } else {
                            break;
                        }
                    }
                    if (rooms.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Não existe nenhuma sala criada, tente novamente mais tarde",
                                "Aviso", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        for (Room room : rooms) {
                            JRadioButton radioButton = new JRadioButton(room.name);
                            radioButton.setFont(new Font("Arial", Font.PLAIN, 12));
                            radioButton.setBackground(new Color(0, 0, 0, 0));
                            radioButton.setOpaque(false);
                            radioButton.setActionCommand(room.name);
                            roomButtonGroup.add(radioButton);
                            chatList.add(radioButton);
                            SwingUtilities.updateComponentTreeUI(chatList);
                            space.write(room, null, Lease.FOREVER);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        updateButton.addActionListener(al);
    }

    public static void main(String[] args) {
        String userID = "";
        new RoomUI(userID);
    }

}