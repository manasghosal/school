using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SchoolApi.Models
{
    public class OTPVerificationRequestModel
    {
        public Guid UniqKey { get; set; }
        public string UserId { get; set; }
        public string OTP { get; set; }
    }
}