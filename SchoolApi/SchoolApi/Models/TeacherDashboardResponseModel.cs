using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
/*
UniqKey: an unique guid value
RespCode: error code
{
From: fromTime
To: toTime
Day: Mon/Tue....Fri
Subject: subject
Class_Section: class and section
Description: any note, if not ''
} 
     
*/
namespace SchoolApi.Models
{
    public class IncompleteTaskModel
    {
        public string Class { get; set; }
        public string Section { get; set; }
        public string Heading { get; set; }
        public string TaskDetail { get; set; }
        public string StudentFName { get; set; }
        public string StudentLName { get; set; }
        public DateTime DueDate { get; set; }
        public string Subject { get; set; }
    }
    public class TeacherDashboardResponseModel
    {
        public Guid UniqKey { get; set; }
        public int RespCode { get; set; }
        public Guid PersonId { get; set; }
        public string Class { get; set; }
        public string Section { get; set; }
        public IList<TimeTableModel> TimeTableToday { get; set; }
        public IList<IncompleteTaskModel> IncompleteTasks { get; set; }
    }
}