package com.example.covid_19alertapp.extras;

public abstract class Constants {

    /*
    store all static constants used in app.
    for example: notification_ids, channel_ids, permission_codes etc.
     */

    public static final String NOTIFICATION_CHANNEL_ID = "all_notifications";
    public static final int PromptTrackerNotification_ID = 274;
    public static final int TrackingLocationNotification_ID = 905;
    public static final int DangerNotification_ID = 540;

    public static final String LOCATION_SETTINGS_SHARED_PREFERENCES="com.example.covid_19alertapp.location-settings-pref";
    public static final String location_tracker_state = "com.example.covid_19alertapp.tracker-state";

    public static final String background_WorkerTag = "com.example.covid_19alertapp.bg-worker-tag";

    public static final String WORKER_SHARED_PREFERENCES="com.example.covid_19alertapp.bg-worker-pref";
    public static final String bg_worker_state = "com.example.covid_19alertapp.worker-state";

    public static final int LOCATION_CHECK_CODE = 101;
    public static final int PERMISSION_CODE = 102;

    /*
    user information preferences
     */
    public static final String USER_INFO_SHARED_PREFERENCES = "Userinfo";

    public static final String user_exists_preference = "Userinfo";
    public static final String username_preference = "name";
    public static final String uid_preference = "uid";
    public static final String user_dob_preference = "dob";
    public static final String user_home_address_preference = "home";
    public static final String user_work_address_preference = "workAddress";
    public static final String user_phone_no_preference = "contactNumber";



}
