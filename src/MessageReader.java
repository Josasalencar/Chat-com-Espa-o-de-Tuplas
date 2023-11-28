
import javax.swing.JTextArea;

import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;

public class MessageReader extends Thread {

    JavaSpace space;
    JTextArea chatArea;
    String userID;
    String roomID;
    public MessageReader(JavaSpace space, JTextArea chatArea, String userID, String roomID) {
        this.userID = userID;
        this.roomID = roomID;
        this.space = space;
        this.chatArea = chatArea;

    }
    @Override
    public void run() {
        while (true) {
            Message template = new Message();
            Message msg;
            try {
                msg = (Message) space.read(template, null, Lease.FOREVER);
                if (msg != null) {
                    if (msg.roomID.equalsIgnoreCase(roomID)) {
                        if (msg.type.contentEquals("connected") || msg.type.contentEquals("disconnected")) {
                            chatArea.append(msg.mesage);
                            chatArea.setCaretPosition(chatArea.getLineStartOffset(chatArea.getLineCount() - 1));
                        }
                        else {
                            if (!msg.userID.equalsIgnoreCase(userID) && !msg.part) {
                                chatArea.append("\n" + msg.userID + ": " + msg.mesage);
                                chatArea.setCaretPosition(chatArea.getLineStartOffset(chatArea.getLineCount() - 1));
                            }
                            if (!msg.userID.equalsIgnoreCase(userID) && msg.part) {
                                if (msg.partMRecieve.equalsIgnoreCase(userID)) {
                                    chatArea.append("\n## Mensagem Particular de " + msg.partMSend + ": " + msg.mesage + " ##");
                                    chatArea.setCaretPosition(chatArea.getLineStartOffset(chatArea.getLineCount() - 1));
                                }
                            }
                        }
                    }
                    Thread.sleep(10);
                }
            } catch (Exception e) {}
        }
    }
}