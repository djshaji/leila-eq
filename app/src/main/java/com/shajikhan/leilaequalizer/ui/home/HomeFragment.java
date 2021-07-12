package com.shajikhan.leilaequalizer.ui.home;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.Equalizer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.shajikhan.leilaequalizer.LeilaService;
import com.shajikhan.leilaequalizer.MainActivity;
import com.shajikhan.leilaequalizer.R;
import com.shajikhan.leilaequalizer.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;

import static android.media.audiofx.AudioEffect.CONTENT_TYPE_MUSIC;
import static android.media.audiofx.AudioEffect.EXTRA_AUDIO_SESSION;
import static android.media.audiofx.AudioEffect.EXTRA_CONTENT_TYPE;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private LinearLayout mLinearLayout;
    private Equalizer mEqualizer;
    LeilaService leilaService ;
    short current_preset = 0 ;
    ArrayList <SeekBar> eqSeeks = new ArrayList <SeekBar> () ;

    SharedPreferences sharedPref ;
    SharedPreferences.Editor pref ;

    short minEQLevel ;
    short maxEQLevel ;

    String tag = "EqualizerView" ;
    Context context ;
    private ServiceConnection serviceConnection ;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(tag, "on create");
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        pref = sharedPref.edit();

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        context = getActivity();
        mLinearLayout = root.findViewById (R.id.eq_ll);

//        mEqualizer = new Equalizer(1, new MediaPlayer().getAudioSessionId());
//        mEqualizer = context.eq ;
//        mEqualizer = leilaService.equalizer ();

        if(ContextCompat.checkSelfPermission(context, Manifest.permission.MODIFY_AUDIO_SETTINGS)
                != PackageManager.PERMISSION_GRANTED)
        {
            // Permission is not granted
            new AlertDialog.Builder(context)
                    .setTitle("Audio Permission not granted")
                    .setMessage("Audio Permission is unavailable, and therefore app wont work. Alas!")

                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton("Uh, um, ok, I guess", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        }


        final Button button = root.findViewById(R.id.open_eq);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                Intent eqIntent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                eqIntent.putExtra(EXTRA_CONTENT_TYPE, CONTENT_TYPE_MUSIC);
                eqIntent.putExtra(EXTRA_AUDIO_SESSION, CONTENT_TYPE_MUSIC);

                startActivityForResult(eqIntent, 0);
            }
        });

        serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                leilaService =
                        ((LeilaService.ServiceBinder) service).getService();
                setupEqualizer();
                //TODO restore eq levels here
//            for(short i = 0; i < 5; i ++) {
//                eqService.equalizer().setBandLevel(i, model.getBandLevel(i));
//            }
            }

            public void onServiceDisconnected(ComponentName className) {
                leilaService = null;
            }
        };

        context.bindService(new Intent(context,LeilaService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupEqualizer () {
        Log.d (tag, "setting up eq");
        View root = binding.getRoot();
        ProgressBar progressBar = root.findViewById(R.id.eq_progressBar);
        mEqualizer = leilaService.equalizer();
        ArrayList<String> presets = new ArrayList<String>();

        int i = 0 ;
        for (i = 0 ; i < mEqualizer.getNumberOfPresets() ; i ++) {
            presets.add(mEqualizer.getPresetName((short) i));
        }

        Spinner preset = (Spinner) root.findViewById(R.id.eq_presets);
        ArrayAdapter<String> stringArrayAdapter =
                new ArrayAdapter<String>(context,  android.R.layout.simple_spinner_dropdown_item, presets);
        stringArrayAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);

        preset.setAdapter(stringArrayAdapter);
        preset.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                short bands = mEqualizer.getNumberOfBands();
                mEqualizer.usePreset((short) position);
                for (short i = 0 ; i < bands; i ++) {
                    Log.d(tag, "EQ " + i + " :" + mEqualizer.getBandLevel(i));
//                    eqSeeks.get(i).setMax(0);
                    eqSeeks.get(i).setMax(maxEQLevel);
                    Log.d (tag, "max" + maxEQLevel);
//                    eqSeeks.get(i).setMin(minEQLevel);
                    eqSeeks.get(i).setProgress(mEqualizer.getBandLevel(i));
                }

                current_preset = (short) position;
                mEqualizer.usePreset((short) position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
//                mEqualizer.usePreset((short) 0);
//                short bands = mEqualizer.getNumberOfBands();
//                for (short i = 0 ; i < bands; i ++) {
//                    eqSeeks.get(i).setProgress(mEqualizer.getBandLevel(i));
//                }

            }
        });

        preset.setSelection(mEqualizer.getCurrentPreset());
        mLinearLayout.removeAllViews();
//        mLinearLayout = root.findViewById (R.id.eq_ll);
//        mLinearLayout = new LinearLayout(context);
        short bands = mEqualizer.getNumberOfBands();

        minEQLevel = mEqualizer.getBandLevelRange()[0];
        maxEQLevel = mEqualizer.getBandLevelRange()[1];

        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
//        short bands = 5 ; //TODO: do this automagically
        int band_freq [] = {60, 500, 1000, 8000, 14000};
        for (i = 0; i < bands; i++) {
            final short band = (short) i;

            TextView freqTextView = new TextView(context);
            freqTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            freqTextView.setGravity(Gravity.CENTER_HORIZONTAL);
//            freqTextView.setText(band_freq [i] + " Hz");
            freqTextView.setText((mEqualizer.getCenterFreq(band) / 1000) + " Hz");

            mLinearLayout.addView(freqTextView);

            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);

            TextView minDbTextView = new TextView(context);
            minDbTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
//            minDbTextView.setText("0 dB");
            minDbTextView.setText((minEQLevel / 100) + " dB");

            TextView maxDbTextView = new TextView(context);
            maxDbTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
//            maxDbTextView.setText("12 dB");
            maxDbTextView.setText((maxEQLevel / 100) + " dB");

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.weight = 1;
            SeekBar bar = new SeekBar(context);
            eqSeeks.add(bar);
            bar.setLayoutParams(layoutParams);
//            bar.setMax(12);
            bar.setMax(maxEQLevel - minEQLevel);
            bar.setMin(minEQLevel);
            bar.setProgress(sharedPref.getInt("eq-" + band, mEqualizer.getBandLevel(band)));

//            bar.setProgress(6);


            bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
//                    mEqualizer.setBandLevel(band, (short) (progress + minEQLevel));
                    mEqualizer.setBandLevel(band, (short) progress);
                    pref.putInt("eq-" + band, progress).apply();

                }

                public void onStartTrackingTouch(SeekBar seekBar) {}
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });



            row.addView(minDbTextView);
            row.addView(bar);
            row.addView(maxDbTextView);

            mLinearLayout.addView(row);
        }

        String [] sinks =  {"System Default"} ;
        Spinner spinner = (Spinner) root.findViewById(R.id.audio_sinks);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.sinks, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

    }
}