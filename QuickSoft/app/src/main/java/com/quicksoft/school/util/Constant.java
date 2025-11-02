package com.quicksoft.school.util;

public class Constant {
    //public static final String SERVER_BASE_ADDRESS = "http://www.wonderzcreations.com/www.schoolapi.agsl.com/";
    public static final String SERVER_BASE_ADDRESS = "http://devatavantgardelab1.ddns.net:55835/";
    public static String FOLDER_PATH = "quicksoft/";


    //User Login Type
    public static int LOGIN_NORMAL = 0;
    public static int LOGIN_WITH_FACEBOOK = 1;
    public static int LOGIN_WITH_GMAIL = 2;

    //User Type
    public static int USER_PARENT = 0;
    public static int USER_TEACHER = 1;
    public static int USER_DRIVER = 2;


    //INTERVAL
    public static int PARENT_LOCATION_UPDATE_INTERVAL = 5 * 1000 ;


    //Parent Menu Items
    public static final int PARENT_DASHBOARD= 0;
    public static final int PARENT_ATTENDANCE = 1;
    public static final int PARENT_PAYMENTS = 2;
    public static final int PARENT_TRACK = 3;
    public static final int PARENT_TASK = 4;
    public static final int PARENT_NOTICE = 5;
    public static final int PARENT_MARKSHEET = 6;
    public static final int PARENT_GOVT_BODY = 7;
    public static final int PARENT_FACULTY = 8;
    public static final int PARENT_ABOUT = 9;
    //Teacher Menu Items
    public static final int TEACHER_DASHBOARD= 0;
    public static final int TEACHER_ATTENDANCE = 1;
    public static final int TEACHER_TASK = 2;
    public static final int TEACHER_NOTICE = 3;
    public static final int TEACHER_JOBS = 4;
    //Teacher Menu Items
    public static final int DRIVER_DASHBOARD= 0;

    //Network Communication
    public static int SUCCESS = 0;
    public static int FAIL = -1;
    public static int NETWORK_FAIL = -2;

    public static int SYNC_LOGIN = 1;
    public static int SYNC_VERIFYOTP = 2;
    public static int SYNC_RESEND_OTP = 3;
    public static int SYNC_PASSWORD = 4;
    public static int SYNC_UPLOAD_IMAGE = 5;

    public static int SYNC_PARENT_DASHBOARD = 6;
    public static int SYNC_PARENT_ATTENDANCE = 7;
    public static int SYNC_PARENT_TASK = 8;
    public static int SYNC_PARENT_NOTICE = 9;
    public static int SYNC_PARENT_FEE = 10;
    public static int SYNC_PARENT_GOVBODY = 11;
    public static int SYNC_PARENT_MARKSHEET = 12;
    public static int SYNC_PARENT_FACULTY = 13;
    public static int SYNC_PARENT_CLASSES = 14;
    public static int SYNC_PARENT_NOTICE_SUBMIT =15;
    public static int SYNC_PARENT_TASK_SUBMIT = 16;
    public static int SYNC_PARENT_TRACKBUS = 17;

    public static int SYNC_TEACHER_DASHBOARD = 18;
    public static int SYNC_TEACHER_STUDENT_ATTENDANCE = 19;
    public static int SYNC_TEACHER_TASK = 20;
    public static int SYNC_TEACHER_NOTICE = 21;
    public static int SYNC_TEACHER_STUDENT_LIST = 22;
    public static int SYNC_TEACHER_CLASSES = 23;
    public static int SYNC_TEACHER_STUDENT_ATTENDANCE_LIST = 24;

    public static int SYNC_DRIVER_DASHBOARD = 25;
    public static int SYNC_DRIVER_LOCATION = 26;
    public static int SYNC_DRIVER_VEHICLE = 27;


    // Various Request code
    public static int PERMISSION_REQUEST_CODE = 1001;
    public static int EX_FILE_PICKER_RESULT = 1002;
    public static int GOOGLE_SIGNIN_RESULT = 1003;
    public static int PARENT_NOTICE_DETAILS_REQUEST_CODE = 1004;
    public static int PARENT_TASK_DETAILS_REQUEST_CODE = 1005;
    public static int TEACHER_ATTENDANCE_SUMMERY_REQUEST_CODE = 1006;
    public static int TEACHER_CAMERA_PIC_REQUEST = 1007;
}
