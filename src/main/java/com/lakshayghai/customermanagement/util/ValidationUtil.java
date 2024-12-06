package com.lakshayghai.customermanagement.util;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.NumberParseException;
import org.apache.commons.validator.routines.EmailValidator;

public class ValidationUtil {

    private static final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    public static boolean isValidPhoneNumber(String phoneNumber, String countryCode) {
        try {
            return phoneNumberUtil.isValidNumber(phoneNumberUtil.parse(phoneNumber, countryCode));
        } catch (NumberParseException e) {
            return false;
        }
    }

    public static boolean isValidEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }
}

