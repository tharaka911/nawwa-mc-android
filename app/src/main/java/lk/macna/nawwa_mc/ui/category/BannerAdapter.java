package lk.macna.nawwa_mc.ui.category;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import lk.macna.nawwa_mc.R;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private List<Integer> bannerImages;

    public BannerAdapter(List<Integer> bannerImages) {
        this.bannerImages = bannerImages;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        int bannerImage = bannerImages.get(position);
        holder.imageViewBanner.setImageResource(bannerImage);
    }

    @Override
    public int getItemCount() {
        return bannerImages.size();
    }

    public List<Integer> getBannerImages() {
        return bannerImages;
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewBanner;

        BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewBanner = itemView.findViewById(R.id.imageViewBanner);
        }
    }
}