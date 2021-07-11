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
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    Context context ;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        context = getActivity();

        mEqualizer = new Equalizer(1, new MediaPlayer().getAudioSessionId());
//        mEqualizer = context.leilaService.equalizer();
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

        mLinearLayout = root.findViewById (R.id.eq_ll);
//        mLinearLayout = new LinearLayout(context);
        short bands = mEqualizer.getNumberOfBands();

        final short minEQLevel = mEqualizer.getBandLevelRange()[0];
        final short maxEQLevel = mEqualizer.getBandLevelRange()[1];

        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
//        short bands = 5 ; //TODO: do this automagically
        int band_freq [] = {60, 500, 1000, 8000, 14000};
        for (short i = 0; i < bands; i++) {
            final short band = i;

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
            bar.setLayoutParams(layoutParams);
//            bar.setMax(12);
            bar.setMax(maxEQLevel - minEQLevel);
            bar.setProgress(mEqualizer.getBandLevel(band));

//            bar.setProgress(6);

            /*
            bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    mEqualizer.setBandLevel(band, (short) (progress + minEQLevel));
                }

                public void onStartTrackingTouch(SeekBar seekBar) {}
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

             */

            row.addView(minDbTextView);
            row.addView(bar);
            row.addView(maxDbTextView);

            mLinearLayout.addView(row);
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}