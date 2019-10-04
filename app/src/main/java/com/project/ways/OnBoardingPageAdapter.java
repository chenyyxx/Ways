package com.project.ways;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class OnBoardingPageAdapter extends FragmentPagerAdapter {
    private static int NUM_ITEMS=2;

    public OnBoardingPageAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    //Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return LoginFragment.newInstance();
            case 1:
                return RegisterFragment.newInstance();
            default:
                return null;
        }
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public CharSequence getPageTitle(int position){
        switch (position){
            case 0:
                return "Login";
            case 1:
                return "Register";
        }
        return null;
    }
}
