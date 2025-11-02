using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SchoolApi.Models
{
    public class OTPVerificationResponseModel
    {
        public Guid UniqKey { get; set; }
        public int RespCode { get; set; }
    }
}