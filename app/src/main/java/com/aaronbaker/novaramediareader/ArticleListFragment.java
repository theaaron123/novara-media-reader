package com.aaronbaker.novaramediareader;

import android.content.Intent;
import android.content.res.Configuration;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ArticleListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener {
    public static final String HTTPS_NOVARAMEDIA_COM_API_ARTICLES = "https://novaramedia.com/wp-json/wp/v2/posts/?categories=2&page=";
    public static final String HTTPS_NOVARAMEDIA_COM_API_VIDEO = "https://novaramedia.com/wp-json/wp/v2/posts/?categories=828&page=";
    public static final String HTTPS_NOVARAMEDIA_COM_API_SEARCH = "https://novaramedia.com/wp-json/wp/v2/posts/?search=";
    public static final String DESCRIPTION = "Description";
    public static final String TITLE = "Title";
    public static final String PERMALINK = "Permalink";
    public static final String IMAGE = "Image";
    public static final String BODY = "BODY";
    public static final String NOVARAMEDIA_COM_ABOUT = "https://novaramedia.com/about/";
    private static final String QUERY = "QUERY";
    private RecyclerView mRecyclerView;
    private List<Article> articleViewItems;
    private List<Article> videoViewItems;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerAdapter mArticleAdapter;
    private RecyclerAdapter mVideoAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private int articlePageNumber = 1;
    private int videoPageNumber = 1;
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
        articleViewItems = new ArrayList<>();
        videoViewItems = new ArrayList<>();

        mRecyclerView = (RecyclerView) view.findViewById(R.id.card_view);

        layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.getItemAnimator().setChangeDuration(0);

        mArticleAdapter = new RecyclerAdapter(getActivity(), articleViewItems, new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Article article) {
                transitionToArticle(article);
            }
        });
        mArticleAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mArticleAdapter);
        mRecyclerView.addOnScrollListener(getScrollListener(false));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(getSimpleItemTouchCallback());
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

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
                    retrievePosts(1, HTTPS_NOVARAMEDIA_COM_API_ARTICLES, articleViewItems, mArticleAdapter);
                }
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                view.findViewById(R.id.bottom_navigation);
        Configuration configuration = getResources().getConfiguration();
        if ((configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
            bottomNavigationView.setBackgroundResource(R.color.cardview_dark_background);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            bottomNavigationView.setOnApplyWindowInsetsListener(null);
        }
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_articles:
                                mRecyclerView.setAdapter(mArticleAdapter);
                                break;
                            case R.id.action_videos:
                                if (mVideoAdapter == null) {
                                    mVideoAdapter = new RecyclerAdapter(getActivity(), videoViewItems, new RecyclerAdapter.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(Article article) {
                                            transitionToArticle(article);
                                        }
                                    });
                                    retrievePosts(1, HTTPS_NOVARAMEDIA_COM_API_VIDEO, videoViewItems, mVideoAdapter);
                                }
                                mRecyclerView.setAdapter(mVideoAdapter);
                                mRecyclerView.addOnScrollListener(getScrollListener(true));
                        }
                        return true;
                    }
                });
    }

    @NotNull
    private RecyclerView.OnScrollListener getScrollListener(final Boolean isVideo) {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (isRecyclerScrollable(mRecyclerView)) {
                        if (isVideo) {
                            retrievePosts(++videoPageNumber, HTTPS_NOVARAMEDIA_COM_API_VIDEO, videoViewItems, mVideoAdapter);
                        }
                        retrievePosts(++articlePageNumber, HTTPS_NOVARAMEDIA_COM_API_ARTICLES, articleViewItems, mArticleAdapter);
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
        };
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

    public void retrievePosts(final int pageNumberToRetrieve, String apiURL, final List<Article> viewItems, final RecyclerAdapter adapter) {
        final RequestQueue queue = ApplicationController.getInstance().getRequestQueue();
        String url = apiURL + pageNumberToRetrieve;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(String response) {
                        Log.d("On response", "page number: " + pageNumberToRetrieve);
                        List<Article> articles = ArticleJsonParser.addItemsFromJSONArticle(response);
                        for (Article article : articles) {
                            StringRequest imageRequest = getArticleImageUrl(article);
                            queue.add(imageRequest);
                        }
                        viewItems.addAll(articles);
                        adapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mSwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getActivity().getApplicationContext(), "Cannot refresh feed", Toast.LENGTH_SHORT).show();
            }
        });
        ApplicationController.getInstance().getRequestQueue().add(stringRequest);
    }

    public void loadPersistedArticles() {
        new AsyncCoroutine<Integer, List<Article>>() {
            @Override
            public List<Article> doInBackground(Integer... params) {
                AppDatabase databaseInstance = AppDatabase.getDatabaseInstance(getContext());
                List<Article> articles = databaseInstance.articleDao().getAll();
                mArticleAdapter.setOfflinePositions(articles.size() - 1);
                return articles;
            }

            @Override
            public void onPostExecute(@Nullable List<Article> o) {
                super.onPostExecute(o);
                mArticleAdapter.addListRecyclerItems(o);
                mArticleAdapter.notifyDataSetChanged();
            }

            @Override
            public void onPreExecute() {
            }
        }.execute();
        mArticleAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        articleViewItems.clear();
        loadPersistedArticles();
        retrievePosts(0, HTTPS_NOVARAMEDIA_COM_API_ARTICLES, articleViewItems, mArticleAdapter);
        articlePageNumber = 1;
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mArticleAdapter.notifyDataSetChanged();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_contact) {
            Article about = new Article();
            about.setTitle("Contact Details: Novara Media");
            about.setBody(getString(R.string.contact_html));
            transitionToArticle(about);
            return true;
        } else if (itemId == R.id.action_share) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Novara Media link:");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.play_store_url));
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
            return false;
        }
        return super.onOptionsItemSelected(item);
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

    private void retrieveSearch(final String query) {
        final RequestQueue queue = ApplicationController.getInstance().getRequestQueue();
        String url = HTTPS_NOVARAMEDIA_COM_API_SEARCH + query;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(String response) {
                        final List<Article> articles = ArticleJsonParser.addItemsFromJSONSearch(response);

                        for (Article article : articles) {
                            StringRequest imageRequest = getArticleImageUrl(article);
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
                Toast.makeText(getActivity().getApplicationContext(), "Cannot search", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(stringRequest);
    }

    private StringRequest getArticleImageUrl(final Article article) {
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

    @NotNull
    private ItemTouchHelper.SimpleCallback getSimpleItemTouchCallback() {
        return new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                mArticleAdapter.removePersistedAt(position);
            }
        };
    }
}