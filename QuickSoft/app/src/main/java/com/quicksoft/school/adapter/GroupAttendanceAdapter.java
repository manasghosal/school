package com.quicksoft.school.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.quicksoft.school.R;
import com.quicksoft.school.connection.callback.AttendanceCallback;
import com.quicksoft.school.model.AttendanceStudent;

import java.util.ArrayList;

import mehdi.sakout.fancybuttons.FancyButton;


public class GroupAttendanceAdapter extends RecyclerView.Adapter<GroupAttendanceAdapter.ViewHolder> {

    private ArrayList<AttendanceStudent> data;
    private Context mContext;
    private AttendanceCallback attendanceCallback;

    public GroupAttendanceAdapter(Context context, ArrayList<AttendanceStudent> data, AttendanceCallback attendanceCallback) {
        this.data = data;
        mContext = context;
        this.attendanceCallback = attendanceCallback;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.adapater_group_attendance, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Glide.with(holder.itemView.getContext()).load(data.get(position).getImageUrl()).apply(new RequestOptions().placeholder(R.drawable.ico_student_individual)).into(holder.imgProfile);
        holder.tvName.setText(data.get(position).getName());
        holder.tvRollNo.setText(""+data.get(position).getRollNo());
        holder.tvPosition.setText(""+position);
        if(data.get(position).getRemark().compareTo("A")==0) {
            holder.btnAttendance.setText("Absent");
            holder.btnAttendance.setBackgroundColor(mContext.getResources().getColor(R.color.dullRed));
        } else if(data.get(position).getRemark().compareTo("P")==0) {
            holder.btnAttendance.setText("Present");
            holder.btnAttendance.setBackgroundColor(mContext.getResources().getColor(R.color.okButtonColor));
        }else if(data.get(position).getRemark().compareTo("L")==0) {
            holder.btnAttendance.setText("Late");
            holder.btnAttendance.setBackgroundColor(mContext.getResources().getColor(R.color.paleRed));
        }
        holder.btnAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = holder.btnAttendance.getText().toString();
                String posString = holder.tvPosition.getText().toString();
                int pos = Integer.valueOf(posString);
                if(text.compareTo("Present")==0){
                    holder.btnAttendance.setText("Absent");
                    holder.btnAttendance.setBackgroundColor(mContext.getResources().getColor(R.color.dullRed));
                    data.get(pos).setRemark("A");
                }else if(text.compareTo("Absent")==0){
                    holder.btnAttendance.setText("Present");
                    holder.btnAttendance.setBackgroundColor(mContext.getResources().getColor(R.color.okButtonColor));
                    data.get(pos).setRemark("P");
                }

                int present=0;
                int absent =0;
                for(int i=0; i< data.size(); i++) {
                    if(data.get(i).getRemark().compareTo("P")==0) {
                        present++;
                    }else if(data.get(i).getRemark().compareTo("A")==0) {
                        absent ++;
                    }
                }
                attendanceCallback.onAttandance(present, absent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public ArrayList<AttendanceStudent> getStudentArray(){
        return data;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgProfile;
        private FancyButton btnAttendance;
        private TextView tvName, tvRollNo, tvPosition;
        public ViewHolder(View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            btnAttendance = itemView.findViewById(R.id.btnAttendance);
            tvName = itemView.findViewById(R.id.tvName);
            tvRollNo = itemView.findViewById(R.id.tvRollNo);
            tvPosition = itemView.findViewById(R.id.tvPosition);
        }
    }
}