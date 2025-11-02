using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SchoolApi.Models
{
    public class UserCredentialModel
    {
        public string UserType { get; set; }
        public string Id { get; set; }
        public string FacebookId { get; set; }
        public string GoogleId { get; set; }
        public string UserId { get; set; }
        public string Password { get; set; }
    }
}