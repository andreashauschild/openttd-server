package de.litexo.security;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SecurityUtils {

    private SecurityUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String toSHA256(String value) {
        return DigestUtils.sha256Hex(value);
    }

    public static boolean isEquals(String value, String sha256Value) {
        return toSHA256(value).equalsIgnoreCase(sha256Value);
    }

    public static String generatePassword() {

        String upperCaseLetters = RandomStringUtils.random(2, 65, 90, true, true);
        String lowerCaseLetters = RandomStringUtils.random(2, 97, 122, true, true);
        String numbers = RandomStringUtils.randomNumeric(2);
        String specialChar = RandomStringUtils.random(2, 33, 47, false, false);
        String totalChars = RandomStringUtils.randomAlphanumeric(2);
        String combinedChars = upperCaseLetters.concat(lowerCaseLetters).concat(numbers).concat(specialChar).concat(totalChars);
        List<Character> pwdChars = combinedChars.chars().mapToObj(c -> (char) c).toList();
        Collections.shuffle(pwdChars);
        String password = pwdChars.stream().collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
        return password;
    }
}
