using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

/*
 
UniqKey: an unique guid value
RespCode: error code
First16Days: attendance for 1st 16 days of the month
RestOfDays: attendance for remaining days of the month
TotalFeeDue: sum of all due amounts
DueDate: Payment due date

attendance values:
16 days (2 bit for each day)
00 01 10 11
00 - Present,
01 - Absent,
10 - Late,
11 - Not Available.     
     
*/
namespace SchoolApi.Models
{
    public class ParentDashboardResponseModel
    {
        public Guid UniqKey { get; set; }
        public int RespCode { get; set; }
        public Guid PersonId { get; set; }
        public int[] Attendance { get; set; }
        public float TotalFeeDue { get; set; }
        public DateTime DueDate { get; set; }
        public string FeeDueNote { get; set; }
        public string Classs { get; set; }
        public string Section { get; set; }
        public int Roll { get; set; }
        public Guid ChildPersonid { get; set; }
        public string Lname { get; set; }
        public string Mname { get; set; }
        public string Fname { get; set; }

        public Guid RouteId { get; set; }
        public Guid DriverPersonId { get; set; }
        public string DriverLname { get; set; }
        public string DriverMname { get; set; }
        public string DriverFname { get; set; }
        public string DriverPhone { get; set; }

    }
}