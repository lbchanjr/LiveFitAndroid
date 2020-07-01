package ca.louisechan.livefitandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

public class SummaryActivity extends AppCompatActivity {
    private static final String TAG = "SummaryActivity";
    String mealkitName;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        Intent i = getIntent();
        mealkitName = i.getStringExtra("mealkit");
        Subscription subscription = (Subscription) i.getSerializableExtra("subscription");

        TextView tvSumOrderCode = (TextView) findViewById(R.id.txtOrderCode);
        TextView tvSumPackageName = (TextView) findViewById(R.id.txtMealPackage);
        TextView tvSumOrderDate = (TextView) findViewById(R.id.txtOrderDate);
        TextView tvSumPrice = (TextView) findViewById(R.id.txtSubPriceAmount);
        TextView tvSumDiscount = (TextView) findViewById(R.id.txtSubDiscPriceAmount);
        TextView tvSumSubTotal = (TextView) findViewById(R.id.txtSubtotalAmount);
        TextView tvSumTax = (TextView) findViewById(R.id.txtSubTaxAmount);
        TextView tvSumGrandTotal = (TextView) findViewById(R.id.txtGrandtotalAmount);

        String orderCode = generateRandomOrderCode();
        String orderDate = LocalDateTime.now().toString();

        tvSumOrderCode.setText("Order code: " + orderCode);
        tvSumPackageName.setText(mealkitName + " Package");
        tvSumOrderDate.setText("Order date: " + orderDate);
        tvSumPrice.setText(String.format("$%.2f", subscription.getPrice()));
        double discountPrice = subscription.getPrice() * (subscription.getDiscount()/100.0);
        tvSumDiscount.setText(String.format("-$%.2f", discountPrice));
        double subTotal = subscription.getPrice()-discountPrice;
        tvSumSubTotal.setText(String.format("$%.2f", subTotal));
        tvSumTax.setText(String.format("$%.2f", subscription.getPrice()*0.13));
        double grandTotal = subTotal + (subscription.getPrice()*0.13);
        tvSumGrandTotal.setText(String.format("$%.2f", grandTotal));

        FirebaseUser user = mAuth.getCurrentUser();
        String name = "";
        String email = "";
        if (user != null) {
            name = user.getDisplayName();
            email = user.getEmail();
        }

        String grandTot = String.format("%.2f", grandTotal);

        order = new Order(orderCode, orderDate, name, email, mealkitName, (int)subscription.getMonths(), grandTot);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent i = new Intent(this, SubscribeActivity.class);
        i.putExtra("mealkit", mealkitName);
        startActivity(i);

    }

    public void confirmOrderPressed(View view) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("orders").add(order).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d(TAG, "onSuccess: Order saved in database.");
            }
        });

        Intent i = new Intent(this, OrderConfirmActivity.class);
        i.putExtra("order", order);
        startActivity(i);
    }

    public String generateRandomOrderCode() {
        final String alphaNum = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(alphaNum.charAt(r.nextInt(alphaNum.length())));
        }

        Log.d(TAG, "generateRandomOrderCode: Generated order code: " + sb.toString());
        return sb.toString();
    }
}
