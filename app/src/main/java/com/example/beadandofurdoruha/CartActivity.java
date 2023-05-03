package com.example.beadandofurdoruha;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private static final String LOG_TAG = CartActivity.class.getName();

    RecyclerView recyclerCart;
    RelativeLayout mainLayout;
    ImageView btnBack;
    TextView txtTotal;
    Button place_Holder;
    private List<CartModel> cart = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseFirestore.getInstance().collection("Cart").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful() && task.getResult().get("cart") != null){
                    List<String> splittedcart = Arrays.asList(task.getResult().get("cart").toString().split(", "));
                    List<String> splittedprice = Arrays.asList(task.getResult().get("price").toString().split(", "));
                    List<String> splittedimage = Arrays.asList(task.getResult().get("imageres").toString().split(", "));

                    for(int i=0; i<splittedcart.size(); i++){
                        cart.add(new CartModel(splittedcart.get(i),splittedprice.get(i),splittedimage.get(i)));
                    }

                    if(cart.size() == 0 || cart.get(0).getItemName().equals("")){
                        Intent intent = new Intent(getApplicationContext(),EmptyCart.class);
                        startActivity(intent);
                        finish();
                    }

                    GridLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 1);
                    recyclerCart.setLayoutManager(layoutManager);
                    CartAdapter adapter = new CartAdapter(
                            getResources().getStringArray(R.array.shopping_item_names),
                            getResources().getStringArray(R.array.shopping_item_price),
                            getResources().obtainTypedArray(R.array.shopping_item_images),
                            cart,
                            findViewById(R.id.priceend));
                    recyclerCart.setAdapter(adapter);
                }


            }
        });

        setContentView(R.layout.activity_cart);

        recyclerCart = findViewById(R.id.recycler_cart);
        mainLayout = findViewById(R.id.mainLayout);
        btnBack = findViewById(R.id.btnBack);
        txtTotal = findViewById(R.id.txtTotal);
        place_Holder = findViewById(R.id.place_holer);
    }


    public void cancel(View view) {
        finish();
    }

    public void place_holder(View view) {
        FirebaseFirestore.getInstance().collection("Cart").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    HashMap<String,Object> map = new HashMap<>();
                    map.put("Items",task.getResult().get("cart").toString());
                    FirebaseFirestore.getInstance().collection("Order").document().set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                FirebaseFirestore.getInstance().collection("Cart").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Intent intent = new Intent(getApplicationContext(),ShopActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            }
                        }
                    });
                    cart.clear();
                }
            }
        });
    }



}