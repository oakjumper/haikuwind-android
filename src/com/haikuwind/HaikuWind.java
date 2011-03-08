package com.haikuwind;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.haikuwind.dialogs.CancelListener;
import com.haikuwind.dialogs.FinishListener;
import com.haikuwind.feed.FeedException;
import com.haikuwind.feed.HttpRequest;
import com.haikuwind.feed.UserInfo;
import com.haikuwind.state.Event;
import com.haikuwind.state.State;
import com.haikuwind.state.StateListener;
import com.haikuwind.state.StateMachine;
import com.haikuwind.tabs.Favorites;
import com.haikuwind.tabs.HallOfFame;
import com.haikuwind.tabs.MyOwn;
import com.haikuwind.tabs.Timeline;
import com.haikuwind.tabs.TopChart;

public class HaikuWind extends TabActivity implements StateListener {

    private static final int ERROR_TRY_AGAIN_REGISTER = 0;
    private static final int ERROR_TRY_AGAIN_POST_HAIKU = 1;
    private static final int POST_HAIKU = 2;
    private static final int USER_INFO = 3;
    private static final int SUGGEST_NETWORK_SETTINGS = 4;
    
    @SuppressWarnings("unused")
    private static String TAG = HaikuWind.class.getSimpleName();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        //TODO: remove when user info is requested separately
        return UserInfo.getCurrent()!=null;
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout;

        switch (id) {
        case (ERROR_TRY_AGAIN_REGISTER):
            builder.setMessage(R.string.oops)
                    .setCancelable(false)
                    .setNegativeButton(R.string.cancel,
                            new FinishListener(this))
                    .setPositiveButton(R.string.try_again,
                            new OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    registerUser();
                                }
                            });
            break;

        case ERROR_TRY_AGAIN_POST_HAIKU:
            builder.setMessage(R.string.oops)
                    .setNegativeButton(R.string.cancel, new CancelListener())
                    .setPositiveButton(R.string.try_again,
                            new OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    showDialog(POST_HAIKU);
                                }
                            });
            break;

        case POST_HAIKU:
            layout = inflater.inflate(R.layout.post_haiku_dialog, null);

            builder.setNegativeButton(R.string.cancel, new CancelListener())
                    .setPositiveButton(R.string.send,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                        int id) {
                                    doPostHaiku(dialog);
                                }
                            });

            builder.setView(layout);
            break;

        case USER_INFO:
            layout = inflater.inflate(R.layout.user_info_dialog, null);

            UserInfo user = UserInfo.getCurrent();

            ((TextView) layout.findViewById(R.id.user_rank)).setText(user
                    .getRank().getRankStringId());
            String value = Integer.toString(user.getRank().getPower());
            ((TextView) layout.findViewById(R.id.user_voting_power))
                    .setText(value);
            value = Integer.toString(user.getScore());
            ((TextView) layout.findViewById(R.id.user_score)).setText(value);
            value = Integer.toString(user.getFavoritedTimes()) + " "
                    + getString(R.string.times);
            ((TextView) layout.findViewById(R.id.user_favorited_times))
                    .setText(value);

            ((ImageView) layout.findViewById(R.id.user_image))
                    .setImageResource(user.getRank().getImageId());

            builder.setNegativeButton(R.string.close, new CancelListener())
                    .setView(layout);
            break;

        case SUGGEST_NETWORK_SETTINGS:
            builder.setTitle(R.string.connection_failed)
                    .setMessage(R.string.suggest_network_settings)
                    .setCancelable(false)
                    .setNegativeButton(R.string.cancel,
                            new FinishListener(this))
                    .setPositiveButton(R.string.accept,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    startActivity(new Intent(
                                            Settings.ACTION_WIRELESS_SETTINGS));
                                }
                            });
            break;

        default:
            return super.onCreateDialog(id);
        }

        return builder.create();
    }

    private void doPostHaiku(DialogInterface dialog) {
        View haikuTextView = ((Dialog) dialog).findViewById(R.id.haiku_text);
        CharSequence haiku = ((TextView) haikuTextView).getText();
        
        try {
            HttpRequest.newHaiku(haiku);
            ((TextView) haikuTextView).setText("");
        } catch(FeedException e) {
            showDialog(ERROR_TRY_AGAIN_POST_HAIKU);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.post_haiku:
            showDialog(POST_HAIKU);
            return true;
        case R.id.user_info:
            showDialog(USER_INFO);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void registerUser() {
        TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String userId = tManager.getDeviceId();

        try {
            HttpRequest.newUser(userId);
            StateMachine.processEvent(Event.REGISTERED);
        } catch(FeedException e) {
            showDialog(ERROR_TRY_AGAIN_REGISTER);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

    }
    
    @Override
    protected void onStart() {
        super.onStart();
        StateMachine.processEvent(Event.APP_START);
        
        StateMachine.addStateListener(this);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        
        StateMachine.removeStateListener(this);
        StateMachine.processEvent(Event.APP_STOP);
    }
    
    private void initTabs() {
        Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost(); // The activity TabHost
        TabHost.TabSpec spec; // Reusable TabSpec for each tab
        Intent intent; // Reusable Intent for each tab

        tabHost.clearAllTabs();
        
        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, Timeline.class);

        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost
                .newTabSpec("Timeline")
                .setIndicator(res.getString(R.string.timeline),
                        res.getDrawable(R.drawable.ic_tab_timeline))
                .setContent(intent);
        tabHost.addTab(spec);

        // Top Chart
        intent = new Intent().setClass(this, TopChart.class);
        spec = tabHost
                .newTabSpec("TopChart")
                .setIndicator(res.getString(R.string.top_chart),
                        res.getDrawable(R.drawable.ic_tab_top))
                .setContent(intent);
        tabHost.addTab(spec);

        // Hall of Fame
        intent = new Intent().setClass(this, HallOfFame.class);
        spec = tabHost
                .newTabSpec("HallOfFame")
                .setIndicator(res.getString(R.string.hall_of_fame),
                        res.getDrawable(R.drawable.ic_tab_halloffame))
                .setContent(intent);
        tabHost.addTab(spec);

        // My Own
        intent = new Intent().setClass(this, MyOwn.class);
        spec = tabHost
                .newTabSpec("MyOwn")
                .setIndicator(res.getString(R.string.my_own),
                        res.getDrawable(R.drawable.ic_tab_myown))
                .setContent(intent);
        tabHost.addTab(spec);

        // Favorites
        intent = new Intent().setClass(this, Favorites.class);
        spec = tabHost
                .newTabSpec("Favorites")
                .setIndicator(res.getString(R.string.favorites),
                        res.getDrawable(R.drawable.ic_tab_favorites))
                .setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(0);
        
        // This part may be not supported in future APIs
        TabWidget tw = getTabWidget();
        for (int i = 0; i < tw.getChildCount(); i++) {
            RelativeLayout relLayout = (RelativeLayout) tw.getChildAt(i);
            TextView tv = (TextView) relLayout.getChildAt(1);
            tv.setTextSize(11.0f); // just example
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        processState(StateMachine.getCurrentState());
    }
    
    @Override
    public void processState(State state) {
        switch(StateMachine.getCurrentState()) {
        case REGISTER:
            NetworkInfo info = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))
                    .getActiveNetworkInfo();
            if(info==null || !info.isConnected()) {
                showDialog(SUGGEST_NETWORK_SETTINGS);
            } else {
                registerUser();
            }
            break;
            
        case INIT_LAYOUT:
            initTabs();
            StateMachine.processEvent(Event.LAYOUT_READY);
            break;
            
        case STARTED:
            break;
        }
        
    }


}