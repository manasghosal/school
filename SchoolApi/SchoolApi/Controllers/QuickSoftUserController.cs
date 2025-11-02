using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using SchoolApi.Models;
using System.Data.SqlClient;
using SchoolApi.DBAccess;
using System.Threading.Tasks;
using System.Net.Http.Headers;
using System.IO;
using System.Drawing.Imaging;
using System.Reflection;
using System.Web;
using System.Text;

namespace SchoolApi.Controllers
{
    public class QuickSoftUserController : ApiController
    {
        SchoolSQLServer dbCon = new SchoolSQLServer();

        [System.Web.Http.HttpPost]
        [ActionName("loginuser")]
        public async Task<SignInResponseModel> LoginUser([FromBody] UserCredentialModel credentialRequest)
        {
            if(credentialRequest.UserType != null && credentialRequest.UserType.Length == 2 )
            {
                if (credentialRequest.UserType == "PA")
                {
                    if (credentialRequest.Id.Length == 16 && credentialRequest.Id.ToCharArray().Any(char.IsDigit))
                    {
                        int yearOfBirth = Int32.Parse(credentialRequest.Id.Substring(0, 4));
                        int classLevel = Int32.Parse(credentialRequest.Id.Substring(4, 2));
                        int schoolId = Int32.Parse(credentialRequest.Id.Substring(6, 4));
                        int studentIC = Int32.Parse(credentialRequest.Id.Substring(10, 6));

                        //select student using student id from TBL_U_Student table and verify class and year of birth.
                        SqlConnection con = await dbCon.ConnectDB();
                        string select = "select Student_id, DateOfBirth, Class from TBL_U_Student where Student_IC = @student_ic";
                        SqlCommand cmd = new SqlCommand(select, con);
                        cmd.Parameters.AddWithValue("@student_ic", studentIC);
                        SqlDataReader reader = await dbCon.ReadData(cmd);

                        DateTime dob = DateTime.Today;
                        int cl = -1;
                        Guid parentSchoolAgeUser_id_1 = Guid.Empty;
                        Guid parentSchoolAgeUser_id_2 = Guid.Empty;
                        Guid uniqKey = Guid.Empty;
                        Guid personid = Guid.Empty;
                        string username = null;
                        string password = null;
                        string linked_fb = null;
                        string linked_google = null;
                        Guid student_id = Guid.Empty;
                        if (reader.Read())
                        {
                            student_id = reader.GetGuid(0);
                            dob = reader.GetDateTime(1);
                            cl = Int32.Parse(reader[2].ToString());
                        }
                        dbCon.CloseReader(reader);

                        //use Parent_Student_id_fk = Student id to find linked parents's Parent_SchoolAgeUser_id_fk from TBL_U_Parent table.
                        select = "select Parent_SchoolAgeUser_id_fk from TBL_U_Parent where Parent_Student_id_fk = @student_id";
                        cmd = new SqlCommand(select, con);
                        cmd.Parameters.AddWithValue("@student_id", student_id);
                        reader = await dbCon.ReadData(cmd);
                        int n = 0;
                        while (reader.Read())
                        {
                            if (n == 0) parentSchoolAgeUser_id_1 = reader.GetGuid(0);
                            else if (n == 1) parentSchoolAgeUser_id_2 = reader.GetGuid(0);
                            n++;
                        }
                        dbCon.CloseReader(reader);

                        //Use SchoolAgeUser_id = Parent_SchoolAgeUser_id_fk from table TBL_U_SchoolAgeUser and verify the user and password.
                        select = "select User_unique_key,  Username, Password, Linked_FB, Linked_Google, SchoolAgeUser_Person_id_fk as Person_id from TBL_U_SchoolAgeUser where " +
                            " SchoolAgeUser_id in (@schoolAgeUser_id_1, @schoolAgeUser_id_2) and SchoolAgeUser_School_id_fk in " +
                            " (select top 1 school_id from TBL_U_School where School_Indx = @school_id) and SchoolAgeUser_UserType_id_fk " +
                            " in (select UserType_id from TBL_U_UserType where User_code = @usercode)";
                        cmd = new SqlCommand(select, con);
                        cmd.Parameters.AddWithValue("@schoolAgeUser_id_1", parentSchoolAgeUser_id_1);
                        cmd.Parameters.AddWithValue("@schoolAgeUser_id_2", parentSchoolAgeUser_id_2);
                        cmd.Parameters.AddWithValue("@school_id", schoolId);
                        cmd.Parameters.AddWithValue("@usercode", "PA");
                        reader = await dbCon.ReadData(cmd);
                        bool match = false;

                        if (dob.Year == yearOfBirth && cl == classLevel)
                        {
                            while (reader.Read())
                            {
                                if (reader["User_unique_key"] != DBNull.Value) uniqKey = Guid.Parse(reader["User_unique_key"].ToString());
                                if (reader["Person_id"] != DBNull.Value) personid = Guid.Parse(reader["Person_id"].ToString());
                                if (reader["Username"] != DBNull.Value) username = reader["Username"].ToString();
                                if (reader["Password"] != DBNull.Value) password = reader["Password"].ToString();
                                if (reader["Linked_FB"] != DBNull.Value) linked_fb = reader["Linked_FB"].ToString();
                                if (reader["Linked_Google"] != DBNull.Value) linked_google = reader["Linked_Google"].ToString();


                                if (linked_fb != null && credentialRequest.FacebookId != null && credentialRequest.FacebookId.Length > 0)
                                {
                                    if (linked_fb != credentialRequest.FacebookId) continue;
                                    else
                                    {
                                        match = true;
                                        break;
                                    }
                                }
                                else if (linked_google != null && credentialRequest.GoogleId != null && credentialRequest.GoogleId.Length > 0)
                                {
                                    if (linked_google != credentialRequest.GoogleId) continue;
                                    else
                                    {
                                        match = true;
                                        break;
                                    }
                                }
                                else if (username != null && password != null && credentialRequest.UserId != null && password != null &&
                                    (username != credentialRequest.UserId || password != credentialRequest.Password)) continue;
                                else
                                {
                                    match = true;
                                    break;
                                }
                            }
                        }
                        dbCon.CloseReader(reader);
                        
                        select = "select Last_name, Middle_name, First_name, Phone from TBL_U_Person where Person_id = @person_id";
                        cmd = new SqlCommand(select, con);
                        cmd.Parameters.AddWithValue("@person_id", personid);
                        reader = await dbCon.ReadData(cmd);
                        string fname = "";
                        string mname = "";
                        string lname = "";
                        string phone = "";
                        if (reader.Read())
                        {
                            lname = reader.GetString(0);
                            mname = reader.GetString(1);
                            fname = reader.GetString(2);
                            phone = reader.GetString(3);
                        }
                        dbCon.CloseReader(reader);
                        if (match)
                        {
                            await SendOTP(uniqKey, phone, con);
                        }
                        dbCon.CloseDB(con);

                        if (match)
                        {
                            
                            return new SignInResponseModel()
                            {
                                UniqKey = uniqKey,
                                RespCode = ResponseCode.OK,
                                PersonId = personid,
                                Fname = fname,
                                Mname = mname,
                                Lname = lname,
                                Phone = phone
                            };
                        }
                        else return new SignInResponseModel()
                        {
                            UniqKey = Guid.Empty,
                            RespCode = ResponseCode.UNAUTHORIZED,
                            PersonId = Guid.Empty,
                            Fname = "",
                            Mname = "",
                            Lname = "",
                            Phone = ""
                        };
                    }
                    else return new SignInResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.UNAUTHORIZED,
                        PersonId = Guid.Empty,
                        Fname = "",
                        Mname = "",
                        Lname = "",
                        Phone = ""
                    };
                }
                else if (credentialRequest.UserType == "TE")
                {
                    if (credentialRequest.Id.Length == 4 && credentialRequest.Id.ToCharArray().Any(char.IsDigit))
                    {
                        int schoolId = Int32.Parse(credentialRequest.Id.Substring(0, 4));

                        Guid uniqKey = Guid.Empty;
                        Guid personid = Guid.Empty;
                        //Use the SchoolAgeUser_School_id_fk = school id, username and password to verify the teacher authentication.
                        string select = "select User_unique_key, Username, Password, Linked_FB, Linked_Google, SchoolAgeUser_Person_id_fk as Person_id from TBL_U_SchoolAgeUser where " +
                            " SchoolAgeUser_School_id_fk in (select top 1 school_id from TBL_U_School where School_Indx = @school_id) " +
                            " and ((Username = @username and Password = @password) or (Linked_FB != null and Linked_FB = @linked_fb and LEN(Linked_FB) > 0) or " +
                            " (Linked_Google != null and Linked_Google = @linked_google and LEN(Linked_Google) > 0)) " +
                            " and SchoolAgeUser_UserType_id_fk in (select top 1 UserType_id from TBL_U_UserType where User_code = @usercode)";
                        SqlConnection con = await dbCon.ConnectDB();
                        SqlCommand cmd = new SqlCommand(select, con);
                        cmd.Parameters.AddWithValue("@school_id", schoolId);
                        cmd.Parameters.AddWithValue("@username", credentialRequest.UserId);
                        cmd.Parameters.AddWithValue("@password", credentialRequest.Password);
                        cmd.Parameters.AddWithValue("@linked_fb", (credentialRequest.FacebookId==null? "": credentialRequest.FacebookId));
                        cmd.Parameters.AddWithValue("@linked_google", (credentialRequest.GoogleId == null ? "" : credentialRequest.GoogleId));
                        cmd.Parameters.AddWithValue("@usercode", "TE");
                        SqlDataReader reader = await dbCon.ReadData(cmd);
                        bool match = false;
                        if (reader.Read())
                        {
                            if(reader["User_unique_key"] != DBNull.Value) uniqKey = Guid.Parse(reader["User_unique_key"].ToString());
                            if (reader["Person_id"] != DBNull.Value)
                                personid = Guid.Parse(reader["Person_id"].ToString());
                            match = true;
                        }
                        dbCon.CloseReader(reader);

                        select = "select Last_name, Middle_name, First_name, Phone from TBL_U_Person where Person_id = @person_id";
                        cmd = new SqlCommand(select, con);
                        cmd.Parameters.AddWithValue("@person_id", personid);
                        reader = await dbCon.ReadData(cmd);
                        string fname = "";
                        string mname = "";
                        string lname = "";
                        string phone = "";
                        if (reader.Read())
                        {
                            lname = reader.GetString(0);
                            mname = reader.GetString(1);
                            fname = reader.GetString(2);
                            phone = reader.GetString(3);
                        }
                        dbCon.CloseReader(reader);
                        if (match)
                        {
                            await SendOTP(uniqKey, phone, con);
                        }
                        dbCon.CloseDB(con);

                        if (match)
                        {
                            return new SignInResponseModel()
                            {
                                UniqKey = uniqKey,
                                RespCode = ResponseCode.OK,
                                PersonId = personid,
                                Fname = fname,
                                Mname = mname,
                                Lname = lname,
                                Phone = phone
                            };
                        }
                        else return new SignInResponseModel()
                        {
                            UniqKey = Guid.Empty,
                            RespCode = ResponseCode.UNAUTHORIZED,
                            PersonId = Guid.Empty,
                            Fname = "",
                            Mname = "",
                            Lname = "",
                            Phone = ""
                        };
                    }
                    else return new SignInResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.UNAUTHORIZED,
                        PersonId = Guid.Empty,
                        Fname = "",
                        Mname = "",
                        Lname = "",
                        Phone = ""
                    };
                }
                else if (credentialRequest.UserType == "DR")
                {

                    if (credentialRequest.Id.Length == 4 && credentialRequest.Id.ToCharArray().Any(char.IsDigit))
                    {
                        int routeId = Int32.Parse(credentialRequest.Id.Substring(0, 4));
                        IList<Guid> driverIdLst = new List<Guid>();
                        string select = "select Driver_SchoolAgeUser_id_fk from TBL_U_Driver where Driver_Route_id_fk in (select top 1 route_id from TBL_U_Route where Route_Indx = @route_Id);";
                        SqlConnection con = await dbCon.ConnectDB();
                        SqlCommand cmd = new SqlCommand(select, con);
                        cmd.Parameters.AddWithValue("@route_Id", routeId);
                        SqlDataReader reader = await dbCon.ReadData(cmd);
                        if (reader.Read())
                        {
                            driverIdLst.Add(reader.GetGuid(0));
                        }
                        dbCon.CloseReader(reader);
                        bool match = false;

                        select = "select User_unique_key, Username, Password, Linked_FB, Linked_Google, SchoolAgeUser_Person_id_fk as Person_id from TBL_U_SchoolAgeUser where " +
                            " SchoolAgeUser_id = @schoolageuser_id and ((Username = @username and Password = @password) or (Linked_FB != null and Linked_FB = @linked_fb  and LEN(Linked_FB) > 0) or (Linked_Google != null and Linked_Google = @linked_google and LEN(Linked_Google) > 0)) " +
                            " and SchoolAgeUser_UserType_id_fk in (select UserType_id from TBL_U_UserType where User_code = @usercode)";
                        cmd = new SqlCommand(select, con);

                        Guid uniqKey = Guid.Empty;
                        Guid personid = Guid.Empty;
                        foreach (Guid driver_SchoolAgeUser_id_fk in driverIdLst)
                        {
                            cmd.Parameters.AddWithValue("@schoolageuser_id", driver_SchoolAgeUser_id_fk);
                            cmd.Parameters.AddWithValue("@username", credentialRequest.UserId);
                            cmd.Parameters.AddWithValue("@password", credentialRequest.Password);
                            cmd.Parameters.AddWithValue("@linked_fb", (credentialRequest.FacebookId == null ? "" : credentialRequest.FacebookId));
                            cmd.Parameters.AddWithValue("@linked_google", (credentialRequest.GoogleId == null ? "" : credentialRequest.GoogleId));
                            cmd.Parameters.AddWithValue("@usercode", "DR");
                            reader = await dbCon.ReadData(cmd);
                            if (reader.Read())
                            {
                                if (reader["User_unique_key"] != DBNull.Value)
                                    uniqKey = Guid.Parse(reader["User_unique_key"].ToString());
                                if (reader["Person_id"] != DBNull.Value)
                                    personid = Guid.Parse(reader["Person_id"].ToString());
                                match = true;
                                dbCon.CloseReader(reader);
                                break;
                            }
                            dbCon.CloseReader(reader);
                        }
                        select = "select Last_name, Middle_name, First_name, Phone from TBL_U_Person where Person_id = @person_id";
                        cmd = new SqlCommand(select, con);
                        cmd.Parameters.AddWithValue("@person_id", personid);
                        reader = await dbCon.ReadData(cmd);
                        string fname = "";
                        string mname = "";
                        string lname = "";
                        string phone = "";
                        if (reader.Read())
                        {
                            lname = reader.GetString(0);
                            mname = reader.GetString(1);
                            fname = reader.GetString(2);
                            phone = reader.GetString(3);
                        }
                        dbCon.CloseReader(reader);
                        if (match)
                        {
                            await SendOTP(uniqKey, phone, con);
                        }
                        dbCon.CloseDB(con);
                        if (match)
                        {
                            return new SignInResponseModel()
                            {
                                UniqKey = uniqKey,
                                RespCode = ResponseCode.OK,
                                PersonId = personid,
                                Fname = fname,
                                Mname = mname,
                                Lname = lname,
                                Phone = phone
                            };
                        }
                        else return new SignInResponseModel()
                        {
                            UniqKey = Guid.Empty,
                            RespCode = ResponseCode.UNAUTHORIZED,
                            PersonId = Guid.Empty,
                            Fname = "",
                            Mname = "",
                            Lname = "",
                            Phone = ""

                        };
                    }
                    else return new SignInResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.UNAUTHORIZED, PersonId = Guid.Empty,
                        Fname = "",
                        Mname = "",
                        Lname = "",
                        Phone = ""
                    };
                }
                else return new SignInResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.UNAUTHORIZED, PersonId = Guid.Empty };
            }
            else
            {
                return new SignInResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.UNAUTHORIZED, PersonId = Guid.Empty,
                    Fname = "",
                    Mname = "",
                    Lname = "",
                    Phone = ""
                };
            }
        }

        [System.Web.Http.HttpPost]
        [ActionName("resendotp")]
        public async Task<OTPVerificationResponseModel> ResendOTP([FromBody] OTPResendRequestModel otpResendRequest)
        {
            OTPVerificationResponseModel ovrm = new OTPVerificationResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.FORBIDDEN };
            string select = "select SchoolAgeUser_Person_id_fk as Person_id, TBL_U_Person.Phone as Phone "+
                            " from (TBL_U_SchoolAgeUser inner join TBL_U_Person on TBL_U_SchoolAgeUser.SchoolAgeUser_Person_id_fk = TBL_U_Person.Person_id) " +
                            " where User_unique_key = @uniqKey and Username = @userId";
            SqlConnection con = await dbCon.ConnectDB();
            SqlCommand cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@uniqKey", otpResendRequest.UniqKey);
            cmd.Parameters.AddWithValue("@userId", otpResendRequest.UserId);
            SqlDataReader reader = await dbCon.ReadData(cmd);
            string phone = "";
            if (reader.Read())
            {
                phone = reader.GetString(1);
            }
            dbCon.CloseReader(reader);

            await SendOTP(otpResendRequest.UniqKey, phone, con);
            dbCon.CloseDB(con);
            ovrm.UniqKey = otpResendRequest.UniqKey;
            ovrm.RespCode = ResponseCode.OK;
            return ovrm;
        }


        [System.Web.Http.HttpPost]
        [ActionName("verifyotp")]
        public async Task<OTPVerificationResponseModel> VerifyOTP([FromBody] OTPVerificationRequestModel otpVerificationRequest)
        {
            OTPVerificationResponseModel ovrm = new OTPVerificationResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.FORBIDDEN };
            SqlConnection con = await dbCon.ConnectDB();
            string select = "select User_login_otp, User_time_otp from TBL_U_SchoolAgeUser where User_unique_key = @uniqKey and Username = @userId";
            SqlCommand cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@uniqKey", otpVerificationRequest.UniqKey);
            cmd.Parameters.AddWithValue("@userId", otpVerificationRequest.UserId);
            SqlDataReader reader = await dbCon.ReadData(cmd);
            string otp = "";
            DateTime now = DateTime.Now;
            DateTime otptime = DateTime.MinValue;
            double otptimeoutsecs = 86400.0;
            if (reader.Read())
            {
                otp = reader.IsDBNull(0) ? "" : reader.GetString(0);
                otptime = reader.IsDBNull(1) ? otptime : reader.GetDateTime(1);
            }
            dbCon.CloseReader(reader);
            TimeSpan duration = now - otptime;
            if (otp.Equals(otpVerificationRequest.OTP) && duration.TotalSeconds <= otptimeoutsecs)
            {
                ovrm.RespCode = ResponseCode.OK;
            }
            else
            {
                if (duration.TotalSeconds > otptimeoutsecs)
                    ovrm.RespCode = ResponseCode.GATEWAY_TIMEOUT;
                else
                    ovrm.RespCode = ResponseCode.UNAUTHORIZED;
                return ovrm;
            }
            Guid uniqKey = Guid.NewGuid();

            string update = "Update TBL_U_SchoolAgeUser set User_unique_key=@ukey where User_unique_key = @curukey and Username=@user_id";
            cmd = new SqlCommand(update, con);
            cmd.Parameters.AddWithValue("@ukey", uniqKey);
            cmd.Parameters.AddWithValue("@curukey", otpVerificationRequest.UniqKey);
            cmd.Parameters.AddWithValue("@user_id", otpVerificationRequest.UserId);
            await cmd.ExecuteNonQueryAsync();
            dbCon.CloseDB(con);
            ovrm.UniqKey = uniqKey;
            return ovrm;
        }
        private async Task SendOTP(Guid uniqKey, string phone, SqlConnection con)
        {
            int _min = 1000;
            int _max = 9999;
            Random _rdm = new Random();
            int otp = _rdm.Next(_min, _max);

            string update = "Update TBL_U_SchoolAgeUser set User_login_otp=@otp, User_time_otp=@otptime where User_unique_key = @curukey";
            SqlCommand cmd = new SqlCommand(update, con);
            cmd.Parameters.AddWithValue("@otp", otp);
            cmd.Parameters.AddWithValue("otptime", DateTime.Now);
            cmd.Parameters.AddWithValue("@curukey", uniqKey);
            await cmd.ExecuteNonQueryAsync();

            //Prepare you post parameters
            StringBuilder sbPostData = new StringBuilder();
            sbPostData.AppendFormat("userId={0}", "rinamsc");
            sbPostData.AppendFormat("&password={0}", "Rinam_nes@17");
            sbPostData.AppendFormat("&senderId={0}", "RSMPLS");
            sbPostData.AppendFormat("&sendMethod={0}", "simpleMsg");
            sbPostData.AppendFormat("&msgType={0}", "TEXT");
            sbPostData.AppendFormat("&msg={0}{1}", "QuickSoft OTP=", otp);
            sbPostData.AppendFormat("&mobile={0}", phone);
            sbPostData.AppendFormat("&duplicateCheck={0}", "true");
            sbPostData.AppendFormat("&format={0}", "json");
            /*
             userId=rinamsc&password=Rinam_nes@17&senderId=RSMPLS&sendMethod=simpleMsg&msgType=TEXT&
             msg=Do you receive the msg&mobile=919830542365&duplicateCheck=true&format=json
             */
            //try
            //{
            //    //Call Send SMS API
            //    string sendSMSUri = "http://enterprise.smsgatewaycenter.com/SMSApi/rest/send?";
            //    //Create HTTPWebrequest
            //    HttpWebRequest httpWReq = (HttpWebRequest)WebRequest.Create(sendSMSUri + sbPostData.ToString());
            //    //Prepare and Add URL Encoded data
            //    UTF8Encoding encoding = new UTF8Encoding();
            //    byte[] data = encoding.GetBytes(sbPostData.ToString());
            //    ////Specify post method
            //    //httpWReq.Method = "GET";
            //    //httpWReq.ContentType = "application/json; charset=utf-8";
            //    //httpWReq.ContentLength = data.Length;
            //    //using (Stream stream = httpWReq.GetRequestStream())
            //    //{
            //    //    stream.Write(data, 0, data.Length);
            //    //}
            //    //Get the response
            //    HttpWebResponse response = (HttpWebResponse)httpWReq.GetResponse();
            //    StreamReader reader = new StreamReader(response.GetResponseStream());
            //    string responseString = reader.ReadToEnd();

            //    //Close the response
            //    reader.Close();
            //    response.Close();
            //}
            //catch (SystemException ex)
            //{

            //}


        }

        /*public IEnumerable<QuickSoftUser> GetAllUsers()
        {
            IList<QuickSoftUser> users = new List<QuickSoftUser>();
            try
            {
                SqlConnection con = new SqlConnection("Data Source = 43.255.152.26; Initial Catalog = MainSchool; Persist Security Info = True; User ID = User_MainSchool; Password = MainSchool123*0");
                con.Open();
                string select = "select First_Name, Middle_Name, Last_Name, Person_id from TBL_U_Person;";
                SqlCommand cmd = new SqlCommand(select, con);
                SqlDataReader myReader = cmd.ExecuteReader();

                while (myReader.Read())
                {
                    string fn = myReader.GetValue(0).ToString();
                    string mn = myReader.GetValue(1).ToString();
                    string ln = myReader.GetValue(2).ToString();
                    Guid g = Guid.Parse(myReader.GetValue(3).ToString());
                    users.Add( new QuickSoftUser { Id = g, FirstName = fn, MiddleName = mn, LastName = ln });
                }
                myReader.Close();
                con.Close();

            }
            catch (Exception ex)
            {  }
            return users;
        }
        public QuickSoftUser GetUser(Guid id)
        {
            QuickSoftUser user = null;
            try
            {
                SqlConnection con = new SqlConnection("Data Source = 43.255.152.26; Initial Catalog = MainSchool; Persist Security Info = True; User ID = User_MainSchool; Password = MainSchool123*0");
                con.Open();
                string select = "select First_Name, Middle_Name, Last_Name, Person_id from TBL_U_Person where Person_id=@Person_id;";

                SqlCommand cmd = new SqlCommand(select, con);
                cmd.Parameters.AddWithValue("@Person_id", id);
                using (SqlDataReader reader = cmd.ExecuteReader())
                {
                    if (reader.Read())
                    {
                        //if (reader["roll_no"] != DBNull.Value)
                        {
                            string fn = reader["First_Name"].ToString();
                            string mn = reader["Middle_Name"].ToString();
                            string ln = reader["Last_Name"].ToString();
                            Guid g = Guid.Parse(reader["Person_id"].ToString());
                            user = new QuickSoftUser { Id = g, FirstName = fn, MiddleName = mn, LastName = ln };
                        }
                    }
                    reader.Close();
                }
                con.Close();
            }
            catch (Exception ex)     { }
            return user;
        }
        */
        [System.Web.Http.HttpPost]
        [ActionName("dboardparent")]
        public async Task<ParentDashboardResponseModel> DboardParent([FromBody] ParentDashboardRequestModel parentDashboardRequest)
        {
            ParentDashboardResponseModel pdrm = new ParentDashboardResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.FORBIDDEN };
            SqlConnection con = await dbCon.ConnectDB();
            string select = "select SchoolAgeUser_id, SchoolAgeUser_UserType_id_fk, SchoolAgeUser_Person_id_fk from TBL_U_SchoolAgeUser where User_unique_key = @uniqKey and Username = @userId";
            SqlCommand cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@uniqKey", parentDashboardRequest.UniqKey);
            cmd.Parameters.AddWithValue("@userId", parentDashboardRequest.UserId);
            SqlDataReader reader = await dbCon.ReadData(cmd);
            Guid schoolAgeUser_id = Guid.Empty;
            Guid schoolAgeUser_UserType_id_fk = Guid.Empty;
            Guid personId = Guid.Empty;
            if (reader.Read())
            {
                schoolAgeUser_id = reader.GetGuid(0);//parent|student
                schoolAgeUser_UserType_id_fk = reader.GetGuid(1);//parent|student
                personId = reader.GetGuid(2);//parent|student
            }
            dbCon.CloseReader(reader);

            select = "select User_code from TBL_U_UserType where UserType_id = @userType_id;";
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@userType_id", schoolAgeUser_UserType_id_fk);

            string usertype = "";
            reader = await dbCon.ReadData(cmd);
            if (reader.Read())
            {
                usertype = reader[0].ToString();
            }
            dbCon.CloseReader(reader);
            Guid student_id = Guid.Empty;
            if (usertype == "PA")
            {
                select = "select Parent_Student_id_fk from TBL_U_Parent where Parent_SchoolAgeUser_id_fk = @parent_SchoolAgeUser_id_fk;";
                cmd = new SqlCommand(select, con);
                cmd.Parameters.AddWithValue("@parent_SchoolAgeUser_id_fk", schoolAgeUser_id);
                reader = await dbCon.ReadData(cmd);
                if (reader.Read())
                {
                    student_id = reader.GetGuid(0);//student
                }
                dbCon.CloseReader(reader);
            }
            else if (usertype != "ST")
            {

                select = "select Student_id from TBL_U_Student where Student_SchoolAgeUser_id_fk = @schoolAgeUser_id_fk;";
                cmd = new SqlCommand(select, con);
                cmd.Parameters.AddWithValue("@schoolAgeUser_id_fk", schoolAgeUser_id);
                
                reader = await dbCon.ReadData(cmd);
                if (reader.Read())
                {
                    student_id = reader.GetGuid(0);//student
                }
                dbCon.CloseReader(reader);
            }
            else return pdrm;

            if (student_id == Guid.Empty) return pdrm;

            select = "select Class, Section, Roll, TBL_U_Person.Person_id as PersonId,  " +
            " TBL_U_Person.Last_name, TBL_U_Person.Middle_name, TBL_U_Person.First_name, TBL_U_RouteMap.Route_id_fk as RouteId " +
            " from(TBL_U_Student " +
            " inner join TBL_U_SchoolAgeUser on TBL_U_Student.Student_SchoolAgeUser_id_fk = TBL_U_SchoolAgeUser.SchoolAgeUser_id " +
            " inner join TBL_U_Person on TBL_U_SchoolAgeUser.SchoolAgeUser_Person_id_fk = TBL_U_Person.Person_id " +
            " inner join TBL_U_RouteMap on TBL_U_Student.Student_RouteMap_id_fk = TBL_U_RouteMap.RouteMap_id) " + 
            " where TBL_U_Student.Student_id = @student_id";

            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@student_id", student_id);
            reader = await dbCon.ReadData(cmd);
            string classs = "";
            string section = "";
            int roll = -1;
            Guid child_personid = Guid.Empty;
            Guid route_id = Guid.Empty;
            string fname = "";
            string mname = "";
            string lname = "";
            if (reader.Read())
            {
                classs = reader.GetString(0);
                section = reader.GetString(1);
                roll = reader.GetInt32(2);
                child_personid = reader.GetGuid(3);
                lname = reader.GetString(4);
                mname = reader.GetString(5);
                fname = reader.GetString(6);
                route_id = reader.IsDBNull(7) ? Guid.Empty : reader.GetGuid(7);
            }
            dbCon.CloseReader(reader);
            pdrm.Classs = classs;
            pdrm.Section = section;
            pdrm.Roll = roll;
            pdrm.ChildPersonid = child_personid;
            pdrm.Lname = lname;
            pdrm.Mname = mname;
            pdrm.Fname = fname;
            pdrm.RouteId = route_id;

            pdrm.PersonId = personId;

            select = "select TBL_U_Person.First_name, TBL_U_Person.Middle_name, TBL_U_Person.Last_name, TBL_U_Person.Phone, TBL_U_Person.Person_id from "+
                        " (TBL_U_Person " +
                        " inner join TBL_U_SchoolAgeUser on TBL_U_Person.Person_id = TBL_U_SchoolAgeUser.SchoolAgeUser_Person_id_fk " +
                        " inner join TBL_U_Driver on TBL_U_Driver.Driver_SchoolAgeUser_id_fk = TBL_U_SchoolAgeUser.SchoolAgeUser_id) " +
                        " where Driver_Route_id_fk = @routeid";

            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@routeid", route_id);
            reader = await dbCon.ReadData(cmd);
            string driverFName = "";
            string driverMName = "";
            string driverLName = "";
            string driverPhone = "";
            Guid driverPersonId = Guid.Empty;
            if (reader.Read())
            {
                driverFName = reader.GetString(0);
                driverMName = reader.GetString(1);
                driverLName = reader.GetString(2);
                driverPhone = reader.GetString(3);
                driverPersonId = reader.GetGuid(4);
            }
            dbCon.CloseReader(reader);
            pdrm.DriverFname = driverFName;
            pdrm.DriverMname = driverMName;
            pdrm.DriverLname = driverLName;
            pdrm.DriverPhone = driverPhone;
            pdrm.DriverPersonId = driverPersonId;
            dbCon.CloseReader(reader);

            DateTime now = DateTime.Now;
            var startDate = new DateTime(now.Year, now.Month, 1);
            var endDate = startDate.AddMonths(1).AddDays(-1);

            select = "select Attendance_date, Remark from TBL_U_Attendance where Attendance_Student_id_fk = @student_id and " +
            " Attendance_date >= @startDate and Attendance_date <= @endDate";
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@student_id", student_id);
            cmd.Parameters.AddWithValue("@startDate", startDate);
            cmd.Parameters.AddWithValue("@endDate", endDate);

            reader = await dbCon.ReadData(cmd);
            int[] Attendance = new int[31];

            byte val = 3;
            while (reader.Read())
            {
                DateTime attDate = reader.GetDateTime(0);
                string remark = reader[1].ToString();
                if (remark != null && remark == "P") val = 3;
                else if (remark != null && remark == "A") val = 1;
                else if (remark != null && remark == "L") val = 2;
                else val = 0;
                int d = attDate.Day - 1;
                Attendance[d] = val;
            }
            dbCon.CloseReader(reader);
            pdrm.Attendance = Attendance;
            select = "select Fee_Amount, Fee_Due_Date, Fee_Description from TBL_U_Fee where Fee_Student_id_fk = @student_id;";
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@student_id", student_id);
            reader = await dbCon.ReadData(cmd);
            float feeAmt = 0.0F;
            DateTime dueDate = new DateTime(1970, 1, 1);
            string note = "";
            int count = 0;
            while (reader.Read())
            {
                feeAmt += (float)reader.GetDecimal(0);
                DateTime dueDate1 = reader.GetDateTime(1);
                if (dueDate1 > dueDate)
                    dueDate = dueDate1;
                note = reader.GetString(2);
                count++;
            }
            if (count > 1)
                note = "Total Fee Due";
            dbCon.CloseReader(reader);
            pdrm.TotalFeeDue = feeAmt;
            pdrm.DueDate = dueDate;
            pdrm.FeeDueNote = note;
            Guid uniqKey = Guid.NewGuid();

            string update = "Update TBL_U_SchoolAgeUser set User_unique_key=@ukey where User_unique_key = @curukey and Username=@user_id";
            cmd = new SqlCommand(update, con);
            cmd.Parameters.AddWithValue("@ukey", uniqKey);
            cmd.Parameters.AddWithValue("@curukey", parentDashboardRequest.UniqKey);
            cmd.Parameters.AddWithValue("@user_id", parentDashboardRequest.UserId);
            await cmd.ExecuteNonQueryAsync();
            dbCon.CloseDB(con);
            pdrm.UniqKey = uniqKey;
            pdrm.RespCode = ResponseCode.OK;
            return pdrm;
        }

        [System.Web.Http.HttpPost]
        [ActionName("dboardteacher")]
        public async Task<TeacherDashboardResponseModel> DashboardTeacher([FromBody] TeacherDashboardRequestModel teacherDashboardRequest)
        {
            TeacherDashboardResponseModel tdrm = new TeacherDashboardResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.FORBIDDEN };
            SqlConnection con = await dbCon.ConnectDB();
            string select = "select SchoolAgeUser_id, SchoolAgeUser_UserType_id_fk, SchoolAgeUser_Person_id_fk from TBL_U_SchoolAgeUser where User_unique_key = @uniqKey and Username = @userId";
            SqlCommand cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@uniqKey", teacherDashboardRequest.UniqKey);
            cmd.Parameters.AddWithValue("@userId", teacherDashboardRequest.UserId);
            SqlDataReader reader = await dbCon.ReadData(cmd);
            Guid schoolAgeUser_id = Guid.Empty;
            Guid schoolAgeUser_UserType_id_fk = Guid.Empty;
            Guid personId = Guid.Empty;
            if (reader.Read())
            {
                schoolAgeUser_id = reader.GetGuid(0);//teacher
                schoolAgeUser_UserType_id_fk = reader.GetGuid(1);//teacher
                personId = reader.GetGuid(2);//parent|student
            }
            dbCon.CloseReader(reader);

            select = "select User_code from TBL_U_UserType where UserType_id = @userType_id;";
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@userType_id", schoolAgeUser_UserType_id_fk);

            string usertype = "";
            reader = await dbCon.ReadData(cmd);
            if (reader.Read())
            {
                usertype = reader[0].ToString();
            }
            dbCon.CloseReader(reader);

            if (usertype != "TE")
                return tdrm;
            tdrm.PersonId = personId;
            select = "select Teacher_id, Class, Section from TBL_U_Teacher where Teacher_SchoolAgeUser_id_fk = @schoolAgeUser_id;";
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@schoolAgeUser_id", schoolAgeUser_id);
            Guid teacher_id = Guid.Empty;
            string teacherClass = "";
            string teacherSection = "";
            reader = await dbCon.ReadData(cmd);
            if (reader.Read())
            {
                teacher_id = reader.GetGuid(0);
                teacherClass = reader.GetString(1);
                teacherSection = reader.GetString(2);
            }
            dbCon.CloseReader(reader);
            tdrm.Class = teacherClass;
            tdrm.Section = teacherSection;
            IList<TimeTableModel> timeTableToday = new List<TimeTableModel>();
            DateTime today = DateTime.Today;
            DayOfWeek day_w = today.DayOfWeek;
            select = "select TBL_U_Teacher_TT.From_time as from_t, TBL_U_Teacher_TT.To_time as to_t, TBL_U_Teacher_TT.Week_day as day_w, TBL_U_Subject.Description as subject, " +
                        " TBL_U_Class.Class as class, TBL_U_Class.Section as sec, TBL_U_Teacher_TT.Description as lectureroom " +
                        " from((TBL_U_Teacher_TT " +
                        " INNER JOIN TBL_U_Subject on TBL_U_Teacher_TT.Teacher_TT_Subject_id_fk = TBL_U_Subject.Subject_id) " +
                        " INNER JOIN TBL_U_Class on TBL_U_Teacher_TT.Teacher_TT_Class_id_fk = TBL_U_Class.Class_id) " +
                        " where TBL_U_Teacher_TT.Teacher_TT_Teacher_id_fk= @teacher_id and TBL_U_Teacher_TT.Week_day = @day_w";
            cmd = new SqlCommand(select, con);
            string weekday = day_w.ToString();
            cmd.Parameters.AddWithValue("@teacher_id", teacher_id);
            cmd.Parameters.AddWithValue("@day_w", weekday);
            reader = await dbCon.ReadData(cmd);
            TimeSpan fromTime = DateTime.Now.TimeOfDay;
            TimeSpan toTime = DateTime.Now.TimeOfDay;
            string day = "";
            string subject = "";
            string cl = "";
            string sec = "";
            string desc = "";

            while (reader.Read())
            {
                fromTime = reader.GetTimeSpan(0);
                toTime = reader.GetTimeSpan(1);
                day = reader[2].ToString();
                subject = reader[3].ToString();
                cl = reader[4].ToString();
                sec = reader[5].ToString();
                desc = reader[6].ToString();
                timeTableToday.Add(new TimeTableModel() { From = fromTime, To = toTime, Day = day, Subject = subject, Class_Section = cl + " " + sec, Description = desc });
            }
            dbCon.CloseReader(reader);

            IList<IncompleteTaskModel> inCompleteTasks = new List<IncompleteTaskModel>();

            select = "select TBL_U_Assignment.AssignmentTxt Heading, TBL_U_Assignment.TeacherRemark as Task, " +
                      "  TBL_U_Assignment.Assignment_due_date as DueDate, TBL_U_Class.Class as Class, " +
                      "  TBL_U_Class.Section as Section, TBL_U_Person.Last_name as LName, TBL_U_Person.First_name as FName " +
                      "  from(TBL_U_Assignment " +
                      "  inner join TBL_U_Class on TBL_U_Assignment.Assignment_Class_id_fk = TBL_U_Class.Class_id " +
                      "  inner join TBL_U_Student on TBL_U_Assignment.Assignment_Student_id_fk = TBL_U_Student.Student_id " +
                      "  inner join TBL_U_SchoolAgeUser on TBL_U_Student.Student_SchoolAgeUser_id_fk = TBL_U_SchoolAgeUser.SchoolAgeUser_id " +
                      "  inner join TBL_U_Person on TBL_U_SchoolAgeUser.SchoolAgeUser_Person_id_fk = TBL_U_Person.Person_id) " +
                      "  where Assignment_Teacher_id_fk = @teacher_id " +
                      "  and Completed = 0";// and Getdate() > Assignment_due_date";//TODO, uncomment this line
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@teacher_id", teacher_id);
            reader = await dbCon.ReadData(cmd);
            while (reader.Read())
            {
                inCompleteTasks.Add(new IncompleteTaskModel()
                { Heading = reader.GetString(0),
                    TaskDetail = reader.GetString(1),
                    DueDate = reader.GetDateTime(2),
                    Class = reader.GetString(3),
                    Section = reader.GetString(4),
                    StudentLName = reader.GetString(5),
                    StudentFName = reader.GetString(6),
                });
            }
            dbCon.CloseReader(reader);

            Guid uniqKey = Guid.NewGuid();
            string update = "Update TBL_U_SchoolAgeUser set User_unique_key=@ukey where User_unique_key = @curukey and Username = @user_id";
            cmd = new SqlCommand(update, con);
            cmd.Parameters.AddWithValue("@ukey", uniqKey);
            cmd.Parameters.AddWithValue("@curukey", teacherDashboardRequest.UniqKey);
            cmd.Parameters.AddWithValue("@user_id", teacherDashboardRequest.UserId);
            await cmd.ExecuteNonQueryAsync();
            dbCon.CloseDB(con);
            tdrm.UniqKey = uniqKey;
            tdrm.RespCode = ResponseCode.OK;
            tdrm.TimeTableToday = timeTableToday;
            tdrm.IncompleteTasks = inCompleteTasks;
            return tdrm;
        }


        [System.Web.Http.HttpPost]
        [ActionName("dboarddriver")]
        public async Task<DriverDashboardResponseModel> DashboardDriver([FromBody] DriverDashboardRequestModel driverDashboardRequest)
        {
            DriverDashboardResponseModel ddrm = new DriverDashboardResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.FORBIDDEN };
            
            SqlConnection con = await dbCon.ConnectDB();
            string select = "select SchoolAgeUser_id, SchoolAgeUser_UserType_id_fk, SchoolAgeUser_Person_id_fk from TBL_U_SchoolAgeUser where User_unique_key = @uniqKey and Username = @userId";
            SqlCommand cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@uniqKey", driverDashboardRequest.UniqKey);
            cmd.Parameters.AddWithValue("@userId", driverDashboardRequest.UserId);
            SqlDataReader reader = await dbCon.ReadData(cmd);
            Guid schoolAgeUser_id = Guid.Empty;
            Guid schoolAgeUser_UserType_id_fk = Guid.Empty;
            Guid personid = Guid.Empty;
            if (reader.Read())
            {
                schoolAgeUser_id = reader.GetGuid(0);//teacher
                schoolAgeUser_UserType_id_fk = reader.GetGuid(1);//teacher
                personid = reader.GetGuid(2);
            }
            dbCon.CloseReader(reader);

            select = "select User_code from TBL_U_UserType where UserType_id = @userType_id;";
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@userType_id", schoolAgeUser_UserType_id_fk);

            string usertype = "";
            reader = await dbCon.ReadData(cmd);
            if (reader.Read())
            {
                usertype = reader[0].ToString();
            }
            dbCon.CloseReader(reader);
            if (usertype != null) usertype.Trim();
            if (!usertype.Contains("DR"))
                return ddrm;

            select = "select " +
                        " Driver_id as DriverId,  " +
                        " TBL_U_Route.Route_id as RouteId,  " +
                        " TBL_U_Route.Vehicle_No as VehicleNo, " +
                        " TBL_U_RouteMap.Latitude_str as Map_Latitude,  " +
                        " TBL_U_RouteMap.Longitude_str as Map_Longitude, " +
                        //                        " TBL_U_Student.Student_SchoolAgeUser_id_fk, " +
                        " TBL_U_Person.Person_id as PersonId, " +
                        " TBL_U_Person.Last_name as LastName, " +
                        " TBL_U_Person.Middle_name as MiddleName, " +
                        " TBL_U_Person.First_name as FirstName " +
                        " from " +
                        " (TBL_U_Driver " +
                        " inner join TBL_U_Route on TBL_U_Driver.Driver_Route_id_fk = TBL_U_Route.Route_id " +
                        " inner join TBL_U_RouteMap on TBL_U_RouteMap.Route_id_fk = TBL_U_Route.Route_id " +
                        " inner join TBL_U_Student on TBL_U_Student.Student_RouteMap_id_fk = TBL_U_RouteMap.RouteMap_id " +
                        " inner join TBL_U_schoolageUser on TBL_U_schoolageUser.schoolageUser_id = TBL_U_Student.Student_SchoolAgeUser_id_fk " +
                        " inner join TBL_U_Person on TBL_U_schoolageUser.SchoolAgeUser_Person_id_fk = TBL_U_Person.Person_id " +
                        " ) " +
                        " where TBL_U_Route.Vehicle_No = @vehicle_no  ORDER BY TBL_U_RouteMap.Sequence_no DESC";// ORDER BY Map_Latitude DESC, Map_Longitude DESC";
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@vehicle_no", driverDashboardRequest.VehicleNo);
            reader = await dbCon.ReadData(cmd);
            string lat = "";
            string lng = "";
            RouteDetailsModel rdm = null;
            ddrm.RouteDetailsLst = new List<RouteDetailsModel>();
            while (reader.Read())
            {
                Guid driverId = reader.IsDBNull(0) ? Guid.Empty : reader.GetGuid(0);
                Guid routeId = reader.IsDBNull(1) ? Guid.Empty : reader.GetGuid(1);
                string vehicleNo = reader.GetString(2);
                string latitude = reader.GetString(3);
                string longitude = reader.GetString(4);
                Guid personId = reader.IsDBNull(5) ? Guid.Empty : reader.GetGuid(5);
                string LName = reader.GetString(6);
                string MName = reader.GetString(7);
                string FName = reader.GetString(8);
                if (!vehicleNo.Equals(driverDashboardRequest.VehicleNo))
                    continue;

                if (!lat.Equals(latitude) || !lng.Equals(longitude))
                {
                    if (rdm != null)
                        ddrm.RouteDetailsLst.Add(rdm);
                    rdm = new RouteDetailsModel() { PickupLatitude = latitude, PickupLongitude = longitude };
                    rdm.passengerDetailsLst = new List<PassengerDetailsModel>();
                    rdm.passengerDetailsLst.Add(new PassengerDetailsModel() { PersonId = personId, FName = FName, MName = MName, LName = LName, Remark = "A" });
                    lat = latitude;
                    lng = longitude;
                }
                else
                {
                    if(rdm.passengerDetailsLst == null)
                        rdm.passengerDetailsLst = new List<PassengerDetailsModel>();
                    rdm.passengerDetailsLst.Add(new PassengerDetailsModel() { PersonId = personId, FName = FName, MName = MName, LName = LName, Remark="A" });
                }
            }
            if (rdm != null)
                ddrm.RouteDetailsLst.Add(rdm);

            dbCon.CloseReader(reader);
            ddrm.SchoolLatitude = "";
            ddrm.SchoolLatitude = "";
            select = "select TBL_U_RouteMap.Latitude_str as Map_Latitude, " +
                "TBL_U_RouteMap.Longitude_str as Map_Longitude " +
                "from " +
                "(TBL_U_RouteMap inner join TBL_U_Route on TBL_U_RouteMap.Route_id_fk = TBL_U_Route.Route_id) " +
                "where TBL_U_Route.Vehicle_No = @vehicle_no and TBL_U_RouteMap.Description = 'School'";
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@vehicle_no", driverDashboardRequest.VehicleNo);
            reader = await dbCon.ReadData(cmd);
            while (reader.Read())
            {
                string latitude = reader.GetString(0);
                string longitude = reader.GetString(1);
                ddrm.SchoolLatitude = latitude;
                ddrm.SchoolLongitude = longitude;
            }
            dbCon.CloseReader(reader);

            select = "select count(*) from TBL_U_BusBoardingLog where Passenger_Person_id_fk=@passenger_id and " +
                                " Creation_date >= DATEADD(day, DATEDIFF(day, 0, GETDATE()), 0) " +
                                " AND Creation_date < DATEADD(day, DATEDIFF(day, 0, GETDATE()) + 1, 0)";

            foreach (RouteDetailsModel itemR in ddrm.RouteDetailsLst)
            {
                foreach(PassengerDetailsModel itemP in itemR.passengerDetailsLst)
                {
                    cmd = new SqlCommand(select, con);
                    cmd.Parameters.AddWithValue("@passenger_id", itemP.PersonId);
                    reader = await dbCon.ReadData(cmd);
                    int count = 0;
                    if (reader.Read())
                    {
                        count = reader.GetInt32(0);
                    }
                    if (count == 0)
                        itemP.Remark = "A";
                    else
                        itemP.Remark = "P";
                    dbCon.CloseReader(reader);
                }
            }

            ddrm.PersonId = personid;
            Guid uniqKey = Guid.NewGuid();
            string update = "Update TBL_U_SchoolAgeUser set User_unique_key=@ukey where User_unique_key = @curukey and Username = @user_id";
            cmd = new SqlCommand(update, con);
            cmd.Parameters.AddWithValue("@ukey", uniqKey);
            cmd.Parameters.AddWithValue("@curukey", driverDashboardRequest.UniqKey);
            cmd.Parameters.AddWithValue("@user_id", driverDashboardRequest.UserId);
            await cmd.ExecuteNonQueryAsync();
            dbCon.CloseDB(con);
            ddrm.UniqKey = uniqKey;
            ddrm.RespCode = ResponseCode.OK;

            return ddrm;
        }

        [System.Web.Http.HttpPost]
        [ActionName("loadvehicledriver")]
        public async Task<DriverVehicleResponseModel> MarkOnBoadrDriver([FromBody] DriverVehicleRequestModel driverVehicleRequest)
        {
            DriverVehicleResponseModel dmobrm = new DriverVehicleResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.FORBIDDEN };
            SqlConnection con = await dbCon.ConnectDB();
            string select = "select SchoolAgeUser_id, SchoolAgeUser_UserType_id_fk, SchoolAgeUser_Person_id_fk from TBL_U_SchoolAgeUser where User_unique_key = @uniqKey and Username = @userId";
            SqlCommand cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@uniqKey", driverVehicleRequest.UniqKey);
            cmd.Parameters.AddWithValue("@userId", driverVehicleRequest.UserId);
            SqlDataReader reader = await dbCon.ReadData(cmd);
            Guid schoolAgeUser_id = Guid.Empty;
            Guid schoolAgeUser_UserType_id_fk = Guid.Empty;
            Guid personid = Guid.Empty;
            if (reader.Read())
            {
                schoolAgeUser_id = reader.GetGuid(0);
                schoolAgeUser_UserType_id_fk = reader.GetGuid(1);
                personid = reader.GetGuid(2);
            }
            dbCon.CloseReader(reader);

            select = "select User_code from TBL_U_UserType where UserType_id = @userType_id;";
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@userType_id", schoolAgeUser_UserType_id_fk);

            string usertype = "";
            reader = await dbCon.ReadData(cmd);
            if (reader.Read())
            {
                usertype = reader[0].ToString();
            }
            dbCon.CloseReader(reader);

            if (usertype != "DR")
                return dmobrm;
            select = "select Vehicle_No, Description from TBL_U_Route inner join TBL_U_Driver on TBL_U_Route.Route_id = TBL_U_Driver.Driver_Route_id_fk " +
                        " where Driver_SchoolAgeUser_id_fk = @schoolageuser_id";

            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@schoolageuser_id", schoolAgeUser_id);
            reader = await dbCon.ReadData(cmd);
            dmobrm.RouteDescription = new List<string>();
            dmobrm.VehicleNo = new List<string>();
            while (reader.Read())
            {
                string vn = reader.GetString(0);
                string des = reader.GetString(1);
                if (null != vn && null != des)
                {
                    dmobrm.VehicleNo.Add(vn);
                    dmobrm.RouteDescription.Add(des);
                }
            }
            dbCon.CloseReader(reader);
            Guid uniqKey = Guid.NewGuid();
            string update = "Update TBL_U_SchoolAgeUser set User_unique_key=@ukey where User_unique_key = @curukey and Username = @user_id";
            cmd = new SqlCommand(update, con);
            cmd.Parameters.AddWithValue("@ukey", uniqKey);
            cmd.Parameters.AddWithValue("@curukey", driverVehicleRequest.UniqKey);
            cmd.Parameters.AddWithValue("@user_id", driverVehicleRequest.UserId);
            await cmd.ExecuteNonQueryAsync();
            dbCon.CloseDB(con);
            dmobrm.UniqKey = uniqKey;
            dmobrm.RespCode = ResponseCode.OK;
            return dmobrm;
        }


        [System.Web.Http.HttpPost]
        [ActionName("markonboarddriver")]
        public async Task<DriverMarkOnBoardResponseModel> MarkOnBoadrDriver([FromBody] DriverMarkOnBoardRequestModel driverMarkOnBoardRequest)
        {
            DriverMarkOnBoardResponseModel dmobrm = new DriverMarkOnBoardResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.FORBIDDEN };
            SqlConnection con = await dbCon.ConnectDB();
            string select = "select SchoolAgeUser_id, SchoolAgeUser_UserType_id_fk, SchoolAgeUser_Person_id_fk from TBL_U_SchoolAgeUser where User_unique_key = @uniqKey and Username = @userId";
            SqlCommand cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@uniqKey", driverMarkOnBoardRequest.UniqKey);
            cmd.Parameters.AddWithValue("@userId", driverMarkOnBoardRequest.UserId);
            SqlDataReader reader = await dbCon.ReadData(cmd);
            Guid schoolAgeUser_id = Guid.Empty;
            Guid schoolAgeUser_UserType_id_fk = Guid.Empty;
            Guid personid = Guid.Empty;
            if (reader.Read())
            {
                schoolAgeUser_id = reader.GetGuid(0);
                schoolAgeUser_UserType_id_fk = reader.GetGuid(1);
                personid = reader.GetGuid(2);
            }
            dbCon.CloseReader(reader);

            select = "select User_code from TBL_U_UserType where UserType_id = @userType_id;";
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@userType_id", schoolAgeUser_UserType_id_fk);

            string usertype = "";
            reader = await dbCon.ReadData(cmd);
            if (reader.Read())
            {
                usertype = reader[0].ToString();
            }
            dbCon.CloseReader(reader);

            if (usertype != "DR")
                return dmobrm;
            //MarkOnBoardList
          string insert = "insert into TBL_U_BusBoardingLog (Passenger_Person_id_fk, Driver_Person_id_fk, Vehicle_No, Latitude_str, Longitude_str) " +
                " values (@passenger, @driver, @vehicle, @latitude, @longitude)";
            foreach (MarkOnBoardModel item in driverMarkOnBoardRequest.MarkOnBoardList)
            {
                cmd = new SqlCommand(insert, con);
                cmd.Parameters.AddWithValue("@passenger", item.PassengerPersonId);
                cmd.Parameters.AddWithValue("@driver", personid);
                cmd.Parameters.AddWithValue("@vehicle", driverMarkOnBoardRequest.VehicleNo);
                cmd.Parameters.AddWithValue("@latitude", item.Latitude);
                cmd.Parameters.AddWithValue("@longitude", item.Longitude);
                await cmd.ExecuteNonQueryAsync();
            }

            Guid uniqKey = Guid.NewGuid();
            string update = "Update TBL_U_SchoolAgeUser set User_unique_key=@ukey where User_unique_key = @curukey and Username = @user_id";
            cmd = new SqlCommand(update, con);
            cmd.Parameters.AddWithValue("@ukey", uniqKey);
            cmd.Parameters.AddWithValue("@curukey", driverMarkOnBoardRequest.UniqKey);
            cmd.Parameters.AddWithValue("@user_id", driverMarkOnBoardRequest.UserId);
            await cmd.ExecuteNonQueryAsync();
            dbCon.CloseDB(con);
            dmobrm.UniqKey = uniqKey;
            dmobrm.RespCode = ResponseCode.OK;
            return dmobrm;
        }

        [System.Web.Http.HttpPost]
        [ActionName("attendanceparent")]
        public async Task<ParentAttendanceResponseModel> AttendanceParent([FromBody] ParentAttendanceRequestModel parenAttendanceRequest)
        {
            ParentAttendanceResponseModel parm = new ParentAttendanceResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.FORBIDDEN };
            SqlConnection con = await dbCon.ConnectDB();
            string select = "select SchoolAgeUser_id, SchoolAgeUser_UserType_id_fk from TBL_U_SchoolAgeUser where User_unique_key = @uniqKey and Username = @userId";
            SqlCommand cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@uniqKey", parenAttendanceRequest.UniqKey);
            cmd.Parameters.AddWithValue("@userId", parenAttendanceRequest.UserId);
            SqlDataReader reader = await dbCon.ReadData(cmd);
            Guid schoolAgeUser_id = Guid.Empty;
            Guid schoolAgeUser_UserType_id_fk = Guid.Empty;
            if (reader.Read())
            {
                schoolAgeUser_id = reader.GetGuid(0);//parent|student
                schoolAgeUser_UserType_id_fk = reader.GetGuid(1);//parent|student
            }
            dbCon.CloseReader(reader);

            select = "select User_code from TBL_U_UserType where UserType_id = @userType_id;";
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@userType_id", schoolAgeUser_UserType_id_fk);

            string usertype = "";
            reader = await dbCon.ReadData(cmd);
            if (reader.Read())
            {
                usertype = reader[0].ToString();
            }
            dbCon.CloseReader(reader);
            Guid student_id = Guid.Empty;
            if (usertype == "PA")
            {
                select = "select Parent_Student_id_fk from TBL_U_Parent where Parent_SchoolAgeUser_id_fk = @parent_SchoolAgeUser_id_fk;";
                cmd = new SqlCommand(select, con);
                cmd.Parameters.AddWithValue("@parent_SchoolAgeUser_id_fk", schoolAgeUser_id);
                reader = await dbCon.ReadData(cmd);
                if (reader.Read())
                {
                    student_id = reader.GetGuid(0);//student
                }
                dbCon.CloseReader(reader);
            }
            else if (usertype != "ST")
            {

                select = "select Student_id from TBL_U_Student where Student_SchoolAgeUser_id_fk = @schoolAgeUser_id_fk;";
                cmd = new SqlCommand(select, con);
                cmd.Parameters.AddWithValue("@schoolAgeUser_id_fk", schoolAgeUser_id);

                reader = await dbCon.ReadData(cmd);
                if (reader.Read())
                {
                    student_id = reader.GetGuid(0);//student
                }
                dbCon.CloseReader(reader);
            }
            else return parm;

            if (student_id == Guid.Empty) return parm;

            parm.MonthAttendanceList = new List<MonthAttendanceModel>();
            DateTime now = DateTime.Now;
            for (int i = 0; i < 12; i++, now = now.AddMonths(-1))
            {
                var startDate = new DateTime(now.Year, now.Month, 1);
                var endDate = startDate.AddMonths(1).AddDays(-1);

                select = "select Attendance_date, Remark from TBL_U_Attendance where Attendance_Student_id_fk = @student_id and " +
                " Attendance_date >= @startDate and Attendance_date <= @endDate";
                cmd = new SqlCommand(select, con);
                cmd.Parameters.AddWithValue("@student_id", student_id);
                cmd.Parameters.AddWithValue("@startDate", startDate);
                cmd.Parameters.AddWithValue("@endDate", endDate);

                reader = await dbCon.ReadData(cmd);
                int[] Attendance = new int[31];
                byte val = 3;
                int schoolDays = 0;
                int absentDays = 0;
                while (reader.Read())
                {
                    DateTime attDate = reader.GetDateTime(0);
                    string remark = reader[1].ToString();
                    if (remark != null && remark == "P") val = 3;
                    else if (remark != null && remark == "A") val = 1;
                    else if (remark != null && remark == "L") val = 2;
                    else val = 0;
                    int d = attDate.Day -1;
                    Attendance[d] = val;
                    if(val != 0)schoolDays = schoolDays + 1;
                    if (val == 1) absentDays = absentDays + 1;
                }
                int attendancePercent = (schoolDays == 0) ? 0 : (100 - (absentDays * 100 )/ schoolDays );
                parm.MonthAttendanceList.Add(new MonthAttendanceModel(){ Month = now.Month, Attendance = Attendance,AttendancePercent = attendancePercent });

                dbCon.CloseReader(reader);
            }

            Guid uniqKey = Guid.NewGuid();

            string update = "Update TBL_U_SchoolAgeUser set User_unique_key=@ukey where User_unique_key = @curukey and Username=@user_id";
            cmd = new SqlCommand(update, con);
            cmd.Parameters.AddWithValue("@ukey", uniqKey);
            cmd.Parameters.AddWithValue("@curukey", parenAttendanceRequest.UniqKey);
            cmd.Parameters.AddWithValue("@user_id", parenAttendanceRequest.UserId);
            await cmd.ExecuteNonQueryAsync();
            dbCon.CloseDB(con);
            parm.UniqKey = uniqKey;
            parm.RespCode = ResponseCode.OK;
            return parm;
        }

        [System.Web.Http.HttpPost]
        [ActionName("taskstodoparent")]
        public async Task<ParentTasksResponseModel> TasksToDoParent([FromBody] ParentTasksRequestModel parentTasksRequest)
        {
            ParentTasksResponseModel ptrm = new ParentTasksResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.FORBIDDEN };
            SqlConnection con = await dbCon.ConnectDB();
            string select = "select SchoolAgeUser_id, SchoolAgeUser_UserType_id_fk from TBL_U_SchoolAgeUser where User_unique_key = @uniqKey and Username = @userId";
            SqlCommand cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@uniqKey", parentTasksRequest.UniqKey);
            cmd.Parameters.AddWithValue("@userId", parentTasksRequest.UserId);
            SqlDataReader reader = await dbCon.ReadData(cmd);
            Guid schoolAgeUser_id = Guid.Empty;
            Guid schoolAgeUser_UserType_id_fk = Guid.Empty;
            if (reader.Read())
            {
                schoolAgeUser_id = reader.GetGuid(0);//parent|student
                schoolAgeUser_UserType_id_fk = reader.GetGuid(1);//parent|student
            }
            dbCon.CloseReader(reader);

            select = "select User_code from TBL_U_UserType where UserType_id = @userType_id;";
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@userType_id", schoolAgeUser_UserType_id_fk);

            string usertype = "";
            reader = await dbCon.ReadData(cmd);
            if (reader.Read())
            {
                usertype = reader[0].ToString();
            }
            dbCon.CloseReader(reader);
            Guid student_id = Guid.Empty;
            if (usertype.Contains("PA"))
            {
                select = "select Parent_Student_id_fk from TBL_U_Parent where Parent_SchoolAgeUser_id_fk = @parent_SchoolAgeUser_id_fk;";
                cmd = new SqlCommand(select, con);
                cmd.Parameters.AddWithValue("@parent_SchoolAgeUser_id_fk", schoolAgeUser_id);
                reader = await dbCon.ReadData(cmd);
                if (reader.Read())
                {
                    student_id = reader.GetGuid(0);//student
                }
                dbCon.CloseReader(reader);
            }
            else if (!usertype.Contains("ST"))
            {

                select = "select Student_id from TBL_U_Student where Student_SchoolAgeUser_id_fk = @schoolAgeUser_id_fk;";
                cmd = new SqlCommand(select, con);
                cmd.Parameters.AddWithValue("@schoolAgeUser_id_fk", schoolAgeUser_id);

                reader = await dbCon.ReadData(cmd);
                if (reader.Read())
                {
                    student_id = reader.GetGuid(0);//student
                }
                dbCon.CloseReader(reader);
            }
            else return ptrm;

            if (student_id == Guid.Empty) return ptrm;

            ptrm.AssignmentList = new List<AssignmentModel>();
            DateTime now = DateTime.Now;
            select = "select Assignment_id, Assignment_date, Assignment_due_date, AssignmentTxt, AssignmentImg, TeacherRemark, ParentRemark from TBL_U_Assignment " +
                " where Completed = 0 and Assignment_due_date > getdate() and Assignment_Student_id_fk = @student_id";
            
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@student_id", student_id);

            reader = await dbCon.ReadData(cmd);

            while (reader.Read())
            {
                ptrm.AssignmentList.Add(new AssignmentModel()
                {
                    AssignmentId = reader.GetGuid(0),
                    AssignmentDate = reader.GetDateTime(1),
                    AssignmentDueDate = reader.GetDateTime(2),
                    AssignmentTxt = reader.GetString(3),
                    Remark = reader.GetString(5)
                });
            }

            dbCon.CloseReader(reader);


            Guid uniqKey = Guid.NewGuid();

            string update = "Update TBL_U_SchoolAgeUser set User_unique_key=@ukey where User_unique_key = @curukey and Username=@user_id";
            cmd = new SqlCommand(update, con);
            cmd.Parameters.AddWithValue("@ukey", uniqKey);
            cmd.Parameters.AddWithValue("@curukey", parentTasksRequest.UniqKey);
            cmd.Parameters.AddWithValue("@user_id", parentTasksRequest.UserId);
            await cmd.ExecuteNonQueryAsync();
            dbCon.CloseDB(con);
            ptrm.UniqKey = uniqKey;
            ptrm.RespCode = ResponseCode.OK;
            return ptrm;
        }
        [System.Web.Http.HttpPost]
        [ActionName("tasksdoneparent")]
        public async Task<ParentTasksDoneResponseModel> TasksDoneParent([FromBody] ParentTasksDoneRequestModel parentTasksDoneRequest)
        {
            ParentTasksDoneResponseModel ptdrm = new ParentTasksDoneResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.FORBIDDEN };
            SqlConnection con = await dbCon.ConnectDB();
            string select = "select SchoolAgeUser_id, SchoolAgeUser_UserType_id_fk from TBL_U_SchoolAgeUser where User_unique_key = @uniqKey and Username = @userId";
            SqlCommand cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@uniqKey", parentTasksDoneRequest.UniqKey);
            cmd.Parameters.AddWithValue("@userId", parentTasksDoneRequest.UserId);
            SqlDataReader reader = await dbCon.ReadData(cmd);
            Guid schoolAgeUser_id = Guid.Empty;
            Guid schoolAgeUser_UserType_id_fk = Guid.Empty;
            if (reader.Read())
            {
                schoolAgeUser_id = reader.GetGuid(0);//parent|student
                schoolAgeUser_UserType_id_fk = reader.GetGuid(1);//parent|student
            }
            dbCon.CloseReader(reader);

            select = "select User_code from TBL_U_UserType where UserType_id = @userType_id;";
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@userType_id", schoolAgeUser_UserType_id_fk);

            string usertype = "";
            reader = await dbCon.ReadData(cmd);
            if (reader.Read())
            {
                usertype = reader[0].ToString();
            }
            dbCon.CloseReader(reader);
            Guid student_id = Guid.Empty;
            if (usertype.Contains("PA"))
            {
                select = "select Parent_Student_id_fk from TBL_U_Parent where Parent_SchoolAgeUser_id_fk = @parent_SchoolAgeUser_id_fk;";
                cmd = new SqlCommand(select, con);
                cmd.Parameters.AddWithValue("@parent_SchoolAgeUser_id_fk", schoolAgeUser_id);
                reader = await dbCon.ReadData(cmd);
                if (reader.Read())
                {
                    student_id = reader.GetGuid(0);//student
                }
                dbCon.CloseReader(reader);
            }
            else if (!usertype.Contains("ST"))
            {

                select = "select Student_id from TBL_U_Student where Student_SchoolAgeUser_id_fk = @schoolAgeUser_id_fk;";
                cmd = new SqlCommand(select, con);
                cmd.Parameters.AddWithValue("@schoolAgeUser_id_fk", schoolAgeUser_id);

                reader = await dbCon.ReadData(cmd);
                if (reader.Read())
                {
                    student_id = reader.GetGuid(0);//student
                }
                dbCon.CloseReader(reader);
            }
            else return ptdrm;

            if (student_id == Guid.Empty) return ptdrm;
            string update = "update TBL_U_Assignment set Completed = 1 where Assignment_id = @assignment_id";
            /* foreach (Guid assignmentId in parentTasksDoneRequest.AssignmentIdList)
             {

                 cmd = new SqlCommand(update, con);
                 cmd.Parameters.AddWithValue("@assignment_id", assignmentId);
                 await cmd.ExecuteNonQueryAsync();
             }*/
            IList<string> taskIdList = parentTasksDoneRequest.AssignmentIdList;
            if (taskIdList != null)
            {

                foreach (string assignmentId in taskIdList)
                {
                    using (SqlCommand cmd1 = new SqlCommand(update, con))
                    {
                        cmd1.Parameters.AddWithValue("@assignment_id", assignmentId);
                        int i = await cmd1.ExecuteNonQueryAsync();
                    }
                }
            }
                Guid uniqKey = Guid.NewGuid();

            update = "Update TBL_U_SchoolAgeUser set User_unique_key=@ukey where User_unique_key = @curukey and Username=@user_id";
            cmd = new SqlCommand(update, con);
            cmd.Parameters.AddWithValue("@ukey", uniqKey);
            cmd.Parameters.AddWithValue("@curukey", parentTasksDoneRequest.UniqKey);
            cmd.Parameters.AddWithValue("@user_id", parentTasksDoneRequest.UserId);
            await cmd.ExecuteNonQueryAsync();
            dbCon.CloseDB(con);
            ptdrm.UniqKey = uniqKey;
            ptdrm.RespCode = ResponseCode.OK;
            return ptdrm;
        }

        [System.Web.Http.HttpPost]
        [ActionName("noticetoparent")]
        public async Task<ParentNoticeResponseModel> NoticeToParent([FromBody] ParentNoticeRequestModel parentNoticeRequest)
        {
            ParentNoticeResponseModel pnrm = new ParentNoticeResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.FORBIDDEN };
            SqlConnection con = await dbCon.ConnectDB();
            string select = "select SchoolAgeUser_id, SchoolAgeUser_UserType_id_fk from TBL_U_SchoolAgeUser where User_unique_key = @uniqKey and Username = @userId";
            SqlCommand cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@uniqKey", parentNoticeRequest.UniqKey);
            cmd.Parameters.AddWithValue("@userId", parentNoticeRequest.UserId);
            SqlDataReader reader = await dbCon.ReadData(cmd);
            Guid schoolAgeUser_id = Guid.Empty;
            Guid schoolAgeUser_UserType_id_fk = Guid.Empty;
            if (reader.Read())
            {
                schoolAgeUser_id = reader.GetGuid(0);//parent|student
                schoolAgeUser_UserType_id_fk = reader.GetGuid(1);//parent|student
            }
            dbCon.CloseReader(reader);

            select = "select User_code from TBL_U_UserType where UserType_id = @userType_id;";
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@userType_id", schoolAgeUser_UserType_id_fk);

            string usertype = "";
            reader = await dbCon.ReadData(cmd);
            if (reader.Read())
            {
                usertype = reader[0].ToString();
            }
            dbCon.CloseReader(reader);
            Guid student_id = Guid.Empty;
            if (usertype.Contains("PA"))
            {
                select = "select Parent_Student_id_fk from TBL_U_Parent where Parent_SchoolAgeUser_id_fk = @parent_SchoolAgeUser_id_fk;";
                cmd = new SqlCommand(select, con);
                cmd.Parameters.AddWithValue("@parent_SchoolAgeUser_id_fk", schoolAgeUser_id);
                reader = await dbCon.ReadData(cmd);
                if (reader.Read())
                {
                    student_id = reader.GetGuid(0);//student
                }
                dbCon.CloseReader(reader);
            }
            else if (!usertype.Contains("ST"))
            {

                select = "select Student_id from TBL_U_Student where Student_SchoolAgeUser_id_fk = @schoolAgeUser_id_fk;";
                cmd = new SqlCommand(select, con);
                cmd.Parameters.AddWithValue("@schoolAgeUser_id_fk", schoolAgeUser_id);

                reader = await dbCon.ReadData(cmd);
                if (reader.Read())
                {
                    student_id = reader.GetGuid(0);//student
                }
                dbCon.CloseReader(reader);
            }
            else return pnrm;

            if (student_id == Guid.Empty) return pnrm;

            pnrm.NoticeList = new List<NoticeModel>();
            DateTime now = DateTime.Now;
            select = "select Notice_id, Description, Valid_From from TBL_U_Notice where Valid_From < GetDate() and Valid_To >=  GetDate()";

            cmd = new SqlCommand(select, con);
            reader = await dbCon.ReadData(cmd);

            while (reader.Read())
            {
                Guid noticeId = reader.GetGuid(0);
                String notice = reader.GetString(1);
                DateTime noticeDate = reader.GetDateTime(2);
                pnrm.NoticeList.Add(new NoticeModel()
                {
                    NoiticeId = noticeId,
                    NoticeDate = noticeDate,
                    NoticeTxt = notice,
                    NoticeType = 1
                });
            }

            dbCon.CloseReader(reader);
            select = "select StudentRemark_id, Remark, Remark_date from TBL_U_StudentRemark where Completed = 0 and StudentRemark_Student_id_fk = @student_id";
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@student_id", student_id);
            reader = await dbCon.ReadData(cmd);
            while (reader.Read())
            {
                Guid noticeId = reader.GetGuid(0);
                String notice = reader.GetString(1);
                DateTime noticeDate = reader.GetDateTime(2);
                pnrm.NoticeList.Add(new NoticeModel()
                {
                    NoiticeId = noticeId,
                    NoticeDate = noticeDate,
                    NoticeTxt = notice,
                    NoticeType = 2
                });
            }

            dbCon.CloseReader(reader);

            Guid uniqKey = Guid.NewGuid();

            string update = "Update TBL_U_SchoolAgeUser set User_unique_key=@ukey where User_unique_key = @curukey and Username=@user_id";
            cmd = new SqlCommand(update, con);
            cmd.Parameters.AddWithValue("@ukey", uniqKey);
            cmd.Parameters.AddWithValue("@curukey", parentNoticeRequest.UniqKey);
            cmd.Parameters.AddWithValue("@user_id", parentNoticeRequest.UserId);
            await cmd.ExecuteNonQueryAsync();
            dbCon.CloseDB(con);
            pnrm.UniqKey = uniqKey;
            pnrm.RespCode = ResponseCode.OK;
            return pnrm;
        }

        [System.Web.Http.HttpPost]
        [ActionName("noticedoneparent")]
        public async Task<ParentNoticeDoneResponseModel> NoticeDoneParent([FromBody] ParentNoticeDoneRequestModel parentNoticeDoneRequest)
        {
            ParentNoticeDoneResponseModel ptdrm = new ParentNoticeDoneResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.FORBIDDEN };
            SqlConnection con = await dbCon.ConnectDB();
            string select = "select SchoolAgeUser_id, SchoolAgeUser_UserType_id_fk from TBL_U_SchoolAgeUser where User_unique_key = @uniqKey and Username = @userId";
            SqlCommand cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@uniqKey", parentNoticeDoneRequest.UniqKey);
            cmd.Parameters.AddWithValue("@userId", parentNoticeDoneRequest.UserId);
            SqlDataReader reader = await dbCon.ReadData(cmd);
            Guid schoolAgeUser_id = Guid.Empty;
            Guid schoolAgeUser_UserType_id_fk = Guid.Empty;
            if (reader.Read())
            {
                schoolAgeUser_id = reader.GetGuid(0);//parent|student
                schoolAgeUser_UserType_id_fk = reader.GetGuid(1);//parent|student
            }
            dbCon.CloseReader(reader);

            select = "select User_code from TBL_U_UserType where UserType_id = @userType_id;";
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@userType_id", schoolAgeUser_UserType_id_fk);

            string usertype = "";
            reader = await dbCon.ReadData(cmd);
            if (reader.Read())
            {
                usertype = reader[0].ToString();
            }
            dbCon.CloseReader(reader);
            Guid student_id = Guid.Empty;
            if (usertype.Contains("PA"))
            {
                select = "select Parent_Student_id_fk from TBL_U_Parent where Parent_SchoolAgeUser_id_fk = @parent_SchoolAgeUser_id_fk;";
                cmd = new SqlCommand(select, con);
                cmd.Parameters.AddWithValue("@parent_SchoolAgeUser_id_fk", schoolAgeUser_id);
                reader = await dbCon.ReadData(cmd);
                if (reader.Read())
                {
                    student_id = reader.GetGuid(0);//student
                }
                dbCon.CloseReader(reader);
            }
            else if (!usertype.Contains("ST"))
            {

                select = "select Student_id from TBL_U_Student where Student_SchoolAgeUser_id_fk = @schoolAgeUser_id_fk;";
                cmd = new SqlCommand(select, con);
                cmd.Parameters.AddWithValue("@schoolAgeUser_id_fk", schoolAgeUser_id);

                reader = await dbCon.ReadData(cmd);
                if (reader.Read())
                {
                    student_id = reader.GetGuid(0);//student
                }
                dbCon.CloseReader(reader);
            }
            else return ptdrm;

            if (student_id == Guid.Empty) return ptdrm;
            IList<string> noticeIdList = parentNoticeDoneRequest.NoticeIdList;
            if (noticeIdList != null)
            {
                foreach (string noticeId in noticeIdList)
                {
                    string update1 = "Update TBL_U_StudentRemark set Completed=1 where StudentRemark_id=@notice_id;";
                    SqlCommand cmd1 = new SqlCommand(update1, con);
                    cmd1.Parameters.AddWithValue("@notice_id", noticeId);
                    int i = await cmd1.ExecuteNonQueryAsync();
                }
            }
            Guid uniqKey = Guid.NewGuid();

            string update = "Update TBL_U_SchoolAgeUser set User_unique_key=@ukey where User_unique_key = @curukey and Username=@user_id";
            cmd = new SqlCommand(update, con);
            cmd.Parameters.AddWithValue("@ukey", uniqKey);
            cmd.Parameters.AddWithValue("@curukey", parentNoticeDoneRequest.UniqKey);
            cmd.Parameters.AddWithValue("@user_id", parentNoticeDoneRequest.UserId);
            await cmd.ExecuteNonQueryAsync();
            dbCon.CloseDB(con);
            ptdrm.UniqKey = uniqKey;
            ptdrm.RespCode = ResponseCode.OK;
            return ptdrm;
        }

        [System.Web.Http.HttpPost]
        [ActionName("feeduetoparent")]
        public async Task<ParentFeeDueResponseModel> FeeDueToParent([FromBody] ParentFeeDueRequestModel parentFeeRequest)
        {
            ParentFeeDueResponseModel pfdrm = new ParentFeeDueResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.FORBIDDEN };
            SqlConnection con = await dbCon.ConnectDB();
            string select = "select SchoolAgeUser_id, SchoolAgeUser_UserType_id_fk from TBL_U_SchoolAgeUser where User_unique_key = @uniqKey and Username = @userId";
            SqlCommand cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@uniqKey", parentFeeRequest.UniqKey);
            cmd.Parameters.AddWithValue("@userId", parentFeeRequest.UserId);
            SqlDataReader reader = await dbCon.ReadData(cmd);
            Guid schoolAgeUser_id = Guid.Empty;
            Guid schoolAgeUser_UserType_id_fk = Guid.Empty;
            if (reader.Read())
            {
                schoolAgeUser_id = reader.GetGuid(0);//parent|student
                schoolAgeUser_UserType_id_fk = reader.GetGuid(1);//parent|student
            }
            dbCon.CloseReader(reader);

            select = "select User_code from TBL_U_UserType where UserType_id = @userType_id;";
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@userType_id", schoolAgeUser_UserType_id_fk);

            string usertype = "";
            reader = await dbCon.ReadData(cmd);
            if (reader.Read())
            {
                usertype = reader[0].ToString();
            }
            dbCon.CloseReader(reader);
            Guid student_id = Guid.Empty;
            if (usertype.Contains("PA"))
            {
                select = "select Parent_Student_id_fk from TBL_U_Parent where Parent_SchoolAgeUser_id_fk = @parent_SchoolAgeUser_id_fk;";
                cmd = new SqlCommand(select, con);
                cmd.Parameters.AddWithValue("@parent_SchoolAgeUser_id_fk", schoolAgeUser_id);
                reader = await dbCon.ReadData(cmd);
                if (reader.Read())
                {
                    student_id = reader.GetGuid(0);//student
                }
                dbCon.CloseReader(reader);
            }
            else if (!usertype.Contains("ST"))
            {

                select = "select Student_id from TBL_U_Student where Student_SchoolAgeUser_id_fk = @schoolAgeUser_id_fk;";
                cmd = new SqlCommand(select, con);
                cmd.Parameters.AddWithValue("@schoolAgeUser_id_fk", schoolAgeUser_id);

                reader = await dbCon.ReadData(cmd);
                if (reader.Read())
                {
                    student_id = reader.GetGuid(0);//student
                }
                dbCon.CloseReader(reader);
            }
            else return pfdrm;

            if (student_id == Guid.Empty) return pfdrm;
            
            pfdrm.FeeForMonthList = new List<FeeForMonthModel>();
            pfdrm.GlobalFeeDue = 0;
            DateTime now = DateTime.Now;
            for (int i = 0; i < 12; i++, now = now.AddMonths(-1))
            {
                var startDate = new DateTime(now.Year, now.Month, 1);
                var endDate = startDate.AddMonths(1).AddDays(-1);
                select = "select Fee_Amount, Fee_Due_Date, Fee_Description from TBL_U_Fee where Fee_Student_id_fk = @student_id " +
                    " and Fee_Due_Date >= @startDate and Fee_Due_Date <= @endDate and IsDue = 1";

                cmd = new SqlCommand(select, con);
                cmd.Parameters.AddWithValue("@student_id", student_id);
                cmd.Parameters.AddWithValue("@startDate", startDate);
                cmd.Parameters.AddWithValue("@endDate", endDate);
                reader = await dbCon.ReadData(cmd);
                FeeForMonthModel ffmm = new FeeForMonthModel();
                ffmm.FeeDetailList = new List<FeeDetailModel>();
                ffmm.Month = -1;
                while (reader.Read())
                {
                    ffmm.Month = startDate.Month;
                    decimal amount = reader.GetDecimal(0);
                    pfdrm.GlobalFeeDue += amount;
                    ffmm.FeeDetailList.Add(new FeeDetailModel()
                    { Amount = amount, FeeDetail = reader.GetString(2), FeeDueDate = reader.GetDateTime(1) });
                }
                dbCon.CloseReader(reader);
                if (ffmm.Month != -1)
                    pfdrm.FeeForMonthList.Add(ffmm);
            }
      
            Guid uniqKey = Guid.NewGuid();

            string update = "Update TBL_U_SchoolAgeUser set User_unique_key=@ukey where User_unique_key = @curukey and Username=@user_id";
            cmd = new SqlCommand(update, con);
            cmd.Parameters.AddWithValue("@ukey", uniqKey);
            cmd.Parameters.AddWithValue("@curukey", parentFeeRequest.UniqKey);
            cmd.Parameters.AddWithValue("@user_id", parentFeeRequest.UserId);
            await cmd.ExecuteNonQueryAsync();
            dbCon.CloseDB(con);
            pfdrm.UniqKey = uniqKey;
            pfdrm.RespCode = ResponseCode.OK;
            return pfdrm;
        }

        [System.Web.Http.HttpPost]
        [ActionName("governingbodyparent")]
        public async Task<ParentGoverningBodyResponseModel> GoverningBodyParent([FromBody] ParentGoverningBodyRequestModel parentNoticeRequest)
        {
            ParentGoverningBodyResponseModel pgbrm = new ParentGoverningBodyResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.FORBIDDEN };
            SqlConnection con = await dbCon.ConnectDB();
            string select = "select SchoolAgeUser_id, SchoolAgeUser_UserType_id_fk, SchoolAgeUser_School_id_fk from TBL_U_SchoolAgeUser where User_unique_key = @uniqKey and Username = @userId";
            SqlCommand cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@uniqKey", parentNoticeRequest.UniqKey);
            cmd.Parameters.AddWithValue("@userId", parentNoticeRequest.UserId);
            SqlDataReader reader = await dbCon.ReadData(cmd);
            Guid schoolAgeUser_id = Guid.Empty;
            Guid schoolAgeUser_UserType_id_fk = Guid.Empty;
            Guid schoolAgeUser_School_id_fk = Guid.Empty;
            if (reader.Read())
            {
                schoolAgeUser_id = reader.GetGuid(0);//parent|student
                schoolAgeUser_UserType_id_fk = reader.GetGuid(1);//parent|student
                schoolAgeUser_School_id_fk = reader.GetGuid(2);//parent|student
            }
            dbCon.CloseReader(reader);

            select = "select User_code from TBL_U_UserType where UserType_id = @userType_id;";
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@userType_id", schoolAgeUser_UserType_id_fk);

            string usertype = "";
            reader = await dbCon.ReadData(cmd);
            if (reader.Read())
            {
                usertype = reader[0].ToString();
            }
            dbCon.CloseReader(reader);
            Guid student_id = Guid.Empty;
            if (usertype.Contains("PA"))
            {
                select = "select Parent_Student_id_fk from TBL_U_Parent where Parent_SchoolAgeUser_id_fk = @parent_SchoolAgeUser_id_fk;";
                cmd = new SqlCommand(select, con);
                cmd.Parameters.AddWithValue("@parent_SchoolAgeUser_id_fk", schoolAgeUser_id);
                reader = await dbCon.ReadData(cmd);
                if (reader.Read())
                {
                    student_id = reader.GetGuid(0);//student
                }
                dbCon.CloseReader(reader);
            }
            else if (!usertype.Contains("ST"))
            {

                select = "select Student_id from TBL_U_Student where Student_SchoolAgeUser_id_fk = @schoolAgeUser_id_fk;";
                cmd = new SqlCommand(select, con);
                cmd.Parameters.AddWithValue("@schoolAgeUser_id_fk", schoolAgeUser_id);

                reader = await dbCon.ReadData(cmd);
                if (reader.Read())
                {
                    student_id = reader.GetGuid(0);//student
                }
                dbCon.CloseReader(reader);
            }
            else return pgbrm;

            if (student_id == Guid.Empty) return pgbrm;

            pgbrm.GoverningBodyList = new List<GoverningBodyModel>();

            select = "select TBL_U_GoverningBody.Designation as Designation, " +
                        " TBL_U_GoverningBody.Qualification as Qualification,  " +
                        " TBL_U_GoverningBody.Department as Department, " +
                        " TBL_U_GoverningBody.LastVisited as LastVisited, " +
                        " TBL_U_Person.First_name as First_name,  " +
                        " TBL_U_Person.Middle_name as Middle_name,  " +
                        " TBL_U_Person.Last_name as Last_name,  " +
                        " TBL_U_Person.Address_1 as Address_1,  " +
                        " TBL_U_Person.Address_2 as Address_2,  " +
                        " TBL_U_Person.Address_3 as Address_3, " +
                        " TBL_U_Person.Phone as Phone,  " +
                        " TBL_U_Person.Email as Email, " +
                        " TBL_U_Person.Person_id as PersonId " +
                        " from(TBL_U_GoverningBody inner join TBL_U_Person on TBL_U_GoverningBody.GoverningBody_Person_id_fk = TBL_U_Person.Person_id) " +
                        " where TBL_U_GoverningBody.GoverningBody_School_id_fk = @school_id";

            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@school_id", schoolAgeUser_School_id_fk);
            reader = await dbCon.ReadData(cmd);
            while (reader.Read())
            {
                pgbrm.GoverningBodyList.Add(new GoverningBodyModel() {
                    Designation = reader.IsDBNull(0) ? "" : reader.GetString(0),
                    Qualification = reader.IsDBNull(1) ? "" : reader.GetString(1),
                    Department = reader.IsDBNull(2) ? "" : reader.GetString(2),
                    LastVisited = reader.IsDBNull(3) ? (DateTime?)null : reader.GetDateTime(3),
                    First_name = reader.IsDBNull(4) ? "" : reader.GetString(4),
                    Middle_name = reader.IsDBNull(5) ? "" : reader.GetString(5),
                    Last_name = reader.IsDBNull(6) ? "" : reader.GetString(6),
                    Address_1 = reader.IsDBNull(7) ? "" : reader.GetString(7),
                    Address_2 = reader.IsDBNull(8) ? "" : reader.GetString(8),
                    Address_3 = reader.IsDBNull(9) ? "" : reader.GetString(9),
                    Phone = reader.IsDBNull(10) ? "" : reader.GetString(10),
                    Email = reader.IsDBNull(11) ? "" : reader.GetString(11),
                    PersonId = reader.IsDBNull(12) ? Guid.Empty : reader.GetGuid(12)
                });
            }
            dbCon.CloseReader(reader);

            Guid uniqKey = Guid.NewGuid();

            string update = "Update TBL_U_SchoolAgeUser set User_unique_key=@ukey where User_unique_key = @curukey and Username=@user_id";
            cmd = new SqlCommand(update, con);
            cmd.Parameters.AddWithValue("@ukey", uniqKey);
            cmd.Parameters.AddWithValue("@curukey", parentNoticeRequest.UniqKey);
            cmd.Parameters.AddWithValue("@user_id", parentNoticeRequest.UserId);
            await cmd.ExecuteNonQueryAsync();
            dbCon.CloseDB(con);
            pgbrm.UniqKey = uniqKey;
            pgbrm.RespCode = ResponseCode.OK;
            return pgbrm;
        }

        [System.Web.Http.HttpPost]
        [ActionName("marksparent")]
        public async Task<ParentMarksResponseModel> MarksParent([FromBody] ParentMarksRequestModel parentMarksRequest)
        {
            ParentMarksResponseModel prm = new ParentMarksResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.FORBIDDEN };
            SqlConnection con = await dbCon.ConnectDB();
            string select = "select SchoolAgeUser_id, SchoolAgeUser_UserType_id_fk, SchoolAgeUser_School_id_fk from TBL_U_SchoolAgeUser where User_unique_key = @uniqKey and Username = @userId";
            SqlCommand cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@uniqKey", parentMarksRequest.UniqKey);
            cmd.Parameters.AddWithValue("@userId", parentMarksRequest.UserId);
            SqlDataReader reader = await dbCon.ReadData(cmd);
            Guid schoolAgeUser_id = Guid.Empty;
            Guid schoolAgeUser_UserType_id_fk = Guid.Empty;
            Guid schoolAgeUser_School_id_fk = Guid.Empty;
            if (reader.Read())
            {
                schoolAgeUser_id = reader.GetGuid(0);//parent|student
                schoolAgeUser_UserType_id_fk = reader.GetGuid(1);//parent|student
                schoolAgeUser_School_id_fk = reader.GetGuid(2);//parent|student
            }
            dbCon.CloseReader(reader);

            select = "select User_code from TBL_U_UserType where UserType_id = @userType_id;";
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@userType_id", schoolAgeUser_UserType_id_fk);

            string usertype = "";
            reader = await dbCon.ReadData(cmd);
            if (reader.Read())
            {
                usertype = reader[0].ToString();
            }
            dbCon.CloseReader(reader);
            Guid student_id = Guid.Empty;
            if (usertype.Contains("PA"))
            {
                select = "select Parent_Student_id_fk from TBL_U_Parent where Parent_SchoolAgeUser_id_fk = @parent_SchoolAgeUser_id_fk;";
                cmd = new SqlCommand(select, con);
                cmd.Parameters.AddWithValue("@parent_SchoolAgeUser_id_fk", schoolAgeUser_id);
                reader = await dbCon.ReadData(cmd);
                if (reader.Read())
                {
                    student_id = reader.GetGuid(0);//student
                }
                dbCon.CloseReader(reader);
            }
            else if (!usertype.Contains("ST"))
            {

                select = "select Student_id from TBL_U_Student where Student_SchoolAgeUser_id_fk = @schoolAgeUser_id_fk;";
                cmd = new SqlCommand(select, con);
                cmd.Parameters.AddWithValue("@schoolAgeUser_id_fk", schoolAgeUser_id);

                reader = await dbCon.ReadData(cmd);
                if (reader.Read())
                {
                    student_id = reader.GetGuid(0);//student
                }
                dbCon.CloseReader(reader);
            }
            else return prm;

            if (student_id == Guid.Empty) return prm;
            
            prm.ExamMarksList = new List<ExamMarksModel>();

            select = "select ExamName, Class, Remark, Rank, Marks, TotalMarks, HighestMarks, TBL_U_Subject.Subject_code as code, " +
                        " TBL_U_Subject.Description as subject " +
                        " from(TBL_U_Marks inner join TBL_U_Subject on TBL_U_Marks.Marks_Subject_id_fk = TBL_U_Subject.Subject_id) " +
                        " where TBL_U_Marks.Marks_Student_id_fk = @student_id  ORDER BY Class DESC, ExamName, subject";

            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@student_id", student_id);
            reader = await dbCon.ReadData(cmd);
            ExamMarksModel examMarks = null;
            while (reader.Read())
            {
                string ExamName = reader.IsDBNull(0) ? "" : reader.GetString(0);
                string Class = reader.IsDBNull(1) ? "" : reader.GetString(1);
                string Remark = reader.IsDBNull(2) ? "" : reader.GetString(2);
                string Rank = reader.GetString(3);
                if (ExamName.Length <= 0 || Class.Length <= 0)
                    continue;
                if (null == examMarks)
                {
                    examMarks = new ExamMarksModel();
                    examMarks.SubjectMarksList = new List<SubjectMarksModel>();
                    examMarks.ExamName = ExamName;
                    examMarks.Class = Class;
                    examMarks.Remark = Remark;
                    examMarks.Rank = Rank;
                }
                else if (Class != examMarks.Class || ExamName != examMarks.ExamName)
                {
                    prm.ExamMarksList.Add(examMarks);
                    examMarks = new ExamMarksModel();
                    examMarks.SubjectMarksList = new List<SubjectMarksModel>();
                    examMarks.ExamName = ExamName;
                    examMarks.Class = Class;
                    examMarks.Remark = Remark;
                    examMarks.Rank = Rank;
                }
                examMarks.SubjectMarksList.Add(new SubjectMarksModel()
                {
                    Marks = reader.GetDecimal(4),
                    TotalMarks = reader.GetDecimal(5),
                    HighestMarks = reader.GetDecimal(6),
                    Subject = reader.IsDBNull(8) ? "" : reader.GetString(8)
                });
            }
            dbCon.CloseReader(reader);
            if(null != examMarks && !prm.ExamMarksList.Contains(examMarks))
                prm.ExamMarksList.Add(examMarks);

            Guid uniqKey = Guid.NewGuid();

            string update = "Update TBL_U_SchoolAgeUser set User_unique_key=@ukey where User_unique_key = @curukey and Username=@user_id";
            cmd = new SqlCommand(update, con);
            cmd.Parameters.AddWithValue("@ukey", uniqKey);
            cmd.Parameters.AddWithValue("@curukey", parentMarksRequest.UniqKey);
            cmd.Parameters.AddWithValue("@user_id", parentMarksRequest.UserId);
            await cmd.ExecuteNonQueryAsync();
            dbCon.CloseDB(con);
            prm.UniqKey = uniqKey;
            prm.RespCode = ResponseCode.OK;
            return prm;
        }

        [System.Web.Http.HttpPost]
        [ActionName("facultybodyparent")]
        public async Task<ParentFacultyBodyResponseModel> FacultyBodyParent([FromBody] ParentFacultyBodyRequestModel parentfacultyRequest)
        {
            ParentFacultyBodyResponseModel pfbrm = new ParentFacultyBodyResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.FORBIDDEN };
            SqlConnection con = await dbCon.ConnectDB();
            string select = "select SchoolAgeUser_id, SchoolAgeUser_UserType_id_fk, SchoolAgeUser_School_id_fk from TBL_U_SchoolAgeUser where User_unique_key = @uniqKey and Username = @userId";
            SqlCommand cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@uniqKey", parentfacultyRequest.UniqKey);
            cmd.Parameters.AddWithValue("@userId", parentfacultyRequest.UserId);
            SqlDataReader reader = await dbCon.ReadData(cmd);
            Guid schoolAgeUser_id = Guid.Empty;
            Guid schoolAgeUser_UserType_id_fk = Guid.Empty;
            Guid schoolAgeUser_School_id_fk = Guid.Empty;
            if (reader.Read())
            {
                schoolAgeUser_id = reader.GetGuid(0);//parent|student
                schoolAgeUser_UserType_id_fk = reader.GetGuid(1);//parent|student
                schoolAgeUser_School_id_fk = reader.GetGuid(2);//parent|student
            }
            dbCon.CloseReader(reader);

            select = "select User_code from TBL_U_UserType where UserType_id = @userType_id;";
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@userType_id", schoolAgeUser_UserType_id_fk);

            string usertype = "";
            reader = await dbCon.ReadData(cmd);
            if (reader.Read())
            {
                usertype = reader[0].ToString();
            }
            dbCon.CloseReader(reader);
            Guid student_id = Guid.Empty;
            if (usertype.Contains("PA"))
            {
                select = "select Parent_Student_id_fk from TBL_U_Parent where Parent_SchoolAgeUser_id_fk = @parent_SchoolAgeUser_id_fk;";
                cmd = new SqlCommand(select, con);
                cmd.Parameters.AddWithValue("@parent_SchoolAgeUser_id_fk", schoolAgeUser_id);
                reader = await dbCon.ReadData(cmd);
                if (reader.Read())
                {
                    student_id = reader.GetGuid(0);//student
                }
                dbCon.CloseReader(reader);
            }
            else if (!usertype.Contains("ST"))
            {

                select = "select Student_id from TBL_U_Student where Student_SchoolAgeUser_id_fk = @schoolAgeUser_id_fk;";
                cmd = new SqlCommand(select, con);
                cmd.Parameters.AddWithValue("@schoolAgeUser_id_fk", schoolAgeUser_id);

                reader = await dbCon.ReadData(cmd);
                if (reader.Read())
                {
                    student_id = reader.GetGuid(0);//student
                }
                dbCon.CloseReader(reader);
            }
            else return pfbrm;

            if (student_id == Guid.Empty) return pfbrm;

            pfbrm.FacultyBodyList = new List<FacultyBodyModel>();

            select = "select "+
                        " TBL_U_Teacher.Designation as Designation, " +
                        " TBL_U_Teacher.Qualification as Qualification, " +
                        " TBL_U_Teacher.Department as Department, " +
                        " TBL_U_Teacher.LastVisited as LastVisited, " +
                        " TBL_U_Person.First_name as First_name, " +
                        " TBL_U_Person.Middle_name as Middle_name, " +
                        " TBL_U_Person.Last_name as Last_name, " +
                        " TBL_U_Person.Address_1 as Address_1, " +
                        " TBL_U_Person.Address_2 as Address_2, " +
                        " TBL_U_Person.Address_3 as Address_3, " +
                        " TBL_U_Person.Phone as Phone, " +
                        " TBL_U_Person.Email as Email, " +
                        " TBL_U_Person.Person_id as PersonId " +
                        " from(TBL_U_Teacher " +
                        " inner join TBL_U_SchoolAgeUser on TBL_U_Teacher.Teacher_SchoolAgeUser_id_fk = TBL_U_SchoolAgeUser.SchoolAgeUser_id " +
                        " inner join TBL_U_Person on TBL_U_SchoolAgeUser.SchoolAgeUser_Person_id_fk = TBL_U_Person.Person_id) " +
                        " where TBL_U_SchoolAgeUser.SchoolAgeUser_School_id_fk = @school_id";

            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@school_id", schoolAgeUser_School_id_fk);
            reader = await dbCon.ReadData(cmd);
            while (reader.Read())
            {
                pfbrm.FacultyBodyList.Add(new FacultyBodyModel()
                {
                    Designation = reader.IsDBNull(0) ? "" : reader.GetString(0),
                    Qualification = reader.IsDBNull(1) ? "" : reader.GetString(1),
                    Department = reader.IsDBNull(2) ? "" : reader.GetString(2),
                    LastVisited = reader.IsDBNull(3) ? (DateTime?)null : reader.GetDateTime(3),
                    First_name = reader.IsDBNull(4) ? "" : reader.GetString(4),
                    Middle_name = reader.IsDBNull(5) ? "" : reader.GetString(5),
                    Last_name = reader.IsDBNull(6) ? "" : reader.GetString(6),
                    Address_1 = reader.IsDBNull(7) ? "" : reader.GetString(7),
                    Address_2 = reader.IsDBNull(8) ? "" : reader.GetString(8),
                    Address_3 = reader.IsDBNull(9) ? "" : reader.GetString(9),
                    Phone = reader.IsDBNull(10) ? "" : reader.GetString(10),
                    Email = reader.IsDBNull(11) ? "" : reader.GetString(11),
                    PersonId = reader.IsDBNull(12) ? Guid.Empty : reader.GetGuid(12)
                });
            }
            dbCon.CloseReader(reader);

            Guid uniqKey = Guid.NewGuid();

            string update = "Update TBL_U_SchoolAgeUser set User_unique_key=@ukey where User_unique_key = @curukey and Username=@user_id";
            cmd = new SqlCommand(update, con);
            cmd.Parameters.AddWithValue("@ukey", uniqKey);
            cmd.Parameters.AddWithValue("@curukey", parentfacultyRequest.UniqKey);
            cmd.Parameters.AddWithValue("@user_id", parentfacultyRequest.UserId);
            await cmd.ExecuteNonQueryAsync();
            dbCon.CloseDB(con);
            pfbrm.UniqKey = uniqKey;
            pfbrm.RespCode = ResponseCode.OK;
            return pfbrm;
        }


        [System.Web.Http.HttpPost]
        [ActionName("studentlistteacher")]
        public async Task<StudentListResponseModel> StudentListTeacher([FromBody] TeacherStudentListRequestModel teacherStudentListRequest)
        {
            StudentListResponseModel slrm = new StudentListResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.FORBIDDEN };
            SqlConnection con = await dbCon.ConnectDB();
            string select = "select SchoolAgeUser_id, SchoolAgeUser_UserType_id_fk from TBL_U_SchoolAgeUser where User_unique_key = @uniqKey and Username = @userId";
            SqlCommand cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@uniqKey", teacherStudentListRequest.UniqKey);
            cmd.Parameters.AddWithValue("@userId", teacherStudentListRequest.UserId);
            SqlDataReader reader = await dbCon.ReadData(cmd);
            Guid schoolAgeUser_id = Guid.Empty;
            Guid schoolAgeUser_UserType_id_fk = Guid.Empty;
            if (reader.Read())
            {
                schoolAgeUser_id = reader.GetGuid(0);//teacher
                schoolAgeUser_UserType_id_fk = reader.GetGuid(1);//teacher
            }
            dbCon.CloseReader(reader);

            select = "select User_code from TBL_U_UserType where UserType_id = @userType_id;";
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@userType_id", schoolAgeUser_UserType_id_fk);

            string usertype = "";
            reader = await dbCon.ReadData(cmd);
            if (reader.Read())
            {
                usertype = reader[0].ToString();
            }
            dbCon.CloseReader(reader);

            if (!usertype.Contains("TE"))
                return slrm;

            select = "select Teacher_id from TBL_U_Teacher where Teacher_SchoolAgeUser_id_fk = @schoolAgeUser_id;";
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@schoolAgeUser_id", schoolAgeUser_id);
            Guid teacher_id = Guid.Empty;
            reader = await dbCon.ReadData(cmd);
            if (reader.Read())
            {
                teacher_id = reader.GetGuid(0);
            }
            dbCon.CloseReader(reader);

            if(teacherStudentListRequest.Class == null || teacherStudentListRequest.Class.Length <= 0)
            {
                select = "select Class, Section from TBL_U_Teacher where Teacher_id = @teacher_id;";
                cmd = new SqlCommand(select, con);
                cmd.Parameters.AddWithValue("@teacher_id", teacher_id);
                
                reader = await dbCon.ReadData(cmd);
                if (reader.Read())
                {
                    teacherStudentListRequest.Class = reader.GetString(0);
                    teacherStudentListRequest.Section = reader.GetString(1);
                }
                dbCon.CloseReader(reader);
            }
            select = "select TBL_U_Student.Student_id as Student_id, TBL_U_Student.Student_IC as Student_IC, TBL_U_Student.Class as Class, TBL_U_Student.Section as Section, " +
                        " TBL_U_Student.Roll as Roll, TBL_U_Student.Position as Position, TBL_U_Student.Student_SchoolAgeUser_id_fk as Student_SchoolAgeUser_id, " +
                        " TBL_U_Person.Person_id as Person_id, TBL_U_Person.Last_name as Last_name, TBL_U_Person.Middle_name as Middle_name,  " +
                        " TBL_U_Person.First_name as First_name " +
                        " from((TBL_U_Student INNER JOIN TBL_U_SchoolAgeUser on TBL_U_Student.Student_SchoolAgeUser_id_fk = TBL_U_SchoolAgeUser.SchoolAgeUser_id) " +
                        " INNER JOIN TBL_U_Person on TBL_U_Person.Person_id = TBL_U_SchoolAgeUser.SchoolAgeUser_Person_id_fk " +
                        " ) " +
                        " where TBL_U_Student.Class = @class and TBL_U_Student.Section = @section ORDER BY Roll ASC";
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@class", teacherStudentListRequest.Class);
            cmd.Parameters.AddWithValue("@section", teacherStudentListRequest.Section);
            reader = await dbCon.ReadData(cmd);
            IList<StudentInfoModel> studentInfoList = new List<StudentInfoModel>();
            IList<int> studentRollList = new List<int>();
            while (reader.Read())
            {
                Guid studentID = reader.GetGuid(0);
                int studentIC = reader.GetInt32(1);
                string classs = reader.GetString(2);
                string section = reader.GetString(3);
                int roll = reader.GetInt32(4);
                int position = reader.GetInt32(5);
                Guid personId = reader.GetGuid(7);
                string lastname = reader.GetString(8);
                string middlename = reader.GetString(9);
                string firstname = reader.GetString(10);
                studentRollList.Add(roll);
                
                studentInfoList.Add(new StudentInfoModel() {
                    StudentLName = lastname,
                    StudentMName = middlename,
                    StudentFName = firstname,
                    Class = classs,
                    Section = section,
                    RollNo = roll,
                    StudentIC = studentIC,
                    Position = position,
                    StudentID = studentID,
                    PersonId = personId
                });
            }
            dbCon.CloseReader(reader);

            Guid uniqKey = Guid.NewGuid();
            string update = "Update TBL_U_SchoolAgeUser set User_unique_key=@ukey where User_unique_key = @curukey and Username = @user_id";
            cmd = new SqlCommand(update, con);
            cmd.Parameters.AddWithValue("@ukey", uniqKey);
            cmd.Parameters.AddWithValue("@curukey", teacherStudentListRequest.UniqKey);
            cmd.Parameters.AddWithValue("@user_id", teacherStudentListRequest.UserId);
            await cmd.ExecuteNonQueryAsync();
            dbCon.CloseDB(con);
            slrm.UniqKey = uniqKey;
            slrm.RespCode = ResponseCode.OK;
            slrm.StudentInfoList = studentInfoList;
            slrm.StudentRollList = studentRollList;
            return slrm;
        }


        [System.Web.Http.HttpPost]
        [ActionName("markattendanceteacher")]
        public async Task<AttendanceResponseModel> MarkAttendanceTeacher([FromBody] StudentAttendanceListRequestModel markAttendanceRequest)
        {
            AttendanceResponseModel arm = new AttendanceResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.FORBIDDEN };
            SqlConnection con = await dbCon.ConnectDB();
            string select = "select SchoolAgeUser_id, SchoolAgeUser_UserType_id_fk from TBL_U_SchoolAgeUser where User_unique_key = @uniqKey and Username = @userId";
            SqlCommand cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@uniqKey", markAttendanceRequest.UniqKey);
            cmd.Parameters.AddWithValue("@userId", markAttendanceRequest.UserId);
            SqlDataReader reader = await dbCon.ReadData(cmd);
            Guid schoolAgeUser_id = Guid.Empty;
            Guid schoolAgeUser_UserType_id_fk = Guid.Empty;
            if (reader.Read())
            {
                schoolAgeUser_id = reader.GetGuid(0);//teacher
                schoolAgeUser_UserType_id_fk = reader.GetGuid(1);//teacher
            }
            dbCon.CloseReader(reader);

            select = "select User_code from TBL_U_UserType where UserType_id = @userType_id;";
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@userType_id", schoolAgeUser_UserType_id_fk);

            string usertype = "";
            reader = await dbCon.ReadData(cmd);
            if (reader.Read())
            {
                usertype = reader[0].ToString();
            }
            dbCon.CloseReader(reader);

            if (!usertype.Contains("TE"))
                return arm;

            foreach (StudentAttendanceDetail item in markAttendanceRequest.StudentAttendanceList)
            {
                DateTime result = new DateTime(item.AttendanceDate.Year, item.AttendanceDate.Month, item.AttendanceDate.Day, 0, 0, 0);
                select = "select * from TBL_U_Attendance " +
                                " where Attendance_Student_id_fk = @student_id1 and Attendance_date = @attendance_date1 ";
                cmd = new SqlCommand(select, con);
                cmd.Parameters.AddWithValue("@student_id1", item.StudentId);
                cmd.Parameters.AddWithValue("@attendance_date1", result);
                reader = await dbCon.ReadData(cmd);
                bool upd = false;
                if (reader.Read())
                {
                    upd = true;
                }
                dbCon.CloseReader(reader);
                string sql = "";
                if(upd == true)
                {
                    sql = "update TBL_U_Attendance set Remark = @remark " +
                                " where Attendance_Student_id_fk = @student_id and Attendance_date = @attendance_date"; 
                }
                else
                {
                    sql = "insert into TBL_U_Attendance (Attendance_Student_id_fk, Attendance_date, Remark) values " +
                                    " (@student_id, @attendance_date, @remark) ";
                }

                /*string insert = "begin tran "+
                                "    if exists( " +
                                "      select * " +
                                "        from TBL_U_Attendance with (updlock, serializable) " +
                                "        where Attendance_Student_id_fk = @student_id1 and Attendance_date = @attendance_date1 " +
                                "      ) " +
                                "    begin " +
                                "        update TBL_U_Attendance " +
                                "          set Remark = @remark1 " +
                                "         where Attendance_Student_id_fk = @student_id2 and Attendance_date = @attendance_date2 " +
                                "    end " +
                                "    else " +
                                "      begin " +
                                "        insert into TBL_U_Attendance (Attendance_Student_id_fk, Attendance_date, Remark) values " +
                                "        (@student_id3, @attendance_date3, @remark) " +
                                "      end; " +
                                " commit tran";*/
                //string insert = "insert into TBL_U_Attendance (Attendance_Student_id_fk, Attendance_date, Remark) values " +
                //    "(@student_id, @attendance_date, @remark)";


                /*cmd = new SqlCommand(insert, con);
                cmd.Parameters.AddWithValue("@student_id1", item.StudentId);
                cmd.Parameters.AddWithValue("@attendance_date1", result);
                cmd.Parameters.AddWithValue("@remark1", item.Remark);
                cmd.Parameters.AddWithValue("@student_id2", item.StudentId);
                cmd.Parameters.AddWithValue("@attendance_date2", result);
                cmd.Parameters.AddWithValue("@student_id3", item.StudentId);
                cmd.Parameters.AddWithValue("@attendance_date3", result);
                cmd.Parameters.AddWithValue("@remark", item.Remark);*/
                cmd = new SqlCommand(sql, con);
                cmd.Parameters.AddWithValue("@student_id", item.StudentId);
                cmd.Parameters.AddWithValue("@attendance_date", result);
                cmd.Parameters.AddWithValue("@remark", item.Remark);
                await cmd.ExecuteNonQueryAsync();
            }

            Guid uniqKey = Guid.NewGuid();
            string update = "Update TBL_U_SchoolAgeUser set User_unique_key=@ukey where User_unique_key = @curukey and Username = @user_id";
            cmd = new SqlCommand(update, con);
            cmd.Parameters.AddWithValue("@ukey", uniqKey);
            cmd.Parameters.AddWithValue("@curukey", markAttendanceRequest.UniqKey);
            cmd.Parameters.AddWithValue("@user_id", markAttendanceRequest.UserId);
            await cmd.ExecuteNonQueryAsync();
            dbCon.CloseDB(con);
            arm.UniqKey = uniqKey;
            arm.RespCode = ResponseCode.OK;
            return arm;
        }


        [System.Web.Http.HttpPost]
        [ActionName("loadattendanceteacher")]
        public async Task<AttendanceListResponseModel> LoadAttendanceTeacher([FromBody] AttendanceListForRequestModel attendanceListRequest)
        {
            AttendanceListResponseModel alrm = new AttendanceListResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.FORBIDDEN };
            SqlConnection con = await dbCon.ConnectDB();
            string select = "select SchoolAgeUser_id, SchoolAgeUser_UserType_id_fk from TBL_U_SchoolAgeUser where User_unique_key = @uniqKey and Username = @userId";
            SqlCommand cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@uniqKey", attendanceListRequest.UniqKey);
            cmd.Parameters.AddWithValue("@userId", attendanceListRequest.UserId);
            SqlDataReader reader = await dbCon.ReadData(cmd);
            Guid schoolAgeUser_id = Guid.Empty;
            Guid schoolAgeUser_UserType_id_fk = Guid.Empty;
            if (reader.Read())
            {
                schoolAgeUser_id = reader.GetGuid(0);//teacher
                schoolAgeUser_UserType_id_fk = reader.GetGuid(1);//teacher
            }
            dbCon.CloseReader(reader);

            select = "select User_code from TBL_U_UserType where UserType_id = @userType_id";
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@userType_id", schoolAgeUser_UserType_id_fk);

            string usertype = "";
            reader = await dbCon.ReadData(cmd);
            if (reader.Read())
            {
                usertype = reader[0].ToString();
            }
            dbCon.CloseReader(reader);
            
            if (!usertype.Contains("TE"))
                return alrm;
            alrm.RespCode = ResponseCode.INTERNAL_SERVER_ERROR;
            select = "select TBL_U_Student.Student_id as Student_id, " +
                        " TBL_U_Student.Student_IC as Student_IC, " +
                        " TBL_U_Student.Class as Class, " +
                        " TBL_U_Student.Section as Section, " +
                        " TBL_U_Student.Roll as Roll, TBL_U_Student.Position as Position, TBL_U_Student.Student_SchoolAgeUser_id_fk as Student_SchoolAgeUser_id, " +
                        " TBL_U_Person.Person_id as Person_id, TBL_U_Person.Last_name as Last_name, TBL_U_Person.Middle_name as Middle_name,  " +
                        " TBL_U_Person.First_name as First_name, " +
                        " TBL_U_Attendance.Remark as Remark " +
                        " from ( TBL_U_Student left outer join TBL_U_Attendance on TBL_U_Student.Student_id = TBL_U_Attendance.Attendance_Student_id_fk and TBL_U_Attendance.Attendance_date = @attendanceDate " +
                        " INNER JOIN TBL_U_SchoolAgeUser on TBL_U_Student.Student_SchoolAgeUser_id_fk = TBL_U_SchoolAgeUser.SchoolAgeUser_id " +
                        " INNER JOIN TBL_U_Person on TBL_U_Person.Person_id = TBL_U_SchoolAgeUser.SchoolAgeUser_Person_id_fk " +
                        " ) " +
                        " where TBL_U_Student.Class = @class and TBL_U_Student.Section = @section ORDER BY Roll ASC, Remark";
            cmd = new SqlCommand(select, con);
            DateTime result = new DateTime(attendanceListRequest.AttendanceDate.Year, attendanceListRequest.AttendanceDate.Month, attendanceListRequest.AttendanceDate.Day, 0, 0, 0);
            cmd.Parameters.AddWithValue("@attendanceDate", result);
            cmd.Parameters.AddWithValue("@class", attendanceListRequest.Class);
            cmd.Parameters.AddWithValue("@section", attendanceListRequest.Section);
            
            reader = await dbCon.ReadData(cmd);
            IList<AttendanceList> studentInfoList = new List<AttendanceList>();
            IList<int> studentRollList = new List<int>();

            while (reader.Read())
            {
                
                Guid studentID = reader.GetGuid(0);
                int studentIC = reader.GetInt32(1);
                string classs = reader.GetString(2);
                string section = reader.GetString(3);
                int roll = reader.GetInt32(4);
                int position = reader.GetInt32(5);
                Guid personId = reader.GetGuid(7);
                string lastname = reader.GetString(8);
                string middlename = reader.GetString(9);
                string firstname = reader.GetString(10);
                string remark = reader.IsDBNull(11)?"A":reader.GetString(11);
                studentRollList.Add(roll);
                
                studentInfoList.Add(new AttendanceList()
                {
                    StudentLName = lastname,
                    StudentMName = middlename,
                    StudentFName = firstname,
                    Class = classs,
                    Section = section,
                    RollNo = roll,
                    StudentIC = studentIC,
                    Position = position,
                    StudentID = studentID,
                    PersonId = personId,
                    Remark = remark
                });
            }

            dbCon.CloseReader(reader);
            alrm.StudentInfoList = studentInfoList;
            alrm.StudentRollList = studentRollList;
            
            Guid uniqKey = Guid.NewGuid();
            string update = "Update TBL_U_SchoolAgeUser set User_unique_key=@ukey where User_unique_key = @curukey and Username = @user_id";
            cmd = new SqlCommand(update, con);
            cmd.Parameters.AddWithValue("@ukey", uniqKey);
            cmd.Parameters.AddWithValue("@curukey", attendanceListRequest.UniqKey);
            cmd.Parameters.AddWithValue("@user_id", attendanceListRequest.UserId);
            await cmd.ExecuteNonQueryAsync();
            dbCon.CloseDB(con);
            alrm.UniqKey = uniqKey;
            alrm.RespCode = ResponseCode.OK;
            return alrm;
        }

        [System.Web.Http.HttpPost]
        [ActionName("givetasknoticeteacher")]
        public async Task<GiveTaskNoticeResponseModel> GiveTaskNoticeTeacher([FromBody] GiveTaskNoticeRequestModel giveTaskNoticeRequest)
        {
            GiveTaskNoticeResponseModel arm = new GiveTaskNoticeResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.FORBIDDEN };
            SqlConnection con = await dbCon.ConnectDB();
            string select = "select SchoolAgeUser_id, SchoolAgeUser_UserType_id_fk, SchoolAgeUser_School_id_fk from TBL_U_SchoolAgeUser where User_unique_key = @uniqKey and Username = @userId";
            SqlCommand cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@uniqKey", giveTaskNoticeRequest.UniqKey);
            cmd.Parameters.AddWithValue("@userId", giveTaskNoticeRequest.UserId);
            SqlDataReader reader = await dbCon.ReadData(cmd);
            Guid schoolAgeUser_id = Guid.Empty;
            Guid schoolAgeUser_UserType_id_fk = Guid.Empty;
            Guid school_id = Guid.Empty;
            if (reader.Read())
            {
                schoolAgeUser_id = reader.GetGuid(0);//teacher
                schoolAgeUser_UserType_id_fk = reader.GetGuid(1);//teacher
                school_id = reader.GetGuid(2);//teacher
            }
            dbCon.CloseReader(reader);

            select = "select User_code from TBL_U_UserType where UserType_id = @userType_id;";
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@userType_id", schoolAgeUser_UserType_id_fk);

            string usertype = "";
            reader = await dbCon.ReadData(cmd);
            if (reader.Read())
            {
                usertype = reader[0].ToString();
            }
            dbCon.CloseReader(reader);

            if (!usertype.Contains("TE"))
                return arm;

            select = "select Teacher_id, Class, Section from TBL_U_Teacher where Teacher_SchoolAgeUser_id_fk = @schoolAgeUser_id";
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@schoolAgeUser_id", schoolAgeUser_id);
            Guid teacher_id = Guid.Empty;
            string teacherClass = "";
            string teacherSection = "";
            reader = await dbCon.ReadData(cmd);
            if (reader.Read())
            {
                teacher_id = reader.GetGuid(0);
                teacherClass = reader.GetString(1);
                teacherSection = reader.GetString(2);
            }
            dbCon.CloseReader(reader);
            bool allStudent = giveTaskNoticeRequest.All;

            select = "select Class_id from TBL_U_Class where Class = @class and Section = @section and Class_School_id_fk=@school_id";
            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@class", giveTaskNoticeRequest.Class);
            cmd.Parameters.AddWithValue("@section", giveTaskNoticeRequest.Section);
            cmd.Parameters.AddWithValue("@school_id", school_id);

            Guid class_id = Guid.Empty;
            reader = await dbCon.ReadData(cmd);
            if (reader.Read())
            {
                class_id = reader.GetGuid(0);
            }
            dbCon.CloseReader(reader);

            foreach (Guid item in giveTaskNoticeRequest.StudentIdList)
            {
                string insert = "insert into TBL_U_Assignment (Assignment_Student_id_fk, Assignment_date, Assignment_due_date, AssignmentTxt, TeacherRemark, Assignment_Teacher_id_fk, Assignment_Class_id_fk) values " +
                    "(@student_id, GetDate(), @assignmentDueDate, @assignmentTxt, @teacherRemark, @teacher_id, @class_id)";

                cmd = new SqlCommand(insert, con);
                cmd.Parameters.AddWithValue("@student_id", item);
                cmd.Parameters.AddWithValue("@assignmentDueDate", giveTaskNoticeRequest.DueDate);
                cmd.Parameters.AddWithValue("@assignmentTxt", giveTaskNoticeRequest.AssignmentTxt);
                cmd.Parameters.AddWithValue("@teacherRemark", giveTaskNoticeRequest.TeacherRemark);
                cmd.Parameters.AddWithValue("@teacher_id", teacher_id);
                cmd.Parameters.AddWithValue("@class_id", class_id);
                await cmd.ExecuteNonQueryAsync();
            }

            Guid uniqKey = Guid.NewGuid();
            string update = "Update TBL_U_SchoolAgeUser set User_unique_key=@ukey where User_unique_key = @curukey and Username = @user_id";
            cmd = new SqlCommand(update, con);
            cmd.Parameters.AddWithValue("@ukey", uniqKey);
            cmd.Parameters.AddWithValue("@curukey", giveTaskNoticeRequest.UniqKey);
            cmd.Parameters.AddWithValue("@user_id", giveTaskNoticeRequest.UserId);
            await cmd.ExecuteNonQueryAsync();
            dbCon.CloseDB(con);
            arm.UniqKey = uniqKey;
            arm.RespCode = ResponseCode.OK;
            return arm;
        }

        [ActionName("trackbusparent")]
        public async Task<TrackLocationResponseModel> TrackBusParent([FromBody] TrackLocationRequestModel trackLocationRequest)
        {
            TrackLocationResponseModel arm = new TrackLocationResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.FORBIDDEN };
            SqlConnection con = await dbCon.ConnectDB();
            string select = "select SchoolAgeUser_id from TBL_U_SchoolAgeUser where User_unique_key = @uniqKey and Username = @userId";
            SqlCommand cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@uniqKey", trackLocationRequest.UniqKey);
            cmd.Parameters.AddWithValue("@userId", trackLocationRequest.UserId);
            SqlDataReader reader = await dbCon.ReadData(cmd);
            Guid schoolAgeUser_id = Guid.Empty;
            Guid schoolAgeUser_UserType_id_fk = Guid.Empty;
            if (reader.Read())
            {
                schoolAgeUser_id = reader.IsDBNull(0) ? Guid.Empty: reader.GetGuid(0);
            }
            dbCon.CloseReader(reader);
            if (!schoolAgeUser_id.Equals(Guid.Empty))
            {
                select = "select Latitude_str, Longitude_str from TBL_U_Driver where Driver_Route_id_fk = @route_id";
                cmd = new SqlCommand(select, con);
                cmd.Parameters.AddWithValue("@route_id", trackLocationRequest.RouteId);
                reader = await dbCon.ReadData(cmd);
                if (reader.Read())
                {
                    arm.Latitude = reader.IsDBNull(0)?"": reader.GetString(0);
                    arm.Longitude = reader.IsDBNull(1) ? "" : reader.GetString(1);
                }
                dbCon.CloseReader(reader);
                arm.UniqKey = trackLocationRequest.UniqKey;
                arm.RespCode = ResponseCode.OK;
            }
            dbCon.CloseDB(con);
            return arm;
        }

        [System.Web.Http.HttpPost]
        [ActionName("locationdriver")]
        public async Task<UpdateLocationResponseModel> UpdateLocationDriver([FromBody] UpdateLocationRequestModel updateLocationRequest)
        {
            UpdateLocationResponseModel arm = new UpdateLocationResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.FORBIDDEN };
            SqlConnection con = await dbCon.ConnectDB();
            string select = "select SchoolAgeUser_id from TBL_U_SchoolAgeUser where User_unique_key = @uniqKey and Username = @userId";
            SqlCommand cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@uniqKey", updateLocationRequest.UniqKey);
            cmd.Parameters.AddWithValue("@userId", updateLocationRequest.UserId);
            SqlDataReader reader = await dbCon.ReadData(cmd);
            Guid schoolAgeUser_id = Guid.Empty;
            if (reader.Read())
            {
                schoolAgeUser_id = (reader.IsDBNull(0)? Guid.Empty: reader.GetGuid(0));
            }
            dbCon.CloseReader(reader);
            if (!schoolAgeUser_id.Equals(Guid.Empty))
            {
                string update = "Update TBL_U_Driver set Latitude_str = @latitude, Longitude_str = @longitude where Driver_SchoolAgeUser_id_fk = @schoolageuser_id";
                cmd = new SqlCommand(update, con);
                cmd.Parameters.AddWithValue("@latitude", updateLocationRequest.Latitude);
                cmd.Parameters.AddWithValue("@longitude", updateLocationRequest.Longitude);
                cmd.Parameters.AddWithValue("@schoolageuser_id", schoolAgeUser_id);
                await cmd.ExecuteNonQueryAsync();
                arm.UniqKey = updateLocationRequest.UniqKey;
                arm.RespCode = ResponseCode.OK;
            }
            dbCon.CloseDB(con);
            
            return arm;
        }

        [System.Web.Http.HttpPost]
        [ActionName("schoolinfo")]
        public async Task<SchoolInfoResponseModel> SchoolInfo([FromBody] SchoolInfoRequestModel schoolInfoRequest)
        {
            SchoolInfoResponseModel sirm = new SchoolInfoResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.FORBIDDEN };
            SqlConnection con = await dbCon.ConnectDB();
            string select = "select SchoolAgeUser_id, SchoolAgeUser_UserType_id_fk, SchoolAgeUser_School_id_fk from TBL_U_SchoolAgeUser where User_unique_key = @uniqKey and Username = @userId";
            SqlCommand cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@uniqKey", schoolInfoRequest.UniqKey);
            cmd.Parameters.AddWithValue("@userId", schoolInfoRequest.UserId);
            SqlDataReader reader = await dbCon.ReadData(cmd);
            Guid schoolAgeUser_id = Guid.Empty;
            Guid schoolAgeUser_UserType_id_fk = Guid.Empty;
            Guid schoolAgeUser_School_id_fk = Guid.Empty;
            if (reader.Read())
            {
                schoolAgeUser_id = reader.GetGuid(0);//parent|student
                schoolAgeUser_UserType_id_fk = reader.GetGuid(1);//parent|student
                schoolAgeUser_School_id_fk = reader.GetGuid(2);//parent|student
            }
            dbCon.CloseReader(reader);
            sirm.ClassList = new List<ClassDetailModel>();

            select = "select TBL_U_Class.Class_id as ClassID, TBL_U_Class.Class as Class, TBL_U_Class.Section as Section, " +
                " TBL_U_Exams.Exams_id as ExamsID, TBL_U_Exams.Exam_Name as Exam_Name, TBL_U_Exams.Exam_Description as Exam_Description " +
                " from(TBL_U_Class left outer join TBL_U_Exams on TBL_U_Class.Class_id = TBL_U_Exams.Exams_Class_id_fk)" +
                " where TBL_U_Class.Class_School_id_fk = @school_id ORDER BY Class ASC, Section, Exam_Name";

            cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@school_id", schoolAgeUser_School_id_fk);
            reader = await dbCon.ReadData(cmd);
            ClassDetailModel classDetails = null;
            while (reader.Read())
            {
                //Guid ClassId = reader.GetGuid(0);
                string Class = reader.IsDBNull(1) ? "" : reader.GetString(1);
                string Section = reader.IsDBNull(2) ? "" : reader.GetString(2);
                //Guid ExamId = reader.GetGuid(3);
                string ExamName = reader.IsDBNull(4) ? "" : reader.GetString(4);
                string ExamDesc = reader.IsDBNull(5) ? "" : reader.GetString(5);

                if (Class.Length <= 0 || Class.Length <= 0)
                    continue;
                if (null == classDetails)
                {
                    classDetails = new ClassDetailModel();
                    classDetails.Class = Class;
                    classDetails.SectionList = new List<string>();
                    classDetails.ExamList = new List<ExamDetailModel>();
                }
                else if (Class != classDetails.Class)
                {
                    sirm.ClassList.Add(classDetails);
                    classDetails = new ClassDetailModel();
                    classDetails.Class = Class;
                    classDetails.SectionList = new List<string>();
                    classDetails.ExamList = new List<ExamDetailModel>();
                }
                if (!classDetails.SectionList.Contains(Section))
                    classDetails.SectionList.Add(Section);

                ExamDetailModel edm = new ExamDetailModel()
                      { ExamName = ExamName, ExamDescription = ExamDesc };
                if (!classDetails.ExamList.Contains(edm))
                    classDetails.ExamList.Add(edm);
            }
            dbCon.CloseReader(reader);
            if (null != classDetails && !sirm.ClassList.Contains(classDetails))
                sirm.ClassList.Add(classDetails);
            sirm.ClassArray = new List<string>();
            foreach (ClassDetailModel item in sirm.ClassList)
            {
                sirm.ClassArray.Add(item.Class);
            }
            if (sirm.ClassArray.Contains("UKG"))
            {
                sirm.ClassArray.Remove("UKG");
                sirm.ClassArray.Insert(0, "UKG");
            }
            if (sirm.ClassArray.Contains("LKG"))
            {
                sirm.ClassArray.Remove("LKG");
                sirm.ClassArray.Insert(0, "LKG");
            }
            dbCon.CloseDB(con);
            sirm.UniqKey = schoolInfoRequest.UniqKey;
            sirm.RespCode = ResponseCode.OK;
            return sirm;
        }

        [System.Web.Http.HttpPost]
        [ActionName("changepassword")]
        public async Task<ChangePasswordResponseModel> ChangePassword([FromBody] ChangePasswordRequestModel changePasswordRequest)
        {
            ChangePasswordResponseModel cprm = new ChangePasswordResponseModel() { UniqKey = Guid.Empty, RespCode = ResponseCode.FORBIDDEN };
            SqlConnection con = await dbCon.ConnectDB();
            string select = "select SchoolAgeUser_id from TBL_U_SchoolAgeUser where User_unique_key = @uniqKey and Username = @userId";
            SqlCommand cmd = new SqlCommand(select, con);
            cmd.Parameters.AddWithValue("@uniqKey", changePasswordRequest.UniqKey);
            cmd.Parameters.AddWithValue("@userId", changePasswordRequest.UserId);
            SqlDataReader reader = await dbCon.ReadData(cmd);
            Guid schoolAgeUser_id = Guid.Empty;
            if (reader.Read())
            {
                schoolAgeUser_id = reader.GetGuid(0);//parent|student
            }
            dbCon.CloseReader(reader);

            if(changePasswordRequest.NewPassword.Length <=0)
            {
                cprm.RespCode = ResponseCode.BAD_REQUEST;
                return cprm;
            }

            string update = "Update TBL_U_SchoolAgeUser set Password=@password where User_unique_key = @curukey and Username=@user_id and Password=@currpassword";
            cmd = new SqlCommand(update, con);
            cmd.Parameters.AddWithValue("@password", changePasswordRequest.NewPassword);
            cmd.Parameters.AddWithValue("@curukey", changePasswordRequest.UniqKey);
            cmd.Parameters.AddWithValue("@user_id", changePasswordRequest.UserId);
            cmd.Parameters.AddWithValue("@currpassword", changePasswordRequest.OldPassword);
            int numberOfRecords = await cmd.ExecuteNonQueryAsync();
            if (numberOfRecords <= 0)
            {
                cprm.RespCode = ResponseCode.UNAUTHORIZED;
                return cprm;
            }

            Guid uniqKey = Guid.NewGuid();

            update = "Update TBL_U_SchoolAgeUser set User_unique_key=@ukey where User_unique_key = @curukey and Username=@user_id";
            cmd = new SqlCommand(update, con);
            cmd.Parameters.AddWithValue("@ukey", uniqKey);
            cmd.Parameters.AddWithValue("@curukey", changePasswordRequest.UniqKey);
            cmd.Parameters.AddWithValue("@user_id", changePasswordRequest.UserId);
            await cmd.ExecuteNonQueryAsync();
            dbCon.CloseDB(con);
            cprm.UniqKey = uniqKey;
            cprm.RespCode = ResponseCode.OK;
            return cprm;
        }

        [System.Web.Http.HttpGet]
        [ActionName("personimage")]
        public HttpResponseMessage PersonImage(Guid personId, string ext)
        {
            MemoryStream ms = null;
            System.Web.HttpContext context = System.Web.HttpContext.Current;
            //Limit access only to images folder at root level  
            string fname = personId.ToString() + "." + ext;
            string filePath = context.Server.MapPath(string.Concat("~/images/", fname));
            string extension = Path.GetExtension(fname);
            if (File.Exists(filePath))
            {
                if (!string.IsNullOrWhiteSpace(extension))
                {
                    extension = extension.Substring(extension.IndexOf(".") + 1);
                }

                //If requested file is an image than load file to memory  
                if (GetImageFormat(extension) != null)
                {
                    ms = CopyFileToMemory(filePath);
                }
            }

            if (ms == null)
            {
                extension = "png";
                ms = CopyFileToMemory(context.Server.MapPath("~/images/fallback.png"));
            }

            HttpResponseMessage result = new HttpResponseMessage(HttpStatusCode.OK);
            result.Content = new ByteArrayContent(ms.ToArray());
            result.Content.Headers.ContentType = new MediaTypeHeaderValue(string.Format("image/{0}", extension));
            return result;
        }

        [System.Web.Http.HttpPost]
        [ActionName("personimage")]
        public HttpResponseMessage PersonImage([FromBody] UploadImageRequestModel uploadImageRequest)
        {
            System.Web.HttpContext context = System.Web.HttpContext.Current;
            //Depending on if you want the byte array or a memory stream, you can use the below. 
            var imageDataByteArray = Convert.FromBase64String(uploadImageRequest.ImageData);
            

            //When creating a stream, you need to reset the position, without it you will see 
            //that you always write files with a 0 byte length. 
            var imageDataStream = new MemoryStream(imageDataByteArray);
            imageDataStream.Position = 0;

            string fname = uploadImageRequest.PersonId.ToString() + "." + uploadImageRequest.FileExt;
            string filefullpath = string.Concat("~/images/", fname);
            
            string filePath = context.Server.MapPath(string.Concat("~/images/", fname));
            //string filePath1 = context.Server.MapPath(string.Concat("~/images/", "base64.txt"));
            //File.WriteAllText(filePath1, uploadImageRequest.ImageData);
            FileStream fsStream = new FileStream(filePath, FileMode.Create) ;
            //string extension = Path.GetExtension(fname);
            using (BinaryWriter writer = new BinaryWriter(fsStream, Encoding.UTF8))
            {
                writer.Write(imageDataByteArray);// imageDataStream);
                //writer.Flush();
               // writer.Close();
            }

            MemoryStream ms = null;
            
            string extension = Path.GetExtension(fname);
            if (File.Exists(filePath))
            {
                if (!string.IsNullOrWhiteSpace(extension))
                {
                    extension = extension.Substring(extension.IndexOf(".") + 1);
                }

                //If requested file is an image than load file to memory  
                if (GetImageFormat(extension) != null)
                {
                    ms = CopyFileToMemory(filePath);
                }
            }

            if (ms == null)
            {
                extension = "png";
                ms = CopyFileToMemory(context.Server.MapPath("~/images/fallback.png"));
            }

            HttpResponseMessage result = new HttpResponseMessage(HttpStatusCode.OK);
            result.Content = new ByteArrayContent(ms.ToArray());
            result.Content.Headers.ContentType = new MediaTypeHeaderValue(string.Format("image/{0}", extension));
            return result;
        }

        [System.Web.Http.HttpGet]
        [ActionName("aboutschool")]
        public async Task<SchoolResponseModel> AboutSchool()
        {
            SchoolResponseModel srm = new SchoolResponseModel() { SchoolText = "" };
            System.Web.HttpContext context = System.Web.HttpContext.Current;
            string fname = "About.txt";
            string filePath = context.Server.MapPath(string.Concat("~/images/", fname));
            if (File.Exists(filePath))
            {
                try
                {   // Open the text file using a stream reader.
                    using (StreamReader sr = new StreamReader(filePath))
                    {
                        // Read the stream to a string, and write the string to the console.
                        String line = sr.ReadToEnd();
                        if (null != line)
                            srm.SchoolText = line;
                    }
                }
                catch (Exception e)
                {
                    srm.SchoolText = "";
                }
            }
            return srm;
        }
        [System.Web.Http.HttpGet]
        [ActionName("missionschool")]
        public async Task<SchoolResponseModel> MissionSchool()
        {
            SchoolResponseModel srm = new SchoolResponseModel() { SchoolText = ""};
            System.Web.HttpContext context = System.Web.HttpContext.Current;
            string fname = "Mission.txt";
            string filePath = context.Server.MapPath(string.Concat("~/images/", fname));
            if (File.Exists(filePath))
            {
                try
                {   // Open the text file using a stream reader.
                    using (StreamReader sr = new StreamReader(filePath))
                    {
                        // Read the stream to a string, and write the string to the console.
                        String line = sr.ReadToEnd();
                        if(null != line)
                            srm.SchoolText = line;
                    }
                }
                catch (Exception e)
                {
                    srm.SchoolText = "";
                }
            }
            return srm;
        }
        private static ImageFormat GetImageFormat(string extension)
        {
            ImageFormat result = null;
            PropertyInfo prop = typeof(ImageFormat).GetProperties().Where(p => p.Name.Equals(extension, StringComparison.InvariantCultureIgnoreCase)).FirstOrDefault();
            if (prop != null)
            {
                result = prop.GetValue(prop) as ImageFormat;
            }
            return result;
        }

        private MemoryStream CopyFileToMemory(string path)
        {
            MemoryStream ms = new MemoryStream();
            FileStream fs = new FileStream(path, FileMode.Open);
            fs.Position = 0;
            fs.CopyTo(ms);
            fs.Close();
            fs.Dispose();
            return ms;
        }

    }
}
