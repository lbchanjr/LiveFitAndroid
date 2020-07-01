package ca.louisechan.livefitandroid;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MealkitViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private static final String TAG = "ViewHolder";
    // Member Variables
    // - Outlets for each item in your row layout
    ImageView ivMealkitPhoto;
    TextView tvMealkitName;
    TextView tvMealkitDesc;

    // member variable for the interface
    private IOnRowClickedListener listenerInterface;

    public MealkitViewHolder(@NonNull View itemView, IOnRowClickedListener listenerInterface) {
        super(itemView);

        this.ivMealkitPhoto = (ImageView) itemView.findViewById(R.id.imgMealkitPhoto);
        this.tvMealkitName = (TextView) itemView.findViewById(R.id.txtMealkitName);
        this.tvMealkitDesc = (TextView) itemView.findViewById(R.id.txtMealkitDescription);

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
