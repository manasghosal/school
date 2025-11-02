using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SchoolApi.Models
{
    public class AttendanceList
    {
        public string StudentLName { get; set; }//Last_name
        public string StudentMName { get; set; }//Middle_name
        public string StudentFName { get; set; }//First_name
        public string Class { get; set; }//Class
        public string Section { get; set; }//Section
        public int RollNo { get; set; }//Roll
        public int StudentIC { get; set; }//Student_IC
        public int Position { get; set; }//Position
        public Guid StudentID { get; set; }//Student_id
        public Guid PersonId { get; set; }//Person_id
        public string Remark { get; set; }

    }
    public class AttendanceListResponseModel
    {
        public Guid UniqKey { get; set; }
        public int RespCode { get; set; }
        public IList<int> StudentRollList { get; set; }
        public IList<AttendanceList> StudentInfoList { get; set; }
    }
}