using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SchoolApi.Models
{
    public class StudentAttendanceDetail
    {
        public Guid StudentId { get; set; }
        public DateTime AttendanceDate { get; set; }
        public string Remark { get; set; }
    }
    public class StudentAttendanceListRequestModel
    {
        public Guid UniqKey { get; set; }
        public string UserId { get; set; }
        public IList<StudentAttendanceDetail> StudentAttendanceList { get; set; }
    }
}