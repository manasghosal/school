using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SchoolApi.Models
{
    public class StudentListResponseModel
    {
        public Guid UniqKey { get; set; }
        public int RespCode { get; set; }
        public IList<int> StudentRollList { get; set; }
        public IList<StudentInfoModel> StudentInfoList { get; set; }
    }
}