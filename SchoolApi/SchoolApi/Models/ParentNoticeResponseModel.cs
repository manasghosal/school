using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SchoolApi.Models
{

    public class NoticeModel
    {
        public Guid NoiticeId { get; set; }
        public DateTime NoticeDate { get; set; }
        public string NoticeTxt { get; set; }
        public int NoticeType { get; set; }
    }
    public class ParentNoticeResponseModel
    {
        public IList<NoticeModel> NoticeList { get; set; }
        public Guid UniqKey { get; set; }
        public int RespCode { get; set; }
    }
}