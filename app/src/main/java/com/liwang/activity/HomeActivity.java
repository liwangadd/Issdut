package com.liwang.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.style.UpdateAppearance;
import android.text.style.UpdateLayout;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.liwang.adapter.NormalRecyclerViewAdapter;
import com.liwang.application.DutApplication;
import com.liwang.bean.HomePage;
import com.liwang.issdut.R;
import com.liwang.utils.ScreenUtils;
import com.liwang.utils.SystemBarTintManager;
import com.liwang.view.LoadingView;
import com.liwang.view.SwipeRefreshLoadLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


public class HomeActivity extends Activity implements SwipeRefreshLoadLayout.OnRefreshListener,
        SwipeRefreshLoadLayout.LoadMoreListener {

    private SwipeRefreshLoadLayout refreshLayout;
    protected static RecyclerView dataView;
    private View appNameView;
    private boolean isFinish = false;
    private ArrayList<HomePage> homePages = new ArrayList<HomePage>();
    private NormalRecyclerViewAdapter dataAdapter;
    private String nextPage;
    private String firstPageUrl, otherPrev;
    private String serverEncode, androidEncode;
    private String homeAttr, homeVal;
    private String nextAttr, nextVal;
    private LoadingView loadView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        initView();

        initData();
    }

    private void initData() {
        firstPageUrl = ((DutApplication) getApplication()).getConfigProperties().getProperty("firstPage");
        otherPrev = ((DutApplication) getApplication()).getConfigProperties().getProperty("otherPagePrev");
        serverEncode = ((DutApplication) getApplication()).getConfigProperties().getProperty("serverEncode");
        androidEncode = ((DutApplication) getApplication()).getConfigProperties().getProperty("androidEncode");
        homeAttr = ((DutApplication) getApplication()).getConfigProperties().getProperty("homeAttr");
        homeVal = ((DutApplication) getApplication()).getConfigProperties().getProperty("homeVal");
        nextAttr = ((DutApplication) getApplication()).getConfigProperties().getProperty("nextAttr");
        nextVal = ((DutApplication) getApplication()).getConfigProperties().getProperty("nextVal");

        StringRequest stringRequest = buildRequest(firstPageUrl);
        ((DutApplication) getApplication()).getQueue().add(stringRequest);
    }

    public StringRequest buildRequest(String url) {
        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (loadView.isShown()) {
                    loadView.setVisibility(View.GONE);
                }
                try {
                    if (refreshLayout.isRefreshing()) {
                        refreshLayout.setRefreshing(false);
                    }
                    refreshLayout.setLoadMore(false);
                    HomePage homePage = null;
                    response = new String(response.getBytes(serverEncode), androidEncode);
                    Document doc = Jsoup.parse(response);
                    Elements es = doc.getElementsByAttributeValue(homeAttr, homeVal);
                    int start = homePages.size();
                    for (Element e : es) {
                        homePage = new HomePage();
                        homePage.setTitle(e.text());
                        homePage.setUrl(e.attr("href"));
                        homePages.add(homePage);
                    }
                    dataAdapter.notifyItemRangeInserted(start, homePages.size() - start);
                    es = doc.getElementsByAttributeValue(nextAttr, nextVal);
                    nextPage = "";
                    if (es.size() > 1) {
                        nextPage = es.get(0).attr("href");
                    }
                } catch (UnsupportedEncodingException e) {
                    refreshLayout.setRefreshing(false);
                    refreshLayout.setLoadMore(false);
                    Toast.makeText(HomeActivity.this, "获取信息失败", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                refreshLayout.setRefreshing(false);
                refreshLayout.setLoadMore(false);
                Toast.makeText(HomeActivity.this, "获取信息失败", Toast.LENGTH_SHORT).show();
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        return stringRequest;
    }

    private void initView() {
        refreshLayout = (SwipeRefreshLoadLayout) findViewById(R.id.swipe_refresh);
        dataView = (RecyclerView) findViewById(R.id.data_view);
        appNameView = findViewById(R.id.app_name);
        dataView.setLayoutManager(new LinearLayoutManager(this));
        dataAdapter = new NormalRecyclerViewAdapter(HomeActivity.this, homePages);
        dataView.setAdapter(dataAdapter);
        loadView = (LoadingView) findViewById(R.id.loadView);
        //dataView.setItemAnimator(new HomeItemAnimator());
        RelativeLayout.LayoutParams nameParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60f, getResources().getDisplayMetrics()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            nameParams.topMargin = ScreenUtils.getStatusHeight(this);
        }
        appNameView.setLayoutParams(nameParams);

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintColor(Color.parseColor("#3367D6"));

        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setLoadMoreListener(this);
    }

    @Override
    public void onRefresh() {
        int size = homePages.size();
        homePages.clear();
        dataAdapter.notifyItemRangeRemoved(0, size);
        StringRequest request = buildRequest(firstPageUrl);
        ((DutApplication) getApplication()).getQueue().add(request);
    }

    @Override
    public void loadMore() {
        if (!"".equals(nextPage)) {
            StringRequest request = buildRequest(otherPrev + nextPage.substring(nextPage.lastIndexOf("/") + 1, nextPage.length()));
            ((DutApplication) getApplication()).getQueue().add(request);
        } else {
            Toast.makeText(this, "已无更多信息", Toast.LENGTH_SHORT).show();
        }
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            isFinish = false;
        }
    };

    private void shouldFinish() {
        if (isFinish == false) {
            isFinish = true;
            handler.sendEmptyMessageDelayed(0x123, 2000);
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
        } else {
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            shouldFinish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}