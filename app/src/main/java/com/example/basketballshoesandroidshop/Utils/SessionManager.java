package com.example.basketballshoesandroidshop.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.basketballshoesandroidshop.Domain.User;
import com.example.basketballshoesandroidshop.Utils.WishlistCache;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_PHONE = "userPhone";
    private static final String KEY_USER_ADDRESS = "userAddress";
    private static final String KEY_REMEMBER_ME = "rememberMe";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // Tạo session khi đăng nhập thành công
    public void createLoginSession(User user, boolean rememberMe) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, user.getUserId());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_PHONE, user.getPhone());
        editor.putString(KEY_USER_ADDRESS, user.getAddress());
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        editor.commit();
    }

    // Kiểm tra trạng thái đăng nhập
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Kiểm tra Remember Me
    public boolean isRememberMe() {
        return pref.getBoolean(KEY_REMEMBER_ME, false);
    }

    // Lấy thông tin user từ session
    public User getUserFromSession() {
        if (isLoggedIn()||isRememberMe()) {
            User user = new User();
            user.setUserId(pref.getString(KEY_USER_ID, ""));
            user.setName(pref.getString(KEY_USER_NAME, ""));
            user.setEmail(pref.getString(KEY_USER_EMAIL, ""));
            user.setPhone(pref.getString(KEY_USER_PHONE, ""));
            user.setAddress(pref.getString(KEY_USER_ADDRESS, ""));
            return user;
        }
        return null;
    }

    // Lấy User ID hiện tại
    public String getCurrentUserId() {
        return pref.getString(KEY_USER_ID, "");
    }

    // Logout - xóa session
    public void logout() {
        editor.clear();
        editor.commit();
        // Clear wishlist cache khi logout
        WishlistCache.getInstance().clear();
    }

    // Logout nhưng giữ Remember Me
    public void logoutKeepRemember() {
        String email = pref.getString(KEY_USER_EMAIL, "");
        boolean rememberMe = pref.getBoolean(KEY_REMEMBER_ME, false);

        editor.clear();
        if (rememberMe) {
            editor.putString(KEY_USER_EMAIL, email);
            editor.putBoolean(KEY_REMEMBER_ME, true);
        }
        editor.commit();
        // Clear wishlist cache khi logout
        WishlistCache.getInstance().clear();
    }
}
