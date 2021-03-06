package shacus.edu.seu.com.shacus.swipecards.swipe;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.rong.imkit.RongIM;
import shacus.edu.seu.com.shacus.Adapter.ImagePagerAdapter;
import shacus.edu.seu.com.shacus.Adapter.PhotoViewAttacher;
import shacus.edu.seu.com.shacus.Adapter.UploadViewPager;
import shacus.edu.seu.com.shacus.Data.Cache.ACache;
import shacus.edu.seu.com.shacus.Data.Model.ImageData;
import shacus.edu.seu.com.shacus.Data.Model.LoginDataModel;
import shacus.edu.seu.com.shacus.Data.Model.RecommandModel;
import shacus.edu.seu.com.shacus.Data.Model.UserModel;
import shacus.edu.seu.com.shacus.Fragment.BaseFragment;
import shacus.edu.seu.com.shacus.MyApplication;
import shacus.edu.seu.com.shacus.Network.OnSingleTapDismissBigPhotoListener;
import shacus.edu.seu.com.shacus.Network.okHttpUtil;
import shacus.edu.seu.com.shacus.Network.okHttpUtil_JsonResponse;
import shacus.edu.seu.com.shacus.R;
import shacus.edu.seu.com.shacus.Utils.CommonUrl;
import shacus.edu.seu.com.shacus.Utils.CommonUtils;
import shacus.edu.seu.com.shacus.Utils.StatusCode;
import shacus.edu.seu.com.shacus.swipecards.test.TestData;
import shacus.edu.seu.com.shacus.swipecards.util.CardEntity;
import shacus.edu.seu.com.shacus.swipecards.view.SwipeFlingBottomLayout;


/**
 * 卡片Fragment
 *
 * @author zc
 */
public class CardFragment extends BaseFragment implements shacus.edu.seu.com.shacus.swipecards.view.SwipeFlingView.OnSwipeFlingListener,
        SwipeFlingBottomLayout.OnBottomItemClickListener, shacus.edu.seu.com.shacus.swipecards.view.SwipeFlingView.OnItemClickListener,
        okHttpUtil_JsonResponse,OnSingleTapDismissBigPhotoListener {

    private final static String TAG = CardFragment.class.getSimpleName();
    private final static boolean DEBUG = true;
    private ACache acache;
    private UserModel user;
    private String authkey;
    private String category;
    private Context mContext;

    //网络请求所用变量
    private int requestTime = 1; //第几次请求
    private static final int MSG_SUCCESS = 0;//获取数据成功的标识
    private static final int MSG_FAILURE = 1;//获取数据失败的标识
    private static final int MSG_DATA_SUCCESS = 2;//获取数据成功的标识




    @InjectView(R.id.frame)
    shacus.edu.seu.com.shacus.swipecards.view.SwipeFlingView mSwipeFlingView;

//    @InjectView(R.id.self_main)
//    RoundImageView mImageView;

    @InjectView(R.id.swipe_fling_bottom)
    SwipeFlingBottomLayout mBottomLayout;

    //点击frame进行轮播
    //@InjectView(R.id.display_photoset_image)
    RelativeLayout display_big_image_layout;

    //个人主页按钮点击
    //CardImageView selfMainView;

    //进行蒙层图片轮播
    private UploadViewPager image_viewpager;
    private ImagePagerAdapter imagePagerAdapter;
    private TextView position_in_total;
    private ArrayList<String> imageBigDatas = new ArrayList<>();
    private ArrayList<ArrayList<String>> imageBigDatasList = new ArrayList<>();
    private ArrayList<ImageData> imageDatas = new ArrayList<>();

    //底部栏隐藏用
    private View navibar;

    //设置自己是什么身份
    private UserAdapter mAdapter;
    private UserModel userModel;
    private View view;
    private CheckBox checkbox_people_photogragher;
    private CheckBox checkbox_people_model;
    private CheckBox checkbox_people_all;

    //从服务器获取推荐并返回保存到mOurList中
    private int mPageIndex = 0;
    private boolean mIsRequestGirlList;
    RecommandModel model;
    private ArrayList<CardEntity> mGrilList = new ArrayList<>();//这个list不用了，但暂时先保留
    private ArrayList<RecommandModel> mOurList = new ArrayList<>();//自己的list

    public ArrayList<RecommandModel> getmOurList() {
        return mOurList;
    }

    public void setNavibar(View navibar) {
        this.navibar = navibar;
    }

    //筛选用
    private boolean[] selector = {true, false,false,true,false,false};
    ArrayList<RecommandModel> mTempList = new ArrayList<>();
    ArrayList<RecommandModel> mMainList = new ArrayList<>();
    ArrayList<RecommandModel> data;

    //like and unlike
    UserModel content;
    LoginDataModel loginModel;
    ACache aCache;
    String myid;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.card_layout, null);

      mRootView=rootView;
        display_big_image_layout = (RelativeLayout) rootView.findViewById(R.id.display_recommand_photoset_image);
        display_big_image_layout.setVisibility(View.GONE);
        image_viewpager=(UploadViewPager)rootView.findViewById(R.id.photoset_detail_viewpager);
        position_in_total=(TextView)rootView.findViewById(R.id.photoset_position_total);

        aCache=ACache.get(getActivity());
        loginModel = (LoginDataModel)aCache.getAsObject("loginModel");
        content = loginModel.getUserModel();
        myid = content.getId();
        authkey = content.getAuth_key();


        ButterKnife.inject(this, rootView);
        initView();
        //requestOurList();
        return rootView;
    }

    private void initView() {

        //mAdapter = new UserAdapter(getActivity(), mGrilList);
        //display_big_image_layout.setVisibility(View.GONE);
//        mTempList.clear();
//        mTempList.addAll(mOurList);

        ACache cache=ACache.get(MyApplication.getContext());
        if(cache.getAsObject("mSelectedList") == null) {
            data = new ArrayList<>();
            data = (ArrayList<RecommandModel>) cache.getAsObject("mOurList");
        }else{
            data = new ArrayList<>();
            data = (ArrayList<RecommandModel>) cache.getAsObject("mSelectedList");
        }

        if (data!=null&&data.size()>0) {
            mOurList.clear();
            for (int i = 0; i < data.size(); i++)
                mOurList.add(data.get(i));

            for (int i = 0; i < mOurList.size(); i++) {
                imageBigDatas = mOurList.get(i).getUcimg();
                imageBigDatasList.add(imageBigDatas);
            }
            mSwipeFlingView.setImageBigDatasList(imageBigDatasList);
            mSwipeFlingView.setOnItemClickListener(CardFragment.this);
        }
        mAdapter = new UserAdapter(getActivity(), mOurList);
        mSwipeFlingView.setAdapter(mAdapter);
        mSwipeFlingView.setOnSwipeFlingListener(this);//SimpleOnSwipeListener/OnSwipeListener

        mSwipeFlingView.setDisplay_big_image_layout(display_big_image_layout);//先setter，再设置点击监听
        mSwipeFlingView.setCardFragment(this);
        //mAdapter.setSelfMainView(mImageView);
        mBottomLayout.setOnBottomItemClickListener(this);

        addselect();
    }

    private void addselect(){
        //临时添加可以选择的样式
        ImageView btn_selector= (ImageView) mRootView.findViewById(R.id.btn_selector);
        btn_selector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //筛选菜单
                final CheckBox checkbox_sex_all;
                final CheckBox checkbox_sex_man;
                final CheckBox checkbox_sex_woman;
                final CheckBox checkbox_people_all;
                final CheckBox checkbox_people_photogragher;
                final CheckBox checkbox_people_model;
                final boolean[] sex_selector = {true, false, false};
                final  boolean[] people_selector = {true, false, false};



                AlertDialog builder = new AlertDialog.Builder(mActivity, R.style.AlertDialog).create();
                // 通过LayoutInflater来加载一个xml的布局文件作为一个View对象
                view = LayoutInflater.from(mActivity).inflate(R.layout.dialog_selector_layout, null);
                // 设置我们自己定义的布局文件作为弹出框的Content
                builder.setView(view);
                // builder.setTitle("筛选");
                checkbox_sex_all = (CheckBox) view.findViewById(R.id.sex_all);
                checkbox_sex_man = (CheckBox) view.findViewById(R.id.sex_man);
                checkbox_sex_woman = (CheckBox) view.findViewById(R.id.sex_woman);
                checkbox_people_all = (CheckBox) view.findViewById(R.id.people_all);
                checkbox_people_photogragher = (CheckBox) view.findViewById(R.id.people_photogragher);
                checkbox_people_model = (CheckBox) view.findViewById(R.id.people_model);
                //保存上次所选择的记录
                if (selector[0]) checkbox_sex_all.setChecked(true);
                if (selector[1]) checkbox_sex_man.setChecked(true);
                if (selector[2]) checkbox_sex_woman.setChecked(true);
                if (selector[3]) checkbox_people_all.setChecked(true);
                if (selector[4]) checkbox_people_photogragher.setChecked(true);
                if (selector[5]) checkbox_people_model.setChecked(true);
                //六个实现每个列表单选的监听方法
                checkbox_people_all.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (checkbox_people_model.isChecked())
                            checkbox_people_model.setChecked(false);
                        if (checkbox_people_photogragher.isChecked()) checkbox_people_photogragher
                                .setChecked(false);
                    }

                });
                checkbox_people_photogragher.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (checkbox_people_all.isChecked()) checkbox_people_all.setChecked(false);
                        if (checkbox_people_model.isChecked()) checkbox_people_model
                                .setChecked(false);
                    }

                });

                checkbox_people_model.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (checkbox_people_all.isChecked()) checkbox_people_all.setChecked(false);
                        if (checkbox_people_photogragher.isChecked()) checkbox_people_photogragher
                                .setChecked(false);
                    }

                });
                checkbox_sex_woman.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (checkbox_sex_all.isChecked()) checkbox_sex_all.setChecked(false);
                        if (checkbox_sex_man.isChecked()) checkbox_sex_man
                                .setChecked(false);
                    }

                });
                checkbox_sex_man.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (checkbox_sex_all.isChecked()) checkbox_sex_all.setChecked(false);
                        if (checkbox_sex_woman.isChecked()) checkbox_sex_woman
                                .setChecked(false);
                    }

                });
                checkbox_sex_all.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (checkbox_sex_woman.isChecked()) checkbox_sex_woman.setChecked(false);
                        if (checkbox_sex_man.isChecked()) checkbox_sex_man
                                .setChecked(false);
                    }

                });

                builder.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (checkbox_sex_all.isChecked()) {
                            sex_selector[0] = true;
                            sex_selector[1] = false;
                            sex_selector[2] = false;
                        }
                        ;
                        if (checkbox_sex_man.isChecked()) {
                            sex_selector[0] = false;
                            sex_selector[1] = true;
                            sex_selector[2] = false;
                        }
                        if (checkbox_sex_woman.isChecked()) {
                            sex_selector[0] = false;
                            sex_selector[1] = false;
                            sex_selector[2] = true;
                        }
                        ;

                        if (checkbox_people_all.isChecked()) {
                            people_selector[0] = true;
                            people_selector[1] = false;
                            people_selector[2] = false;
                        }
                        if (checkbox_people_photogragher.isChecked()) {
                            people_selector[0] = false;
                            people_selector[1] = true;
                            people_selector[2] = false;
                        }
                        ;
                        if (checkbox_people_model.isChecked()) {
                            people_selector[0] = false;
                            people_selector[1] = false;
                            people_selector[2] = true;
                        }
                        //转移到一个数组中
                        selector = new boolean[]{sex_selector[0], sex_selector[1], sex_selector[2],
                                people_selector[0], people_selector[1], people_selector[2]};
                    }

                });
                builder.show();
                Button btnPositive =
                        builder.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
                btnPositive.setTextColor(getResources().getColor(R.color.ff_white));
                btnPositive.setTextSize(15);

            }
        });
    }

    private void initView(ArrayList<RecommandModel> tempList) {

        //mAdapter = new UserAdapter(getActivity(), mGrilList);
        //display_big_image_layout.setVisibility(View.GONE);
        mAdapter = new UserAdapter(getActivity(), tempList);
        mSwipeFlingView.setAdapter(mAdapter);
        mSwipeFlingView.setOnSwipeFlingListener(this);//SimpleOnSwipeListener/OnSwipeListener
        mSwipeFlingView.setDisplay_big_image_layout(display_big_image_layout);//先setter，再设置点击监听
        mSwipeFlingView.setCardFragment(this);

        mBottomLayout.setOnBottomItemClickListener(this);
    }

    //筛选之后调用这个方法
    public void updateOurSelectListView(ArrayList<RecommandModel> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        mTempList.clear();
        mTempList.addAll(list);
        mAdapter.notifyDataSetChanged();
    }

    public void updateOurListView(ArrayList<RecommandModel> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        mOurList.addAll(list);
        mTempList.clear();
        mTempList.addAll(mOurList);
        mAdapter.notifyDataSetChanged();
    }

    //筛选函数
    public ArrayList<RecommandModel> selectMethod(ArrayList<RecommandModel> mOurList, boolean[] selector){
        mTempList.clear();
        //筛选性别
        if (selector[1]) {
            for (int i = 0; i < mOurList.size(); i++) {
                if (mOurList.get(i).getUserpublish().getSex().equals("1"))
                    mTempList.add(mOurList.get(i));
            }
        } else if (selector[2]) {
            for (int i = 0; i < mOurList.size(); i++) {
                if (mOurList.get(i).getUserpublish().getSex().equals("0"))
                    mTempList.add(mOurList.get(i)); //筛选这里不对，remove之后下标也会有变化，然后后面需要进行判断不允许确定按下
            }
        }
        //筛选摄影师和模特
        if (selector[4]) {
            for (int i = 0; i < mOurList.size(); i++) {
                if (mOurList.get(i).getUserpublish().getUcategory().equals("1"))
                    mTempList.add(mOurList.get(i));
                ;
            }
        } else if (selector[5]) {
            for (int i = 0; i < mOurList.size(); i++) {
                if (mOurList.get(i).getUserpublish().getUcategory().equals("2"))
                    mTempList.add(mOurList.get(i));
            }
        }

        ArrayList<RecommandModel> arrayList = new ArrayList<>();
        if(selector[0] && selector[3]){
            arrayList.addAll(mOurList);
//            updateOurSelectListView(arrayList);
//            mSwipeFlingView.setOnItemClickListener(CardFragment.this);
        }
        else{
            arrayList.addAll(mTempList);
//            updateOurSelectListView(arrayList);
//            mSwipeFlingView.setOnItemClickListener(CardFragment.this);
        }
//        initView(mTempList);
        return arrayList;
    }


    private void updateListView(ArrayList<CardEntity> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        mGrilList.addAll(list);
        mAdapter.notifyDataSetChanged();
    }


    private void requestOurList() {
        acache = ACache.get(getActivity());
        LoginDataModel loginModel = (LoginDataModel) acache.getAsObject("loginModel");
        userModel = loginModel.getUserModel();
        user = loginModel.getUserModel();
        authkey = user.getAuth_key();
        Map<String,String>  map= new HashMap();
        if (mIsRequestGirlList) {
            return;
        }
        mIsRequestGirlList = true;
        if (requestTime == 3 || requestTime == 1) {   //之后的请求次数多，把3放在前面提高效率
            //先从缓存拿摄影师模特列表
            ACache cache=ACache.get(MyApplication.getContext());
            ArrayList<RecommandModel> data = (ArrayList<RecommandModel>) cache.getAsObject("mOurList");
            if (data!=null&&data.size()>0){//缓存命中，直接发成功消息到handler
                for(int i = 0; i< data.size(); i++)
                    mOurList.add(data.get(i));

                for(int i = 0; i< mOurList.size(); i++){
                    imageBigDatas = mOurList.get(i).getUcimg();
                    imageBigDatasList.add(imageBigDatas);
                }
                mSwipeFlingView.setImageBigDatasList(imageBigDatasList);
                Message msg = mHandler.obtainMessage();
                msg.what = MSG_DATA_SUCCESS;
                msg.obj = mOurList;
                mHandler.sendMessage(msg);
            }else{//缓存没有命中再进行网络请求获取
            map.put("type", String.valueOf(StatusCode.RECOMMAND_PHOTOGRAPHER_MODEL_LIST));  //10850
            map.put("authkey", authkey);
                okHttpUtil.instance.post(mContext, CommonUrl.requestModel,map,this);
            }
        }
//        else if(requestTime == 2){
//            category = user.getCategory();
//            map.put("type", StatusCode.CHANGE_USER_CATEGORY);   //10852
//            map.put("authkey", authkey);
//            if (category == "摄影师") map.put("category", 1);
//            if (category == "模特") map.put("category", 2);
//            requestFragment.httpRequest(map, CommonUrl.requestModel);
//        }
    }

    private void alertDialogShow() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialog);
        builder.setPositiveButton("确定", null);
        // 通过LayoutInflater来加载一个xml的布局文件作为一个View对象
        view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_selector_category, null);

        // 设置我们自己定义的布局文件作为弹出框的Content
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        checkbox_people_photogragher = (CheckBox) view.findViewById(R.id.people_photogragher);
        checkbox_people_model = (CheckBox) view.findViewById(R.id.people_model);
        checkbox_people_all = (CheckBox) view.findViewById(R.id.people_all);

        checkbox_people_photogragher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkbox_people_model.isChecked()) checkbox_people_model.setChecked(false);
                if (checkbox_people_all.isChecked()) checkbox_people_all.setChecked(false);
            }
        });
        checkbox_people_model.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkbox_people_photogragher.isChecked())
                    checkbox_people_photogragher.setChecked(false);
                if (checkbox_people_all.isChecked()) checkbox_people_all.setChecked(false);
            }
        });
        checkbox_people_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkbox_people_photogragher.isChecked())
                    checkbox_people_photogragher.setChecked(false);
                if (checkbox_people_model.isChecked()) checkbox_people_model.setChecked(false);
            }
        });

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acache = ACache.get(getActivity());
                LoginDataModel loginModel = (LoginDataModel) acache.getAsObject("loginModel");
                userModel = loginModel.getUserModel();
                if ((!checkbox_people_all.isChecked()) && (!checkbox_people_model.isChecked()) &&
                        (!checkbox_people_photogragher.isChecked())) {
                    CommonUtils commonUtils = new CommonUtils();
                    commonUtils.showToast(getContext(), "尚未选择！");
                } else if (checkbox_people_all.isChecked()) {
                    userModel.setUcategory("两者都是");
                    alertDialog.dismiss();
                } else if (checkbox_people_photogragher.isChecked()) {
                    userModel.setUcategory("摄影师");
                    alertDialog.dismiss();
                } else if (checkbox_people_model.isChecked()) {
                    userModel.setUcategory("模特");
                    alertDialog.dismiss();
                }


                authkey = userModel.getAuth_key();
                Map<String,String> map = new HashMap();
                category = userModel.getUcategory();
                map.put("type", String.valueOf(StatusCode.CHANGE_USER_CATEGORY));   //10852
                map.put("authkey", authkey);
                if (category.equals("摄影师")) map.put("category", "1");
                if (category.equals("模特")) map.put("category", "2");
                if (category.equals("两者都是")) map.put("category", "12");
                okHttpUtil.instance.post(mContext, CommonUrl.requestModel,map,CardFragment.this); //调试弹框需要注释掉这一句话，否则传到服务器后就不弹了
            }
        });
        Button btnPositive =
                alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        btnPositive.setTextColor(getResources().getColor(R.color.ff_white));
        btnPositive.setTextSize(15);
        requestOurList();
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {//此方法在ui线程运行
            switch (msg.what) {
                case MSG_SUCCESS:
                    alertDialogShow();
                    break;
                case MSG_DATA_SUCCESS:
                    updateOurListView(mOurList);
                    mSwipeFlingView.setOnItemClickListener(CardFragment.this);
                    ++mPageIndex;
                    break;
                case MSG_FAILURE:

            }
        }
    };


    public RelativeLayout getDisplay_big_image_layout() {
        return display_big_image_layout;
    }

    public void showImagePager(String startPositionUrl){
        int position=-1;
        final int size=imageBigDatas.size();
        for (int index=0;index<size;index++){
            if (imageBigDatas.get(index).equals(startPositionUrl)){
                position=index;
                break;
            }
        }
        imagePagerAdapter=new ImagePagerAdapter(this.getFragmentManager(),imageBigDatas);
        image_viewpager.setAdapter(imagePagerAdapter);
        display_big_image_layout.setVisibility(View.VISIBLE);
        imagePagerAdapter.notifyDataSetChanged();
        image_viewpager.setOffscreenPageLimit(imagePagerAdapter.getCount());
        image_viewpager.setCurrentItem(position,true);
        position_in_total.setText((position + 1) + "/" + size);
        image_viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                position_in_total.setText((position + 1) + "/" + size);
            }



            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        PhotoViewAttacher.setOnSingleTapToPhotoViewListener(this);
    }


    private void setSelectState(String tag, boolean state) {
        for (int index=0;index<imageDatas.size();index++){
            if (imageDatas.get(index).getImageUrl().equals(tag)){
                imageDatas.get(index).setChecked(state);
            }
        }
    }


    private void addTestData() {
        updateListView(TestData.getApiData(getActivity()));
    }

    @Override
    public void onStartDragCard() {
        if (DEBUG) {
            Log.d(TAG, "SwipeFlingView onStartDragCard");
        }
    }

    @Override
    public void onPreCardExit() {
        if (DEBUG) {
            Log.d(TAG, "SwipeFlingView onPreCardExit");
        }
    }

    @Override
    public void onTopCardViewFinish() {
        if (DEBUG) {
            Log.d(TAG, "SwipeFlingView onTopCardViewFinish");
        }
    }

    @Override
    public boolean canLeftCardExit() {
        if (DEBUG) {
            Log.d(TAG, "SwipeFlingView canLeftCardExit");
        }
        return true;
    }

    @Override
    public boolean canRightCardExit() {
        if (DEBUG) {
            Log.d(TAG, "SwipeFlingView canRightCardExit");
        }
        return true;
    }

    /*
    * 向左滑或点击不喜欢按钮
    * */
    @Override
    public void onLeftCardExit(View view, Object dataObject, boolean triggerByTouchMove) {
        if (DEBUG) {
            Log.d(TAG, "SwipeFlingView onLeftCardExit");
            int cur = Integer.valueOf(dataObject.toString());
            RecommandModel card = mAdapter.getItem(cur);
            String excited = card.getUcFirstimg();
            Log.d("excited", "不喜欢 :" + excited);
        }
        if (triggerByTouchMove)
            mBottomLayout.getUnLikeView().animateDragAnimation();
    }

    /*
    * 向右滑或点击感兴趣按钮
    * */
    @Override
    public void onRightCardExit(View view, Object dataObject, boolean triggerByTouchMove) {
        if (DEBUG) {
            Log.d(TAG, "SwipeFlingView onRightCardExit");
            int cur = Integer.valueOf(dataObject.toString());
            RecommandModel card = mAdapter.getItem(cur);
            String excited = card.getUcFirstimg();
            Log.d("excited", "感兴趣 :" + excited);
        }
        if (triggerByTouchMove)
            mBottomLayout.getLikeView().animateDragAnimation();
    }

    @Override
    public void onSelfChat(View view, Object dataObject, boolean triggerByTouchMove) {
        if (DEBUG) {
            Log.d(TAG, "SwipeFlingView onSelfChat");

//            int cur = Integer.valueOf(dataObject.toString());
//            RecommandModel card = mAdapter.getItem(cur);
//            Intent intent = new Intent(getActivity(), OtherUserDisplayActivity.class);
//            intent.putExtra("id",card.getUserpublish().getId());
//            startActivity(intent);
        }
        //if (triggerByTouchMove)
            //mBottomLayout.getSuperLikeView().animateDragAnimation();
    }

    /*
    * 从服务器加载新的卡片数据
    * */
    @Override
    public void onAdapterAboutToEmpty(int itemsInAdapter) {
        if (DEBUG) {
            Log.d(TAG, "SwipeFlingView onAdapterAboutToEmpty");
        }
        requestOurList();
    }

    @Override
    public void onAdapterEmpty() {
        if (DEBUG) {
            Log.d(TAG, "SwipeFlingView onAdapterEmpty");
        }
    }

    @Override
    public void onScroll(View selectedView, float scrollProgressPercent) {
        if (DEBUG) {
            Log.d(TAG, "SwipeFlingView onScroll " + scrollProgressPercent);
        }
    }

    @Override
    public void onEndDragCard() {
        if (DEBUG) {
            Log.d(TAG, "SwipeFlingView onEndDragCard");
        }
    }

    /*@Override
    public void onComeBackClick() {
        //参数决定动画开始位置是从左边还是右边出现
        mSwipeFlingView.selectComeBackCard(true);
    }*/

    @Override
    public void onSelfChatClick() {
//        if (mSwipeFlingView.isAnimationRunning()) {
//            return;
//        }
//        mSwipeFlingView.selectSuperLike(false);
        //int cur = Integer.valueOf(dataObject.toString());
        if(mAdapter.getCount() == 0){
            return;
        }
        RecommandModel card = mAdapter.getItem(mSwipeFlingView.getmCurPositon());
        ACache cache = ACache.get(CardFragment.this.getActivity());
        cache.put("rongusermodel"+card.getUserpublish().getId(),card.getUserpublish());
        if (RongIM.getInstance() != null)
        RongIM.getInstance().startPrivateChat(CardFragment.this.getActivity(), card.getUserpublish().getId(), "title");
//        Intent intent = new Intent(getActivity(), ConversationDynamicFragment.class);
//        intent.putExtra("id",card.getUserpublish().getId());
//        startActivity(intent);

    }

    @Override
    public void onLikeClick() {
        if (mSwipeFlingView.isAnimationRunning()) {
            return;
        }
        mSwipeFlingView.selectRight(false);


        Map map1=new HashMap();
        map1.put("uid", myid);
        map1.put("authkey", authkey);

        RecommandModel card = mAdapter.getItem(mSwipeFlingView.getmCurPositon());

        map1.put("followerid", card.getUserpublish().getId());


        map1.put("type", StatusCode.REQUEST_FOLLOW_USER);

//        map1.put("type",StatusCode.REQUEST_CANCEL_FOLLOWING);

      //  request1.httpRequest(map1, CommonUrl.getFollowInfo);
        Toast.makeText(CardFragment.this.getActivity(),"点击了关注按钮",Toast.LENGTH_SHORT);
    }

    @Override
    public void onUnLikeClick() {
        if (mSwipeFlingView.isAnimationRunning()) {
            return;
        }
        mSwipeFlingView.selectLeft(false);


        Map map1=new HashMap();
        map1.put("uid", myid);
        map1.put("authkey", authkey);

        RecommandModel card = mAdapter.getItem(mSwipeFlingView.getmCurPositon());

        map1.put("followerid", card.getUserpublish().getId());


//        map1.put("type", StatusCode.REQUEST_FOLLOW_USER);

        map1.put("type",StatusCode.REQUEST_CANCEL_FOLLOWING);

        Toast.makeText(CardFragment.this.getActivity(),"点击了不喜欢按钮",Toast.LENGTH_SHORT);
    }

    @Override
    public void onItemClicked(int itemPosition, Object dataObject) {
        if (DEBUG) {
            Log.d("excited", "onItemClicked itemPosition:" + itemPosition);
            RecommandModel card = mAdapter.getItem(itemPosition);
            String excited = card.getUcFirstimg();
            Log.d("excited", "clicked url :" + excited);
            display_big_image_layout.setVisibility(View.VISIBLE);
            int mCurPosition = mSwipeFlingView.getmCurPositon();
            if(mSwipeFlingView.getmCurPositon() >= imageBigDatasList.size()) {
                mCurPosition = mSwipeFlingView.getmCurPositon() % imageBigDatasList.size();
            }
            imageBigDatas = imageBigDatasList.get(mCurPosition);
            for (int i = 0; i < imageBigDatas.size(); i++) {
                String url = imageBigDatas.get(i);
                ImageData imageData = new ImageData(url);
                imageDatas.add(imageData);
            }
            ImageData imageData = imageDatas.get(0);
          //  navibar.setVisibility(View.GONE);
            showImagePager(parseBigImgUrl(imageData.getImageUrl()));
        }
    }

    private String parseBigImgUrl(String imageUrl) {
        String ret = "";
        for (int i = 0; i < imageDatas.size(); i++) {
            if (imageDatas.get(i).getImageUrl().equals(imageUrl)) {
                ret = imageBigDatas.get(i);
                break;
            }
        }
        return ret;
    }


    @Override
    public void onDismissBigPhoto() {
        display_big_image_layout.setVisibility(View.GONE);
      //  navibar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResponse(JSONObject jsonObject) throws JSONException {
        //Message msg = new Message();
            mIsRequestGirlList = false;

            //Log.d("CradFragment", object.getJSONArray("contents").getJSONObject(0).toString());
            int code = Integer.valueOf(jsonObject.getString("code"));
            switch (code) {
                case StatusCode.RECOMMAND_PHOTOGRAPHER_MODEL_LIST://返回了推荐的卡片，返回了两组{}{}
                    Gson gson = new Gson();
                    //此处不使用json转gson
                    //RecommandModel data = gson.fromJson(object.getJSONArray("contents").getJSONObject(0).toString(), RecommandModel.class);
                    JSONArray data = jsonObject.getJSONArray("contents");
                    for(int i = 0; i< data.length(); i++){
                        JSONObject info = data.getJSONObject(i);
                        model = new RecommandModel();
                        //model.setUcFirstimg(info.getString("UcFirstimg"));
                        //model.setHeadimg(info.getString("headimg"));
                        model = gson.fromJson(info.toString(),RecommandModel.class);
                        mOurList.add(model);
                    }
                    for(int i = 0; i< mOurList.size(); i++){
                        imageBigDatas = mOurList.get(i).getUcimg();
                        imageBigDatasList.add(imageBigDatas);
                    }
                    mSwipeFlingView.setImageBigDatasList(imageBigDatasList);
                    Message msg = mHandler.obtainMessage();
                    msg.what = MSG_DATA_SUCCESS;
                    msg.obj = mOurList;
                    mHandler.sendMessage(msg);
                    break;
                case StatusCode.RETURN_SET_CATEGORY://设置字段
                    requestTime++;
                    mHandler.obtainMessage(MSG_SUCCESS, requestTime).sendToTarget();
                    break;
                case StatusCode.CHANGE_USER_CATEGORY://设置成功返回字段
                    String contents = String.valueOf(jsonObject.getString("contents"));
                    if (contents == "修改用户类型成功") requestTime++;//添加错误逻辑
                    requestOurList();
                    break;

            }
            Log.d("CardFragment", "已接受到返回数据");


    }

    @Override
    public void onFailure(IOException e) {
        Log.d(TAG, "onFailure: "+e);
    }
}
