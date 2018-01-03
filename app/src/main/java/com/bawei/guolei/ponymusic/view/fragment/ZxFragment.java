package com.bawei.guolei.ponymusic.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bawei.guolei.ponymusic.R;
import com.bawei.guolei.ponymusic.applicon.AppCache;
import com.bawei.guolei.ponymusic.applicon.Extras;
import com.bawei.guolei.ponymusic.applicon.LoadStateEnum;
import com.bawei.guolei.ponymusic.bean.SongListInfo;
import com.bawei.guolei.ponymusic.net.HttpClient;
import com.bawei.guolei.ponymusic.utils.NetworkUtils;
import com.bawei.guolei.ponymusic.utils.ViewUtils;
import com.bawei.guolei.ponymusic.utils.binding.Bind;
import com.bawei.guolei.ponymusic.view.activity.WebActivity;
import com.bawei.guolei.ponymusic.view.adapter.PlaylistAdapter;
import com.bawei.guolei.ponymusic.view.adapter.TitleApdater;

import java.util.List;

/**
 * date:2017/9/28
 * author:徐帅(acer)
 * funcation:在线音乐界面的Fragment
 */

public class ZxFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ListView listView;
    private List<SongListInfo> list;
    private TextView textView;
    private TitleApdater titleApdater;
    private HttpClient httpClient;
    @Bind(R.id.ll_loading)
    private LinearLayout llLoading;
    @Bind(R.id.ll_load_fail)
    private LinearLayout llLoadFail;
    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = View.inflate(getActivity(), R.layout.fragment_playlist, null);
        //查找控件
        initView();
        //添加数据
        initData();
        return view;
    }

    private void initView() {
        textView = (TextView) view.findViewById(R.id.tv_mess);
        listView = (ListView) view.findViewById(R.id.lv_song_list);
    }

    private void initData() {
        if (!NetworkUtils.isNetworkAvailable(getContext())) {
            ViewUtils.changeViewState(listView, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
            return;
        }
        list = AppCache.getSongListInfos();
        if (list.isEmpty()) {
            String[] titles = getResources().getStringArray(R.array.online_music_list_title);
            String[] types = getResources().getStringArray(R.array.online_music_list_type);
            for (int i = 0; i < titles.length; i++) {
                SongListInfo info = new SongListInfo();
                info.setTitle(titles[i]);
                info.setType(types[i]);
                list.add(info);
            }
        }

        PlaylistAdapter playlistAdapter = new PlaylistAdapter(list);
        listView.setAdapter(playlistAdapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        SongListInfo songListInfo = list.get(i);
        Intent intent = new Intent(getContext(), WebActivity.class);
        intent.putExtra(Extras.MUSIC_LIST_TYPE, songListInfo);
        startActivity(intent);
    }
}