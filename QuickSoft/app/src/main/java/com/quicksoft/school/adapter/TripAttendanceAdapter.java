package com.quicksoft.school.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.quicksoft.school.R;
import com.quicksoft.school.model.Passanger;

import java.util.ArrayList;

import mehdi.sakout.fancybuttons.FancyButton;


public class TripAttendanceAdapter extends RecyclerView.Adapter<TripAttendanceAdapter.ViewHolder> {

    private ArrayList<Passanger> data;
    private Context mContext;

    public TripAttendanceAdapter(Context context, ArrayList<Passanger> data) {
        this.data = data;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.adapater_trip_attendance, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Glide.with(holder.itemView.getContext()).load(data.get(position).getImageUrl()).apply(new RequestOptions().placeholder(R.drawable.ico_student_individual)).into(holder.imgProfile);
        holder.tvName.setText(data.get(position).getPassangerName());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgProfile;
        private FancyButton btnAttendance;
        private TextView tvName;
        public ViewHolder(View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            btnAttendance = itemView.findViewById(R.id.btnAttendance);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
}