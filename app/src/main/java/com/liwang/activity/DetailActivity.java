package com.liwang.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.liwang.anim.AnimatorPath;
import com.liwang.anim.PathEvaluator;
import com.liwang.anim.PathPoint;
import com.liwang.application.DutApplication;
import com.liwang.issdut.R;
import com.liwang.utils.DownloadFile;
import com.liwang.utils.HtmlImageGetter;
import com.liwang.utils.OpenFile;
import com.liwang.utils.ScreenUtils;
import com.liwang.view.LoadToast;
import com.liwang.view.RevealLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

/**
 * Created by Nikolas on 2015/5/21.
 */
public class DetailActivity extends SwipeBackActivity implements View.OnClickListener,
        DownloadFile.OnDownLoadListener {

    private TextView messageView;
    private String message;
    private View statusView, topStatusView;
    private View loadView;
    private ImageView prevView;
    private RevealLayout revealLayout;
    private ImageView mFab;
    private FrameLayout mFabContainer;
    private LinearLayout mControlsContainer;
    private TextView titleView;
    private View[] downViews = new View[6];
    private float mFabSize;
    private final static float SCALE_FACTOR = 13f;
    private final static int ANIMATION_DURATION = 300;
    private final static int MINIMUN_X_DISTANCE = 200;
    private boolean mRevealFlag, nextFlag = true;
    //下载进度展示的toast
    private LoadToast loadToast;
    //下载文件的工具类
    private DownloadFile mDownloadFile;
    //需要解析的url
    private String url;
    //去掉注释的正则表达式
    private Pattern escapteNote;
    //完成url的正则表达式
    private Pattern compileUrl;
    //附件下载地址
    private String[] downUrls = new String[6];
    private String detailPrev;
    private String attachAttr, attachVal;
    private String contentAttr, contentVal;
    private String serverEncode, androidEncode;
    //解析html之后的spanned
    private Spanned spanned;
    //周知标题
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);

        getSwipeBackLayout().setEdgeSize(getResources().getDisplayMetrics().widthPixels / 2);

        initView();

        initData();
    }

    private void initView() {
        //展示周知信息的view
        messageView = (TextView) findViewById(R.id.newsarticle);
        //等待界面的view
        loadView = findViewById(R.id.loadView);
        //展示上一个activity的view
        prevView = (ImageView) findViewById(R.id.prev_view);
        //重点击位置展开的view
        revealLayout = (RevealLayout) findViewById(R.id.reveal_layout);
        //覆盖状态栏的view
        statusView = findViewById(R.id.status_view);
        topStatusView = findViewById(R.id.top_status);
        //展开下载栏的view
        mFab = (ImageView) findViewById(R.id.fab);
        //展示标题view
        titleView = (TextView) findViewById(R.id.app_name);
        //展开下载栏view的大小
        mFabSize = getResources().getDimensionPixelSize(R.dimen.fab_size);

        mFabContainer = (FrameLayout) findViewById(R.id.fab_container);
        mControlsContainer = (LinearLayout) findViewById(R.id.media_controls_container);

        View view = HomeActivity.dataView;
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        prevView.setImageBitmap(view.getDrawingCache());
        messageView.setMovementMethod(LinkMovementMethod.getInstance());
        //下载附件的按钮
        for (int i = 0; i < mControlsContainer.getChildCount(); ++i) {
            downViews[i] = mControlsContainer.getChildAt(i);
            ((ViewGroup) downViews[i]).getChildAt(0).setOnClickListener(this);
        }


        //初始化覆盖状态栏view的高度
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    ScreenUtils.getStatusHeight(this));
            statusView.setLayoutParams(params);
            topStatusView.setLayoutParams(params);
        } else {
            statusView.setVisibility(View.GONE);
            topStatusView.setVisibility(View.GONE);
        }

        //初始化下载进度view
        loadToast = new LoadToast(this).setText("download...").setTranslationY(120);
        mDownloadFile = new DownloadFile();
        mDownloadFile.setOnDownLoadListener(this);

        //去掉注解的正则表达式
        escapteNote = Pattern.compile("(<style>[\\s\\S]*</style>)");
        compileUrl = Pattern.compile("[(.*/)]+");
    }

    private void initData() {
        url = getIntent().getStringExtra("url");
        title = getIntent().getStringExtra("title");
        titleView.setText(title);

        detailPrev = ((DutApplication) getApplication()).getConfigProperties().getProperty("detailPrev");
        contentAttr = ((DutApplication) getApplication()).getConfigProperties().getProperty("contentAttr");
        contentVal = ((DutApplication) getApplication()).getConfigProperties().getProperty("contentVal");
        attachAttr = ((DutApplication) getApplication()).getConfigProperties().getProperty("attachAttr");
        attachVal = ((DutApplication) getApplication()).getConfigProperties().getProperty("attachVal");
        serverEncode = ((DutApplication) getApplication()).getConfigProperties().getProperty("serverEncode");
        androidEncode = ((DutApplication) getApplication()).getConfigProperties().getProperty("androidEncode");

        Matcher matcher = compileUrl.matcher(url);
        url = matcher.replaceFirst(detailPrev);
        Log.e("url", url);
        ((DutApplication) getApplication()).getQueue().add(buildRequest(url));
    }

    private StringRequest buildRequest(final String url) {
        StringRequest stringRequest = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            response = new String(response.getBytes(serverEncode), androidEncode);
                            Document doc = Jsoup.parse(response);
                            Elements es = doc.getElementsByAttributeValue(attachAttr, attachVal);

                            if (es.size() > 0) {
                                Element e = es.last();
                                //获取附件下载标签
                                es = e.getElementsByTag("li");
                                for (int i = 0; i < es.size() && i < 6; ++i) {
                                    downViews[i].setVisibility(View.VISIBLE);
                                    String downUrl = es.get(i).getElementsByTag("a").attr("href");
                                    Matcher matcher = compileUrl.matcher(downUrl);
                                    downUrl = matcher.replaceFirst(detailPrev);
                                    downUrls[i] = downUrl;
                                }
                            } else {
                                mFab.setVisibility(View.GONE);
                            }

                            es = doc.getElementsByAttributeValue(contentAttr, contentVal);
                            if (es.size() > 0) {
                                message = es.get(0).html();
                                Matcher matcher = escapteNote.matcher(message);
                                message = matcher.replaceFirst("");
                                handler.sendEmptyMessage(0x123);
                            } else {
                                ((DutApplication) getApplication()).getQueue().getCache().remove(url);
                                Toast.makeText(DetailActivity.this, "信息获取失败", Toast.LENGTH_SHORT).show();
                            }
                        } catch (UnsupportedEncodingException e) {
                            Toast.makeText(DetailActivity.this, "信息获取失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(DetailActivity.this, "信息获取失败", Toast.LENGTH_SHORT).show();
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        return stringRequest;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            loadView.setVisibility(View.GONE);
            mFabContainer.setVisibility(View.VISIBLE);
            HtmlImageGetter getter = new HtmlImageGetter(DetailActivity.this, messageView);
            getter.setListener(new HtmlImageGetter.onCompleteListener() {
                @Override
                public void complete() {
                    messageView.setText(messageView.getText());
                }
            });
            spanned = Html.fromHtml(message, getter, null);
            messageView.setText(spanned);
        }
    };

    public void onFabPressed(View view) {
        final float startX = mFab.getX();
        AnimatorPath path = new AnimatorPath();
        path.moveTo(0, 0);
        path.curveTo(-200, 200, -400, 100, -600, 50);
        final ObjectAnimator anim = ObjectAnimator.ofObject(this, "fabLoc",
                new PathEvaluator(), path.getPoints().toArray());
        anim.setInterpolator(new AccelerateInterpolator());
        anim.setDuration(ANIMATION_DURATION);
        anim.start();
        mFab.setImageBitmap(null);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (Math.abs(startX - mFab.getX()) > MINIMUN_X_DISTANCE) {
                    if (!mRevealFlag) {
                        mFabContainer.setY(mFabContainer.getY() + mFabSize / 2);
                        mFab.animate()
                                .scaleXBy(SCALE_FACTOR)
                                .scaleYBy(SCALE_FACTOR)
                                .setListener(mEndRevealListener)
                                .setDuration(ANIMATION_DURATION);
                        mRevealFlag = true;
                    }
                }
            }
        });
    }

    private AnimatorListenerAdapter mEndRevealListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            mFab.setVisibility(View.INVISIBLE);
            mFabContainer.setBackgroundColor(getResources()
                    .getColor(R.color.brand_accent));
            for (int i = 0; i < downViews.length; ++i) {
                View view = downViews[i];
                ViewPropertyAnimator animator = view.animate()
                        .scaleX(1).scaleY(1)
                        .setDuration(ANIMATION_DURATION);
                animator.setStartDelay(i * 50);
                animator.start();
            }
        }
    };

    public void setFabLoc(PathPoint newLoc) {
        mFab.setTranslationX(newLoc.mX);
        if (mRevealFlag)
            mFab.setTranslationY(newLoc.mY - (mFabSize / 2));
        else
            mFab.setTranslationY(newLoc.mY);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        titleView.setVisibility(View.VISIBLE);
        if (nextFlag) {
            Intent intent = getIntent();
            int x = intent.getIntExtra("x", 0);
            int y = intent.getIntExtra("y", 0);
            revealLayout.next(x, y, 2000);
            nextFlag = false;
        }
    }

    @Override
    public void onClick(View v) {
        loadToast.show();
        switch (v.getId()) {
            case R.id.download_one:
                mDownloadFile.downLoad(downUrls[0]);
                break;
            case R.id.download_two:
                mDownloadFile.downLoad(downUrls[1]);
                break;
            case R.id.download_three:
                mDownloadFile.downLoad(downUrls[2]);
                break;
            case R.id.download_four:
                mDownloadFile.downLoad(downUrls[3]);
                break;
            case R.id.download_five:
                mDownloadFile.downLoad(downUrls[4]);
                break;
            case R.id.download_six:
                mDownloadFile.downLoad(downUrls[5]);
                break;
            default:
                break;
        }
    }

    @Override
    public void success(File file) {
        loadToast.success();
        OpenFile.openFile(this, file);
    }

    @Override
    public void fail() {
        loadToast.error();
    }
}
