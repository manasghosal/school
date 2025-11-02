using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SchoolApi.Models
{
    public class SignInResponseModel
    {
        public Guid UniqKey { get; set; }
        public int RespCode { get; set; }
        public Guid PersonId { get; set; }
        public string Fname { get; set; }
        public string Mname { get; set; }
        public string Lname { get; set; }
        public string Phone { get; set; }
    }
}