using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SchoolApi.Models
{
    public class FeeDetailModel
    {
        public decimal Amount { get; set; }
        public string FeeDetail { get; set; }
        public DateTime FeeDueDate { get; set; }
    }
    public class FeeForMonthModel
    {
        public int Month { get; set; }
        public IList<FeeDetailModel> FeeDetailList { get; set; }
    }

    public class ParentFeeDueResponseModel
    {
        public IList<FeeForMonthModel> FeeForMonthList { get; set; }
        public decimal GlobalFeeDue { get; set; }
        public Guid UniqKey { get; set; }
        public int RespCode { get; set; }

    }
}