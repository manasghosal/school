package com.quicksoft.school.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.quicksoft.school.R;
import com.quicksoft.school.activity.parent.TaskDetailsParentActivity;
import com.quicksoft.school.activity.teacher.SummeryAttendanceTeacherActivity;
import com.quicksoft.school.model.AttendanceStudent;
import com.quicksoft.school.model.TaskParent;
import com.quicksoft.school.util.Constant;

import java.util.ArrayList;


public class TaskParentAdapter extends RecyclerView.Adapter<TaskParentAdapter.ViewHolder> {

    private ArrayList<TaskParent> data;
    private Context mContext;

    public TaskParentAdapter(Context context, ArrayList<TaskParent> data) {
        this.data = data;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.adapater_task_parent, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.tvTeacherName.setText("Date: " + data.get(position).getTimestamp());
        holder.tvDate.setText("Due Date: " + data.get(position).getDueTimestamp());
        holder.tvTitle.setText("" + data.get(position).getTitle());
        holder.tvDesc.setText("" + data.get(position).getDescription());
        holder.tvTaskId.setText("" + data.get(position).getTaskId());
        if(data.get(position).getSubmit())
            holder.tvSubmitted.setText("YES"+position);
        else
            holder.tvSubmitted.setText("NOT"+position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTeacherName;
        private TextView tvDate;
        private TextView tvTitle, tvDesc;
        private  TextView tvSubmitted;
        private  TextView tvTaskId;
        public ViewHolder(View itemView) {
            super(itemView);
            tvTeacherName = itemView.findViewById(R.id.tvTeacherName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvSubmitted = itemView.findViewById(R.id.tvSubmitted);
            tvTaskId = itemView.findViewById(R.id.tvTaskId);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, TaskDetailsParentActivity.class);
                    intent.putExtra("TEACHER", tvTeacherName.getText().toString());
                    intent.putExtra("DATE", tvDate.getText().toString());
                    intent.putExtra("TITLE", tvTitle.getText().toString());
                    intent.putExtra("DESC", tvDesc.getText().toString());
                    intent.putExtra("ID", tvTaskId.getText().toString());
                    String val = tvSubmitted.getText().toString().substring(0, 3);
//                    LogUtils.i(tvSubmitted.getText().toString());
                    if(val.compareTo("YES")==0)
                        intent.putExtra("SUBMITTED", tvSubmitted.getText().toString());
                    else
                        intent.putExtra("SUBMITTED", tvSubmitted.getText().toString());
                    ((Activity) mContext).startActivityForResult(intent,Constant.PARENT_TASK_DETAILS_REQUEST_CODE);
                }
            });
        }
    }
}