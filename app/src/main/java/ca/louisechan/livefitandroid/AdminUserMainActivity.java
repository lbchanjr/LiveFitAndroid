package ca.louisechan.livefitandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class AdminUserMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_main);
    }

    public void btnAddSubscriptionPressed(View view) {
        startActivity(new Intent(this, AddSubscriptionActivity.class));
    }

    public void btnAddMealPressed(View view) {
        startActivity(new Intent(this, AddMealActivity.class));
    }

    public void btnAddMealkitPressed(View view) {
        startActivity(new Intent(this, AddMealkitActivity.class));
    }

    public void logoutButtonPressed(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }
}
