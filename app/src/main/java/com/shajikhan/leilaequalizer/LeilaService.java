package com.shajikhan.leilaequalizer;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
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
}
