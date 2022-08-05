package com.pekoeli.smsos;

public class PhoneContact {
    private String mName;
    private String mPhone;
    private boolean mEmergencyContact = false;

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

    public boolean isEmergencyContact() {
        return mEmergencyContact;
    }

    public void toggleIsEmergencyContact() {
        mEmergencyContact = !mEmergencyContact;
    }
}
