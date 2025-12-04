package com.postkar.project3dmodel.util;

public class OTPUtil {
    public static String generateOTP() {
        return String.valueOf((int) (Math.random() * 900000 + 100000)); // 6-digit
    }
}
