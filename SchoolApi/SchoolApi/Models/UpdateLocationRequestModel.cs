using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SchoolApi.Models
{
    public class UpdateLocationRequestModel
    {
        public Guid UniqKey { get; set; }
        public string UserId { get; set; }
        public string Latitude { get; set; }
        public string Longitude { get; set; }
    }
}