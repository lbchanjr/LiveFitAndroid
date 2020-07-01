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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class MealkitAdapter extends RecyclerView.Adapter<MealkitViewHolder>{
    private static final String TAG = "MealkitAdapter";

    private FirebaseStorage storage;
    private StorageReference storageRef;

    List<Mealkit> mealkits;

    Context context;

    // member variable for the interface
    IOnRowClickedListener listenerInterface;

    public MealkitAdapter(List<Mealkit> mealkits, IOnRowClickedListener listener) {
        this.mealkits = mealkits;
        this.listenerInterface = listener;
    }

    @NonNull
    @Override
    public MealkitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        this.context = context;


        // get the custom row layout
        View mealkitRowView = inflater.inflate(R.layout.mealkit_row_layout, parent, false);
        mealkitRowView.setBackgroundColor(Color.argb(255, 5, 136, 140));

        // Return the view holder
        MealkitViewHolder holder = new MealkitViewHolder(mealkitRowView, listenerInterface);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MealkitViewHolder holder, int position) {
        Mealkit mk = mealkits.get(position);
        //int id = holder.itemView.getResources().getIdentifier(mk.getPhoto(), "drawable", "ca.louisechan.recyclerviewinclass");

        holder.tvMealkitName.setTextColor(Color.BLACK);
        holder.tvMealkitDesc.setTextColor(Color.BLACK);
        holder.tvMealkitName.setText(mk.getName());
        holder.tvMealkitDesc.setText(mk.getDescription());

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference("/mealkits/" + mk.getImageName());
        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String imageURL = uri.toString();
                Log.d(TAG, "onSuccess: Image url: " + imageURL);
                Glide.with(MealkitAdapter.this.context).load(imageURL).into(holder.ivMealkitPhoto);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: Number of mealkits in list is " + mealkits.size());
        return mealkits.size();
    }
}
