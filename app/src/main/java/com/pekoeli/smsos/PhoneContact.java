package com.pekoeli.smsos;

public class PhoneContact {
    private String mName;
    private String mPhone;

    public PhoneContact(String name, String phone) {
        mName = name;
        mPhone = phone;
    }

    public String getName() {
        return mName;
    }

    public String getPhone() {
        return mPhone;
    }
}
