package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends BaseAdapter {
    private Context context;
    private List<Review> reviewList;

    public ReviewAdapter(Context context, List<Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    @Override
    public int getCount() { return reviewList.size(); }

    @Override
    public Object getItem(int position) { return reviewList.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        }

        Review review = reviewList.get(position);

        TextView tvUser = convertView.findViewById(R.id.tv_review_user);
        TextView tvDate = convertView.findViewById(R.id.tv_review_date);
        RatingBar rbReview = convertView.findViewById(R.id.rb_review);
        TextView tvComment = convertView.findViewById(R.id.tv_review_comment);

        tvUser.setText(review.getUserName());
        rbReview.setRating(review.getRating());
        tvComment.setText(review.getComment());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        tvDate.setText(sdf.format(new Date(review.getTimestamp())));

        return convertView;
    }
}
