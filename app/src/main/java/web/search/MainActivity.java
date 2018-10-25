package web.search;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity implements AsyncDelegate {
    private static final String TAG = "MainActivity";
    private final int PERMISSIONS_REQUEST_INTERNET = 12;
    private EditText eText;
    private GObjectAdapter mGObjectAdapter;
    private boolean loading;
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private LinearLayoutManager mLayoutManager;
    private RadioGroup group;
    private boolean currentType = false;
    private RecyclerView recyclerView;
    private GridLayoutManager mGridLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_t);
        resolvePermissionAndInitView();
    }

    private void resolvePermissionAndInitView() {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, PERMISSIONS_REQUEST_INTERNET);
        } else {
            initView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_INTERNET: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initView();
                } else {
                    this.finish();
                }
            }
        }
    }

    private void runAsyncTask(String query, int firstID) {
        mGObjectAdapter.getData().add(null);
        mGObjectAdapter.notifyItemInserted(mGObjectAdapter.getData().size() - 1);
        SearchTask searcher = new SearchTask(this, getApplicationContext());
        searcher.setFirstItemID(firstID);
        searcher.setType(currentType);
        searcher.execute(query);
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        eText = (EditText) findViewById(R.id.editText);
        //Button button = (Button) findViewById(R.id.searchButton);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        group = (RadioGroup) findViewById(R.id.radio);
       // assert button != null;

        eText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (eText.getRight() - eText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        if (eText.getText().toString().trim().length() > 0) {
                            callAPI();
                            return true;
                        } else {
                            Toast.makeText(getApplicationContext(), "Please Enter Input ", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                return false;
            }
        });


        mGObjectAdapter = new GObjectAdapter(getApplicationContext());
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mGridLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        assert recyclerView != null;
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mGObjectAdapter);
        loading = true;
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    if (!currentType) {
                        visibleItemCount = mLayoutManager.getChildCount();
                        totalItemCount = mLayoutManager.getItemCount();
                        pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();
                    } else {
                        visibleItemCount = mGridLayoutManager.getChildCount();
                        totalItemCount = mGridLayoutManager.getItemCount();
                        pastVisibleItems = mGridLayoutManager.findFirstVisibleItemPosition();
                    }

                    if (loading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading = false;
                            runAsyncTask(eText.getText().toString(), mGObjectAdapter.getItemCount() + 1);
                        }
                    }
                }
            }
        });
    }

    private void callAPI() {
        Log.d(TAG, "callAPI: ");
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(eText.getWindowToken(), 0);
        updateType();
        switchRecyclerLayout();
        mGObjectAdapter.getData().clear();
        mGObjectAdapter.notifyDataSetChanged();
        runAsyncTask(eText.getText().toString(), 1);
    }

    private void switchRecyclerLayout() {
        if (!currentType) {
            mLayoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(mGObjectAdapter);
        } else {
            mGridLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
            recyclerView.setLayoutManager(mGridLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(mGObjectAdapter);
            SpacesItemDecoration decoration = new SpacesItemDecoration(16);
            recyclerView.addItemDecoration(decoration);
        }

    }

    private void updateType() {
        this.currentType = false;
    }

    @Override
    public void asyncComplete(List<GObject> data, int error_code) {
        Log.d(TAG, "asyncComplete: ");
        loading = true;
        mGObjectAdapter.getData().remove(mGObjectAdapter.getData().size() - 1);
        mGObjectAdapter.notifyItemRemoved(mGObjectAdapter.getData().size());
        if (data != null && !data.isEmpty()) {
            mGObjectAdapter.getData().addAll(data);
            mGObjectAdapter.notifyDataSetChanged();
            Log.d(TAG, "asyncComplete: " + mGObjectAdapter.getData().size());
/*
            if (mGObjectAdapter.getData().size() < 15) {
                runAsyncTask(eText.getText().toString(), mGObjectAdapter.getItemCount() + 1);
            }
*/
        } else {
            Context context = getApplicationContext();
            CharSequence text = null;
            if (error_code == 1) {
                text = "No Internet";
            } else if (error_code == 2) {
                text = "Oops ! ,Something went wrong";
            } else if (error_code == 3) {
                text = "dailyLimitExceeded !! Please try again later ";
            } else {
                text = "No result found ";
            }
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }
}
