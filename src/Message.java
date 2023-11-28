import net.jini.core.entry.Entry;

public class Message implements Entry {

    public String type;
    public String userID;
    public String mesage;
    public String roomID;
    public Boolean part;
    public String partMSend;
    public String partMRecieve;

    public Message() {}
}
