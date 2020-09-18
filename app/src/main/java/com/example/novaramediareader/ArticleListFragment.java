package com.example.novaramediareader;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.List;

public class ArticleListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    public static final String HTTPS_NOVARAMEDIA_COM_API_ARTICLES = "https://novaramedia.com/api/articles/";
    private RecyclerView mRecyclerView;
    private List<Object> viewItems = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;


    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private int pageNumber = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.article_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.card_view);

        layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new RecyclerAdapter(getActivity(), viewItems, new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Article article) {
                Bundle bundle = new Bundle();
                bundle.putString("Description", article.getDescription());
                bundle.putString("Title", article.getTitle());
                bundle.putString("Permalink", article.getPermalink());

                FragmentTransaction tx = getActivity().getSupportFragmentManager().beginTransaction();
                ArticleFullscreenFragment articleFullscreenFragment = new ArticleFullscreenFragment();
                articleFullscreenFragment.setArguments(bundle);
                tx.add(R.id.fragment_container, articleFullscreenFragment).addToBackStack("articleFullScreen").commit();
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    retrieveArticles(++pageNumber);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int topRowVerticalPosition =
                        (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                mSwipeRefreshLayout.setEnabled(topRowVerticalPosition >= 0);
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.article_list_fragment);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        mSwipeRefreshLayout.post(new Runnable() {

            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                retrieveArticles(1);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public void retrieveArticles(final int pageNumberToRetrieve) {
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = HTTPS_NOVARAMEDIA_COM_API_ARTICLES + pageNumberToRetrieve;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(String response) {
                        Log.d("On response", "page number: " + pageNumberToRetrieve);
                        viewItems.addAll(ArticleJsonParser.addItemsFromJSON(response));
                        mAdapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mSwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getActivity().getApplicationContext(), "Cannot refresh feed", Toast.LENGTH_SHORT);
            }
        });
        queue.add(stringRequest);
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        viewItems.clear();
        retrieveArticles(0);
        pageNumber = 1;
        mSwipeRefreshLayout.setRefreshing(false);
    }
}