using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SchoolApi.Models
{
    public class AttendanceResponseModel
    {
        public Guid UniqKey { get; set; }
        public int RespCode { get; set; }
    }
}