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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import lk.macna.nawwa_mc.R;
import lk.macna.nawwa_mc.model.Order;

/**
 * MyOrdersAdapter manages the list of user orders, displaying product info,
 * order status, and total pricing, and providing an interface for item clicks.
 */
public class MyOrdersAdapter extends RecyclerView.Adapter<MyOrdersAdapter.MyOrdersViewHolder> {

    private static final String TAG = "MyOrdersAdapter";
    private final List<Order> orderList;
    private final OnOrderItemClickListener listener;

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
        holder.bind(order, listener);
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    /**
     * ViewHolder for order items.
     */
    static class MyOrdersViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImageView;
        private final TextView productNameTextView;
        private final TextView orderStatusTextView;
        private final TextView totalPriceTextView;
        private final Button addToCartButton;

        public MyOrdersViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.imageViewProduct);
            productNameTextView = itemView.findViewById(R.id.textViewProductName);
            orderStatusTextView = itemView.findViewById(R.id.textViewOrderStatus);
            totalPriceTextView = itemView.findViewById(R.id.textViewTotalPrice);
            addToCartButton = itemView.findViewById(R.id.ButtonaddToCart);
        }

        public void bind(Order order, OnOrderItemClickListener listener) {
            productNameTextView.setText(order.getProductName());
            orderStatusTextView.setText(order.getOrderStatus());
            totalPriceTextView.setText(String.format("Total Price: LKR %.2f", order.getTotalPrice()));

            // Improved image loading with Glide to ensure clarity and caching
            Glide.with(itemView.getContext())
                    .load(order.getImageUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.new_product)
                            .error(R.drawable.new_product)
                            .diskCacheStrategy(DiskCacheStrategy.ALL))
                    .centerCrop()
                    .into(productImageView);

            addToCartButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddToCartClicked(order.getOrderID());
                }
            });
        }
    }
}
