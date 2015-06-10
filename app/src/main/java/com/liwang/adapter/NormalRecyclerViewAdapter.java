package com.liwang.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.liwang.activity.DetailActivity;
import com.liwang.bean.HomePage;
import com.liwang.issdut.R;

import java.util.ArrayList;
import java.util.List;

public class NormalRecyclerViewAdapter extends RecyclerView.Adapter<NormalRecyclerViewAdapter.NormalTextViewHolder> {

    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private List<HomePage> homePages;

    public NormalRecyclerViewAdapter(Context context, List<HomePage> homePages) {
        this.homePages = homePages;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public NormalTextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NormalTextViewHolder(mLayoutInflater.inflate(R.layout.item_text, parent, false));
    }

    @Override
    public void onBindViewHolder(NormalTextViewHolder holder, final int position) {
        holder.messageView.setText(homePages.get(position).getTitle());
        holder.dateView.setText(homePages.get(position).getDate());
        holder.backView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Intent intent = new Intent(mContext, DetailActivity.class);
                    intent.putExtra("x", (int) event.getRawX());
                    intent.putExtra("y", (int) event.getRawY());
                    Log.e("sendUrl", homePages.get(position).getUrl());
                    intent.putExtra("url", homePages.get(position).getUrl());
                    intent.putExtra("title", homePages.get(position).getTitle());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    mContext.startActivity(intent);
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return homePages == null ? 0 : homePages.size();
    }

    public class NormalTextViewHolder extends RecyclerView.ViewHolder {

        TextView messageView;
        ImageView arrowView;
        View backView;
        TextView dateView;

        NormalTextViewHolder(View view) {
            super(view);
            backView = view;
            messageView = (TextView) view.findViewById(R.id.message);
            arrowView = (ImageView) view.findViewById(R.id.arrow);
            dateView= (TextView) view.findViewById(R.id.date);
//            rippleView= (RippleView) view.findViewById(R.id.ripple_view);
//            rippleView.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
//                @Override
//                public void onComplete(RippleView rippleView) {
//                    mContext.startActivity(new Intent(mContext, DetailActivity.class));
//                }
//            });
        }
    }
}
