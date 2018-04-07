package com.bignerdranch.android.photogallery;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {

  private static final String TAG = "PhotoGalleryFragment";

  private RecyclerView mPhotoRecyclerView;
  private List<GalleryItem> mItems = new ArrayList<>();

  public static PhotoGalleryFragment newInstance() {
    return new PhotoGalleryFragment();
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    setHasOptionsMenu(true);
    updateItems();
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
    mPhotoRecyclerView = (RecyclerView) view.findViewById(R.id.photo_recycler_view);
    mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
    setupAdapter();
    return view;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.fragment_photo_gallery, menu);

    MenuItem searchItem = menu.findItem(R.id.menu_item_search);
    final SearchView searchView = (SearchView) searchItem.getActionView();

    searchView.setOnQueryTextListener(
        new SearchView.OnQueryTextListener() {
          @Override
          public boolean onQueryTextSubmit(String query) {
            Log.d(TAG, "QueryTextSubmit: " + query);
            QueryPreferences.setStoredQuery(getActivity(), query);
            updateItems();
            return true;
          }

          @Override
          public boolean onQueryTextChange(String newText) {
            Log.d(TAG, "QueryTextChange: " + newText);
            return false;
          }
        });

    searchView.setOnSearchClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            String query = QueryPreferences.getStoredQuery(getActivity());
            searchView.setQuery(query, false);
          }
        });
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_item_clear:
        QueryPreferences.setStoredQuery(getActivity(), null);
        updateItems();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void updateItems() {
    String query = QueryPreferences.getStoredQuery(getActivity());
    new FetchItemsTask(query).execute();
  }

  private void setupAdapter() {
    if (isAdded()) {
      mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
    }
  }

  private class PhotoHolder extends RecyclerView.ViewHolder {
    private ImageView imageView;

    public PhotoHolder(View itemView) {
      super(itemView);
      imageView = (ImageView) itemView.findViewById(R.id.item_image_view);
    }

    public void bindDrawable(Drawable drawable) {
      imageView.setImageDrawable(drawable);
    }
  }

  private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
    private List<GalleryItem> galleryItems;

    public PhotoAdapter(List<GalleryItem> galleryItems) {
      this.galleryItems = galleryItems;
    }

    @NonNull
    @Override
    public PhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      LayoutInflater inflater = LayoutInflater.from(getActivity());
      View view = inflater.inflate(R.layout.gallery_item, parent, false);
      return new PhotoHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
      GalleryItem galleryItem = galleryItems.get(position);
      // Drawable placeHolder = getResources().getDrawable(R.drawable.bill_up_close);
      // holder.bindDrawable(placeHolder);
      // page 530
      Picasso.with(getContext())
          .load(galleryItem.getmUrl())
          .placeholder(R.drawable.bill_up_close)
          .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
      return galleryItems.size();
    }
  }

  private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {
    private String query;

    public FetchItemsTask(String query) {
      this.query = query;
    }

    @Override
    protected List<GalleryItem> doInBackground(Void... voids) {
      if (query == null) {
        return new FlickrFetchr().fetchRecentPhotos();
      } else {
        return new FlickrFetchr().searchPhotos(query);
      }
    }

    @Override
    protected void onPostExecute(List<GalleryItem> items) {
      mItems = items;
      setupAdapter();
    }
  }
}
