package com.quicksoft.school.connection;

import android.content.Context;
import android.net.Uri;

import com.quicksoft.school.connection.callback.SyncCompleteCallback;
import com.quicksoft.school.connection.callback.TaskCompleteCallback;
import com.quicksoft.school.connection.task.DriverDashboardAsyncTask;
import com.quicksoft.school.connection.task.DriverVehicleAsyncTask;
import com.quicksoft.school.connection.task.DriverLocationAsyncTask;
import com.quicksoft.school.connection.task.LoginAsyncTask;
import com.quicksoft.school.connection.task.ParentAttendanceAsyncTask;
import com.quicksoft.school.connection.task.ParentClassesAsyncTask;
import com.quicksoft.school.connection.task.ParentDashboardAsyncTask;
import com.quicksoft.school.connection.task.ParentFacultyAsyncTask;
import com.quicksoft.school.connection.task.ParentFeeAsyncTask;
import com.quicksoft.school.connection.task.ParentGovBodyAsyncTask;
import com.quicksoft.school.connection.task.ParentMarksheetAsyncTask;
import com.quicksoft.school.connection.task.ParentNoticeAsyncTask;
import com.quicksoft.school.connection.task.ParentSubmitNoticeAsyncTask;
import com.quicksoft.school.connection.task.ParentSubmitTaskAsyncTask;
import com.quicksoft.school.connection.task.ParentTaskAsyncTask;
import com.quicksoft.school.connection.task.ParentTrackBusAsyncTask;
import com.quicksoft.school.connection.task.ResendOTPAsyncTask;
import com.quicksoft.school.connection.task.TeacherClassesAsyncTask;
import com.quicksoft.school.connection.task.TeacherDashboardAsyncTask;
import com.quicksoft.school.connection.task.TeacherPostStudentAttendanceAsyncTask;
import com.quicksoft.school.connection.task.TeacherPostTaskAsyncTask;
import com.quicksoft.school.connection.task.TeacherStudentAttendanceAsyncTask;
import com.quicksoft.school.connection.task.TeacherStudentListAsyncTask;
import com.quicksoft.school.connection.task.UpdatePasswordAsyncTask;
import com.quicksoft.school.connection.task.UploadImageAsyncTask;
import com.quicksoft.school.connection.task.VerifyOTPAsyncTask;
import com.quicksoft.school.util.Constant;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by arindam on 12/6/16.
 */
public class SyncManager implements TaskCompleteCallback {


    Context mContext;
    SyncCompleteCallback syncCompleteCallback;

    public SyncManager(Context context, SyncCompleteCallback syncCompleteCallback) {
        this.mContext = context;
        this.syncCompleteCallback = syncCompleteCallback;
    }


    public void userLogin(String email, String password,  String userType, String fbId, String googleId, String id) {
        LoginAsyncTask postLoginAsyncTask = new LoginAsyncTask(mContext, Constant.SYNC_LOGIN, email, password, userType, fbId, googleId, id);
        postLoginAsyncTask.mTaskCompleteCallback = this;
        postLoginAsyncTask.execute();
    }


    public void verifyOTP(String email, String uniqueId, String otp) {
        VerifyOTPAsyncTask verifyOTPAsyncTask = new VerifyOTPAsyncTask(mContext, Constant.SYNC_VERIFYOTP, email, uniqueId, otp);
        verifyOTPAsyncTask.mTaskCompleteCallback = this;
        verifyOTPAsyncTask.execute();
    }

    public void resendOTP(String email, String uniqueId) {
        ResendOTPAsyncTask resendOTPAsyncTask = new ResendOTPAsyncTask(mContext, Constant.SYNC_RESEND_OTP, email, uniqueId);
        resendOTPAsyncTask.mTaskCompleteCallback = this;
        resendOTPAsyncTask.execute();
    }

    public void changePassword(String email, String uniqueId, String oldPass, String newPass) {
        UpdatePasswordAsyncTask updatePasswordAsyncTask = new UpdatePasswordAsyncTask(mContext, Constant.SYNC_PASSWORD, email, uniqueId, oldPass, newPass);
        updatePasswordAsyncTask.mTaskCompleteCallback = this;
        updatePasswordAsyncTask.execute();
    }

    public void uploadImage(String personID, String fileExt, Uri uri) {
        UploadImageAsyncTask uploadImageAsyncTask = new UploadImageAsyncTask(mContext, Constant.SYNC_UPLOAD_IMAGE, personID, fileExt, uri);
        uploadImageAsyncTask.mTaskCompleteCallback = this;
        uploadImageAsyncTask.execute();
    }

    // Parent
    public void parentDashBoard(String email, String uniqueId) {
        ParentDashboardAsyncTask parentDashboardAsyncTask = new ParentDashboardAsyncTask(mContext, Constant.SYNC_PARENT_DASHBOARD, email, uniqueId);
        parentDashboardAsyncTask.mTaskCompleteCallback = this;
        parentDashboardAsyncTask.execute();
    }

    public void parentAttendance(String email, String uniqueId) {
        ParentAttendanceAsyncTask parentAttendanceAsyncTask = new ParentAttendanceAsyncTask(mContext, Constant.SYNC_PARENT_ATTENDANCE, email, uniqueId);
        parentAttendanceAsyncTask.mTaskCompleteCallback = this;
        parentAttendanceAsyncTask.execute();
    }

    public void parentTasks(String email, String uniqueId) {
        ParentTaskAsyncTask parentTaskAsyncTask = new ParentTaskAsyncTask(mContext, Constant.SYNC_PARENT_TASK, email,uniqueId);
        parentTaskAsyncTask.mTaskCompleteCallback = this;
        parentTaskAsyncTask.execute();
    }

    public void parentNotice(String email, String uniqueId) {
        ParentNoticeAsyncTask parentNoticeAsyncTask = new ParentNoticeAsyncTask(mContext, Constant.SYNC_PARENT_NOTICE, email,uniqueId);
        parentNoticeAsyncTask.mTaskCompleteCallback = this;
        parentNoticeAsyncTask.execute();
    }

    public void parentFee(String email, String uniqueId) {
        ParentFeeAsyncTask parentFeeAsyncTask = new ParentFeeAsyncTask(mContext, Constant.SYNC_PARENT_FEE, email, uniqueId);
        parentFeeAsyncTask.mTaskCompleteCallback = this;
        parentFeeAsyncTask.execute();
    }

    public void parentGovBody(String email, String uniqueId) {
        ParentGovBodyAsyncTask parentGovBodyAsyncTask = new ParentGovBodyAsyncTask(mContext, Constant.SYNC_PARENT_GOVBODY, email, uniqueId);
        parentGovBodyAsyncTask.mTaskCompleteCallback = this;
        parentGovBodyAsyncTask.execute();
    }

    public void parentFaculty(String email, String uniqueId) {
        ParentFacultyAsyncTask parentFacultyAsyncTask = new ParentFacultyAsyncTask(mContext, Constant.SYNC_PARENT_FACULTY, email, uniqueId);
        parentFacultyAsyncTask.mTaskCompleteCallback = this;
        parentFacultyAsyncTask.execute();
    }

    public void parentMarksheet(String email, String uniqueId) {
        ParentMarksheetAsyncTask parentMarksheetAsyncTask = new ParentMarksheetAsyncTask(mContext, Constant.SYNC_PARENT_MARKSHEET, email, uniqueId);
        parentMarksheetAsyncTask.mTaskCompleteCallback = this;
        parentMarksheetAsyncTask.execute();
    }

    public void parentGetClasses(String email, String uniqueId) {
        ParentClassesAsyncTask parentClassesAsyncTask = new ParentClassesAsyncTask(mContext, Constant.SYNC_PARENT_CLASSES, email, uniqueId);
        parentClassesAsyncTask.mTaskCompleteCallback = this;
        parentClassesAsyncTask.execute();
    }

    public void parentSubmitNotice(String email, String uniqueId, String noticeId) {
        ParentSubmitNoticeAsyncTask submitNoticeAsyncTask = new ParentSubmitNoticeAsyncTask(mContext, Constant.SYNC_PARENT_NOTICE_SUBMIT, email, uniqueId, noticeId);
        submitNoticeAsyncTask.mTaskCompleteCallback = this;
        submitNoticeAsyncTask.execute();
    }

    public void parentSubmitTask(String email, String uniqueId, String taskId) {
        ParentSubmitTaskAsyncTask submitTaskAsyncTask = new ParentSubmitTaskAsyncTask(mContext, Constant.SYNC_PARENT_TASK_SUBMIT, email, uniqueId, taskId);
        submitTaskAsyncTask.mTaskCompleteCallback = this;
        submitTaskAsyncTask.execute();
    }

    public void parentTrackBus(String email, String uniqueId, String routeId) {
        ParentTrackBusAsyncTask parentTrackBusAsyncTask = new ParentTrackBusAsyncTask(mContext, Constant.SYNC_PARENT_TRACKBUS, email, uniqueId, routeId);
        parentTrackBusAsyncTask.mTaskCompleteCallback = this;
        parentTrackBusAsyncTask.execute();
    }



    // Teacher
    public void teacherDashBoard(String email, String uniqueId) {
        TeacherDashboardAsyncTask teacherDashboardAsyncTask = new TeacherDashboardAsyncTask(mContext, Constant.SYNC_TEACHER_DASHBOARD, email, uniqueId);
        teacherDashboardAsyncTask.mTaskCompleteCallback = this;
        teacherDashboardAsyncTask.execute();
    }

    public void teacherGetClasses(String email, String uniqueId) {
        TeacherClassesAsyncTask teacherClassesAsyncTask = new TeacherClassesAsyncTask(mContext, Constant.SYNC_TEACHER_CLASSES, email, uniqueId);
        teacherClassesAsyncTask.mTaskCompleteCallback = this;
        teacherClassesAsyncTask.execute();
    }

    public void teacherGetStudent(String email, String uniqueId, String classs, String section) {
        TeacherStudentListAsyncTask teacherStudentListAsyncTask = new TeacherStudentListAsyncTask(mContext, Constant.SYNC_TEACHER_STUDENT_LIST, email, uniqueId, classs, section);
        teacherStudentListAsyncTask.mTaskCompleteCallback = this;
        teacherStudentListAsyncTask.execute();
    }

    public void teacherAttendanceStudentList(String email, String uniqueId, String classs, String section, String date) {
        TeacherStudentAttendanceAsyncTask teacherStudentAttendanceAsyncTask = new TeacherStudentAttendanceAsyncTask(mContext, Constant.SYNC_TEACHER_STUDENT_ATTENDANCE_LIST, email, uniqueId, classs, section, date);
        teacherStudentAttendanceAsyncTask.mTaskCompleteCallback = this;
        teacherStudentAttendanceAsyncTask.execute();
    }

    public void teacherPostTask(String email, String uniqueId, String classs, String section, String dueDate, String topic, String desc, boolean isAll, ArrayList<String> studentList) {
        TeacherPostTaskAsyncTask teacherPostTaskAsyncTask = new TeacherPostTaskAsyncTask(mContext, Constant.SYNC_TEACHER_TASK, email, uniqueId, classs, section, dueDate, topic, desc, isAll, studentList);
        teacherPostTaskAsyncTask.mTaskCompleteCallback = this;
        teacherPostTaskAsyncTask.execute();
    }

    public void teacherPostNotice(String email, String uniqueId, String classs, String section, String dueDate, String topic, String desc, boolean isAll, ArrayList<String> studentList) {
        TeacherPostTaskAsyncTask teacherPostTaskAsyncTask = new TeacherPostTaskAsyncTask(mContext, Constant.SYNC_TEACHER_NOTICE, email, uniqueId, classs, section, dueDate, topic, desc, isAll, studentList);
        teacherPostTaskAsyncTask.mTaskCompleteCallback = this;
        teacherPostTaskAsyncTask.execute();
    }

    public void teacherPostAttendance(String email, String uniqueId, JSONArray jsonArray) {
        TeacherPostStudentAttendanceAsyncTask teacherPostStudentAttendanceAsyncTask = new TeacherPostStudentAttendanceAsyncTask(mContext, Constant.SYNC_TEACHER_STUDENT_ATTENDANCE, email, uniqueId, jsonArray);
        teacherPostStudentAttendanceAsyncTask.mTaskCompleteCallback = this;
        teacherPostStudentAttendanceAsyncTask.execute();
    }



    // Driver
    public void driverDashboard(String email, String uniqueId, String vehicle) {
        DriverDashboardAsyncTask driverDashboardAsyncTask = new DriverDashboardAsyncTask(mContext, Constant.SYNC_DRIVER_DASHBOARD, email, uniqueId, vehicle);
        driverDashboardAsyncTask.mTaskCompleteCallback = this;
        driverDashboardAsyncTask.execute();
    }
    public void driverVehicles(String email, String uniqueId) {
        DriverVehicleAsyncTask driverVehicleAsyncTask = new DriverVehicleAsyncTask(mContext, Constant.SYNC_DRIVER_VEHICLE, email, uniqueId);
        driverVehicleAsyncTask.mTaskCompleteCallback = this;
        driverVehicleAsyncTask.execute();
    }
    public void sendDriverLocation(String email, String uniqueId, String latitude, String longitude) {
        DriverLocationAsyncTask driverLocationAsyncTask = new DriverLocationAsyncTask(mContext, Constant.SYNC_DRIVER_LOCATION, email, uniqueId, latitude, longitude);
        driverLocationAsyncTask.mTaskCompleteCallback = this;
        driverLocationAsyncTask.execute();
    }


    @Override
    public void onTaskCompleteCallback(int task, int response, Object result) {
        if (task == Constant.SYNC_LOGIN) {
            if (response == Constant.SUCCESS)
                syncCompleteCallback.onSyncComplete(task, response, result);
            else
                syncCompleteCallback.onSyncComplete(task, response, (int) result);
        }else if (task == Constant.SYNC_VERIFYOTP) {
            if (response == Constant.SUCCESS)
                syncCompleteCallback.onSyncComplete(task, response,  result);
            else
                syncCompleteCallback.onSyncComplete(task, response, (int) result);
        }else if (task == Constant.SYNC_RESEND_OTP) {
            if (response == Constant.SUCCESS)
                syncCompleteCallback.onSyncComplete(task, response,  result);
            else
                syncCompleteCallback.onSyncComplete(task, response, (int) result);
        }else if (task == Constant.SYNC_PASSWORD) {
            if (response == Constant.SUCCESS)
                syncCompleteCallback.onSyncComplete(task, response, result);
            else
                syncCompleteCallback.onSyncComplete(task, response, (int) result);
        }else if (task == Constant.SYNC_UPLOAD_IMAGE) {
            if (response == Constant.SUCCESS)
                syncCompleteCallback.onSyncComplete(task, response, result);
            else
                syncCompleteCallback.onSyncComplete(task, response, (int) result);
        }






        // Parent
        else if (task == Constant.SYNC_PARENT_DASHBOARD) {
            if (response == Constant.SUCCESS)
                syncCompleteCallback.onSyncComplete(task, response,  result);
            else
                syncCompleteCallback.onSyncComplete(task, response, (int) result);
        }else if (task == Constant.SYNC_PARENT_ATTENDANCE) {
            if (response == Constant.SUCCESS)
                syncCompleteCallback.onSyncComplete(task, response, result);
            else
                syncCompleteCallback.onSyncComplete(task, response, (int) result);
        }else if (task == Constant.SYNC_PARENT_TASK) {
            if (response == Constant.SUCCESS)
                syncCompleteCallback.onSyncComplete(task, response, result);
            else
                syncCompleteCallback.onSyncComplete(task, response, (int) result);
        }else if (task == Constant.SYNC_PARENT_NOTICE) {
            if (response == Constant.SUCCESS)
                syncCompleteCallback.onSyncComplete(task, response, result);
            else
                syncCompleteCallback.onSyncComplete(task, response, (int) result);
        }else if (task == Constant.SYNC_PARENT_FEE) {
            if (response == Constant.SUCCESS)
                syncCompleteCallback.onSyncComplete(task, response, result);
            else
                syncCompleteCallback.onSyncComplete(task, response, (int) result);
        }else if (task == Constant.SYNC_PARENT_GOVBODY) {
            if (response == Constant.SUCCESS)
                syncCompleteCallback.onSyncComplete(task, response, result);
            else
                syncCompleteCallback.onSyncComplete(task, response, (int) result);
        }else if (task == Constant.SYNC_PARENT_FACULTY) {
            if (response == Constant.SUCCESS)
                syncCompleteCallback.onSyncComplete(task, response, result);
            else
                syncCompleteCallback.onSyncComplete(task, response, (int) result);
        }else if (task == Constant.SYNC_PARENT_MARKSHEET) {
            if (response == Constant.SUCCESS)
                syncCompleteCallback.onSyncComplete(task, response, result);
            else
                syncCompleteCallback.onSyncComplete(task, response, (int) result);
        }else if (task == Constant.SYNC_PARENT_CLASSES) {
            if (response == Constant.SUCCESS)
                syncCompleteCallback.onSyncComplete(task, response, result);
            else
                syncCompleteCallback.onSyncComplete(task, response, (int) result);
        }else if (task == Constant.SYNC_PARENT_NOTICE_SUBMIT) {
            if (response == Constant.SUCCESS)
                syncCompleteCallback.onSyncComplete(task, response, result);
            else
                syncCompleteCallback.onSyncComplete(task, response, (int) result);
        }else if (task == Constant.SYNC_PARENT_TASK_SUBMIT) {
            if (response == Constant.SUCCESS)
                syncCompleteCallback.onSyncComplete(task, response, result);
            else
                syncCompleteCallback.onSyncComplete(task, response, (int) result);
        }else if (task == Constant.SYNC_PARENT_TRACKBUS) {
            if (response == Constant.SUCCESS)
                syncCompleteCallback.onSyncComplete(task, response, result);
            else
                syncCompleteCallback.onSyncComplete(task, response, (int) result);
        }

        // Teacher
        else if (task == Constant.SYNC_TEACHER_DASHBOARD) {
            if (response == Constant.SUCCESS)
                syncCompleteCallback.onSyncComplete(task, response, result);
            else
                syncCompleteCallback.onSyncComplete(task, response, (int) result);
        }else if (task == Constant.SYNC_TEACHER_CLASSES) {
            if (response == Constant.SUCCESS)
                syncCompleteCallback.onSyncComplete(task, response, result);
            else
                syncCompleteCallback.onSyncComplete(task, response, (int) result);
        } else if (task == Constant.SYNC_TEACHER_STUDENT_ATTENDANCE) {
            if (response == Constant.SUCCESS)
                syncCompleteCallback.onSyncComplete(task, response, result);
            else
                syncCompleteCallback.onSyncComplete(task, response, (int) result);
        } else if (task == Constant.SYNC_TEACHER_STUDENT_LIST) {
            if (response == Constant.SUCCESS)
                syncCompleteCallback.onSyncComplete(task, response, result);
            else
                syncCompleteCallback.onSyncComplete(task, response, (int) result);
        }else if (task == Constant.SYNC_TEACHER_TASK) {
            if (response == Constant.SUCCESS)
                syncCompleteCallback.onSyncComplete(task, response, result);
            else
                syncCompleteCallback.onSyncComplete(task, response, (int) result);
        }else if (task == Constant.SYNC_TEACHER_NOTICE) {
            if (response == Constant.SUCCESS)
                syncCompleteCallback.onSyncComplete(task, response, result);
            else
                syncCompleteCallback.onSyncComplete(task, response, (int) result);
        }else if (task == Constant.SYNC_TEACHER_STUDENT_ATTENDANCE_LIST) {
            if (response == Constant.SUCCESS)
                syncCompleteCallback.onSyncComplete(task, response, result);
            else
                syncCompleteCallback.onSyncComplete(task, response, (int) result);
        }
        //Driver
        else if (task == Constant.SYNC_DRIVER_DASHBOARD) {
            if (response == Constant.SUCCESS)
                syncCompleteCallback.onSyncComplete(task, response, result);
            else
                syncCompleteCallback.onSyncComplete(task, response, (int) result);
        }else if (task == Constant.SYNC_DRIVER_LOCATION) {
            if (response == Constant.SUCCESS)
                syncCompleteCallback.onSyncComplete(task, response, result);
            else
                syncCompleteCallback.onSyncComplete(task, response, (int) result);
        }
        else if (task == Constant.SYNC_DRIVER_VEHICLE) {
            if (response == Constant.SUCCESS)
                syncCompleteCallback.onSyncComplete(task, response, result);
            else
                syncCompleteCallback.onSyncComplete(task, response, (int) result);
        }
    }
}
