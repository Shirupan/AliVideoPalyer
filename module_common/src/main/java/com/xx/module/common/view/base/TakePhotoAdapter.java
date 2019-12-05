package com.xx.module.common.view.base;/**
 * Created by someone on 2016/12/17.
 */


import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mrkj.lib.common.util.ScreenUtils;
import com.xx.lib.db.entity.SmContextWrap;
import com.xx.module.common.R;
import com.xx.module.common.imageload.ImageLoader;
import com.xx.module.common.imageload.TakePhotoUtil;
import com.xx.module.common.imageload.photo.CropOptions;
import com.xx.module.common.imageload.photo.ITakePhoto;
import com.xx.module.common.view.loading.SparseArrayViewHolder;
import com.xx.module.common.view.widget.SquareImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author
 * @Create 2016/12/17
 */
public class TakePhotoAdapter extends RecyclerView.Adapter<SparseArrayViewHolder> {
    public static final int TYPE_NORMAL = 1;
    public static final int TYPE_ADD = 2;
    private List<String> images;
    private View[] imageViews = new View[5];
    private int limt = 3;
    private ITakePhoto takePhoto;
    private Fragment mFragment;
    private Activity mActivity;
    private Context mContext;
    private boolean justShow;  //仅查看，不添加修改
    private boolean isFromNet; //网络图片
    private CropOptions cropOptions;

    public TakePhotoAdapter(Fragment context, ITakePhoto takePhoto) {
        this.takePhoto = takePhoto;
        mFragment = context;
        mContext = context.getContext();
    }

    public TakePhotoAdapter(Activity context, ITakePhoto takePhoto) {
        this.takePhoto = takePhoto;
        mActivity = context;
        mContext = context;
    }

    @Override
    public SparseArrayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImageView view = new SquareImageView(mContext);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        int size = ScreenUtils.dp2px(mContext, 2);
        view.setPadding(size, size, size, size);
        view.setLayoutParams(lp);
        view.setAdjustViewBounds(true);
        view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        view.setId(0);
        return new SparseArrayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SparseArrayViewHolder holder, final int position) {
        final ImageView view = holder.getView(0);
        if (getItemViewType(position) == TYPE_ADD) {
            view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            view.setImageResource(R.drawable.add_img_press);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(mContext)
                            .setTitle("图片选择")
                            .setItems(new String[]{"相册", "拍照", "取消"}, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.dismiss();
                                    if (takePhoto == null) {
                                        return;
                                    }
                                    switch (which) {
                                        case 0:
                                            int count = images == null ? limt : limt - images.size();
                                            if (mFragment == null) {
                                                if (cropOptions != null) {
                                                    TakePhotoUtil.pickImageAndCrop(mActivity, takePhoto, cropOptions);
                                                } else {
                                                    TakePhotoUtil.pickImages(mActivity, takePhoto, count);
                                                }
                                            } else {
                                                if (cropOptions != null) {
                                                    TakePhotoUtil.pickImageAndCrop(mFragment.getActivity(), takePhoto, cropOptions);
                                                } else {
                                                    TakePhotoUtil.pickImages(mFragment.getActivity(), takePhoto, count);
                                                }
                                            }

                                            break;
                                        case 1:
                                            if (mFragment == null) {
                                                if (cropOptions != null) {
                                                    TakePhotoUtil.takePhotoAndCrop(mActivity, takePhoto, cropOptions);
                                                } else {
                                                    TakePhotoUtil.takePhoto(mActivity, takePhoto);
                                                }
                                            } else {
                                                if (cropOptions != null) {
                                                    TakePhotoUtil.takePhotoAndCrop(mFragment.getActivity(), takePhoto, cropOptions);
                                                } else {
                                                    TakePhotoUtil.takePhoto(mFragment.getActivity(), takePhoto);
                                                }
                                            }
                                            break;
                                        default:

                                    }
                                }
                            }).show();
                }
            });
        } else {
            if (position > imageViews.length - 1) {
                return;
            }
            imageViews[position] = view;
            if (view == null) {
                return;
            }
            String fileStr = images.get(position);
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            view.setImageResource(R.drawable.icon_default_round);
            if (!TextUtils.isEmpty(fileStr)) {
                if (isFromNet) {
                    if (!fileStr.contains("http")) {  //本地图片
                        File file = new File(fileStr);
                        Uri uri = Uri.fromFile(file);
                        if (mFragment != null) {
                            ImageLoader.getInstance().loadUri(SmContextWrap.obtain(mFragment),
                                    uri, view, R.drawable.icon_default_round);
                        } else if (mActivity != null) {
                            ImageLoader.getInstance().loadUri(SmContextWrap.obtain(mActivity), uri, view, R.drawable.icon_default_round);
                        }
                    } else {
                        //缩略图
                        String url = fileStr.replace("_hd", "");
                        if (mFragment != null) {
                            ImageLoader.getInstance().load(SmContextWrap.obtain(mFragment), url,
                                    R.drawable.icon_default_round, view);
                        } else if (mActivity != null) {
                            ImageLoader.getInstance().load(SmContextWrap.obtain(mActivity), url,
                                    R.drawable.icon_default_round, view);
                        }
                    }
                } else {
                    File file = new File(fileStr);
                    Uri uri = Uri.fromFile(file);
                    if (mFragment != null) {
                        ImageLoader.getInstance().loadUri(SmContextWrap.obtain(mFragment), uri, view, R.drawable.icon_default_round);
                    } else if (mActivity != null) {
                        ImageLoader.getInstance().loadUri(SmContextWrap.obtain(mActivity), uri, view, R.drawable.icon_default_round);
                    }
                }
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (justShow) {
                        String[] strs = new String[images.size()];
                        for (int i = 0; i < images.size(); i++) {
                            strs[i] = images.get(i);
                        }
                        if (mFragment != null) {
                            TakePhotoUtil.openImagesShower(mFragment, strs, position);
                        } else if (mActivity != null) {
                            TakePhotoUtil.openImagesShower(mActivity, position, strs);
                        }
                    } else {
                        if (mFragment != null) {
                            TakePhotoUtil.openImageSelectPage(mFragment, (ArrayList<String>) images, position);
                        } else if (mActivity != null) {
                            TakePhotoUtil.openImageSelectPage(mActivity, (ArrayList<String>) images, position);
                        }
                    }
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        if (justShow) {
            return images.size();
        }

        if (images == null) {
            return 1;
        } else if (images.size() == limt) {  //如果选择的照片已经满了，就需要最后的+号
            return images.size();
        } else {
            return images.size() + 1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (justShow) {
            return TYPE_NORMAL;
        }

        if (images == null) {
            return TYPE_ADD;
        } else if (position == getItemCount() - 1 && images.size() < limt) { //最后一项，并且没有满
            return TYPE_ADD;
        } else {
            return TYPE_NORMAL;
        }
    }

    /**
     * 传给的图片需要为完整地址（带http）
     *
     * @param images
     */
    public void setImages(List<String> images) {
        this.images = images;
    }

    public void setLimit(int limt) {
        this.limt = limt;
        imageViews = new View[limt];
    }

    public void setJustShow(boolean justShow) {
        this.justShow = justShow;
    }

    public void setFromNet(boolean fromNet) {
        isFromNet = fromNet;
        justShow = true;
    }

    /**
     * @param fromNet  图片集合中是否包含网络图片
     * @param justShow 仅查看还是带有删除
     */
    public void setFromNet(boolean fromNet, boolean justShow) {
        isFromNet = fromNet;
        this.justShow = justShow;
    }

    /**
     * 裁剪配置
     *
     * @param options
     */
    public void setCropOptions(CropOptions options) {
        cropOptions = options;
    }

    /**
     * 拖拽移动item位置
     *
     * @param recyclerView
     */
    public void attachToRecyclerViewForDrag(RecyclerView recyclerView) {
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new DragItemTouchHelper(recyclerView));
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public class DragItemTouchHelper extends ItemTouchHelper.Callback {

        private RecyclerView mRecyclerView;

        public DragItemTouchHelper(RecyclerView recyclerView) {
            mRecyclerView = recyclerView;
        }


        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            int dragFlags = 0;
            int swipeFlags = 0;
            if (layoutManager instanceof GridLayoutManager) {
                // 如果是Grid布局，则不能滑动，只能上下左右拖动
                dragFlags =
                        ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                swipeFlags = 0;
            } else if (layoutManager instanceof LinearLayoutManager) {
                // 如果是纵向Linear布局，则能上下拖动，左右滑动
                if (((LinearLayoutManager) layoutManager).getOrientation() == LinearLayoutManager.VERTICAL) {
                    dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                    swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                } else {
                    // 如果是横向Linear布局，则能左右拖动，上下滑动
                    swipeFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                    dragFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                }
            }
            return makeMovementFlags(dragFlags, swipeFlags); //该方法指定可进行的操作
        }

        /**
         * 拖动时回调，在这里处理拖动事件
         *
         * @param viewHolder 被拖动的view
         * @param target     目标位置的view
         */
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) {
            //处理拖动排序
            //使用Collection对数组进行重排序，目的是把我们拖动的Item换到下一个目标Item的位置
            if (mRecyclerView != null && mRecyclerView.getAdapter() instanceof TakePhotoAdapter) {
                TakePhotoAdapter adapter = (TakePhotoAdapter) mRecyclerView.getAdapter();
                if (adapter.justShow
                        || viewHolder.getAdapterPosition() == adapter.getItemCount() - 1
                        || target.getAdapterPosition() == adapter.getItemCount() - 1) {
                    return false;
                }
                List<String> datas = adapter.images;
                Collections.swap(datas, viewHolder.getAdapterPosition(), target.getAdapterPosition());
                //通知Adapter它的Item发生了移动
                mRecyclerView.getAdapter().notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }
            return false;
        }

        /**
         * 滑动时回调
         *
         * @param direction 回调方向
         */
        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        }

        Vibrator mVibrator;

        /**
         * 在这个回调中，如果返回true，表示可以触发长按拖动事件，false则表示不能
         */
        @Override
        public boolean isLongPressDragEnabled() {
            if (mVibrator == null && mRecyclerView != null) {
                mVibrator = (Vibrator) mRecyclerView.getContext().getSystemService(Service.VIBRATOR_SERVICE);
            }
            //震动70毫秒
            if (mVibrator != null) {
                mVibrator.vibrate(70);
            }
            return true;
        }

        /**
         * 在这个回调中，如果返回true，表示可以触发滑动事件，false表示不能
         */
        @Override
        public boolean isItemViewSwipeEnabled() {
            return false;
        }

    }
}