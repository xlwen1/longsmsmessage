package demo.smpp;

import ie.omk.smpp.AlreadyBoundException;
import ie.omk.smpp.BadCommandIDException;
import ie.omk.smpp.message.SMPPProtocolException;
import ie.omk.smpp.message.SubmitSM;
import ie.omk.smpp.version.VersionException;

import java.io.IOException;

public class SmsSender extends AbstractSmsSender {

    public static void main(String[] argv) throws Exception {
        AbstractSmsSender smsSender = new SmsSender();
        smsSender.run();
    }


    @Override
    protected void sendMessage(String shortMessage)
            throws VersionException,
            AlreadyBoundException, SMPPProtocolException, UnsupportedOperationException, IOException, BadCommandIDException {

        SubmitSM sm = createSubmitSM();
        sm.setMessageText(shortMessage);
        con.sendRequest(sm);
        System.out.println("Send SMS message: " + shortMessage);
    }

    @Override
    protected String getMessage() {
        return "A short SMS message";
    }
}

/**
 * For SMPP environments, these are the rules that NowSMS follows for setting the source_addr_ton and source_addr_npi values (e.g., TON and NPI for the sender address):
 * If the sender address is alphanumeric (contains both letters and numbers) or non-numeric, TON is set to 5 and NPI to 0.
 * If the sender address is a short code, TON is set to 3, and NPI is set to 0. (By default, a number is considered to be a short code if the length of the number is 5 digts or less. You can change this length setting by editing SMSGW.INI, and under the [SMSGW] header specifying MaxSMPPShortCodeLen=#, where # is the max length of a short code.)
 * If the sender starts with a ¡°+¡±, TON is set to 1, and NPI is set to 1.
 * If none of the above conditions is met, TON is set to 0 and NPI is set to 1 (unless the sender is blank, in which case both are set to 0).
 * For setting the destination TON and NPI (dest_addr_ton and dest_addr_npi), only the condition of whether or not the recipient phone number starts with the international prefix of ¡°+¡± is considered.
 * If the recipient starts with a ¡°+¡±, TON is set to 1, and NPI is set to 1.
 * Otherwise, TON is set to 0 and NPI is set to 1.
 * What do these values mean anyway, and why are they important?
 * Basically, they tell the SMSC how to interpret the address. The SMPP specification defines the following TON values:
 * Unknown = 0
 * International = 1
 * National = 2
 * Network Specific = 3
 * Subscriber Number = 4
 * Alphanumeric = 5
 * Abbreviated = 6
 * These definitions are derived from the ETSI GSM 03.40 specification, which defines the SMS protocol.
 * The only unusual observation that might be made regarding NowSMS defaults is that if a number is expressed without a ¡°+¡± sign, NowSMS defaults to using a TON value of 0, or unknown. While this may seem unusual, it actually makes sense because NowSMS is telling the SMSC that the SMSC should use its own internal rules for making a determination of the number type.
 * Possible NPI values are defines as follows:
 * Unknown = 0
 * ISDN/telephone numbering plan (E163/E164) = 1
 * Data numbering plan (X.121) = 3
 * Telex numbering plan (F.69) = 4
 * Land Mobile (E.212) =6
 * National numbering plan = 8
 * Private numbering plan = 9
 * ERMES numbering plan (ETSI DE/PS 3 01-3) = 10
 * Internet (IP) = 13
 * WAP Client Id (to be defined by WAP Forum) = 18
 * So, generally speaking, 1 is the correct value. If the sender address is alphanumeric or shortcode, NowSMS uses 0 (it might also be argued that a short code could be considered part of a private numbering plan, but convention seems to expect a value of 0).
 * ÆäËü²Î¿¼£ºhttp://blog.csdn.net/shenpeng_sgua/article/details/5429752
 */