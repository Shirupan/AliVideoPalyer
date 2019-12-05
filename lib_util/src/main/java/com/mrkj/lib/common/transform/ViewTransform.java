package com.mrkj.lib.common.transform;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author someone
 * @date 2019/3/27 0027
 */
public class ViewTransform implements Parcelable {
    private int[] positions;
    private int mWidth;
    private int mHeight;

    public ViewTransform() {
    }


    protected ViewTransform(Parcel in) {
        positions = in.createIntArray();
        mWidth = in.readInt();
        mHeight = in.readInt();
    }

    public static final Creator<ViewTransform> CREATOR = new Creator<ViewTransform>() {
        @Override
        public ViewTransform createFromParcel(Parcel in) {
            return new ViewTransform(in);
        }

        @Override
        public ViewTransform[] newArray(int size) {
            return new ViewTransform[size];
        }
    };

    public void setPosition(int[] positions) {
        this.positions = positions;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(positions);
        dest.writeInt(mWidth);
        dest.writeInt(mHeight);
    }
}
