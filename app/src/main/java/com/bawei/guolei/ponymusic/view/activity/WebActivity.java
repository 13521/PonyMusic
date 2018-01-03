package com.bawei.guolei.ponymusic.view.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bawei.guolei.ponymusic.R;
import com.bawei.guolei.ponymusic.applicon.Extras;
import com.bawei.guolei.ponymusic.applicon.LoadStateEnum;
import com.bawei.guolei.ponymusic.bean.DownloadInfo;
import com.bawei.guolei.ponymusic.bean.Music;
import com.bawei.guolei.ponymusic.bean.OnlineMusic;
import com.bawei.guolei.ponymusic.bean.OnlineMusicList;
import com.bawei.guolei.ponymusic.bean.SongListInfo;
import com.bawei.guolei.ponymusic.net.HttpCallback;
import com.bawei.guolei.ponymusic.net.HttpClient;
import com.bawei.guolei.ponymusic.utils.ImageUtils;
import com.bawei.guolei.ponymusic.utils.ViewUtils;
import com.bawei.guolei.ponymusic.utils.binding.Bind;
import com.bawei.guolei.ponymusic.view.adapter.TitleApdater;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.ArrayList;
import java.util.List;

public class WebActivity extends AppCompatActivity {

    private RecyclerView web_list;
    private static final int MUSIC_LIST_SIZE = 20;
    @Bind(R.id.ll_loading)
    private LinearLayout llLoading;
    @Bind(R.id.ll_load_fail)
    private LinearLayout llLoadFail;
    private View vHeader;
    private SongListInfo mListInfo;
    OnlineMusic onlineMusic;
    private OnlineMusicList mOnlineMusicList;
    private List<OnlineMusic> mMusicList = new ArrayList<>();
    private int mOffset = 0;
    private TitleApdater titApdater;
    private String file_link;
    private Boolean check = false;
    private Music music;
    private Handler handler = new Handler();
    int dex;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        initView();

        initData();

        getMusic(mOffset);
    }

    private void initView() {
        web_list = (RecyclerView) findViewById(R.id.web_list);
        mListInfo = (SongListInfo) getIntent().getSerializableExtra(Extras.MUSIC_LIST_TYPE);
        setTitle(mListInfo.getTitle());
    }

    private void initData() {
        //布局管理器
        web_list.setLayoutManager(new LinearLayoutManager(this));

        //添加条目分割线
        web_list.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        //添加适配器
        titApdater = new TitleApdater(this, mMusicList);
        web_list.setAdapter(titApdater);

        //点击事件
        titApdater.setItemClickListener(new TitleApdater.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //用于传值播放音乐
                onlineMusic = mMusicList.get(position);
                play(onlineMusic);
                Toast.makeText(WebActivity.this, onlineMusic.getTitle(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(WebActivity.this, SongActivity.class);
                intent.putExtra("title", onlineMusic.getTitle());
                intent.putExtra("songer_name", onlineMusic.getArtist_name());
                intent.putExtra("song_id", onlineMusic.getSong_id());
                startActivity(intent);
            }
        });
    }

    private void getMusic(final int offset) {
        HttpClient.getSongListInfo(mListInfo.getType(), MUSIC_LIST_SIZE, offset, new HttpCallback<OnlineMusicList>() {
            @Override
            public void onSuccess(OnlineMusicList onlineMusicList) {
                if (offset == 0 && onlineMusicList == null) {
                    ViewUtils.changeViewState(web_list, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
                    return;
                } else if (offset == 0) {
//                    initHeader();
//                   setHeader(listView);
//                    ViewUtils.changeViewState(listView, llLoading, llLoadFail, LoadStateEnum.LOAD_SUCCESS);
                }
                if (onlineMusicList == null || onlineMusicList.getSong_list() == null || onlineMusicList.getSong_list().size() == 0) {
                    web_list.setEnabled(false);
                    return;
                }
                mOffset += MUSIC_LIST_SIZE;
                mMusicList.addAll(onlineMusicList.getSong_list());
                titApdater.notifyDataSetChanged();
            }

            @Override
            public void onFail(Exception e) {
                System.out.println("报错了报错了--------- = " + e);
            }
        });
    }

    private void initHeader() {
        vHeader = LayoutInflater.from(this).inflate(R.layout.activity_online_music_list_header, null);
        final ImageView ivHeaderBg = (ImageView) vHeader.findViewById(R.id.iv_header_bg);
        final ImageView ivCover = (ImageView) vHeader.findViewById(R.id.iv_cover);
        TextView tvTitle = (TextView) vHeader.findViewById(R.id.tv_title);
        TextView tvUpdateDate = (TextView) vHeader.findViewById(R.id.tv_update_date);
        TextView tvComment = (TextView) vHeader.findViewById(R.id.tv_comment);
        tvTitle.setText(mOnlineMusicList.getBillboard().getName());
        tvUpdateDate.setText(getString(R.string.recent_update, mOnlineMusicList.getBillboard().getUpdate_date()));
        tvComment.setText(mOnlineMusicList.getBillboard().getComment());
        Glide.with(this)
                .load(mOnlineMusicList.getBillboard().getPic_s640())
                .asBitmap()
                .placeholder(R.drawable.default_cover)
                .error(R.drawable.default_cover)
                .override(200, 200)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        ivCover.setImageBitmap(resource);
                        ivHeaderBg.setImageBitmap(ImageUtils.blur(resource));
                    }
                });
        titApdater.setHeaderView(vHeader);
    }

    private void play(OnlineMusic onlineMusic) {
        music = new Music();
        HttpClient.getMusicDownloadInfo(onlineMusic.getSong_id(), new HttpCallback<DownloadInfo>() {
            @Override
            public void onSuccess(DownloadInfo downloadInfo) {
                if (downloadInfo == null || downloadInfo.getBitrate() == null) {
                    onFail(null);
                    return;
                }
                file_link = downloadInfo.getBitrate().getFile_link();
                //getStart();
            }

            @Override
            public void onFail(Exception e) {

            }
        });
    }
}