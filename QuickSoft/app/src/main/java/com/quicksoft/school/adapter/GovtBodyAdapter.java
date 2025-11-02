package com.quicksoft.school.adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.quicksoft.school.R;
import com.quicksoft.school.model.AttendanceStudent;
import com.quicksoft.school.model.GovtBodyParent;

import java.util.ArrayList;

import mehdi.sakout.fancybuttons.FancyButton;

import static com.blankj.utilcode.util.ActivityUtils.startActivity;


public class GovtBodyAdapter extends RecyclerView.Adapter<GovtBodyAdapter.ViewHolder> {

    ArrayList<GovtBodyParent> govtBodyParentArrayList;
    private Context mContext;

    public GovtBodyAdapter(Context context, ArrayList<GovtBodyParent> govtBodyParentArrayList) {
        this.govtBodyParentArrayList = govtBodyParentArrayList;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.adapater_govt_body, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Glide.with(holder.itemView.getContext()).load(govtBodyParentArrayList.get(position).getImageProfile()).apply(new RequestOptions().placeholder(R.drawable.ico_user_placeholder)).into(holder.imgProfile);
        holder.tvName.setText(govtBodyParentArrayList.get(position).getName());
        holder.tvDesignation.setText(govtBodyParentArrayList.get(position).getDesignation());
        holder.tvEducation.setText(govtBodyParentArrayList.get(position).getEducation());
        holder.tvJobType.setText(govtBodyParentArrayList.get(position).getJobType());
        if(govtBodyParentArrayList.get(position).getLastVisit().compareTo("")!=0) {
            holder.tvLastVisit.setText("Last visited at " + govtBodyParentArrayList.get(position).getLastVisit());
        }

        holder.btnPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + govtBodyParentArrayList.get(position).getPhone()));
                //startActivity(intent);
                if (Build.VERSION.SDK_INT > 23) {
                    startActivity(intent);
                } else {

                    if (ActivityCompat.checkSelfPermission(mContext,
                            Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(mContext, "Permission Not Granted ", Toast.LENGTH_SHORT).show();
                    } else {
                        final String[] PERMISSIONS_STORAGE = {Manifest.permission.CALL_PHONE};
                        ActivityCompat.requestPermissions((Activity)mContext, new String[]{Manifest.permission.CALL_PHONE}, 9);
                        startActivity(intent);
                    }
                }
            }
        });
        holder.btnSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", govtBodyParentArrayList.get(position).getPhone(), null));
                if (Build.VERSION.SDK_INT > 23) {
                    startActivity(intent);
                } else {

                    if (ActivityCompat.checkSelfPermission(mContext,
                            Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(mContext, "Permission Not Granted ", Toast.LENGTH_SHORT).show();
                    } else {
                        final String[] PERMISSIONS_STORAGE = {Manifest.permission.SEND_SMS};
                        ActivityCompat.requestPermissions((Activity)mContext, new String[]{Manifest.permission.SEND_SMS}, 9);
                        startActivity(intent);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return govtBodyParentArrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgProfile;
        private FancyButton btnPhone;
        private FancyButton btnSMS;
        private TextView tvName, tvDesignation, tvEducation, tvJobType, tvLastVisit;
        public ViewHolder(View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            btnPhone = itemView.findViewById(R.id.btnPhone);
            btnSMS = itemView.findViewById(R.id.btnSMS);
            tvName = itemView.findViewById(R.id.tvName);
            tvDesignation = itemView.findViewById(R.id.tvDesignation);
            tvEducation = itemView.findViewById(R.id.tvEducation);
            tvJobType = itemView.findViewById(R.id.tvJobType);
            tvLastVisit = itemView.findViewById(R.id.tvLastVisit);
        }
    }
}