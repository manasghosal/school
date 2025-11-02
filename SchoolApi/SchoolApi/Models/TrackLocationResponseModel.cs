using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SchoolApi.Models
{
    public class TrackLocationResponseModel
    {
        public Guid UniqKey { get; set; }
        public int RespCode { get; set; }
        public string Latitude { get; set; }
        public string Longitude { get; set; }
    }
}