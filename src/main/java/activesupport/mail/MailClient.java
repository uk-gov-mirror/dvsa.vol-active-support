package activesupport.mail;

import activesupport.mail.enums.MailProtocol;
import org.apache.commons.lang3.StringUtils;

import javax.mail.*;
import javax.mail.search.SearchTerm;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class MailClient implements AutoCloseable {

    private Session session;
    private Store store;
    private Folder folder;

    private MailClient(){}

    private MailClient(Builder builder) throws MessagingException {
        Properties defaults = protocolProperties(builder.protocol, builder.host, builder.port);
        defaults.putAll(builder.properties);

        session = Session.getDefaultInstance(defaults);
        store = session.getStore(builder.protocol.toString());
        store.connect(builder.host, builder.email, builder.password);

        try {
            folder = store.getFolder("Inbox"); // Defaults to getting the inbox folder.
        } catch (MessagingException e) {
            System.out.println("Unable to set folder to Inbox");
        }
    }

    public List<Message> findBySubject(String subject) throws MessagingException {
        return find(new SearchTerm() {
            @Override
            public boolean match(Message message) {
                try {
                    if (StringUtils.containsIgnoreCase(message.getSubject(), subject))
                        return true;
                } catch (MessagingException ex) {
                    ex.printStackTrace();
                }

                return false;
            }
        });
    }

    public List<Message> findByRecipient(String recipient, Message.RecipientType type) throws MessagingException {
        return find(new SearchTerm() {
            @Override
            public boolean match(Message message) {
                try {
                    return Arrays.asList(message.getRecipients(type)).parallelStream()
                            .anyMatch((address) -> StringUtils.containsIgnoreCase(address.toString(), recipient));
                } catch (MessagingException ex) {
                    ex.printStackTrace();
                }

                return false;
            }
        });
    }

    public List<Message> find(SearchTerm criterion) throws MessagingException {
        return find(folder, criterion);
    }

    public List<Message> find(Folder folder, SearchTerm criterion) throws MessagingException {
        if (!this.folder.isOpen())
            this.folder.open(Folder.READ_ONLY);

        return new ArrayList<>(Arrays.asList(this.folder.search(criterion))); // Done this to return a mutable list.
    }

    private Properties protocolProperties(MailProtocol mailProtocol, String host, int portNumber){
        String port = String.valueOf(portNumber);
        Properties properties = new Properties();

        switch (mailProtocol) {
            case Imap:
                properties.setProperty("mail.store.protocol", "imap");
                properties.setProperty("mail.imap.host", host);
                properties.setProperty("mail.imap.port", port);
                properties.put("mail.imap.starttls.enable", "true");
                break;
            case Pop3:
                properties.put("mail.store.protocol", "pop3");
                properties.put("mail.pop3.host", host);
                properties.put("mail.pop3.port", port);
                properties.put("mail.pop3.starttls.enable", "true");
                break;
            default:
                throw new IllegalArgumentException("Unsupported mail protocol: ".concat(mailProtocol.toString()));
        }

        return properties;
    }

    public Folder getFolder() throws MessagingException {
        if (!folder.isOpen())
            folder.open(Folder.READ_ONLY);
        return folder;
    }

    public Folder getFolder(String folder) throws MessagingException {
        this.folder = store.getFolder(folder);
        return this.folder;
    }

    @Override
    public void close() throws MessagingException {
        if (folder.isOpen())
            folder.close(true);
        if (store.isConnected())
            store.close();
    }

    public static class Builder {

        private Properties properties = new Properties();
        private MailProtocol protocol;
        private String email;
        private String password;
        private String host;
        private int port;

        public Builder() {}

        public Builder withProperties(Properties properties) {
            this.properties = properties;
            return this;
        }

        public Builder withProtocol(MailProtocol protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder withHost(String host) {
            this.host = host;
            return this;
        }

        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public MailClient build() throws MessagingException {
            return new MailClient(this);
        }

    }

}
