using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SchoolApi.Models
{
    public class ExamDetailModel : IEquatable<ExamDetailModel>
    {
        
        public string ExamName { get; set; }
        public string ExamDescription { get; set; }
        public bool Equals(ExamDetailModel other)
        {
            return ExamName == other.ExamName;
        }
    }
    public class ClassDetailModel : IEquatable<ClassDetailModel>
    {
        
        public string Class { get; set; }
        public IList<ExamDetailModel> ExamList { get; set; }
        public IList<string> SectionList { get; set; }

        public bool Equals(ClassDetailModel other)
        {
            return Class == other.Class;
        }

    }
    public class SchoolInfoResponseModel
    {
        public IList<ClassDetailModel> ClassList { get; set; }
        public List<string> ClassArray { get; set; }
        public Guid UniqKey { get; set; }
        public int RespCode { get; set; }
    }
}