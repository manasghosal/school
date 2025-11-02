package com.quicksoft.school.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.quicksoft.school.R;
import com.quicksoft.school.model.GovtBodyParent;
import com.quicksoft.school.model.MarksheetParent;

import java.util.ArrayList;

import mehdi.sakout.fancybuttons.FancyButton;

import static com.blankj.utilcode.util.ActivityUtils.startActivity;


public class MarksheetAdapter extends RecyclerView.Adapter<MarksheetAdapter.ViewHolder> {

    ArrayList<MarksheetParent.MarksSubject> marksSubjectArrayList;
    private Context mContext;

    public MarksheetAdapter(Context context, ArrayList<MarksheetParent.MarksSubject> marksSubjectArrayList) {
        this.marksSubjectArrayList = marksSubjectArrayList;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.adapater_marksheet, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        holder.tvSubject.setText(marksSubjectArrayList.get(position).getSubject());
        holder.tvMarkes.setText(""+marksSubjectArrayList.get(position).getMark());
        holder.tvHighMarks.setText(""+marksSubjectArrayList.get(position).getHighestMark());
        holder.tvParcentage.setText(""+marksSubjectArrayList.get(position).getParcentage());

    }

    @Override
    public int getItemCount() {
        return marksSubjectArrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSubject, tvMarkes, tvHighMarks, tvParcentage;
        public ViewHolder(View itemView) {
            super(itemView);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvMarkes = itemView.findViewById(R.id.tvMarks);
            tvHighMarks = itemView.findViewById(R.id.tvHighMarks);
            tvParcentage = itemView.findViewById(R.id.tvParcentage);
        }
    }
}