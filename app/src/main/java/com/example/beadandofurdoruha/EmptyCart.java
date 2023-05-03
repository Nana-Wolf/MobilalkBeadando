package com.example.beadandofurdoruha;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class EmptyCart extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty_cart);
    }

    public void cancel(View view) {
        Intent intent = new Intent(getApplicationContext(),ShopActivity.class);
        startActivity(intent);
        finish();
    }
}