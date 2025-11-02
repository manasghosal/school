using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SchoolApi.Models
{
    public class DriverVehicleResponseModel
    {
        public Guid UniqKey { get; set; }
        public int RespCode { get; set; }
        public IList<string> VehicleNo { get; set; }
        public IList<string> RouteDescription { get; set; }
    }
}