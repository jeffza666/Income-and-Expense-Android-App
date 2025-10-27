package com.example.cs361v2;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;


public class settingsFragment extends Fragment {

    private DatabaseHelper databaseHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        databaseHelper = new DatabaseHelper(getActivity());

        // Inflate layout for the fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Button deleteButton = view.findViewById(R.id.ID_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });


        // ค้นหาปุ่ม btnChangeLanguage และกำหนด OnClickListener
        Button btnChangeLanguage = view.findViewById(R.id.btnChangeLanguage);
        btnChangeLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLanguagePopup();
            }
        });

        // ค้นหาปุ่ม btnChangeFontSize และกำหนด OnClickListener
        Button btnChangeFontSize = view.findViewById(R.id.btnchangefontsize);
        btnChangeFontSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFontSizePopup();
            }
        });

        // ค้นหาปุ่ม btnBackToHome และกำหนด OnClickListener
        FloatingActionButton btnBackToHome = view.findViewById(R.id.btn_backtohome);
        btnBackToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // เปิด MainActivity
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);

                // ปิด Fragment หรือ Activity ปัจจุบัน (ถ้าจำเป็น)
                getActivity().finish();
            }
        });

        return view; // คืนค่า view
    }


    // แสดง Popup สำหรับการเลือกภาษา
    private void showLanguagePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_langbox, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        Button btnEnglish = dialogView.findViewById(R.id.btn_eng);
        Button btnThai = dialogView.findViewById(R.id.btn_th);
        ImageView cancelBtn = dialogView.findViewById(R.id.btn_cancel); // อ้างอิงปุ่ม cancel_btn

        // การเปลี่ยนเป็นภาษาอังกฤษ
        btnEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLanguage(new Locale("en"));
                dialog.dismiss();
            }
        });

        // การเปลี่ยนเป็นภาษาไทย
        btnThai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLanguage(new Locale("th"));
                dialog.dismiss();
            }
        });

        // ปิด Popup เมื่อกดปุ่ม cancel_btn
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    // แสดง Popup สำหรับการเลือกขนาดฟอนต์
    private void showFontSizePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_fontsize, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        Button btnSmall = dialogView.findViewById(R.id.ID_small);
        Button btnMedium = dialogView.findViewById(R.id.ID_medium);
        Button btnLarge = dialogView.findViewById(R.id.ID_large);
        ImageView btnCancel = dialogView.findViewById(R.id.btn_cancel); // อ้างอิง btn_cancel

        // เมื่อเลือกขนาดฟอนต์เล็ก
        btnSmall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFontSize(12); // ขนาดฟอนต์เล็ก
                dialog.dismiss();
            }
        });

        // เมื่อเลือกขนาดฟอนต์กลาง
        btnMedium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFontSize(16); // ขนาดฟอนต์กลาง
                dialog.dismiss();
            }
        });

        // เมื่อเลือกขนาดฟอนต์ใหญ่
        btnLarge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFontSize(20); // ขนาดฟอนต์ใหญ่
                dialog.dismiss();
            }
        });

        // เมื่อกดปุ่ม cancel
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // ปิด Popup
            }
        });

        dialog.show();
    }


    // เปลี่ยนขนาดฟอนต์
    private void changeFontSize(int size) {
        // กำหนดขนาดฟอนต์ที่เลือก
        Configuration config = new Configuration();
        config.fontScale = size / 16f; // ใช้ค่า 16 เป็นค่าเริ่มต้นสำหรับขนาดฟอนต์
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        // รีเฟรช Activity หรือ Fragment เพื่อให้การเปลี่ยนแปลงมีผล
        getActivity().recreate(); // รีโหลด Activity
    }

    // เปลี่ยนภาษา
    private void changeLanguage(Locale locale) {
        // ตั้งค่า default locale ใหม่
        Locale.setDefault(locale);

        // สร้าง Configuration ใหม่เพื่ออัพเดตการตั้งค่า locale
        Configuration config = new Configuration();
        config.setLocale(locale); // ใช้ setLocale แทน locale
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        // รีเฟรช Activity หรือ Fragment
        getActivity().recreate(); // รีโหลด Activity
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.confirm_delete_title));  // ใช้ getString เพื่อดึงข้อความจาก strings.xml
        builder.setMessage(getString(R.string.confirm_delete_message)); // ใช้ getString เพื่อดึงข้อความจาก strings.xml

        // ปุ่ม "ใช่" สำหรับลบข้อมูล
        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
            databaseHelper.deleteAllTransactions();
            Toast.makeText(getActivity(), getString(R.string.deleted_all_data), Toast.LENGTH_SHORT).show(); // ใช้ getString
        });

        // ปุ่ม "ไม่" สำหรับยกเลิก
        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss());

        // แสดง AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


}