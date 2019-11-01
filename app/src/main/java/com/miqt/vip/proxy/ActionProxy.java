package com.miqt.vip.proxy;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.miqt.vip.adapter.TAdapter;
import com.miqt.vip.adapter.houlder.ActionHolder;
import com.miqt.vip.bean.Action;
import com.miqt.vip.bean.Constant;
import com.miqt.vip.utils.HttpClient;
import com.miqt.vip.utils.USMUtils;
import com.miqt.wand.activity.ProxyActivity;
import com.miqt.wand.anno.AddToFixPatch;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;


/**
 * Created by t54 on 2019/4/3.
 */
@AddToFixPatch
public class ActionProxy extends BaseProxy implements SwipeRefreshLayout.OnRefreshListener {
    RecyclerView rv_list;
    SwipeRefreshLayout ref_layout;
    private List<Action> data;
    public TAdapter adapter;

    String[] perms = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.INTERNET
            , Manifest.permission.ACCESS_FINE_LOCATION
            , Manifest.permission.ACCESS_COARSE_LOCATION
            , Manifest.permission.WRITE_SETTINGS
    };

    public ActionProxy(ProxyActivity acty) {
        super(acty);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mActy.setContentView($("R.layout.activity_action"));
        trySerTitleBar("首页");
        if (!EasyPermissions.hasPermissions(mActy, perms)) {
            EasyPermissions.requestPermissions(mActy, "用户播放视频时的缓存", 1, perms);
        }

        this.rv_list = mActy.findViewById($("R.id.rv_list"));
        this.ref_layout = mActy.findViewById($("R.id.ref_layout"));

        rv_list.setLayoutManager(new GridLayoutManager(mActy, 4));
        data = new ArrayList<>();
        adapter = new TAdapter<>(data, mActy, $(" R.layout.item_action"), ActionHolder.class);
        rv_list.setAdapter(adapter);
        ref_layout.setOnRefreshListener(this);

        getData();

        showTS();

        if (Build.VERSION.SDK_INT >= 21) {
            if (USMUtils.isNoOption(mActy) && USMUtils.isNoSwitch(mActy)) {
                if (!SPUtils.getInstance().getBoolean("isShowUSM", true)) {
                    return;
                }
                new AlertDialog.Builder(mActy)
                        .setTitle("提示")
                        .setMessage(
                                "能否请您打开辅助功能,帮助我们更好的完善产品?\n这对您的使用无任何影响,但对我们却至关重要." +
                                        "打开方式:点下发去打开,然后找到小埋Vip点击打开就好了,谢谢"
                        )
                        .setNegativeButton("不在提示", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SPUtils.getInstance().put("isShowUSM", false);
                            }
                        }).setPositiveButton("去打开", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        USMUtils.openUSMSetting(mActy);
                    }
                }).create().show();

            }
        }
    }

    private void showTS() {
        if (!SPUtils.getInstance().getBoolean("isShowTS", true)) {
            return;
        }
        new AlertDialog.Builder(mActy)
                .setTitle("使用方法")
                .setMessage(
                        "本软件是一个可以免费看各大平台vip视频的软件，使用方法如下:\n" +
                                "1. 打开一个视频平台。\n" +
                                "2. 打开想要观看的视频页面等待加载完成。\n" +
                                "3. 屏幕向右滑出侧边栏，选择线路耐心等待一会解析加载结束播放就可以了。不同线路可能对平台的支持性不同，如果资源无法播放，可以多尝试其它的线路。\n" +
                                "注意：视频解析线路配置需要网络获取，因此请保持手机网络畅通。部分线路可能因时间久远,出现无法访问,广告等,请您最好使用最新发布的解析地址."
                )
                .setNegativeButton("不在提示", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SPUtils.getInstance().put("isShowTS", false);
                    }
                }).setPositiveButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).create().show();
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onRestart() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    }

    @Override
    public void onRefresh() {
        getData();
        ref_layout.setRefreshing(false);
    }

    private void getData() {
        new Thread() {
            @Override
            public void run() {
                String parcog = com.miqt.wand.utils.SPUtils.get(mActy, "ACTION_CONTENT");
                if (TextUtils.isEmpty(parcog)) {
                    parcog = Constant.ACTION_CONTENT;
                }
                update(new HttpClient.Resp(200, parcog));
                try {
                    HttpClient.Resp resp = HttpClient.get(Constant.ACTION_UTL).happy();
                    if (resp.code == 200) {
                        com.miqt.wand.utils.SPUtils.put(mActy, "ACTION_CONTENT", resp.content);
                        update(resp);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void update(HttpClient.Resp resp) {
        Gson gson = new Gson();
        final List<Action> object = gson.fromJson(resp.content, new TypeToken<List<Action>>() {
        }.getType());
        if (object != null && object.size() > 0) {
            mActy.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    data.clear();
                    data.addAll(object);
                    adapter.notifyDataSetChanged();
                }
            });
        } else {
            ToastUtils.showShort("获取平台信息失败：" + resp.code);
        }
    }

}
