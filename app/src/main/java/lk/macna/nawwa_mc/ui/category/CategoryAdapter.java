package lk.macna.nawwa_mc.ui.category;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lk.macna.nawwa_mc.R;
import lk.macna.nawwa_mc.model.Category;

/**
 * CategoryAdapter handles the binding of Category data to the category list UI.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final List<Category> categories;

    public CategoryAdapter(List<Category> categories) {
        this.categories = categories;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_item, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    /**
     * ViewHolder class for Category items.
     */
    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView categoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.categoryName);
        }

        /**
         * Binds category data to the views and sets up click listeners.
         */
        public void bind(Category category) {
            categoryName.setText(category.getName());
            
            itemView.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("categoryId", category.getId());
                bundle.putString("categoryName", category.getName());
                
                // Navigate to products fragment (nav_product)
                Navigation.findNavController(v).navigate(R.id.nav_product, bundle);
            });
        }
    }
}