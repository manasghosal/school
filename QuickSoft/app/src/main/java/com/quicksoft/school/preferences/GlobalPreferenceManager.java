package com.quicksoft.school.preferences;

import com.orhanobut.hawk.Hawk;

public class GlobalPreferenceManager {
    public static void setAllPermissionGranted(Boolean val) {
        Hawk.put("PERMISSION", val);
    }
    public static boolean isAllPermissionGranted() {
        if(Hawk.contains("PERMISSION")) {
            return Hawk.get("PERMISSION");
        }else
            return false;
    }

    public static void setUserLoggedIn(Boolean val) {
        Hawk.put("USER_LOGIN", val);
    }
    public static boolean isUserLoggedIn() {
        if(Hawk.contains("USER_LOGIN")) {
            return Hawk.get("USER_LOGIN");
        }else
            return false;
    }

    public static void setUserType(int val) {
        Hawk.put("USER_TYPE", val);
    }
    public static int getUserType() {
        if(Hawk.contains("USER_TYPE")) {
            return Hawk.get("USER_TYPE");
        }else
            return -1;
    }

    public static void setLoginType(int val) {
        Hawk.put("LOGIN_TYPE", val);
    }
    public static int getLoginType() {
        if(Hawk.contains("LOGIN_TYPE")) {
            return Hawk.get("LOGIN_TYPE");
        }else
            return -1;
    }

    public static void saveUniqueId(String val) {
        Hawk.put("USER_ID", val);
    }
    public static String getUniqueId() {
        if(Hawk.contains("USER_ID")) {
            return Hawk.get("USER_ID");
        }else
            return "";
    }

    public static void saveProfilePic(String val) {
        Hawk.put("USER_IMAGE", val);
    }
    public static String getProfilePic() {
        if(Hawk.contains("USER_IMAGE")) {
            return Hawk.get("USER_IMAGE");
        }else
            return "";
    }

    public static void saveUserFullName(String val) {
        Hawk.put("USER_FULL_NAME", val);
    }
    public static String getUserFullName() {
        if(Hawk.contains("USER_FULL_NAME")) {
            return Hawk.get("USER_FULL_NAME");
        }else
            return "";
    }

    public static void saveUserEmail(String val) {
        Hawk.put("USER_EMAIL", val);
    }
    public static String getUserEmail() {
        if(Hawk.contains("USER_EMAIL")) {
            return Hawk.get("USER_EMAIL");
        }else
            return "";
    }

    public static void saveUserPhone(String val) {
        Hawk.put("USER_PHONE", val);
    }
    public static String getUserPhone() {
        if(Hawk.contains("USER_PHONE")) {
            return Hawk.get("USER_PHONE");
        }else
            return "";
    }

    public static void savePersonID(String val) {
        Hawk.put("USER_PERSONID", val);
    }
    public static String getPersonID() {
        if(Hawk.contains("USER_PERSONID")) {
            return Hawk.get("USER_PERSONID");
        }else
            return "";
    }



    //  Facbook Related
    public static void setFacebookProfileInfo(String email, String name, String fbId, String profileURL) {
        Hawk.put("USER_EMAIL", email);
        Hawk.put("FB_NAME", name);
        Hawk.put("FB_ID", fbId);
        Hawk.put("FB_PROFILE_URL", profileURL);
    }
    public static String getFBEmail() {
        if(Hawk.contains("USER_EMAIL")) {
            return Hawk.get("USER_EMAIL");
        }else
            return "";
    }
    public static String getFBName() {
        if(Hawk.contains("FB_NAME")) {
            return Hawk.get("FB_NAME");
        }else
            return "";
    }
    public static String getFBId() {
        if(Hawk.contains("FB_ID")) {
            return Hawk.get("FB_ID");
        }else
            return "";
    }
    public static String getFBProfileURL() {
        if(Hawk.contains("FB_PROFILE_URL")) {
            return Hawk.get("FB_PROFILE_URL");
        }else
            return "";
    }


    //  Google Related
    public static void setGoogleProfileInfo(String email, String name, String googleId, String profileURL) {
        Hawk.put("USER_EMAIL", email);
        Hawk.put("G_NAME", name);
        Hawk.put("G_ID", googleId);
        Hawk.put("G_PROFILE_URL", profileURL);
    }
    public static String getGoogleEmail() {
        if(Hawk.contains("USER_EMAIL")) {
            return Hawk.get("USER_EMAIL");
        }else
            return "";
    }
    public static String getGoogelName() {
        if(Hawk.contains("G_NAME")) {
            return Hawk.get("G_NAME");
        }else
            return "";
    }
    public static String getGoogleId() {
        if(Hawk.contains("G_ID")) {
            return Hawk.get("G_ID");
        }else
            return "";
    }
    public static String getGoogleProfileURL() {
        if(Hawk.contains("G_PROFILE_URL")) {
            return Hawk.get("G_PROFILE_URL");
        }else
            return "";
    }



    //Parent RElated
    public static void saveParentDashBoardInfo(String val) {
        Hawk.put("PARENT_DASHBOARD", val);
    }
    public static String getParentDashBoardInfo() {
        if(Hawk.contains("PARENT_DASHBOARD")) {
            return Hawk.get("PARENT_DASHBOARD");
        }else
            return "";
    }

    public static void saveParentAttendance(String val) {
        Hawk.put("PARENT_ATTENDANCE", val);
    }
    public static String getParentAttendance() {
        if(Hawk.contains("PARENT_ATTENDANCE")) {
            return Hawk.get("PARENT_ATTENDANCE");
        }else
            return "";
    }

    public static void saveParentTasks(String val) {
        Hawk.put("PARENT_TASKS", val);
    }
    public static String getParentTasks() {
        if(Hawk.contains("PARENT_TASKS")) {
            return Hawk.get("PARENT_TASKS");
        }else
            return "";
    }

    public static void saveParentNotice(String val) {
        Hawk.put("PARENT_NOTICE", val);
    }
    public static String getParentNotice() {
        if(Hawk.contains("PARENT_NOTICE")) {
            return Hawk.get("PARENT_NOTICE");
        }else
            return "";
    }

    public static void saveParentFee(String val) {
        Hawk.put("PARENT_FEE", val);
    }
    public static String getParentFee() {
        if(Hawk.contains("PARENT_FEE")) {
            return Hawk.get("PARENT_FEE");
        }else
            return "";
    }

    public static void saveParentGovBody(String val) {
        Hawk.put("PARENT_GOVBODY", val);
    }
    public static String getParentGovBody() {
        if(Hawk.contains("PARENT_GOVBODY")) {
            return Hawk.get("PARENT_GOVBODY");
        }else
            return "";
    }

    public static void saveParentFaculty(String val) {
        Hawk.put("PARENT_FACULTY", val);
    }
    public static String getParentFaculty() {
        if(Hawk.contains("PARENT_FACULTY")) {
            return Hawk.get("PARENT_FACULTY");
        }else
            return "";
    }

    public static void saveParentMarksheet(String val) {
        Hawk.put("PARENT_MARKSHEET", val);
    }
    public static String getParentMarksheet() {
        if(Hawk.contains("PARENT_MARKSHEET")) {
            return Hawk.get("PARENT_MARKSHEET");
        }else
            return "";
    }

    public static void saveParentClasses(String val) {
        Hawk.put("PARENT_CLASSES", val);
    }
    public static String getParentClasses() {
        if(Hawk.contains("PARENT_CLASSES")) {
            return Hawk.get("PARENT_CLASSES");
        }else
            return "";
    }

    public static void saveParentRouteId(String val) {
        Hawk.put("PARENT_ROUTEID", val);
    }
    public static String getParentRouteID() {
        if(Hawk.contains("PARENT_ROUTEID")) {
            return Hawk.get("PARENT_ROUTEID");
        }else
            return "";
    }


    //Teacher Related
    public static void saveTeacherDashBoardInfo(String val) {
        Hawk.put("TEACHER_DASHBOARD", val);
    }
    public static String getTeacherDashBoardInfo() {
        if(Hawk.contains("TEACHER_DASHBOARD")) {
            return Hawk.get("TEACHER_DASHBOARD");
        }else
            return "";
    }

    public static void saveTeacherClasses(String val) {
        Hawk.put("TEACHER_CLASSES", val);
    }
    public static String getTeacherClasses() {
        if(Hawk.contains("TEACHER_CLASSES")) {
            return Hawk.get("TEACHER_CLASSES");
        }else
            return "";
    }

    public static void saveTeacherStudentList(String val) {
        Hawk.put("TEACHER_STUDENT_LIST", val);
    }
    public static String getTeacherStudentList() {
        if(Hawk.contains("TEACHER_STUDENT_LIST")) {
            return Hawk.get("TEACHER_STUDENT_LIST");
        }else
            return "";
    }

    public static void saveTeacherClass(String val) {
        Hawk.put("TEACHER_CLASS", val);
    }
    public static String getTeacherClass() {
        if(Hawk.contains("TEACHER_CLASS")) {
            return Hawk.get("TEACHER_CLASS");
        }else
            return "";
    }

    public static void saveTeacherSection(String val) {
        Hawk.put("TEACHER_SECTION", val);
    }
    public static String getTeacherSection() {
        if(Hawk.contains("TEACHER_SECTION")) {
            return Hawk.get("TEACHER_SECTION");
        }else
            return "";
    }

    public static void saveDriverDashBoardInfo(String vehicle, String val) {
        Hawk.put(vehicle, val);
    }

    public static String getDriverDashBoardInfo(String vehicle) {
        if(Hawk.contains(vehicle)) {
            return Hawk.get(vehicle);
        }else
            return "";
    }
    public static void saveDriverVehicle(String val) {
        Hawk.put("DRIVER_VEHICLE", val);
    }
    public static String getDriverVehicle() {
        if(Hawk.contains("DRIVER_VEHICLE")) {
            return Hawk.get("DRIVER_VEHICLE");
        }else
            return "";
    }
}

