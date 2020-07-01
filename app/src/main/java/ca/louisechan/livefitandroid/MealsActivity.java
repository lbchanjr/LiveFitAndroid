package ca.louisechan.livefitandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MealsActivity extends AppCompatActivity {
    private static final String TAG = "MealsActivity";
    private FirebaseFirestore db;

    ArrayList<Meal> meals = new ArrayList<Meal>();
    RecyclerView rv;
    MealAdapter adapter;
    String mealkitName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meals);

        Intent i = getIntent();
        mealkitName = i.getStringExtra("mealkit");

        TextView tvMealTitle = (TextView) findViewById(R.id.txtMealsLabel);
        tvMealTitle.setText(mealkitName + " Package Meals");

        // Read meals for the mealkit
        db = FirebaseFirestore.getInstance();
        db.collection("mealpackages").whereEqualTo("name", mealkitName).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    for (QueryDocumentSnapshot document: task.getResult()) {
                        String name = document.get("name").toString();
                        db.collection("mealpackages").document(document.getId()).collection("meals").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()) {

                                    // check if there are meals available for this package
                                    if (task.getResult().size() == 0) {
                                        // Disable subscribe button since there are no meals available
                                        Button button = (Button) findViewById(R.id.btnSubscribe);
                                        button.setEnabled(false);
                                        Toast.makeText(MealsActivity.this, "No meals available.\nPress back and select another meal package.", Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    for (QueryDocumentSnapshot mealDoc: task.getResult()) {
                                        Object nameObj = mealDoc.get("name");
                                        Object descObj = mealDoc.get("description");
                                        Object calObj = mealDoc.get("calories");
                                        Object warnObj = mealDoc.get("warning");
                                        Object imgObj = mealDoc.get("imageName");

                                        String name = nameObj == null ? "": nameObj.toString();
                                        String description = descObj == null ? "": descObj.toString();
                                        String calories = calObj == null ? "": calObj.toString();
                                        String imageFilename = imgObj == null ? null: imgObj.toString();
                                        String warning = warnObj == null ? "": warnObj.toString();

                                        // Add mealkit to array list.
                                        Meal m = new Meal(name, description, calories, imageFilename, warning);
                                        meals.add(m);
                                    }

                                    MealsActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // initialize the recycler view outlet
                                            rv = (RecyclerView) findViewById(R.id.rvMeals);

                                            // create an adapter & attachs the data source
                                            adapter = new MealAdapter(meals);

                                            // attach the adapter to the rv
                                            rv.setAdapter(adapter);

                                            rv.setLayoutManager(new LinearLayoutManager(MealsActivity.this));
                                        }
                                    });
                                }
                            }
                        });

                        break;
                    }

                }

            }
        });

    }

    public void subscribeButtonClicked(View view) {
        Log.d(TAG, "subscribeButtonClicked: Subscribe button clicked.");
        Intent i = new Intent(this, SubscribeActivity.class);
        i.putExtra("mealkit", mealkitName);
        startActivity(i);
    }
}
