using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SchoolApi.Models
{
    public class MarkOnBoardModel
    {
        public string Latitude { get; set; }
        public string Longitude { get; set; }
        public Guid PassengerPersonId { get; set; }
    }
    public class DriverMarkOnBoardRequestModel
    {
        public Guid UniqKey { get; set; }
        public string UserId { get; set; }
        public string VehicleNo { get; set; }
        public IList<MarkOnBoardModel> MarkOnBoardList { set; get; }
    }
}