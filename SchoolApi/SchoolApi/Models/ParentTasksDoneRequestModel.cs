using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SchoolApi.Models
{
    public class ParentTasksDoneRequestModel
    {
        public IList<string> AssignmentIdList { get; set; }
        public Guid UniqKey { get; set; }
        public string UserId { get; set; }
    }
}