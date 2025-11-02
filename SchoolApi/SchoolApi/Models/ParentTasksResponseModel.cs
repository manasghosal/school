using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SchoolApi.Models
{
    public class AssignmentModel
    {
        public Guid AssignmentId { get; set; }
        public DateTime AssignmentDate { get; set; }
        public DateTime AssignmentDueDate { get; set; }
        public string AssignmentTxt { get; set; }
        public string Remark { get; set; }
    }
    public class ParentTasksResponseModel
    {
        public IList<AssignmentModel> AssignmentList { get; set; }
        public Guid UniqKey { get; set; }
        public int RespCode { get; set; }
    }
}