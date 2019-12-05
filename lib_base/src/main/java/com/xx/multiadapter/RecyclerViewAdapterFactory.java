package com.xx.multiadapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.GridLayoutManager.SpanSizeLookup;
import android.support.v7.widget.RecyclerView.ItemAnimator;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.View.OnClickListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapterFactory {
    private static Class<? extends BaseFooterAdapter> footerAdapterTemple = DefaultFooterAdapter.class;
    private static Class<? extends BaseLoadingAdapter> emptyAdapterTemple = DefaultLoadingAdapter.class;

    public RecyclerViewAdapterFactory() {
    }

    public static void registerFooterAdapterTemple(@NonNull Class<? extends BaseFooterAdapter> clz) {
        footerAdapterTemple = clz;
    }

    public static void registerEmptyAdapterTemple(@NonNull Class<? extends BaseLoadingAdapter> clz) {
        emptyAdapterTemple = clz;
    }

    public static BaseLoadingAdapter getEmptyAdapterFromTemple(Context context) {
        try {
            Constructor<? extends BaseLoadingAdapter> constructor = emptyAdapterTemple.getConstructor(Context.class);
            return (BaseLoadingAdapter)constructor.newInstance(context);
        } catch (NoSuchMethodException var2) {
            var2.printStackTrace();
        } catch (IllegalAccessException var3) {
            var3.printStackTrace();
        } catch (InstantiationException var4) {
            var4.printStackTrace();
        } catch (InvocationTargetException var5) {
            var5.printStackTrace();
        }

        return null;
    }

    public static BaseFooterAdapter getFooterAdapterFromTemple(Context context) {
        try {
            Constructor<? extends BaseFooterAdapter> constructor = footerAdapterTemple.getConstructor(Context.class);
            return (BaseFooterAdapter)constructor.newInstance(context);
        } catch (NoSuchMethodException var2) {
            var2.printStackTrace();
        } catch (IllegalAccessException var3) {
            var3.printStackTrace();
        } catch (InstantiationException var4) {
            var4.printStackTrace();
        } catch (InvocationTargetException var5) {
            var5.printStackTrace();
        }

        return null;
    }

    private static void closeDefaultAnimator(RecyclerView rv) {
        ItemAnimator animator = rv.getItemAnimator();
        if (animator != null && animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator)animator).setSupportsChangeAnimations(false);
        }

    }

    public interface OnCreateAdaptersListener {
        void onCreate(List<MultilItemAdapter> var1);
    }

    public static class Params {
        boolean closeAllRvAnim = false;
        Context mContext;
        RecyclerView recyclerView;
        List<MultilItemAdapter> adapters;
        LayoutManager manager;
        OnCreateAdaptersListener listener;
        BaseFooterAdapter footerAdapter;
        BaseLoadingAdapter loadingAdapter;

        public Params() {
        }
    }

    public static class Builder {
        Params params = new Params();

        public Builder(Context context) {
            this.params.mContext = context;
        }

        public Builder setLayoutManager(LayoutManager manager) {
            this.params.manager = manager;
            return this;
        }

        public Builder setEmptyLoadingAdapter(BaseLoadingAdapter adapter) {
            this.params.loadingAdapter = adapter;
            return this;
        }

        public Builder setDefaultEmptyLoadingAdapter(String loadingMessage, OnClickListener onErrorListener) {
            BaseLoadingAdapter adapter = RecyclerViewAdapterFactory.getEmptyAdapterFromTemple(this.params.mContext);
            if (adapter != null) {
                adapter.setLoadingMessage(loadingMessage);
                adapter.setRetryListener(onErrorListener);
            }

            this.params.loadingAdapter = adapter;
            return this;
        }

        public Builder setFooterAdapter(BaseFooterAdapter adapter) {
            this.params.footerAdapter = adapter;
            return this;
        }

        public Builder setDefaultFooter(String loadingMessage, OnClickListener errorListener) {
            BaseFooterAdapter adapter = RecyclerViewAdapterFactory.getFooterAdapterFromTemple(this.params.mContext);
            if (adapter != null) {
                adapter.setRetryListener(errorListener);
                adapter.setLoadingMessage(loadingMessage);
            }

            this.setFooterAdapter(adapter);
            return this;
        }

        public Builder setOnCreateAdaptersListener(OnCreateAdaptersListener listener) {
            this.params.listener = listener;
            return this;
        }

        public Builder attachToRecyclerView(RecyclerView recyclerView) {
            this.params.recyclerView = recyclerView;
            return this;
        }

        public Builder closeAllAnim() {
            this.params.closeAllRvAnim = true;
            return this;
        }

        public RvComboAdapter build() {
            RvComboAdapter adapter = new RvComboAdapter();
            this.params.adapters = new ArrayList();
            if (this.params.footerAdapter != null) {
                adapter.setFooterAdapter(this.params.footerAdapter);
            }

            if (this.params.loadingAdapter != null) {
                adapter.setEmptyAdapter(this.params.loadingAdapter);
            } else {
                if (this.params.listener != null) {
                    this.params.listener.onCreate(this.params.adapters);
                }

                adapter.setMultiItems(this.params.adapters);
            }

            if (this.params.recyclerView != null) {
                if (this.params.manager == null) {
                    this.params.manager = new LinearLayoutManager(this.params.mContext);
                }

                this.params.recyclerView.setLayoutManager(this.params.manager);
                if (this.params.manager instanceof GridLayoutManager) {
                    final GridLayoutManager manager = (GridLayoutManager)this.params.manager;
                    manager.setSpanSizeLookup(new SpanSizeLookup() {
                        public int getSpanSize(int position) {
                            if (Builder.this.params.recyclerView.getAdapter() instanceof RvComboAdapter) {
                                RvComboAdapter layoutAdapter = (RvComboAdapter)Builder.this.params.recyclerView.getAdapter();
                                MultilItemAdapter pair = layoutAdapter.findMultiItemByPosition(position);
                                if (!layoutAdapter.checkAdapter(pair)) {
                                    return 1;
                                } else {
                                    int type = pair.getItemMatchType();
                                    int count;
                                    if (type != 1 && type <= manager.getSpanCount()) {
                                        if (type <= 0) {
                                            count = 1;
                                        } else {
                                            count = type;
                                        }
                                    } else {
                                        count = manager.getSpanCount();
                                    }

                                    return count;
                                }
                            } else {
                                return 1;
                            }
                        }
                    });
                }

                this.params.recyclerView.setAdapter(adapter);
                if (this.params.closeAllRvAnim) {
                    RecyclerViewAdapterFactory.closeDefaultAnimator(this.params.recyclerView);
                }
            }

            return adapter;
        }
    }
}