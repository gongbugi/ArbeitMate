package OpenSourceSW.ArbeitMate.infra.impl;

import OpenSourceSW.ArbeitMate.infra.InviteCodeGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class StringInviteCodeGenerator implements InviteCodeGenerator {
    private static final String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789";
    private static final int LENGTH = 8;
    private final SecureRandom random = new SecureRandom();

    @Override
    public String next() {
        char[] buf = new char[LENGTH];
        for(int i = 0; i < LENGTH; i++) {
            buf[i] = chars.charAt(random.nextInt(chars.length()));
        }
        return new String(buf);
    }
}
