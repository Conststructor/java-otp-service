package otpservice.util;

import java.util.Random;

public class OtpGenerator {
    private static final Random random = new Random();

    public static String generate(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}