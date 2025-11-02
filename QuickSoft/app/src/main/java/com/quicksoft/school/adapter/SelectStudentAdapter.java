package com.quicksoft.school.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.quicksoft.school.R;
import com.quicksoft.school.model.AttendanceStudent;

import java.util.ArrayList;

import mehdi.sakout.fancybuttons.FancyButton;


public class SelectStudentAdapter extends RecyclerView.Adapter<SelectStudentAdapter.ViewHolder> {

    private ArrayList<AttendanceStudent> data;
    private Context mContext;

    public SelectStudentAdapter(Context context, ArrayList<AttendanceStudent> data) {
        this.data = data;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.adapater_select_student, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if(position==0){
            holder.imgProfile.setVisibility(View.INVISIBLE);
            //holder.tvRollNo.setVisibility(View.INVISIBLE);
            holder.tvName.setText(data.get(position).getName());
            holder.checkBox.setTag(position);
            holder.checkBox.setChecked(data.get(position).getChecked());
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                    Integer pos = (Integer) holder.checkBox.getTag();
//                    data.get(pos).setChecked(b);
                    setAllChecked(b);
                }
            });
        }else {
            Glide.with(holder.itemView.getContext()).load(data.get(position).getImageUrl()).apply(new RequestOptions().placeholder(R.drawable.ico_student_individual)).into(holder.imgProfile);
            holder.tvName.setText(data.get(position).getName());
            //holder.tvRollNo.setText("" + data.get(position).getRollNo());

            holder.checkBox.setTag(position);
            holder.checkBox.setChecked(data.get(position).getChecked());
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    Integer pos = (Integer) holder.checkBox.getTag();
                    data.get(pos).setChecked(b);
                }
            });

        }
    }

    public void setAllChecked(boolean val){
        if(val) {
            for (int i = 0; i < data.size(); i++) {
                data.get(i).setChecked(true);
            }
        }else{
            for (int i = 0; i < data.size(); i++) {
                data.get(i).setChecked(false);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgProfile;
        private CheckBox checkBox;
        private TextView tvName;//, tvRollNo;
        public ViewHolder(View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            checkBox = itemView.findViewById(R.id.checkBox);
            tvName = itemView.findViewById(R.id.tvName);
            //tvRollNo = itemView.findViewById(R.id.tvRollNo);
        }
    }
}