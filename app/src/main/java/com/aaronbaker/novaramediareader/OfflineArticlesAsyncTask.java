package com.aaronbaker.novaramediareader;

import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.List;

class OfflineArticlesAsyncTask extends AsyncTask<Object, Object, List<Article>> {

    RecyclerAdapter adapter;
    private WeakReference<Context> activityReference;

    public OfflineArticlesAsyncTask(Context context, RecyclerAdapter adapter) {
        this.adapter = adapter;
        this.activityReference = new WeakReference<>(context);
    }

    @Override
    protected List<Article> doInBackground(Object... objects) {
        AppDatabase databaseInstance = AppDatabase.getDatabaseInstance(activityReference.get());
        List<Article> articles = databaseInstance.userDao().getAll();
        adapter.setOfflinePositions(articles.size() - 1);
        return articles;
    }

    @Override
    protected void onPostExecute(List<Article> o) {
        super.onPostExecute(o);
        adapter.addListRecyclerItems(o);
        adapter.notifyDataSetChanged();
    }
}
