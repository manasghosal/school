package com.quicksoft.school.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.quicksoft.school.R;
import com.quicksoft.school.activity.parent.NoticeDetailsParentActivity;
import com.quicksoft.school.activity.parent.TaskDetailsParentActivity;
import com.quicksoft.school.model.NoticeParent;
import com.quicksoft.school.model.TaskParent;
import com.quicksoft.school.util.Constant;

import java.util.ArrayList;


public class NoticeParentAdapter extends RecyclerView.Adapter<NoticeParentAdapter.ViewHolder> {

    private ArrayList<NoticeParent> data;
    private Context mContext;

    public NoticeParentAdapter(Context context, ArrayList<NoticeParent> data) {
        this.data = data;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.adapater_notice_parent, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.tvTeacherName.setText(""+data.get(position).getType());
        holder.tvDate.setText("" + data.get(position).getTimestamp());
        holder.tvTitle.setText("" + data.get(position).getTitle());
        holder.tvNoticeId.setText("" + data.get(position).getNoticeId());
        holder.tvNoticeType.setText("" + data.get(position).getType());
       // holder.tvDesc.setText("" + data.get(position).getDescription());
        if(data.get(position).getSubmit())
            holder.tvSubmitted.setText("YES"+position);
        else
            holder.tvSubmitted.setText("NO"+position);
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
        private  TextView tvNoticeId;
        private  TextView tvNoticeType;
        public ViewHolder(View itemView) {
            super(itemView);
            tvTeacherName = itemView.findViewById(R.id.tvTeacherName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvSubmitted = itemView.findViewById(R.id.tvSubmitted);
            tvNoticeId = itemView.findViewById(R.id.tvNoticeId);
            tvNoticeType = itemView.findViewById(R.id.tvNoticeType);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, NoticeDetailsParentActivity.class);
                    intent.putExtra("TEACHER", tvTeacherName.getText().toString());
                    intent.putExtra("DATE", tvDate.getText().toString());
                    intent.putExtra("TITLE", tvTitle.getText().toString());
                    intent.putExtra("DESC", tvDesc.getText().toString());
                    intent.putExtra("ID", tvNoticeId.getText().toString());
                    intent.putExtra("TYPE", tvNoticeType.getText().toString());
                    String val = tvSubmitted.getText().toString().substring(0, 2);
//                    LogUtils.i(tvSubmitted.getText().toString());
                    if(val.compareTo("YES")==0)
                        intent.putExtra("SUBMITTED", tvSubmitted.getText().toString());
                    else
                        intent.putExtra("SUBMITTED", tvSubmitted.getText().toString());
                    ((Activity) mContext).startActivityForResult(intent,Constant.PARENT_NOTICE_DETAILS_REQUEST_CODE);
                }
            });
        }
    }
}