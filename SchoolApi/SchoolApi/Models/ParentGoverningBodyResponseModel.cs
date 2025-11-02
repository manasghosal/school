using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SchoolApi.Models
{
    public class GoverningBodyModel
    {
        public Guid PersonId { get; set; }
        public string Designation { get; set; }
        public string Qualification { get; set; }
        public string Department { get; set; }
        public string First_name { get; set; }
        public string Middle_name { get; set; }
        public string Last_name { get; set; }
        public string Address_1 { get; set; }
        public string Address_2 { get; set; }
        public string Address_3 { get; set; }
        public string Phone { get; set; }
        public string Email { get; set; }
        public DateTime? LastVisited { get; set; }
    }
    public class ParentGoverningBodyResponseModel
    {
        public IList<GoverningBodyModel> GoverningBodyList { get; set; }
        public Guid UniqKey { get; set; }
        public int RespCode { get; set; }
    }
}