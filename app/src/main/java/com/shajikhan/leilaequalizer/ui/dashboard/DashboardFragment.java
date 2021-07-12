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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.shajikhan.leilaequalizer.LeilaService;
import com.shajikhan.leilaequalizer.R;
import com.shajikhan.leilaequalizer.databinding.FragmentDashboardBinding;

public class DashboardFragment extends Fragment {

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
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                leilaService =
                        ((LeilaService.ServiceBinder) service).getService();
                setupBass();
                setupSurround();
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
        spinner.setSelection(sharedPref.getInt("virtualizerMode",leilaService.virtualizer().getVirtualizationMode() + 1));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                leilaService.virtualizer().forceVirtualizationMode(position + 1);
                pref.putInt("virtualizerMode", position + 1).apply();
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