package com.example.zhixin.tally;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zhixin.tally.DataLayer.CategoryArray;
import com.example.zhixin.tally.DataLayer.Tally;
import com.example.zhixin.tally.DataLayer.TallyLab;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 */
public class ImportExportFragment extends Fragment {
    private static final int DATE_REQUEST_CODE = 0;
    private static final int FILE_REQUEST_CODE = 1;
    private static final String DIALOG_DATE = "DialogDate";

    private TextView mMonthTextView;
    private TextView mPathTextView;
    private Button mImportBtn;
    private Button mExportBtn;

    public ImportExportFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_import_export, container, false);
        mMonthTextView = (TextView) v.findViewById(R.id.import_export_month);
        mPathTextView = (TextView) v.findViewById(R.id.selected_import_path);
        mImportBtn = (Button) v.findViewById(R.id.btn_import_csv);
        mExportBtn = (Button) v.findViewById(R.id.btn_export_csv);

        mMonthTextView.setText(CalendarHelper.getDateString("yyyy"));
        mImportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
            }
        });

        mMonthTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                try {
                    Calendar calendar = CalendarHelper.getCalendar(mMonthTextView.getText().toString(), "yyyy");
                    DatePickerFragment dialog = DatePickerFragment.newInstance(calendar.getTime(), true);
                    dialog.setTargetFragment(ImportExportFragment.this, DATE_REQUEST_CODE);
                    dialog.show(manager, DIALOG_DATE);
                } catch (ParseException pe) {
                    Toast.makeText(getContext(), "Parse date error!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mExportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.ok)
                        .setMessage(R.string.confirm_export)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    Calendar calendar = CalendarHelper.getCalendar(mMonthTextView.getText().toString(), "yyyy");
                                    List<Tally> tallies = TallyLab.get(getActivity()).getTallies(TallyLab.getStartOfYear(calendar), TallyLab.getEndOfYear(calendar));
                                    if (tallies != null && tallies.size() > 0) {
                                        writeTallyToCsv(tallies);
                                    } else {
                                        mPathTextView.setText(R.string.nodata_to_export);
                                    }
                                } catch (ParseException pe) {
                                    Toast.makeText(getContext(), "Parse date error!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .show();
            }
        });
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case DATE_REQUEST_CODE:
                Date result = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(result.getTime());
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
                mMonthTextView.setText(dateFormat.format(calendar.getTime()));
                break;
            case FILE_REQUEST_CODE:
                Uri uri = data.getData();
                String path = FileHelper.getPath(getActivity(), uri);
                if (path.endsWith(".csv")) {
                    mPathTextView.setText(path);
                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.ok)
                            .setMessage(R.string.confirm_import)
                            .setNegativeButton(R.string.cancel, null)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    boolean isImportSucceed = false;
                                    List<Tally> tallies = readTallyFromCsv(mPathTextView.getText().toString());
                                    if (tallies != null && tallies.size() > 0) {
                                        isImportSucceed = TallyLab.get(getActivity()).importOneYearTallies(tallies);
                                    }
                                    String result = isImportSucceed ? "导入成功: " : "导入失败: ";
                                    mPathTextView.setText(result + mPathTextView.getText().toString());
                                }
                            })
                            .show();
                } else {
                    mPathTextView.setText(R.string.not_supported_file);
                }
                break;
            default:
                break;
        }

    }

    private List<Tally> readTallyFromCsv(String path) {
        boolean isSucceed = false;
        List<Tally> tallies = new ArrayList<>();
//        String date = mMonthTextView.getText().toString() + "-01";
        File file = new File(path);
        FileInputStream fileInputStream = null;
        Scanner in = null;
        String newLine = null;
        try {
            fileInputStream = new FileInputStream(file);
            in = new Scanner(fileInputStream, "GBK");

            while (in.hasNextLine()) {
                newLine = in.nextLine();
                if (newLine.trim().isEmpty()) {
                    continue;
                }
                String[] line = newLine.split(",");
                Tally tally = new Tally(UUID.randomUUID());
                tally.setDescription(line[0]);
                tally.setAmount(Double.parseDouble(line[1]));
                tally.setCategory(CategoryArray.getIndex(line[2]));
                tally.setDate(line[3]);
                tallies.add(tally);
            }
            isSucceed = true;
        } catch (Exception e) {
            mPathTextView.setText(mPathTextView.getText().toString() + "," + newLine);
            Log.i("tag_import", e.getMessage());
            e.printStackTrace();
        } finally {
            in.close();
            try {
                fileInputStream.close();
            } catch (IOException e) {
                Log.i("tag_import", e.getMessage());
                e.printStackTrace();
            }

            return isSucceed ? tallies : null;
        }
    }

    private void writeTallyToCsv(List<Tally> tallies) {
        String path = Environment.getExternalStorageDirectory() + "/Download/" + mMonthTextView.getText().toString() + ".csv";
        File file = new File(path);
        FileOutputStream fileOutputStream = null;

        try {
            file.createNewFile();
            fileOutputStream = new FileOutputStream(file);
            for (Tally tally : tallies) {
                String string = tally.getDescription() + "," + tally.getAmount().toString() + "," + CategoryArray.getArray()[tally.getCategory()]+","+tally.getDate();
                fileOutputStream.write(string.getBytes("GBK"));
                fileOutputStream.write("\r\n".getBytes());
            }
            mPathTextView.setText("导出成功: " + path);
        } catch (Exception e) {
            Log.i("tag_export", e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                Log.i("tag_export", e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
