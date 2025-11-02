using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SchoolApi.Models
{
    public class TeacherStudentListRequestModel
    {
        public Guid UniqKey { get; set; }
        public string UserId { get; set; }
        public string Class { get; set; }
        public string Section { get; set; }
    }
}