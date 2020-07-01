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

public class MealAdapter extends RecyclerView.Adapter<MealViewHolder> {
    private static final String TAG = "MealAdapter";

    private FirebaseStorage storage;
    private StorageReference storageRef;
    Context context;

    List<Meal> meals;

    public MealAdapter(List<Meal> meals) {
        this.meals = meals;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        this.context = context;


        // get the custom row layout
        View mealRowView = inflater.inflate(R.layout.meal_row_layout, parent, false);

        // Return the view holder
        MealViewHolder holder = new MealViewHolder(mealRowView);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MealViewHolder holder, int position) {
        Meal m = meals.get(position);

        holder.tvMealName.setTextColor(Color.BLACK);
        holder.tvMealDesc.setTextColor(Color.BLACK);
        holder.tvMealCals.setTextColor(Color.BLACK);
        holder.tvMealName.setText(m.getName());
        holder.tvMealDesc.setText(m.getDescription());
        holder.tvMealCals.setText(m.getCalories());

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference("/meals/" + m.getImageName());
        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String imageURL = uri.toString();
                Log.d(TAG, "onSuccess: Image url: " + imageURL);
                Glide.with(MealAdapter.this.context).load(imageURL).into(holder.ivMealPhoto);
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
        Log.d(TAG, "getItemCount: Number of meals in list is " + meals.size());
        return meals.size();
    }
}
