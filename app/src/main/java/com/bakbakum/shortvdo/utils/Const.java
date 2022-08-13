package com.bakbakum.shortvdo.utils;

public interface Const {
    String BASE_URL = "https://bak-bakum.com/sam/api/";
    String ITEM_BASE_URL = "";
    //String ITEM_BASE_URL = "https://bakbakumshortsapp.s3.ap-southeast-1.amazonaws.com/uploads/";

    String PROFILE_BASE_URL = "https://bak-bakum.com/sam/uploads/";

    String IS_LOGIN = "is_login";
    String PREF_NAME = "com.sk.romoj";
    String GOOGLE_LOGIN = "google";
    String FACEBOOK_LOGIN = "facebook";
    String USER = "user";
    String Fav = "fav";

    //Minimum Video you want to buffer while Playing
    public static final int MIN_BUFFER_DURATION = 3000;
    //Max Video you want to buffer during PlayBack
    public static final int MAX_BUFFER_DURATION = 5000;
    //Min Video you want to buffer before start Playing it
    public static final int MIN_PLAYBACK_START_BUFFER = 500;
    //Min video You want to buffer when user resumes video
    public static final int MIN_PLAYBACK_RESUME_BUFFER = 2000;
}
