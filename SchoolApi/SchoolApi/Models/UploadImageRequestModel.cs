using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SchoolApi.Models
{
    public class UploadImageRequestModel
    {
        public Guid PersonId { get; set; }
        public string FileExt { get; set; }
        public string ImageData { get; set; }
    }
}