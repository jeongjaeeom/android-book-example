package com.example.paging;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PageKeyedDataSource;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PokeAPI pokeAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);
        final MainRecyclerViewAdapter adapter = new MainRecyclerViewAdapter();
        recyclerView.setAdapter(adapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://pokeapi.co/api/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        pokeAPI = retrofit.create(PokeAPI.class);

        createLiveData().observe(this, results -> adapter.submitList(results));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private LiveData<PagedList<Result>> createLiveData() {
        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(20)
                .setPageSize(20)
                .setPrefetchDistance(10)
                .build();
        return new LivePagedListBuilder<>(new androidx.paging.DataSource.Factory<String, Result>() {
            @Override
            public androidx.paging.DataSource<String, Result> create() {
                return new MainActivity.DataSource();
            }
        }, config).build();
    }

    private class DataSource extends PageKeyedDataSource<String, Result> {

        @Override
        public void loadInitial(@NonNull LoadInitialParams<String> params, @NonNull LoadInitialCallback<String, Result> callback) {
            try {
                Response body = pokeAPI.listPokemons().execute().body();
                callback.onResult(body.results, body.previous, body.next);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void loadBefore(@NonNull LoadParams<String> params, @NonNull LoadCallback<String, Result> callback) {
            String queryPart = params.key.split("\\?")[1];
            String[] queries = queryPart.split("&");
            Map<String, String> map = new HashMap<>();
            for (String query : queries) {
                String[] splited = query.split("=");
                map.put(splited[0], splited[1]);
            }
            try {
                Response body = pokeAPI.listPokemons(map.get("offset"), map.get("limit")).execute().body();
                callback.onResult(body.results, body.previous);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void loadAfter(@NonNull LoadParams<String> params, @NonNull LoadCallback<String, Result> callback) {
            String queryPart = params.key.split("\\?")[1];
            String[] queries = queryPart.split("&");
            Map<String, String> map = new HashMap<>();
            for (String query : queries) {
                String[] splited = query.split("=");
                map.put(splited[0], splited[1]);
            }
            try {
                Response body = pokeAPI.listPokemons(map.get("offset"), map.get("limit")).execute().body();
                callback.onResult(body.results, body.next);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class MainRecyclerViewAdapter extends PagedListAdapter<Result, MainRecyclerViewViewHolder> {
        protected MainRecyclerViewAdapter() {
            super(new DiffUtil.ItemCallback<Result>() {
                @Override
                public boolean areItemsTheSame(Result oldItem, Result newItem) {
                    return oldItem.name.equals(newItem.name);
                }

                @Override
                public boolean areContentsTheSame(Result oldItem, Result newItem) {
                    return oldItem.name.equals(newItem.name) && oldItem.url.equals(newItem.url);
                }
            });
        }

        @NonNull
        @Override
        public MainRecyclerViewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview, parent, false);
            return new MainRecyclerViewViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MainRecyclerViewViewHolder holder, int position) {
            Result item = getItem(position);
            holder.setTitle(item.name);
        }
    }

    private static class MainRecyclerViewViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;

        public MainRecyclerViewViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
        }

        public void setTitle(String title) {
            this.title.setText(title);
        }
    }
}
