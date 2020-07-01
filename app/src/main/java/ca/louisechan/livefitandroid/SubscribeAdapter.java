package ca.louisechan.livefitandroid;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class SubscribeAdapter extends RecyclerView.Adapter<SubscribeViewHolder> {

    private static final String TAG = "SubscribeAdapter";

    List<Subscription> subscriptions;

    Context context;

    // member variable for the interface
    IOnRowClickedListener listenerInterface;

    public SubscribeAdapter(List<Subscription> subscriptions, IOnRowClickedListener listener) {
        this.subscriptions = subscriptions;
        this.listenerInterface = listener;
    }

    @NonNull
    @Override
    public SubscribeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        this.context = context;


        // get the custom row layout
        View subscribeRowView = inflater.inflate(R.layout.subscribe_row_layout, parent, false);


        // Return the view holder
        SubscribeViewHolder holder = new SubscribeViewHolder(subscribeRowView, listenerInterface);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull SubscribeViewHolder holder, int position) {
        Subscription sub = subscriptions.get(position);

        holder.tvSubPlan.setText(String.format("%d-Month Subscription Plan", (int)sub.getMonths()));

        if (sub.getDiscount() != 0) {
            holder.tvSubOrigPrice.setText(String.format("Original price: $%.2f", sub.getPrice()));
            holder.tvSubDiscPercent.setText(String.format("(-%.1f%%)", sub.getDiscount()));
            double discountedPrice = (1-sub.getDiscount()/100.0)*sub.getPrice();
            holder.tvSubDiscPrice.setText(String.format("Discounted Price: $%.2f", discountedPrice));
            holder.tvSubSavings.setText(String.format("Savings: $%.2f", sub.getPrice()*(sub.getDiscount()/100.0)));
        } else {
            holder.tvSubOrigPrice.setText(String.format("Price: $%.2f", sub.getPrice()));
            holder.tvSubDiscPercent.setText("");
            holder.tvSubDiscPrice.setText("");
            holder.tvSubSavings.setText("");
        }
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: Number of subscriptions in list is " + subscriptions.size());
        return subscriptions.size();
    }


}
