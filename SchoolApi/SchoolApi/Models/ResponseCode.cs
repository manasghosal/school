using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SchoolApi.Models
{
    public class ResponseCode
    {

        public static int OK = 200;
        public static int BAD_REQUEST = 400;
        public static int UNAUTHORIZED = 401;
        public static int FORBIDDEN = 403;
        public static int NOT_FOUND = 404;
        public static int PROXY_AUTH_REQUIRED = 407;
        public static int INTERNAL_SERVER_ERROR = 500;
        public static int NOT_IMPLEMENTED = 501;
        public static int SERVICE_UNAVAILABLE = 503;
        public static int GATEWAY_TIMEOUT = 504;
    }
}