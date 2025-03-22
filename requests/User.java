package requests;

public class User {

    private Integer id;
    private String username;
    private String password;
    private int numMessages;
    private String lastMessageDate;

    public User(Integer id, String username, String password, int numMessages, String lastMessageDate) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.numMessages = numMessages;
        this.lastMessageDate = lastMessageDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getNumMessages() {
        return numMessages;
    }

    public void setNumMessages(int numMessages) {
        this.numMessages = numMessages;
    }

    public String getLastMessageDate() {
        return lastMessageDate;
    }

    public void setLastMessageDate(String lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
    }
}
