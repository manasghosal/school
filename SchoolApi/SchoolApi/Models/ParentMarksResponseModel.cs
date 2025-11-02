using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SchoolApi.Models
{

    public class SubjectMarksModel
    {
        public decimal Marks { get; set; }
        public decimal TotalMarks { get; set; }
        public decimal HighestMarks { get; set; }
        public string Subject { get; set; }
        
    }
    public class ExamMarksModel : IEquatable<ExamMarksModel>
    {
        public string ExamName { get; set; }
        public string Class { get; set; }
        public string Remark { get; set; }
        public string Rank { get; set; }
        public IList<SubjectMarksModel> SubjectMarksList { get; set; }

        public bool Equals(ExamMarksModel other)
        {
            return ExamName == other.ExamName && Class == other.Class;
        }

    }
    public class ParentMarksResponseModel
    {
        public IList<ExamMarksModel> ExamMarksList { get; set; }
        public Guid UniqKey { get; set; }
        public int RespCode { get; set; }
    }
}