package com.rifqi.dude2;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabsAccessorAdapter extends FragmentPagerAdapter {

    public TabsAccessorAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i){
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;

            case 1:
                GroupsFragment groupsFragment = new GroupsFragment();
                return groupsFragment;

            case 2:
                StoriesFragment storiesFragment = new StoriesFragment();
                return storiesFragment;

            case 3:
                ContactsFragment contactsFragment = new ContactsFragment();
                return contactsFragment;

            case 4:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;


            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 5;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Chats";
            case 1:
                return "Groups";
            case 2:
                return "Status";
            case 3:
                return "Contacts";
            case 4:
                return "Requests";

            default:
                return null;
        }
    }
}
