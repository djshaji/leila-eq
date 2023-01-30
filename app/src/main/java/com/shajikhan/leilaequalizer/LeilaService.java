package com.shajikhan.leilaequalizer;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.audiofx.BassBoost;
import android.media.audiofx.DynamicsProcessing;
import android.media.audiofx.EnvironmentalReverb;
import android.media.audiofx.Equalizer;
import android.media.audiofx.LoudnessEnhancer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Virtualizer;
import android.os.Binder;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.app.Service;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;


public class LeilaService extends Service {
    private final ServiceBinder binder = new ServiceBinder();
    String tag = "LeilaService" ;
    Context context ;
    public Equalizer eq;
    private BassBoost bass ;
    private Virtualizer virtual;
    private EnvironmentalReverb reverb ;
    private PresetReverb pReverb ;
    private DynamicsProcessing dynamicsProcessing ;
    private DynamicsProcessing.Mbc mbc ;
    private DynamicsProcessing.MbcBand mbcBand ;
    private DynamicsProcessing.Limiter limiter ;
    private LoudnessEnhancer loudnessEnhancer ;
    public static final int NOTIFICATION_ID = 888;
    private NotificationManagerCompat mNotificationManagerCompat;
    String TAG = getClass().getName() ;
    /**
     * This gets called first when the service is created.
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onCreate()
    {
        super.onCreate();

        eq = new Equalizer(1, 0);
//        for(short i = 0; i < 5; i++) {
//            eq.setBandLevel(i, (short) 0);
//        }

//        eq.setEnabled(true);
        Log.d(tag, "Equalizer connected");
        bass = new BassBoost(0,0);
        virtual = new Virtualizer (0, 0);
        reverb = new EnvironmentalReverb(0, 0);
        pReverb = new PresetReverb(0, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            dynamicsProcessing = new DynamicsProcessing(0);
//        mbc = new DynamicsProcessing.Mbc (0);
            mbcBand = new DynamicsProcessing.MbcBand(true, 16000.0f, 50f,
                    100f, 2.0f, -50f, 0.0f,
                    -80f, 3.0f, 0.0f, 6.0f);
//            for (int i = 0; i < 10; i++)
//                dynamicsProcessing.setMbcBandAllChannelsTo(i, mbcBand);
            DynamicsProcessing.Config.Builder builder = new DynamicsProcessing.Config.Builder(
                    DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
                    1,
                    false,
                    0,
                    true,
                    1,
                    false,
                    0,
                    true
            );
            builder.setPreferredFrameDuration(10);
            DynamicsProcessing.Config config = builder.build();
            mbc = config.getChannelByChannelIndex(0).getMbc();
//            mbcBand = mbc.getBand(0);

            limiter = config.getLimiterByChannelIndex(0);

            mbc.setEnabled(true);
//            mbc.setBand(0,mbcBand);
            mbcBand.setEnabled(true);
            dynamicsProcessing = new DynamicsProcessing(0, 0, config);
            dynamicsProcessing.setEnabled(true);

            loudnessEnhancer = new LoudnessEnhancer(0);
        }


//        Log.d(tag, dynamicsProcessing.getConfig().toString());
    }


    /**
     * This gets called after onCreate() and only gets called
     * when another component calls startService(Intent);
     * @param intent the Intent
     * @param flags int argument 1
     * @param startId int argument 2
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        Log.d(tag, "onStart");

        mNotificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        generateNotification();
        return START_STICKY; //This makes the system restart our
        //process if it gets interrupted.
    }

    /**
     * This is called when another component calls bindService()
     * @return IBinder the binder used to communicate with other
     * components.
     */
    @Override
    public IBinder onBind(Intent arg0)
    {
        return binder;
    }

    /**
     * This gets called when the service is stopped.
     */
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        eq.release ();
        bass.release () ;
        virtual.release();
        reverb.release() ;
        pReverb.release() ;
         dynamicsProcessing.release() ;
         loudnessEnhancer.release();
//         mbc ;
//         mbcBand.release() ;
//        private DynamicsProcessing.Limiter limiter ;

        //.
    }

    /**
     * This is only a test to make sure that the activity
     * has access to the methods of the service.
     * @return String the printout
     */
    public String testPrintout()
    {



        String output = eq.getProperties().toString() + "\n";
        for(short i = 0; i < 5; i ++) {
            output += eq.getBandLevel(i) + " ";
        }
        output += "\n";
        short band = 1;
        short level = 300;
        eq.setBandLevel(band, level);
        for(short i = 0; i < 5; i ++) {
            output += eq.getBandLevel(i) + " ";
        }
        output += "\n\n";
        for(short i = 0; i < 10; i ++) {
            output += i + "  " + eq.getPresetName(i) + "\n";
        }
        short[] range = eq.getBandLevelRange();
        output += "\n" +"Level Range:  " + range[0] + " to " + range[1] + "\n";

        output += "\nThe Center Frequencies of each band\n";
        for(short i = 0; i < 5; i++) {
            output += i + "  " + eq.getCenterFreq(i) + "\n";
        }
        return "THIS IS A TEST!!!\n\n" + output;

    }

    /**
     * Returns an instance of the equalizer.
     * @return Equalizer the equalizer.
     */
    public Equalizer equalizer()
    {
        return eq;
    }
    public BassBoost bassBoost () { return bass ;}
    public Virtualizer virtualizer () {return virtual ;}
    public EnvironmentalReverb environmentalReverb () { return reverb ;}
    public PresetReverb presetReverb () { return pReverb ;}
    @RequiresApi(api = Build.VERSION_CODES.P)
    public DynamicsProcessing.MbcBand compressor () { return dynamicsProcessing.getMbcBandByChannelIndex(0,0); }
    public DynamicsProcessing getDynamicsProcessing () { return dynamicsProcessing ;}
    public LoudnessEnhancer getLoudnessEnhancer () { return loudnessEnhancer ; }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public DynamicsProcessing.Limiter getLimiter() {
        return dynamicsProcessing.getConfig().getLimiterByChannelIndex(0);
    }

    /**
     * T// ----------------------------------------------------------------
     /**
     *  This inner binder class allows the activity to interact with the
     *  service.
     *
     *  @author J. Taylor O'Connor
     *  @version 2012.04.11
     */
    public class ServiceBinder extends Binder {
        /**
         * This returns an instance of this service for whoever
         * called bindservice().
         * @return EqualizerService this
         */
        public LeilaService getService() {
            return LeilaService.this;
        }
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
