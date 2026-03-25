package lk.macna.nawwa_mc.ui.my_orders;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import lk.macna.nawwa_mc.R;
import lk.macna.nawwa_mc.model.Order;

public class MyOrdersAdapter extends RecyclerView.Adapter<MyOrdersAdapter.MyOrdersViewHolder> {

    private List<Order> orderList;
    private OnOrderItemClickListener listener;
    private static final String TAG = "MyOrdersAdapterLog";

    public interface OnOrderItemClickListener {
        void onAddToCartClicked(String orderId);
    }

    public MyOrdersAdapter(List<Order> orderList, OnOrderItemClickListener listener) {
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyOrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_item, parent, false);
        return new MyOrdersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyOrdersViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.textViewProductName.setText(order.getProductName());
        holder.textViewOrderStatus.setText(order.getOrderStatus());
        holder.textViewTotalPrice.setText(String.format("Total Price: $%.2f", order.getTotalPrice()));

        // Load image using Glide
        Glide.with(holder.itemView.getContext())
                .load(order.getImageUrl())
                .placeholder(R.drawable.new_product)
                .into(holder.imageViewProduct);

        holder.buttonAddToCart.setOnClickListener(v -> {
            Toast.makeText(holder.itemView.getContext(), "Fill your order dispatch address for, " + order.getProductName() + " the product", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "ButtonAddToCart clicked for orderId: " + order.getOrderID());
            listener.onAddToCartClicked(order.getOrderID());
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class MyOrdersViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewProduct;
        TextView textViewProductName;
        TextView textViewOrderStatus;
        TextView textViewTotalPrice;
        Button buttonAddToCart;

        public MyOrdersViewHolder(View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewOrderStatus = itemView.findViewById(R.id.textViewOrderStatus);
            textViewTotalPrice = itemView.findViewById(R.id.textViewTotalPrice);
            buttonAddToCart = itemView.findViewById(R.id.ButtonaddToCart);
        }
    }
}