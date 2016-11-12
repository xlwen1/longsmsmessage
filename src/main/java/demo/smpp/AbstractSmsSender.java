package demo.smpp;

import ie.omk.smpp.Address;
import ie.omk.smpp.BadCommandIDException;
import ie.omk.smpp.Connection;
import ie.omk.smpp.message.SubmitSM;
import ie.omk.smpp.util.ASCIIEncoding;
import ie.omk.smpp.util.BinaryEncoding;
import ie.omk.smpp.util.MessageEncoding;
import ie.omk.smpp.util.UCS2Encoding;
import ie.omk.smpp.version.SMPPVersion;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

public abstract class AbstractSmsSender {
    protected Connection con;
    private SMPPVersion smppVersion = SMPPVersion.V34;
    private String srcAddress = "123";
    /**
     * TON (type of number) and NPI (numbering plan identification)
     */
    private int srcTON = 5;
    private int srcNPI = 1;
    private int dstTON = 0;
    private int dstNPI = 0;
    private int encoding = 1;
    private String dstPhone = "09187654";
    private String smscHost = "127.0.0.1";
    private int smscPort = 34567;
    private String smscUser = "user";
    private String smscPassword = "pass";
    /**
     * The system_type parameter is used to categorize the type of ESME that is binding to the SMSC.
     * Examples include “VMS” (voice mail system) and “OTA” (over-the-air activation system).
     * Specification of the system_type is optional - some SMSCs may not require ESMEs to provide this detail.
     * In this case, the ESME can set the system_type to NULL.
     */
    private String systemtype = "VMA";

    public void run() throws Exception {
        try {
            loadConf();
            connect();
            sendMessage(getMessage());
        } finally {
            disconnect();
        }

    }

    protected abstract String getMessage();

    protected abstract void sendMessage(String message) throws Exception;

    protected SubmitSM createSubmitSM() throws BadCommandIDException, UnsupportedEncodingException {
        SubmitSM sm = (SubmitSM) con.newInstance(4);
        sm.setSource(new Address(srcTON, srcNPI, srcAddress));
        sm.setDestination(new Address(dstTON, dstNPI, dstPhone));
        sm.setMessageEncoding(getMessageEncoding((byte) encoding));
        return sm;
    }

    protected MessageEncoding getMessageEncoding() throws UnsupportedEncodingException {
        return getMessageEncoding((byte) encoding);
    }

    private void loadConf() {
        InputStream resourceAsStream = this.getClass().getResourceAsStream("conf.properties");
        Properties p = new Properties();
        try {
            p.load(resourceAsStream);
            this.srcTON = getInt(p, "srcTON", "5");
            this.srcNPI = getInt(p, "srcNPI", "1");
            this.srcAddress = p.getProperty("srcAddress","123");
            this.dstTON = getInt(p, "dstTON", "0");
            this.dstNPI = getInt(p, "dstNPI", "0");
            this.encoding = getInt(p, "encoding", "1");
            this.smscHost = p.getProperty("smscIp", "127.0.0.1");
            this.smscPort = getInt(p, "smscPort", "34567");
            this.smscUser = p.getProperty("smscUser", "user");
            this.smscPassword = p.getProperty("smscUser", "pass");
            this.systemtype = p.getProperty("systemtype", "VMA");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getInt(Properties p, String key, String defaultValue) {
        return Integer.parseInt(p.getProperty(key, defaultValue));
    }

    private void disconnect() throws IOException {
        if (con != null) {
            con.unbind();
            con.closeLink();
        }
    }

    private void connect() throws Exception {
        con = new Connection(smscHost, smscPort);
        con.setVersion(smppVersion);
        con.bind(Connection.TRANSMITTER,smscUser, smscPassword, systemtype);
    }

    private MessageEncoding getMessageEncoding(byte encoding) throws UnsupportedEncodingException {
        switch (encoding) {
            case 0:
                return new ASCIIEncoding();
            case 1:
                return new BinaryEncoding();
            case 2:
                return new UCS2Encoding();
        }
        return new ASCIIEncoding();
    }
}
