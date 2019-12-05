package com.xx.module.common.view.contract;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<Fragment> fragmentsList;
    private List<String> titleList;

    public MyFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public MyFragmentPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
        super(fm);
        this.fragmentsList = fragments;
    }

    public MyFragmentPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments, List<String> titleList) {
        super(fm);
        this.fragmentsList = fragments;
        this.titleList = titleList;
    }

    @Override
    public int getCount() {
        return fragmentsList.size();
    }

    @Override
    public Fragment getItem(int arg0) {
        return fragmentsList.get(arg0);
    }


    @Override
    public CharSequence getPageTitle(int position) {
        if (titleList != null && !titleList.isEmpty()) {
            return titleList.get(position);
        } else {
            return super.getPageTitle(position);
        }

    }
}
