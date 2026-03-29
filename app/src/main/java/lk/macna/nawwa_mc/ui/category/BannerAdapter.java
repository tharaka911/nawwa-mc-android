package lk.macna.nawwa_mc.ui.category;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lk.macna.nawwa_mc.R;

/**
 * BannerAdapter manages the display of promotional images in a horizontal slider.
 */
public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private List<Integer> bannerImages;

    public BannerAdapter(List<Integer> bannerImages) {
        this.bannerImages = bannerImages;
    }

    public void setBannerImages(List<Integer> bannerImages) {
        this.bannerImages = bannerImages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        if (bannerImages != null && !bannerImages.isEmpty()) {
            holder.bind(bannerImages.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return bannerImages != null ? bannerImages.size() : 0;
    }

    /**
     * ViewHolder for individual banner image items.
     */
    static class BannerViewHolder extends RecyclerView.ViewHolder {
        private final ImageView bannerImageView;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            bannerImageView = itemView.findViewById(R.id.imageViewBanner);
        }

        public void bind(int imageResId) {
            bannerImageView.setImageResource(imageResId);
        }
    }
}