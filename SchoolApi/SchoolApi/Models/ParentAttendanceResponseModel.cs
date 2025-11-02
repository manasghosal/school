using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SchoolApi.Models
{
    public class MonthAttendanceModel
    {
        public int Month { get; set; }
        public int[] Attendance { get; set; }
        public int AttendancePercent { get; set; }
    }

    public class ParentAttendanceResponseModel
    {
        public IList<MonthAttendanceModel> MonthAttendanceList { get; set; }
        public Guid UniqKey { get; set; }
        public int RespCode { get; set; }
    }
}