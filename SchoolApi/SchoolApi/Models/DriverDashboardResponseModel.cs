using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SchoolApi.Models
{
    public class PassengerDetailsModel
    {
        public Guid PersonId { get; set; }
        public string FName { get; set; }
        public string MName { get; set; }
        public string LName { get; set; }
        public string Remark { get; set; }


    }
    public class RouteDetailsModel
    {
        public string PickupLatitude { get; set; }
        public string PickupLongitude { get; set; }
        public IList<PassengerDetailsModel> passengerDetailsLst { get; set; }
    }
    public class DriverDashboardResponseModel
    {
        public Guid UniqKey { get; set; }
        public int RespCode { get; set; }
        public Guid PersonId { get; set; }
        public string SchoolLatitude { get; set; }
        public string SchoolLongitude { get; set; }
        public IList<RouteDetailsModel> RouteDetailsLst { get; set; }
    }
}