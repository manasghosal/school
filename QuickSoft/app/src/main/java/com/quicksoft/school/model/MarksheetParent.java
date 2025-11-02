package com.quicksoft.school.model;

import java.util.ArrayList;

public class MarksheetParent {

    String rank;
    String status;
    String classs;
    String terms;

    ArrayList<MarksSubject> marksSubjectArrayList;

    public MarksheetParent(String rank, String status, String classs, String terms) {
        this.rank = rank;
        this.status = status;
        this.classs = classs;
        this.terms = terms;
    }

    public String getRank() {
        return rank;
    }

    public String getStatus() {
        return status;
    }

    public String getClasss() {
        return classs;
    }

    public String getTerms() {
        return terms;
    }

    public void setMarksSubjectArrayList(ArrayList<MarksSubject> marksSubjectArrayList){
        this.marksSubjectArrayList = marksSubjectArrayList;
    }

    public ArrayList<MarksSubject> getSubjctMarksArray() {
        return marksSubjectArrayList;
    }

    public class MarksSubject {
        String subject;
        int mark;
        int highestMark;
        int parcentage;

        public MarksSubject(String subject, int mark, int highestMark, int parcentage) {
            this.subject = subject;
            this.mark = mark;
            this.highestMark = highestMark;
            this.parcentage = parcentage;
        }

        public String getSubject() {
            return subject;
        }

        public int getMark() {
            return mark;
        }

        public int getHighestMark() {
            return highestMark;
        }

        public int getParcentage() {
            return parcentage;
        }
    }
}
