package com.example.basketballshoesandroidshop.Utils;

public class Option {
    String name;
    boolean checked;

    public Option() {
    }

    public Option(String name, boolean checked) {
        this.name = name;
        this.checked = checked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
