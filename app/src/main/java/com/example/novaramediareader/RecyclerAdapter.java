package com.example.novaramediareader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE = 1;
    private final Context context;
    private final List<Object> listRecyclerItem;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Article article);
    }

    public RecyclerAdapter(Context context, List<Object> listRecyclerItem, OnItemClickListener listener) {
        this.context = context;
        this.listRecyclerItem = listRecyclerItem;
        this.listener = listener;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView description;
        private ImageView imageView;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.title);
            description = (TextView) itemView.findViewById(R.id.description);
            imageView = (ImageView) itemView.findViewById(R.id.list_image);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        switch (i) {
            case TYPE:

            default:
                View layoutView = LayoutInflater.from(viewGroup.getContext()).inflate(
                        R.layout.list_item, viewGroup, false);
                return new ItemViewHolder((layoutView));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        bind(viewHolder, listener, position);
    }

    public void bind(final RecyclerView.ViewHolder viewHolder, final OnItemClickListener listener, int position) {

        ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
        final Article article = (Article) listRecyclerItem.get(position);

        itemViewHolder.name.setText(article.getTitle());
        itemViewHolder.description.setText(article.getDescription());
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(article);
            }
        });
        Glide.with(itemViewHolder.itemView.getContext()).load(article.getImagelink())
                .placeholder(R.drawable.ic_launcher_background)
                .dontAnimate()
                .into(itemViewHolder.imageView);
    }

    @Override
    public int getItemCount() {
        return listRecyclerItem.size();
    }

    @Override
    public long getItemId(int position) {
        Article product = (Article) listRecyclerItem.get(position);
        return product.hashCode();
    }
}
