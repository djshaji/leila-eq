package com.shajikhan.leilaequalizer.ui.notifications;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.audiofx.DynamicsProcessing;
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
import android.widget.Spinner;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.shajikhan.leilaequalizer.LeilaService;
import com.shajikhan.leilaequalizer.R;
import com.shajikhan.leilaequalizer.databinding.FragmentNotificationsBinding;

import java.util.ArrayList;
import java.util.Arrays;

import static android.content.Context.MODE_PRIVATE;

public class NotificationsFragment extends Fragment {

    public static float [][] compressorPresets = {
            // attack time, release time, ratio, threshold, knee, NoiseGate threshold,
            // expander ratio, pre gain, post gain
            {3, 80, 3, -25, 0, -90, 5, 0, 0},
            {29, 80, 4, -30, 0, -90, 8, 0, 0}, // ambient
            {50, 100, 4, -18, 6, -80, 8, 0, 0}, // music
//            {100, 200, 8, -20, 6, -80, 10, 6, 6}, // movie
            {200, 400, 8, -12, 0, -80, 20, 6, 6}, // speech
            {10, 25, 4, -25, 6, -90, 10, 0, 0} // porn
    } ;

    private NotificationsViewModel notificationsViewModel;
    private FragmentNotificationsBinding binding;
    LeilaService leilaService ;
    String tag = "CompressorView" ;
    Context context ;
    SharedPreferences sharedPref ;
    SharedPreferences.Editor pref ;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            leilaService =
                    ((LeilaService.ServiceBinder) service).getService();
            Switch s = binding.getRoot().findViewById(R.id.compressor_toggle) ;
            s .setChecked(leilaService.getDynamicsProcessing().getEnabled());

        }

        public void onServiceDisconnected(ComponentName className) {
            leilaService = null;
        }
    };


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        sharedPref = getActivity().getPreferences(MODE_PRIVATE);
        pref = sharedPref.edit();

        notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Spinner compressor = (Spinner) root.findViewById(R.id.compressor);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.compressor, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);

        compressor.setAdapter(adapter);
        compressor.setSelection(sharedPref.getInt("compressorPreset", 0));

        Spinner limiter = (Spinner) root.findViewById(R.id.limiter);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getActivity(),
                R.array.limiter, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);

        limiter.setAdapter(adapter2);
        context.bindService(new Intent(context,LeilaService.class), serviceConnection, Context.BIND_AUTO_CREATE);

        Switch toggle = root.findViewById(R.id.compressor_toggle);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                leilaService.getDynamicsProcessing().setEnabled(isChecked);
                pref.putBoolean("compressorEnabled", isChecked).apply();
//                leilaService.compressor().setEnabled(isChecked);
            }
        });

        Button button = root.findViewById(R.id.compressor_advanced);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Compressor.class);
                startActivity(intent);

            }
        });

        Spinner spinner = root.findViewById(R.id.compressor);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pref.putInt("compressorPreset", position).apply();
                if (position >= compressorPresets.length) {// custom preset
                    Log.d(getClass().getName(), "looking for custom compressor settings");
                    int [] arrayList = restoreCustomCompressor();
                    if (arrayList != null) {
                        DynamicsProcessing.MbcBand mbcBand = new DynamicsProcessing.MbcBand(
                                true, 20000f,
                                arrayList[0],
                                arrayList[1],
                                arrayList [2],
                                arrayList[3],
                                arrayList[4],
                                arrayList[5],
                                arrayList[6],
                                arrayList[7],
                                arrayList[8]
                        );

                        leilaService.getDynamicsProcessing().setMbcBandAllChannelsTo(0, mbcBand);
                    }

                    return;
                }

                DynamicsProcessing.MbcBand mbcBand = new DynamicsProcessing.MbcBand(
                        true, 20000f,
                        compressorPresets [position][0],
                        compressorPresets [position][1],
                        compressorPresets [position][2],
                        compressorPresets [position][3],
                        compressorPresets [position][4],
                        compressorPresets [position][5],
                        compressorPresets [position][6],
                        compressorPresets [position][7],
                        compressorPresets [position][8]
                );

                Log.d("CompressormbcBand", mbcBand.toString());
                leilaService.getDynamicsProcessing().setMbcBandAllChannelsTo(0, mbcBand);
                Log.d ("LoadedCompressorPreset:", leilaService.getDynamicsProcessing().getConfig().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private int[] restoreCustomCompressor () {
//        String data = sharedPref.getString("compressorCustom", null);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("compressor", MODE_PRIVATE);
        String data = sharedPreferences.getString("custom", null);
//        String data = sharedPref.getString("compressorCustom", null);

        if (data == null) {
            Log.d (getClass().getCanonicalName(), "compressor data null");
            return null;
        }

        Log.d (this.getClass().getName(), "Fetching custom compressor settings" + data);
        String str = data ;
        int[] arr = Arrays.stream(str.substring(1, str.length()-1).split(","))
                .map(String::trim).mapToInt(Integer::parseInt).toArray();


        return arr ;
    }

}