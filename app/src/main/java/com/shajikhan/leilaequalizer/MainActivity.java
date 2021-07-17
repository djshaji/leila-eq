package com.shajikhan.leilaequalizer;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.media.audiofx.Equalizer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.shajikhan.leilaequalizer.databinding.ActivityMainBinding;
import com.shajikhan.leilaequalizer.ui.Constants;
import com.shajikhan.leilaequalizer.ui.Service2;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    public static final String TAG = "MainActivity";

    public static final int NOTIFICATION_ID = 888;
    private NotificationManagerCompat mNotificationManagerCompat;

    String tag = "Main" ;
    private Intent intent;
    public LeilaService leilaService;
    private boolean isServiceOn;

    private NotificationManager notificationManager;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            leilaService =
                    ((LeilaService.ServiceBinder) service).getService();
            //TODO restore eq levels here
//            for(short i = 0; i < 5; i ++) {
//                eqService.equalizer().setBandLevel(i, model.getBandLevel(i));
//            }
            final Switch s = findViewById(R.id.eq_toggle);
            s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d (tag, "service status" +  isChecked);
                    leilaService.equalizer().setEnabled(isChecked);
//                    leilaService.bassBoost().setEnabled(isChecked);
//                    leilaService.virtualizer().setEnabled(isChecked);

                }
            });
        }

        public void onServiceDisconnected(ComponentName className) {
            leilaService = null;
        }
    };




    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Create Service
//        Intent notificationIntent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent =
//                PendingIntent.getActivity(this, 0, notificationIntent, 0);
//
//
        Activity activity = this ;
//        this.leilaService = new LeilaService() ;

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);


//        Intent startIntent = new Intent(MainActivity.this, LeilaService.class);
//        startIntent.setAction(Constants.ACTION.START_ACTION);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(startIntent);
//        }else{
//            startService(startIntent);
//        }




//        leilaService = new LeilaService() ;

        mNotificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        generateNotification();

        ActionBar actionBar = getSupportActionBar() ;
        actionBar.hide();

        Button quit = findViewById(R.id.quit_button1);
//        quit = new Button(this) ;
//        quit.setText("X");
//        quit.setText("Close");

        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leilaService.stopForeground(true);
                leilaService.onDestroy();
//                leilaService.stopSelf();
//                stopService(intent);
                mNotificationManagerCompat.cancelAll();
                finishAffinity();
                finish();
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar3);
        setActionBar(toolbar);

        intent = new Intent(this, LeilaService.class);
//        startService(intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        }else{
            startService(intent);
        }

//        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        isServiceOn = true;
        bindService(new Intent(this,LeilaService.class), serviceConnection, Context.BIND_AUTO_CREATE);

    }

    public LeilaService getService () {
        return leilaService;
    }

    private void generateNotification() {

        Log.d(TAG, "generateBigTextStyleNotification()");

        // Main steps for building a BIG_TEXT_STYLE notification:
        //      0. Get your data
        //      1. Create/Retrieve Notification Channel for O and beyond devices (26+)
        //      2. Build the BIG_TEXT_STYLE
        //      3. Set up main Intent for notification
        //      4. Create additional Actions for the Notification
        //      5. Build and issue the notification


        // 1. Create/Retrieve Notification Channel for O and beyond devices (26+).
        String notificationChannelId =
                NotificationUtil.createNotificationChannel(this);


        // 2. Build the BIG_TEXT_STYLE.
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
                // Overrides ContentText in the big form of the template.
                .bigText(getString(R.string.app_name))
                // Overrides ContentTitle in the big form of the template.
                .setBigContentTitle(getString(R.string.notification));
                // Summary line after the detail section in the big form of the template.
                // Note: To improve readability, don't overload the user with info. If Summary Text
                // doesn't add critical information, you should skip it.
//                .setSummaryText("Summary");


        // 3. Set up main Intent for notification.
        Intent notifyIntent = new Intent(this, MainActivity.class);

        // When creating your Intent, you need to take into account the back state, i.e., what
        // happens after your Activity launches and the user presses the back button.

        // There are two options:
        //      1. Regular activity - You're starting an Activity that's part of the application's
        //      normal workflow.

        //      2. Special activity - The user only sees this Activity if it's started from a
        //      notification. In a sense, the Activity extends the notification by providing
        //      information that would be hard to display in the notification itself.

        // For the BIG_TEXT_STYLE notification, we will consider the activity launched by the main
        // Intent as a special activity, so we will follow option 2.

        // For an example of option 1, check either the MESSAGING_STYLE or BIG_PICTURE_STYLE
        // examples.

        // For more information, check out our dev article:
        // https://developer.android.com/training/notify-user/navigation.html

        // Sets the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );


        // 4. Create additional Actions (Intents) for the Notification.

        // In our case, we create two additional actions: a Snooze action and a Dismiss action.
        // Snooze Action.
        Intent snoozeIntent = new Intent(this, ServiceManager.class);
        snoozeIntent.setAction(ServiceManager.ACTION_SNOOZE);

        PendingIntent snoozePendingIntent = PendingIntent.getService(this, 0, snoozeIntent, 0);
        NotificationCompat.Action snoozeAction =
                new NotificationCompat.Action.Builder(
                        R.drawable.ic_launcher_foreground,
                        "Snooze",
                        snoozePendingIntent)
                        .build();


        // Dismiss Action.
        Intent dismissIntent = new Intent(this, ServiceManager.class);
        dismissIntent.setAction(ServiceManager.ACTION_DISMISS);

        PendingIntent dismissPendingIntent = PendingIntent.getService(this, 0, dismissIntent, 0);
        NotificationCompat.Action dismissAction =
                new NotificationCompat.Action.Builder(
                        R.drawable.ic_launcher_foreground,
                        "Dismiss",
                        dismissPendingIntent)
                        .build();


        // 5. Build and issue the notification.

        // Because we want this to be a new notification (not updating a previous notification), we
        // create a new Builder. Later, we use the same global builder to get back the notification
        // we built here for the snooze action, that is, canceling the notification and relaunching
        // it several seconds later.

        // Notification Channel Id is ignored for Android pre O (26).
        NotificationCompat.Builder notificationCompatBuilder =
                new NotificationCompat.Builder(
                        getApplicationContext(), notificationChannelId);

        GlobalNotificationBuilder.setNotificationCompatBuilderInstance(notificationCompatBuilder);

        Notification notification = notificationCompatBuilder
                // BIG_TEXT_STYLE sets title and content for API 16 (4.1 and after).
                .setStyle(bigTextStyle)
                // Title for API <16 (4.0 and below) devices.
                .setContentTitle(getString(R.string.app_name))
                // Content for API <24 (7.0 and below) devices.
                .setContentText(getString(R.string.notification))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setLargeIcon(BitmapFactory.decodeResource(
                        getResources(),
                        R.drawable.ic_launcher_foreground))
                .setContentIntent(notifyPendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                // Set primary color (important for Wear 2.0 Notifications).
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.Blue_Orchid))

                // SIDE NOTE: Auto-bundling is enabled for 4 or more notifications on API 24+ (N+)
                // devices and all Wear devices. If you have more than one notification and
                // you prefer a different summary notification, set a group key and create a
                // summary notification via
                // .setGroupSummary(true)
                // .setGroup(GROUP_KEY_YOUR_NAME_HERE)

                .setCategory(Notification.CATEGORY_SERVICE)

                // Sets priority for 25 and below. For 26 and above, 'priority' is deprecated for
                // 'importance' which is set in the NotificationChannel. The integers representing
                // 'priority' are different from 'importance', so make sure you don't mix them.
                .setPriority(1)

                // Sets lock-screen visibility for 25 and below. For 26 and above, lock screen
                // visibility is set in the NotificationChannel.
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                // Adds additional actions specified above.
//                .addAction(snoozeAction)
//                .addAction(dismissAction)
                .setAutoCancel(false)
                .setOngoing(true)

                .build();

        mNotificationManagerCompat.notify(NOTIFICATION_ID, notification);
    }

}