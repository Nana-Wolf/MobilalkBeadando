package com.example.beadandofurdoruha;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder>{

    private static final String LOG_TAG = CartAdapter.class.getName();
    private ArrayList<ShoppingItem> mShoppingData;
    private ArrayList<ShoppingItem> mSoppingDataAll;
    private FirebaseUser user;
    private CollectionReference mItems;
    private ArrayList<ShoppingItem> mItemsData;
    private Integer itemLimit = 5;
    private String[] itemList;
    private String[] itemPrice;
    private TypedArray itemImageResources;
    private Context mContext;
    private List<CartModel> cart;
    private TextView editText;

    @SuppressLint("DefaultLocale")
    public CartAdapter(String[] itemList, String[] itemPrice, TypedArray itemImageResources, List<CartModel> cartList, TextView priceattheend) {

        this.itemList = itemList;
        this.itemPrice = itemPrice;
        this.itemImageResources = itemImageResources;
        cart = cartList;
        editText = priceattheend;

        int sum = 0;

        for(int position = 0; position<cart.size(); position++){
            String priceString = cart.get(position).getItemPrice();
            int price = Integer.parseInt(priceString.substring(0,priceString.length()-3));
            sum += price;
        }

        editText.setText(String.valueOf(sum) + " Ft");

        Log.i(LOG_TAG,this.itemList.toString());
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.layout_cart_item, parent, false));
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mContext = recyclerView.getContext();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


        //editText.setText(String.valueOf(Double.parseDouble(editText.getText().toString()) +Double.parseDouble(cart.get(position).getItemPrice().substring(0,cart.get(position).getItemPrice().length()-3)))) ;

        holder.getmTitleText().setText(cart.get(position).getItemName());
        holder.getTxtPrice().setText(cart.get(position).getItemPrice());


        holder.getBtnDelete().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String,Object> map = new HashMap<>();

                FirebaseFirestore.getInstance().collection("Cart").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){

                            String ujsplittedcart = "";
                            String ujsplittedprice ="";
                            String ujsplittedimage ="";

                            cart.remove(holder.getAdapterPosition());

                            for(int i = 0; i<cart.size();i++){
                                ujsplittedcart += cart.get(i).getItemName() + ", ";
                                ujsplittedprice += cart.get(i).getItemPrice() + ", ";
                                ujsplittedimage += cart.get(i).getItemImage() + ", ";
                            }

                            if(!ujsplittedcart.isEmpty()){
                                ujsplittedcart = ujsplittedcart.substring(0,ujsplittedcart.length()-2);
                                ujsplittedprice = ujsplittedprice.substring(0,ujsplittedprice.length()-2);
                                ujsplittedimage = ujsplittedimage.substring(0,ujsplittedimage.length()-2);
                            }

                                map.put("cart",ujsplittedcart);
                                map.put("price",ujsplittedprice);
                                map.put("imageres",ujsplittedimage);
                                FirebaseFirestore.getInstance().collection("Cart").
                                        document(FirebaseAuth.getInstance().
                                                getCurrentUser().
                                                getUid()).update(map).
                                        addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.i(LOG_TAG,"Törölve");
                                        Intent intent = new Intent(mContext,CartActivity.class);
                                        mContext.startActivity(intent);

                                    }
                                });
                            }
                        }

                });
            }
        });



    }

    @Override
    public int getItemCount() {
        return cart.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private TextView mTitleText;
        private ImageView btnMinus;
        private TextView txtQuantity;
        private ImageView btnPlus;
        private TextView txtPrice;
        private ImageView btnDelete;
        private Button place_Order;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mTitleText = itemView.findViewById(R.id.txtName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            btnDelete = itemView.findViewById(R.id.btnDelete);

        }

        public TextView getmTitleText() {
            return mTitleText;
        }

        public TextView getTxtPrice() {
            return txtPrice;
        }

        public ImageView getBtnDelete() {
            return btnDelete;
        }
    }

    private void queryData() {
        mItemsData.clear();
        mItems.orderBy("price", Query.Direction.DESCENDING).limit(itemLimit).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                ShoppingItem item = document.toObject(ShoppingItem.class);
                item.setId(document.getId());
                mItemsData.add(item);
            }

            if (mItemsData.size() == 0) {
                queryData();
            }
        });
    }

    public void delete(ShoppingItem item){
        DocumentReference ref = mItems.document(item._getId());

        ref.delete().addOnSuccessListener(success -> {
            Log.d(LOG_TAG,"Item is successfully deleted: "+ item._getId());
        }).addOnFailureListener(failure ->{
            Log.d(LOG_TAG, item._getId() + "cannot be deleted.");
        });

        queryData();
    }





}
