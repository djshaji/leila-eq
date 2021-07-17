package com.shajikhan.leilaequalizer.ui.dashboard;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.audiofx.BassBoost;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.shajikhan.leilaequalizer.LeilaService;
import com.shajikhan.leilaequalizer.R;
import com.shajikhan.leilaequalizer.Reverb;
import com.shajikhan.leilaequalizer.databinding.FragmentDashboardBinding;

public class DashboardFragment extends Fragment {

    private static final String TAG = "BASS/SURROUND";
    private DashboardViewModel dashboardViewModel;
    private FragmentDashboardBinding binding;
    Context context ;
    LeilaService leilaService ;
    SharedPreferences sharedPref ;
    SharedPreferences.Editor pref ;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        pref = sharedPref.edit();

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        context = getContext();

        ServiceConnection serviceConnection = new ServiceConnection() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                leilaService =
                        ((LeilaService.ServiceBinder) service).getService();
                setupBass();
                setupSurround();
                setupReverb();
                Switch bb = root.findViewById(R.id.bb_toggle),
                        ss = root.findViewById(R.id.ss_toggle);
                bb.setChecked(sharedPref.getBoolean("bassBoostEnabled", false));
                ss.setChecked(sharedPref.getBoolean("surroundSoundEnabled", false));
                //TODO restore eq levels here
//            for(short i = 0; i < 5; i ++) {
//                eqService.equalizer().setBandLevel(i, model.getBandLevel(i));
//            }
            }

            public void onServiceDisconnected(ComponentName className) {
                leilaService = null;
            }
        };

        Switch bb = root.findViewById(R.id.bb_toggle);
        bb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                leilaService.bassBoost().setEnabled(isChecked);
                pref.putBoolean("bassBoostEnabled", isChecked).commit();
            }
        });

        Switch ss = root.findViewById(R.id.ss_toggle);
        ss.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                leilaService.virtualizer().setEnabled(isChecked);
                pref.putBoolean("surroundSoundEnabled", isChecked).commit();

            }
        });

        context.bindService(new Intent(context,LeilaService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupReverb () {
        Spinner spinner = (Spinner) getActivity().findViewById(R.id.reverb);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.reverb, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        Button button = getActivity().findViewById(R.id.reverb_advanced_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Reverb.class);
                startActivity(intent);

            }
        });

        Switch toggle = getActivity().findViewById(R.id.reverb_toggle);
        toggle.setChecked(leilaService.environmentalReverb().getEnabled());
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged: " + isChecked);
                leilaService.environmentalReverb().setEnabled(isChecked);
                leilaService.presetReverb().setEnabled(isChecked);
            }
        });

        Spinner reverb = getActivity().findViewById(R.id.reverb);
        reverb.setSelection(leilaService.presetReverb().getPreset());
        reverb.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                leilaService.presetReverb().setPreset((short) position);
                Log.d(TAG, "onItemSelected: " + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        SeekBar amount = getActivity().findViewById(R.id.reverb_amount);
        amount.setMax(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            amount.setMin(-9000);
        }
        amount.setProgress(leilaService.environmentalReverb().getRoomLevel());
        amount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                leilaService.environmentalReverb().setRoomLevel((short) progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setupSurround () {
        SeekBar virt = getActivity().findViewById(R.id.surround);
        virt.setMax(1000);
        virt.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                leilaService.virtualizer().setStrength((short) progress);
                pref.putInt("virtualizer", progress).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        virt.setProgress(sharedPref.getInt("virtualizer",leilaService.virtualizer().getRoundedStrength()));
        Spinner spinner = (Spinner) getActivity().findViewById(R.id.surround_mode);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.surround, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setSelection(sharedPref.getInt("virtualizerMode",(int) (leilaService.virtualizer().getVirtualizationMode() + 1))-1);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                view.setEnabled(false);
                leilaService.virtualizer().forceVirtualizationMode(position + 1);
                pref.putInt("virtualizerMode", position + 1).apply();
                view.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }


    private void setupBass () {
        SeekBar bass = getActivity().findViewById(R.id.bass);
        bass.setProgress(sharedPref.getInt("bass", leilaService.bassBoost().getRoundedStrength()));
        bass.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                leilaService.bassBoost().setStrength((short) progress);
                pref.putInt("bass", progress).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}