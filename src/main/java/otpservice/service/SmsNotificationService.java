package otpservice.service;

import org.jsmpp.bean.*;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Properties;


public class SmsNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(SmsNotificationService.class);

    private String host;
    private int port;
    private String systemId;
    private String password;
    private String systemType;
    private String sourceAddress;

    public SmsNotificationService(){
        Properties config = loadConfig();
        this.host = config.getProperty("smpp.host");
        this.port = Integer.parseInt(config.getProperty("smpp.port"));
        this.systemId = config.getProperty("smpp.system_id");
        this.password = config.getProperty("smpp.password");
        this.systemType = config.getProperty("smpp.system_type");
        this.sourceAddress=config.getProperty("smpp.source_addr");
    }

    private Properties loadConfig() {
        try {
            Properties props = new Properties();
            props.load(EmailNotificationService.class.getClassLoader()
                    .getResourceAsStream("sms.properties"));
            return props;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load email configuration", e);
        }
    }

    public void sendCode(String destination, String code) {
        SMPPSession session = new SMPPSession();

        try {
            BindParameter bindParameter = new BindParameter(
                    BindType.BIND_TX,
                    systemId,
                    password,
                    systemType,
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    sourceAddress
            );

            session.connectAndBind(host, port, bindParameter);

            session.submitShortMessage(
                    systemType,
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    sourceAddress,
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    destination,
                    new ESMClass(),
                    (byte) 0,
                    (byte) 1,
                    null,
                    null,
                    new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT),
                    (byte) 0,
                    new GeneralDataCoding(Alphabet.ALPHA_DEFAULT),
                    (byte) 0,
                    ("Your code: " + code).getBytes(StandardCharsets.UTF_8)
            );

            logger.info("SMS send to {}", destination);
        } catch (Exception e) {
            logger.warn("SMPP server error: {}", e.getMessage());
        } finally {
            session.unbindAndClose();
        }
    }
}
