package com.videoplayback.videoStream.Payload;


import java.security.SecureRandom;

public class RandomStringGenerator {

        private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        private static final SecureRandom RANDOM = new SecureRandom();

        public static String generateRandomString(int minLength, int maxLength) {
            if (minLength < 2 || maxLength > 12 || minLength > maxLength) {
                throw new IllegalArgumentException("Length must be between 2 and 12 characters.");
            }
            int length = RANDOM.nextInt(maxLength - minLength + 1) + minLength;
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
            }
            return sb.toString();
        }

    }

