package demo.smpp;

import ie.omk.smpp.AlreadyBoundException;
import ie.omk.smpp.BadCommandIDException;
import ie.omk.smpp.message.SMPPProtocolException;
import ie.omk.smpp.message.SubmitSM;
import ie.omk.smpp.util.ASCIIEncoding;
import ie.omk.smpp.version.VersionException;

import java.io.IOException;
import java.util.Random;

public class LongSmsSenderByUDH extends AbstractSmsSender {
    private static final int MAX_SMS_LENGTH = 134;
    private static final int UDH_SIZE = 6;


    public static void main(String[] argv) throws Exception {
        AbstractSmsSender smsSender = new LongSmsSenderByUDH();
        smsSender.run();
    }

    protected String getMessage() {
        return "It's a long message start from aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                + "and bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
                + "and ccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc"
                + "and ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"
                + "and eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeend";
    }

    protected void sendMessage(String shortMessage)
            throws VersionException,
            AlreadyBoundException, SMPPProtocolException, UnsupportedOperationException, IOException, BadCommandIDException {

        byte[] mesageBytes = new ASCIIEncoding().encodeString(shortMessage);

        int totalSegments = mesageBytes.length / MAX_SMS_LENGTH + 1;
        System.out.println("the total segments is " + totalSegments);

        byte referenceNumber = (byte) new Random().nextInt();
        for (int i = 0; i < totalSegments; i++) {
            SubmitSM sm = createSubmitSM();
            sm.setEsmClass(0x40);

            int segementLen = (i == totalSegments - 1) ? (mesageBytes.length % MAX_SMS_LENGTH) : MAX_SMS_LENGTH;
            byte[] segementBytes = new byte[segementLen + UDH_SIZE];
            fillUserDataHeader(segementBytes, referenceNumber, (byte) totalSegments, i);
            System.arraycopy(mesageBytes, i * MAX_SMS_LENGTH, segementBytes, UDH_SIZE, segementLen);
            sm.setMessage(segementBytes);

            con.sendRequest(sm);
            System.out.println("Sent SMS message:" + new String(segementBytes, UDH_SIZE, segementBytes.length - UDH_SIZE, "US-ASCII"));
        }
    }

    private void fillUserDataHeader(byte[] byteBuffer, byte referenceNumber, byte totalSegments, int index) {
        byteBuffer[0] = 5;
        byteBuffer[1] = 0;
        byteBuffer[2] = 3;
        byteBuffer[3] = referenceNumber;
        byteBuffer[4] = totalSegments;
        byteBuffer[5] = (byte) (index + 1);
    }
}
