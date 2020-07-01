package ca.louisechan.livefitandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class RegularUserMainActivity extends AppCompatActivity implements IOnRowClickedListener {
    private static final String TAG = "RegularUserMainActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    ImageView ivUserPhoto;
    TextView tvUsernameLabel;
    TextView tvUserEmailLabel;

    ArrayList<Mealkit> mealkits = new ArrayList<Mealkit>();
    RecyclerView rv;
    MealkitAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String name="", email="", urlString = null;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regular_user_main);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                // Name, email address, and profile photo Url
                name = profile.getDisplayName();
                email = profile.getEmail();
                Uri photoUrl = profile.getPhotoUrl();
                if (photoUrl != null) {
                    urlString = photoUrl.toString();
                } else {
                    urlString = null;
                }

                Log.d(TAG, "onCreate: Name: " + name + ", Email: " + email + ", photoUrl: " + urlString);
                break;
            }
        }

        // Show logged user info
        ivUserPhoto = (ImageView) findViewById(R.id.imgLoggedUserPhoto);
        if (urlString == null ) {
            ivUserPhoto.setImageResource(R.drawable.noimage);
        } else {
            Glide.with(getApplicationContext()).load(urlString).into(ivUserPhoto);
        }

        tvUserEmailLabel = (TextView) findViewById(R.id.txtRegUserEmailLabel);
        tvUserEmailLabel.setText(email);
        tvUsernameLabel = (TextView) findViewById(R.id.txtRegUserNameLabel);
        tvUsernameLabel.setText("Hi, " + name);

        // initialize the data set
        createDataSet();

    }

    // Helper function to create the data
    private void createDataSet() {
        // Read all mealkits from firebase
        db.collection("mealpackages").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document: task.getResult()) {
                        String name = document.get("name").toString();
                        String description = document.get("description").toString();
                        Object imageObj = document.get("imageName");
                        String imageFilename = imageObj == null ? null: imageObj.toString();

                        // Add mealkit to array list.
                        Mealkit mk = new Mealkit(name, description, imageFilename);
                        mealkits.add(mk);
                    }

                    RegularUserMainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // initialize the recycler view outlet
                            rv = (RecyclerView) findViewById(R.id.rvMealkits);

                            // create an adapter & attachs the data source
                            adapter = new MealkitAdapter(mealkits, RegularUserMainActivity.this);

                            // attach the adapter to the rv
                            rv.setAdapter(adapter);

                            // 4. EXTRA STEP!
                            rv.setLayoutManager(new LinearLayoutManager(RegularUserMainActivity.this));
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onClick(int position) {
        // output the name of the mealkit that was clicked
        Mealkit m = this.mealkits.get(position);
        Log.d(TAG, "onClick: Mealkit " + m.getName() + " was clicked.");

        // Switch to meal details display activity
        Intent i = new Intent(this, MealsActivity.class);
        i.putExtra("mealkit", m.getName());
        startActivity(i);
    }

    public void logoutButtonPressed(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }
}
