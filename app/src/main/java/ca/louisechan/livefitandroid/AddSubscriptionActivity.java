package ca.louisechan.livefitandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddSubscriptionActivity extends AppCompatActivity {
    private static final String TAG = "AddSubscriptionActivity";
    String mealUID = null;
    String mkUID = null;

    FirebaseFirestore db;
    HashMap<String, String> mealkitsHashMap = new HashMap<>();
    ArrayList<String> mkNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_subscription);

        db = FirebaseFirestore.getInstance();
        //mAuth = FirebaseAuth.getInstance();

        // Read all the mealkit name and uids and store it in an array
        db.collection("mealpackages").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document: task.getResult()) {
                        mealkitsHashMap.put(document.getString("name"), document.getId());
                        mkNames.add(document.getString("name"));
                    }
                }

                AddSubscriptionActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Populate spinner with mealkit names
                        Spinner spMealkit = (Spinner) findViewById(R.id.spinMealkit);

                        ArrayAdapter<String> mkAdapter = new ArrayAdapter<String>(AddSubscriptionActivity.this, android.R.layout.simple_spinner_item, mkNames);
                        mkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spMealkit.setAdapter(mkAdapter);
                    }
                });

            }
        });
    }

    public void submitNewSubs(View view) {
        EditText etMonth = (EditText) findViewById(R.id.edtSubsMonth);
        EditText etDisc = (EditText) findViewById(R.id.edtSubsDisc);
        EditText etPrice = (EditText) findViewById(R.id.edtSubsPrice);

        String price = etPrice.getText().toString();

        // Check if any of the fields are empty
        if (etMonth.getText().toString().isEmpty() || etDisc.getText().toString().isEmpty() || price.isEmpty()) {
            Toast.makeText(this, "No fields should be left blank.", Toast.LENGTH_SHORT).show();
            return;
        }
        double month = Double.parseDouble(etMonth.getText().toString());
        double discount = Double.parseDouble(etDisc.getText().toString());

        // All fields are filled up.
        // Get the doc uid of the mealkit so that we may be able to add the meal to its meals collection.
        Spinner spMealkit = (Spinner) findViewById(R.id.spinMealkit);
        String selectedMealkit = spMealkit.getSelectedItem().toString();

        mkUID = mealkitsHashMap.get(selectedMealkit);
        Log.d(TAG, "submitNewMeal: Mealkit is " + selectedMealkit + ", document UID is " + mkUID);

        addSubscriptionData(month, discount, price);


    }

    public void addSubscriptionData(double months, double discount, String price) {

        Map<String, Object> subsMap = new HashMap<>();
        subsMap.put("months", months);
        subsMap.put("discount", discount);
        subsMap.put("price", price);

        db.collection("mealpackages").document(mkUID).collection("subscriptions")
                .add(subsMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());

                        Toast.makeText(AddSubscriptionActivity.this, "Subscription added to database.", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(AddSubscriptionActivity.this, AdminUserMainActivity.class);
                        startActivity(i);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                        Toast.makeText(AddSubscriptionActivity.this, "Failure adding subscription to database.\nException: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }
}
