using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SchoolApi.Models
{
    public class TrackLocationRequestModel
    {
        public Guid UniqKey { get; set; }
        public string UserId { get; set; }
        public Guid? RouteId { get; set; }
    }
}