package demo.smpp;

import ie.omk.smpp.AlreadyBoundException;
import ie.omk.smpp.BadCommandIDException;
import ie.omk.smpp.message.SMPPProtocolException;
import ie.omk.smpp.message.SubmitSM;
import ie.omk.smpp.message.tlv.TLVTable;
import ie.omk.smpp.message.tlv.Tag;
import ie.omk.smpp.util.ASCIIEncoding;
import ie.omk.smpp.version.VersionException;

import java.io.IOException;
import java.util.Random;

public class LongSmsSenderByOP extends AbstractSmsSender {
    private static final int MAX_SMS_LENGTH = 134;

    public static void main(String[] argv) throws Exception {
        AbstractSmsSender smsSender = new LongSmsSenderByOP();
        smsSender.run();
    }

    protected String getMessage() {
        return "It's a long message start from aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                + "and bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
                + "and ccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc"
                + "and ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"
                + "and eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeend";
    }

    protected void sendMessage(String message)
            throws VersionException,
            AlreadyBoundException, SMPPProtocolException, UnsupportedOperationException, IOException, BadCommandIDException {

        ASCIIEncoding asciiEncoding = new ASCIIEncoding();
        byte[] messageBytes = asciiEncoding.encodeString(message);

        int segementsNumber = messageBytes.length / MAX_SMS_LENGTH + 1;
        System.out.println("the total segments is " + segementsNumber);

        Random random = new Random(System.currentTimeMillis());
        byte referenceNumber = (byte) random.nextInt();

        for (int i = 0; i < segementsNumber; i++) {
            SubmitSM sm = createSubmitSM();
            sm.setEsmClass(0x40);

            int segementLen = (i == segementsNumber - 1) ? (messageBytes.length % MAX_SMS_LENGTH) : MAX_SMS_LENGTH;
            sm.setMessage(messageBytes, i * MAX_SMS_LENGTH, segementLen, asciiEncoding);
            TLVTable tlv = createSegmentOp(referenceNumber, segementsNumber, i + 1);
            sm.setTLVTable(tlv);

            con.sendRequest(sm);
            System.out.println("Sent SMS message:" + new String(messageBytes, i * MAX_SMS_LENGTH, segementLen, "US-ASCII"));
        }
    }

    private TLVTable createSegmentOp(int messageRefNum, int total, int index) {
        TLVTable tlv = new TLVTable();
        tlv.set(Tag.SAR_MSG_REF_NUM, messageRefNum);
        tlv.set(Tag.SAR_TOTAL_SEGMENTS, total);
        tlv.set(Tag.SAR_SEGMENT_SEQNUM, index);
        return tlv;
    }
}