package ca.louisechan.livefitandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class SubscribeActivity extends AppCompatActivity implements IOnRowClickedListener {
    private static final String TAG = "SubscribeActivity";

    private FirebaseFirestore db;

    ArrayList<Subscription> subscriptions = new ArrayList<Subscription>();
    RecyclerView rv;
    SubscribeAdapter adapter;
    String mealkitName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe);

        mealkitName = getIntent().getStringExtra("mealkit");

        // Read meals for the mealkit
        db = FirebaseFirestore.getInstance();
        db.collection("mealpackages").whereEqualTo("name", mealkitName).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    for (QueryDocumentSnapshot document: task.getResult()) {
                        String name = document.get("name").toString();
                        db.collection("mealpackages").document(document.getId()).collection("subscriptions").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()) {

                                    if (task.getResult().size() == 0) {
                                        Toast.makeText(SubscribeActivity.this, "No subscription options available.\nPress back and select another meal package.", Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    for (QueryDocumentSnapshot subDoc: task.getResult()) {

                                        double months = subDoc.getDouble("months");
                                        double discount = subDoc.getDouble("discount");
                                        String priceStr = subDoc.getString("price");
                                        double price =  Double.parseDouble(priceStr);


                                        // Add Subscription to array list.
                                        Subscription s = new Subscription(months, discount, price);
                                        subscriptions.add(s);
                                    }

                                    SubscribeActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // initialize the recycler view outlet
                                            rv = (RecyclerView) findViewById(R.id.rvSubscription);

                                            // create an adapter & attach the data source
                                            adapter = new SubscribeAdapter(subscriptions, SubscribeActivity.this);

                                            // attach the adapter to the rv
                                            rv.setAdapter(adapter);

                                            rv.setLayoutManager(new LinearLayoutManager(SubscribeActivity.this));
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

    @Override
    public void onClick(int position) {
        // output the name of the mealkit that was clicked
        Subscription s = this.subscriptions.get(position);
        Log.d(TAG, "onClick: Subscription: " + s.getMonths() + "-Month Plan was clicked.");

        // Switch to meal details display activity
        Intent i = new Intent(this, SummaryActivity.class);
        i.putExtra("subscription", s);
        i.putExtra("mealkit", mealkitName);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (subscriptions.size() == 0) {
            // Pressing back when no subscription options are available will return user to package list.
            startActivity(new Intent(this, RegularUserMainActivity.class));
            return;
        }

        Intent i = new Intent(this, MealsActivity.class);
        i.putExtra("mealkit", mealkitName);
        startActivity(i);

    }
}
