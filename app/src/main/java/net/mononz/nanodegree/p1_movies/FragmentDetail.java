package net.mononz.nanodegree.p1_movies;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import net.mononz.nanodegree.R;
import net.mononz.nanodegree.p1_movies.api.Api;
import net.mononz.nanodegree.p1_movies.data.MoviesContract;

public class FragmentDetail extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private Toolbar toolbar;
    private Cursor mDetailCursor;
    private View mRootView;
    private int mPosition;
    private ImageView mImageView;
    private TextView mTextView;
    private TextView mUriText;
    private Uri mUri;
    private static final int CURSOR_LOADER_ID = 0;

    public static FragmentDetail newInstance(int position, Uri uri) {
        FragmentDetail fragment = new FragmentDetail();
        Bundle args = new Bundle();
        fragment.mPosition = position;
        fragment.mUri = uri;
        args.putInt(MoviesContract.MovieEntry._ID, position);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentDetail() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mImageView = (ImageView) rootView.findViewById(R.id.flavor_icon);
        mTextView = (TextView) rootView.findViewById(R.id.version_description);
        mUriText = (TextView) rootView.findViewById(R.id.uri);
        Bundle args = this.getArguments();
        getLoaderManager().initLoader(CURSOR_LOADER_ID, args, FragmentDetail.this);

        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            getFragmentManager().popBackStack();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        String selection = null;
        String [] selectionArgs = null;
        if (args != null) {
            selection = MoviesContract.MovieEntry._ID;
            selectionArgs = new String[]{String.valueOf(mPosition)};
        }
        return new CursorLoader(getActivity(),
                mUri,
                null,
                selection,
                selectionArgs,
                null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
    }

    // Set the cursor in our CursorAdapter once the Cursor is loaded
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mDetailCursor = data;
        mDetailCursor.moveToFirst();
        DatabaseUtils.dumpCursor(data);

        int nameIndex = mDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_TITLE);
        toolbar.setTitle(mDetailCursor.getString(nameIndex));

        int imageIndex = mDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_BACKDROP);
        Glide.with(this)
                .load(Api.getImage(mDetailCursor.getString(imageIndex)))
                .into(mImageView);

        int releaseIndex = mDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE);
        mTextView.setText(mDetailCursor.getString(releaseIndex));

        int synopsisIndex = mDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_OVERVIEW);
        mUriText.setText(mDetailCursor.getString(synopsisIndex));
    }

    // reset CursorAdapter on Loader Reset
    @Override
    public void onLoaderReset(Loader<Cursor> loader){
        mDetailCursor = null;
    }

}
