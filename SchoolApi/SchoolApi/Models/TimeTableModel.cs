using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
/*
From: fromTime
To: toTime
Day: Mon/Tue....Fri
Subject: subject
Class_Section: class and section
Description: any note, if not ''
 */
namespace SchoolApi.Models
{
    public class TimeTableModel
    {
        public TimeSpan From { get; set; }
        public TimeSpan To { get; set; }
        public string Day { get; set; }
        public string Subject { get; set; }
        public string Class_Section { get; set; }
        public string Description { get; set; }
    }
}