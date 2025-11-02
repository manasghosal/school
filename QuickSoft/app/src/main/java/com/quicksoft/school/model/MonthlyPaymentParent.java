package com.quicksoft.school.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MonthlyPaymentParent {
    private ArrayList<Payment> paymentArrayList;
    private double dueFees;

    public MonthlyPaymentParent() {
    }

    public ArrayList<Payment> getPaymentArrayList() {
        return paymentArrayList;
    }

    public double getDueFees() {
        return dueFees;
    }

    public void setPaymentArrayList(ArrayList<Payment> paymentArrayList) {
        this.paymentArrayList = paymentArrayList;
    }

    public void setDueFees(double dueFees) {
        this.dueFees = dueFees;
    }

    public class Payment{
        String paymentType;
        double fees;
        double fine;
        String dueDate;
        private int month;
        private int year;
        public Payment(String paymentType, double fees, double fine, int month, int year){
            this.paymentType = paymentType;
            this.fees = fees;
            this.fine = fine;
            this.month = month;
            this.year = year;
        }

        public Payment(String paymentType, double fees, double fine, String dueDate, int month, int year){
            this.paymentType = paymentType;
            this.fees = fees;
            this.fine = fine;
            this.dueDate =dueDate;
            this.month = month;
            this.year = year;
        }

        public String getPaymentType() {
            return paymentType;
        }

        public double getFees() {
            return fees;
        }

        public double getFine() {
            return fine;
        }

        public int getMonth() {
            return month;
        }

        public int getYear() {
            return year;
        }

        public String getDueDate() {
            try {
                SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss");
                SimpleDateFormat dt1 = new SimpleDateFormat("dd-MM-yyyy");
                Date date  = dt.parse(dueDate);
                return  dt1.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
                return "";
            }
        }
    }
}
