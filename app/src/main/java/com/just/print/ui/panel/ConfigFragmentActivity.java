package com.just.print.ui.panel;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import com.just.print.R;
import com.just.print.app.BaseFragmentActivity;
import com.just.print.app.EventBus;
import com.just.print.ui.fragment.ConfigMainFragment;

import java.util.HashMap;
import java.util.Map;

public class ConfigFragmentActivity extends BaseFragmentActivity implements EventBus.EventHandler {

    public static final String CHANGE_PAGE = "CHANGE_PAGE";

    //to be able to support multiple fragments
    private Map<Class<? extends Fragment>, Fragment> fragments = new HashMap<Class<? extends Fragment>, Fragment>();

    {//while for now, seems we put only one fragment into it.
        fragments.put(ConfigMainFragment.class, new ConfigMainFragment());
    }

    private Fragment getFragment(Class<? extends Fragment> aClass) {
        Fragment fragment = fragments.get(aClass);
        if (fragment == null) {
            try {
                fragment = aClass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (fragment != null)
                fragments.put(aClass, fragment);
        }
        return fragment;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        register(CHANGE_PAGE, this);
        //why not use beginTransaction().add()?
        getSupportFragmentManager().beginTransaction().add(R.id.content, getFragment(ConfigMainFragment.class), ConfigMainFragment.class.getName())
                .commit();
    }

    @Override
    public void handleEvent(String eventName, Object... argument) {
        if (CHANGE_PAGE.equals(eventName) && argument.length > 0) {
            Class<? extends Fragment> clz = (Class<? extends Fragment>) argument[0];
            getSupportFragmentManager().beginTransaction().replace(R.id.content, getFragment(clz), clz.getName())
                    .addToBackStack(clz.getName()).commit();
        }
    }

}
