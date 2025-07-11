package com.example.basketballshoesandroidshop.Utils;

import java.text.DecimalFormat;

public class CurrencyFormat {
    public static String getFormattedPrice(Double price) {
        DecimalFormat decimalFormat = new DecimalFormat("0.##");
        String formattedPrice = decimalFormat.format(price);
        return "$" + formattedPrice;
    }
    
    public static String format(double price) {
        DecimalFormat decimalFormat = new DecimalFormat("0.##");
        String formattedPrice = decimalFormat.format(price);
        return "$" + formattedPrice;
    }
}
