using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SchoolApi.Models
{

    public class GiveTaskNoticeRequestModel
    {
        public Guid UniqKey { get; set; }
        public string UserId { get; set; }
        public IList<Guid> StudentIdList { get; set; }
        public DateTime DueDate { get; set; }
        public string AssignmentTxt { get; set; }
        public string TeacherRemark { get; set; }
        public string Class { get; set; }
        public string Section { get; set; }
        public bool All { get; set; }
    }
}