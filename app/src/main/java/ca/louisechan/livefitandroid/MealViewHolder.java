package ca.louisechan.livefitandroid;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MealViewHolder extends RecyclerView.ViewHolder {
    // Member Variables
    // - Outlets for each item in your row layout
    ImageView ivMealPhoto;
    TextView tvMealName;
    TextView tvMealDesc;
    TextView tvMealCals;

    public MealViewHolder(@NonNull View itemView) {
        super(itemView);

        this.ivMealPhoto = (ImageView) itemView.findViewById(R.id.imgMealPhoto);
        this.tvMealName = (TextView) itemView.findViewById(R.id.txtMealName);
        this.tvMealDesc = (TextView) itemView.findViewById(R.id.txtMealDesc);
        this.tvMealCals = (TextView) itemView.findViewById(R.id.txtMealCals);

    }
}
