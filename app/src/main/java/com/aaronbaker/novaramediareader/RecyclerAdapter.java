package com.aaronbaker.novaramediareader;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE = 1;
    private final Context context;
    private final List<Article> listRecyclerItem;
    private final OnItemClickListener listener;
    private int offlinePositions = -1;

    public interface OnItemClickListener {
        void onItemClick(Article article);
    }

    public RecyclerAdapter(Context context, List<Article> listRecyclerItem, OnItemClickListener listener) {
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        final Button offlineButton = (Button) viewHolder.itemView.findViewById(R.id.article_pin_button);
        offlineButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int iPosition = 0;
                if (v.getParent().getParent().getParent() instanceof RecyclerView) {
                    iPosition = ((RecyclerView) v.getParent().getParent().getParent())
                            .getChildAdapterPosition((View) v.getParent().getParent());
                    // TODO: consider checking database for entry rather than using offlinePositions var to track
                    if (iPosition <= offlinePositions) {
                        if (iPosition <= listRecyclerItem.size() && iPosition >= 0) {
                            removePersistedAt(iPosition);
                        }
                        return;
                    }
                    addPersistAt(iPosition);
                }
            }
        });
        if (offlinePositions >= (position)) {
            offlineButton.setBackgroundResource((R.drawable.ic_baseline_offline_pin_green_24));
        } else {
            offlineButton.setBackgroundResource((R.drawable.ic_baseline_offline_pin_24));
        }
        bind(viewHolder, listener, position);
    }

    public void addPersistAt(int iPosition) {
        listRecyclerItem.add(0, listRecyclerItem.get(iPosition));
        persistArticle(listRecyclerItem.get(0));
        offlinePositions++;
        listRecyclerItem.remove(iPosition + 1);
        notifyDataSetChanged();
    }

    public void removePersistedAt(final int iPosition) {
        final String title = listRecyclerItem.get(iPosition).getTitle();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (AppDatabase.getDatabaseInstance(context).articleDao().findByTitle(title) != null) {
                    deleteArticle(title);
                    offlinePositions--;
                }
            }
        });
        listRecyclerItem.remove(iPosition);
        notifyItemRemoved(iPosition);
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
        Glide.with(itemViewHolder.itemView.getContext()).load(article.getImage())
                .placeholder(R.drawable.ic_nm_logo)
                .fitCenter()
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

    public void addListRecyclerItems(List<Article> listRecyclerItems) {
        this.listRecyclerItem.addAll(listRecyclerItems);
        notifyDataSetChanged();
    }

    public void setOfflinePositions(int offlinePositions) {
        this.offlinePositions = offlinePositions;
    }

    private void deleteArticle(final String title) {
        final AppDatabase db = AppDatabase.getDatabaseInstance(context);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                db.articleDao().deleteByTitle(title);
            }
        });
    }

    private void persistArticle(final Article article) {
        RequestQueue queue = Volley.newRequestQueue(context.getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, article.getPermalink(),
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(String response) {
                        article.setBody(response);
                        final AppDatabase db = AppDatabase.getDatabaseInstance(context);
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                article.setUid(db.articleDao().insertArticle(article));
                            }
                        });
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("RecyclerAdapter", "Could not persist article" + article.getTitle());
            }
        });
        queue.add(stringRequest);
    }
}
