package com.example.basketballshoesandroidshop.Utils;

import android.util.Patterns;

public class ValidationUtils {

    // Kiểm tra email hợp lệ
    public static boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty() &&
                Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    // Kiểm tra tên hợp lệ
    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() && name.trim().length() >= 2;
    }

    // Kiểm tra mật khẩu hợp lệ
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    // Kiểm tra số điện thoại hợp lệ (Vietnam format)
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return false;

        String phonePattern = "^(0|\\+84)(3[2-9]|5[2689]|7[06-9]|8[1-689]|9[0-46-9])[0-9]{7}$";
        return phone.matches(phonePattern);
    }

    // Kiểm tra địa chỉ hợp lệ
    public static boolean isValidAddress(String address) {
        return address != null && !address.trim().isEmpty() && address.trim().length() >= 10;
    }

    // Lấy thông báo lỗi cho email
    public static String getEmailError(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email không được để trống";
        }
        if (!isValidEmail(email)) {
            return "Email không đúng định dạng";
        }
        return null;
    }

    // Lấy thông báo lỗi cho tên
    public static String getNameError(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Tên không được để trống";
        }
        if (name.trim().length() < 2) {
            return "Tên phải có ít nhất 2 ký tự";
        }
        return null;
    }

    // Lấy thông báo lỗi cho mật khẩu
    public static String getPasswordError(String password) {
        if (password == null || password.isEmpty()) {
            return "Mật khẩu không được để trống";
        }
        if (password.length() < 6) {
            return "Mật khẩu phải có ít nhất 6 ký tự";
        }
        return null;
    }

    // Lấy thông báo lỗi cho số điện thoại
    public static String getPhoneError(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "Số điện thoại không được để trống";
        }
        if (!isValidPhone(phone)) {
            return "Số điện thoại không đúng định dạng";
        }
        return null;
    }

    // Lấy thông báo lỗi cho địa chỉ
    public static String getAddressError(String address) {
        if (address == null || address.trim().isEmpty()) {
            return "Địa chỉ không được để trống";
        }
        if (address.trim().length() < 10) {
            return "Địa chỉ phải có ít nhất 10 ký tự";
        }
        return null;
    }
}
