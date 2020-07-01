package ca.louisechan.livefitandroid;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SubscribeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    private static final String TAG = "SubscribeViewHolder";

    // Member Variables
    // - Outlets for each item in your row layout
    TextView tvSubPlan;
    TextView tvSubDiscPercent;
    TextView tvSubDiscPrice;
    TextView tvSubSavings;
    TextView tvSubOrigPrice;

    // member variable for the interface
    private IOnRowClickedListener listenerInterface;

    public SubscribeViewHolder(@NonNull View itemView, IOnRowClickedListener listenerInterface) {
        super(itemView);

        this.tvSubPlan = (TextView) itemView.findViewById(R.id.txtSubPlanName);
        this.tvSubDiscPercent = (TextView) itemView.findViewById(R.id.txtSubDiscount);
        this.tvSubDiscPrice = (TextView) itemView.findViewById(R.id.txtSubDiscPrice);
        this.tvSubSavings = (TextView) itemView.findViewById(R.id.txtSubSavings);
        this.tvSubOrigPrice = (TextView) itemView.findViewById(R.id.txtSubOrigPrice);

        // configure listener interface that we received from adapter
        this.listenerInterface = listenerInterface;

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // 1. we detected a click on the row
        Log.d(TAG, "onClick: Something was clicked! Row: " + getAdapterPosition());

        // 2. "do" some code based on the click
        this.listenerInterface.onClick(getAdapterPosition());
    }
}
