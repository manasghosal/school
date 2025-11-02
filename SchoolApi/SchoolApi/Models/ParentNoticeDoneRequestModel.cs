using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SchoolApi.Models
{
    public class ParentNoticeDoneRequestModel
    {
        public IList<string> NoticeIdList { get; set; }
        public Guid UniqKey { get; set; }
        public string UserId { get; set; }
    }
}