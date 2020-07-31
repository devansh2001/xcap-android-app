package com.example.xcapproject;

public enum AndroidPermissions {
    ACCESS_BACKGROUND_LOCATION(1),
    ACCESS_COARSE_LOCATION(1),
    ACCESS_FINE_LOCATION(1),
    ACTIVITY_RECOGNITION(2),
    BIND_ACCESSIBILITY_SERVICE(3),
    BIND_NOTIFICATION_LISTENER_SERVICE(4),
    BIND_TEXT_SERVICE(4),
    BLUETOOTH(5),
    BLUETOOTH_ADMIN(5),
    BODY_SENSORS(6),
    CALL_PHONE(7),
    CAMERA(8),
    GET_ACCOUNTS(9),
    GET_ACCOUNTS_PRIVILEGED(9),
    INTERNET(10),
    NFC(11),
    NFC_TRANSACTION_EVENT(11),
    PACKAGE_USAGE_STATS(12),
    READ_CALENDAR(13),
    READ_CALL_LOG(13),
    READ_CONTACTS(13),
    READ_EXTERNAL_STORAGE(13),
    READ_PHONE_NUMBERS(13),
    READ_PHONE_STATE(13),
    READ_SMS(13),
    RECEIVE_MMS(13),
    RECEIVE_SMS(13),
    READ_VOICEMAIL(13),
    RECORD_AUDIO(14),
    SMS_FINANCIAL_TRANSACTIONS(15),
    SYSTEM_ALERT_WINDOW(16),
    USE_SIP(17),
    WAKE_LOCK(18),
    WRITE_CALENDAR(18),
    WRITE_CALL_LOG(18),
    WRITE_CONTACTS(18),
    WRITE_EXTERNAL_STORAGE(18),
    WRITE_SETTINGS(18),
    WRITE_VOICEMAIL(18);

    private int group;

    AndroidPermissions() {
        this(0);
    }

    AndroidPermissions(int group) {
        this.group = group;
    }

    public int getGroup() {
        return this.group;
    }

}
