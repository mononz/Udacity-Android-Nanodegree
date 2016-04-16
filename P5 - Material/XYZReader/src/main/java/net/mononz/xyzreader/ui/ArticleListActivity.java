package net.mononz.xyzreader.ui;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;

import net.mononz.xyzreader.R;
import net.mononz.xyzreader.data.ArticleLoader;
import net.mononz.xyzreader.data.ItemsContract;
import net.mononz.xyzreader.data.UpdaterService;

public class ArticleListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,SwipeRefreshLayout.OnRefreshListener {

    private Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    private boolean mIsRefreshing = false;
    private AdapterArticleList mAdapterHeroes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mAdapterHeroes = new AdapterArticleList(this, null, new AdapterArticleList.Callback() {
            @Override
            public void onItemClick(long id) {
                Intent intent = new Intent(Intent.ACTION_VIEW, ItemsContract.Items.buildItemUri(id));
                startActivity(intent);
            }
        });
        mAdapterHeroes.setHasStableIds(true);

        StaggeredGridLayoutManager sglm = new StaggeredGridLayoutManager(getResources().getInteger(R.integer.list_column_count), StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(sglm);
        mRecyclerView.setAdapter(mAdapterHeroes);

        getLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null) {
            refresh();
        }

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(ArticleListActivity.this, R.color.theme_accent));
    }

    @Override
    public void onRefresh() {
        mIsRefreshing = true;
        refresh();
    }

    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mRefreshingReceiver, new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mRefreshingReceiver);
    }

    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };

    private void updateRefreshingUI() {
        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data) {
        mAdapterHeroes.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapterHeroes.swapCursor(null);
    }

}
