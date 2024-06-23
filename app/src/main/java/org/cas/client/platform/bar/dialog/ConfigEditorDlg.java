package org.cas.client.platform.bar.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.just.print.R;

import org.cas.client.platform.cascustomize.CustOpts;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class ConfigEditorDlg extends DialogFragment {

    private EditText editText;
    private Button saveButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_config_editor, container, false);

        editText = view.findViewById(R.id.editTextConfig);
        saveButton = view.findViewById(R.id.buttonSave);

        saveButton.setOnClickListener(v -> {
            saveConfig();
            dismiss();
        });

        initContent();

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("PIMConfig.ini");
        return dialog;
    }

    private void initContent() {
        StringBuilder sb = new StringBuilder();

        Iterator<Map.Entry<String, String>> iterator = CustOpts.custOps.userConfigs.entrySet().iterator();

        // 使用 TreeMap 对 Entry 进行排序
        Map<String, String> sortedMap = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String key1, String key2) {
                return key1.compareTo(key2); // 按键的自然顺序排序
            }
        });

        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            sortedMap.put(entry.getKey(), String.valueOf(entry.getValue()));
        }

        // 遍历排序后的 Entry
        iterator = sortedMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String key = entry.getKey();
            Object value = entry.getValue();
            if (!key.startsWith("[")) { // show only part of the node.
                sb.append(key);
                sb.append("=");
                sb.append(value);
                sb.append("\n");
            }
        }

        editText.setText(sb.toString());
    }

    private void saveConfig() {
        String[] lines = editText.getText().toString().split("\n");
        for (String line : lines) {
            String[] keyValue = line.split("=");
            if (keyValue.length == 2) {
                CustOpts.custOps.userConfigs.put(keyValue[0].trim(), keyValue[1].trim());
            } else {
                CustOpts.custOps.userConfigs.put(keyValue[0].trim(), "");
            }
        }
    }
}
