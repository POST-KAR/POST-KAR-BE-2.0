package com.postkar.project3dmodel.util;

public class OTPUtil {
    public static String generateOTP() {
        return String.valueOf((int)(Math.random() * 9000) + 1000); // 4-digit
    }
}
