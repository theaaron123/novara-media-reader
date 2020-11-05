package com.aaronbaker.novaramediareader;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
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

public class ArticleListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener {
    public static final String HTTPS_NOVARAMEDIA_COM_API_ARTICLES = "https://novaramedia.com/api/articles/";
    public static final String HTTPS_NOVARAMEDIA_COM_API_SEARCH = "https://novaramedia.com/wp-json/wp/v2/posts/?search=";
    public static final String DESCRIPTION = "Description";
    public static final String TITLE = "Title";
    public static final String PERMALINK = "Permalink";
    public static final String IMAGE = "Image";
    private static final String QUERY = "QUERY";
    public static final String BODY = "BODY";
    private RecyclerView mRecyclerView;
    private List<Article> viewItems = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SearchView mSearchView;

    private RecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private int pageNumber = 1;
    private RecyclerAdapter mSearchAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.article_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.card_view);

        layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.getItemAnimator().setChangeDuration(0);

        mAdapter = new RecyclerAdapter(getActivity(), viewItems, new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Article article) {
                transitionToArticle(article);
            }
        });
        mAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (isRecyclerScrollable(mRecyclerView)) {
                        retrieveArticles(++pageNumber);
                    }
                }
            }

            public boolean isRecyclerScrollable(RecyclerView recyclerView) {
                return recyclerView.computeHorizontalScrollRange() > recyclerView.getWidth() || recyclerView.computeVerticalScrollRange() > recyclerView.getHeight();
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
                Bundle bundle = getArguments();
                if (bundle != null && bundle.containsKey(QUERY)) {
                    retrieveSearch(bundle.getString(QUERY));
                } else {
                    loadPersistedArticles();
                    retrieveArticles(1);
                }
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (menu.size() == 0) {
            getActivity().getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        }
        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(this);
    }

    private void retrieveSearch(final String query) {
        final RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = HTTPS_NOVARAMEDIA_COM_API_SEARCH + query;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(String response) {
                        final List<Article> articles = ArticleJsonParser.addItemsFromJSONSearch(response);

                        for (Article article : articles) {
                            StringRequest imageRequest = getSearchedArticleImageUrl(article);
                            queue.add(imageRequest);
                        }

                        mSearchAdapter = new RecyclerAdapter(getActivity(), articles, new RecyclerAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(Article article) {
                                transitionToArticle(article);
                            }
                        });
                        mRecyclerView.swapAdapter(mSearchAdapter, true);
                        Log.d("On response", "page number: " + query);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mSwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getActivity().getApplicationContext(), "Cannot search", Toast.LENGTH_SHORT);
            }
        });
        queue.add(stringRequest);
    }

    private StringRequest getSearchedArticleImageUrl(final Article article) {
        String url = "https://novaramedia.com/wp-json/wp/v2/media/" + article.getImage();
        return new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(String response) {
                        String imageUrlFromJSON = ArticleJsonParser.parseImageUrlFromJSON(response);
                        article.setImage(imageUrlFromJSON);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mSwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getActivity().getApplicationContext(), "Cannot search", Toast.LENGTH_SHORT).show();
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
                        viewItems.addAll(ArticleJsonParser.addItemsFromJSONArticle(response));
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

    public void loadPersistedArticles() {
        new OfflineArticlesAsyncTask(getContext(), mAdapter).execute();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        viewItems.clear();
        loadPersistedArticles();
        retrieveArticles(0);
        pageNumber = 1;
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Bundle bundle = new Bundle();
        bundle.putString(QUERY, query);
        FragmentTransaction tx = getActivity().getSupportFragmentManager().beginTransaction();
        ArticleListFragment searchListFragment = new ArticleListFragment();
        searchListFragment.setArguments(bundle);
        tx.add(R.id.fragment_container, searchListFragment).addToBackStack("articleFullScreen").commit();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    private void transitionToArticle(Article article) {
        Bundle bundle = new Bundle();
        bundle.putString(DESCRIPTION, article.getDescription());
        bundle.putString(TITLE, article.getTitle());
        bundle.putString(PERMALINK, article.getPermalink());
        bundle.putString(IMAGE, article.getImage());
        bundle.putString(BODY, article.getBody());

        FragmentTransaction tx = getActivity().getSupportFragmentManager().beginTransaction();
        ArticleFullscreenFragment articleFullscreenFragment = new ArticleFullscreenFragment();
        articleFullscreenFragment.setArguments(bundle);
        tx.add(R.id.fragment_container, articleFullscreenFragment).addToBackStack("articleFullScreen").commit();
    }
}