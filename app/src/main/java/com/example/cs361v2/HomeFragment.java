package com.example.cs361v2;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceControl;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class HomeFragment extends Fragment {
    private CalendarView calendarView;
    private Calendar calendar;
    private LinearLayout historyList; // Used to display transaction history
    private TextView noTransactionsText; // TextView for "No Transactions" message
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private static final String TAG = "HomeFragment";
    private TextView editTextDate;
    private DatabaseHelper databaseHelper; // Database helper

    private final String[] incomeCategories = {"Salary", "Other Income", "Transfer In", "Interest"};
    private final String[] outcomeCategories = {"Food & Beverages", "Bills & Utilities", "Shopping",
            "Household Items", "Family", "Travel", "Health & Fitness", "Education", "Entertainment",
            "Giving & Donations", "Insurance", "Other Expenses"};

    private String selectedDate;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        calendar = Calendar.getInstance();

        databaseHelper = new DatabaseHelper(getActivity());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        selectedDate = dateFormat.format(calendar.getTime());

        getDate();

        Button conclusionButton = view.findViewById(R.id.conclusionButton);
        historyList = view.findViewById(R.id.history_list);

        EditText goalInput = view.findViewById(R.id.goal_input);
        Button saveGoalButton = view.findViewById(R.id.save_goal_button);
        EditText searchBillText = view.findViewById(R.id.searchBillText);

        noTransactionsText = view.findViewById(R.id.no_transactions_message);
        noTransactionsText.setVisibility(View.VISIBLE);

        ImageButton addTransactionButton = view.findViewById(R.id.addTransactionButton);

        List<Transaction> transactions = databaseHelper.getTransactionsByDate(selectedDate);

        double firstdailyGoal = databaseHelper.getDailyGoal(selectedDate);
        // แสดง Daily Goal ใน EditText
        goalInput.setText(String.format("%.2f", firstdailyGoal));

        if (transactions.isEmpty()) {
            noTransactionsText.setVisibility(View.VISIBLE);
        } else {
            noTransactionsText.setVisibility(View.GONE);
            for (Transaction transaction : transactions) {
                addTransactionToLayout(transaction);
            }
        }

        addTransactionButton.setOnClickListener(v -> {
            if (selectedDate != null) {
                // ใช้วันที่ที่เก็บไว้จาก CalendarView
                String[] dateParts = selectedDate.split("/");
                int year = Integer.parseInt(dateParts[2]);
                int month = Integer.parseInt(dateParts[1]);
                int day = Integer.parseInt(dateParts[0]);
                showAddTransactionDialog(year, month, day);
            } else {
                Toast.makeText(getActivity(), getString(R.string.select_date_first), Toast.LENGTH_SHORT).show();
            }
        });


        saveGoalButton.setOnClickListener(v -> {
            String goalText = goalInput.getText().toString();

            if (!goalText.isEmpty()) {
                double goalAmount = Double.parseDouble(goalText);

                // ตรวจสอบว่ามี Daily Goal ของ selectedDate หรือยัง
                if (databaseHelper.hasDailyGoal(selectedDate)) {
                    databaseHelper.updateDailyGoal(selectedDate, goalAmount);
                } else {
                    databaseHelper.insertDailyGoal(selectedDate, goalAmount);
                }

                goalInput.setText(String.valueOf(goalAmount));

                // แสดงข้อความแจ้งเตือน
                Toast.makeText(getActivity(), getString(R.string.goal_saved_successfully, selectedDate), Toast.LENGTH_SHORT).show();
            } else {
                // แจ้งเตือนเมื่อไม่มีการกรอกข้อมูล
                Toast.makeText(getActivity(), getString(R.string.enter_goal_amount), Toast.LENGTH_SHORT).show();
            }
        });


        // ใช้ TextWatcher เพื่อตรวจสอบการพิมพ์ของผู้ใช้
        searchBillText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ไม่ต้องทำอะไรในส่วนนี้
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // เรียกใช้เมธอดกรองรายการเมื่อมีการพิมพ์
                filterBills(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // ไม่ต้องทำอะไรในส่วนนี้
            }
        });

        conclusionButton.setOnClickListener(v -> {
            // ตรวจสอบว่า selectedDate ไม่เป็น null และมีค่า
            if (selectedDate != null && !selectedDate.isEmpty()) {
                // ดึงข้อมูลธุรกรรมทั้งหมดจากฐานข้อมูลตามวันที่ที่เลือก
                List<Transaction> conclusionTransactions = databaseHelper.getTransactionsByDate(selectedDate);

                if (conclusionTransactions != null && !conclusionTransactions.isEmpty()) {
                    int dailyTotal = 0;
                    for (Transaction transaction : conclusionTransactions) {
                        // ตรวจสอบว่าเป็นธุรกรรมประเภท "outcome" หรือไม่
                        if ("Outcome".equals(transaction.getType()) || "รายจ่าย".equals(transaction.getType()) || "Expenses".equals(transaction.getType())) {
                            dailyTotal += transaction.getAmount();

                            // แสดงข้อมูล transaction ใน Logcat
                            Log.d("TransactionDetails", "Transaction Type: " + transaction.getType() +
                                    ", Amount: " + transaction.getAmount() +
                                    ", Date: " + transaction.getDate());
                        }
                    }


                    // ตรวจสอบเป้าหมายจาก goalInput
                    String goalString = goalInput.getText().toString().trim();
                    if (!goalString.isEmpty()) {
                        try {
                            // แปลงค่าจาก String เป็น Integer โดยใช้ Double.parseDouble() ถ้าเป็นจำนวนทศนิยม
                            double goal = Double.parseDouble(goalString);  // เปลี่ยนจาก Integer เป็น Double
                            String statusMessage;

                            // ตรวจสอบสถานะการใช้จ่าย
                            if (dailyTotal > goal) {
                                statusMessage = getString(R.string.spending_goal_exceeded);
                            } else if (dailyTotal == goal) {
                                statusMessage = getString(R.string.spending_goal_met);
                            } else {
                                statusMessage = getString(R.string.spending_below_goal);
                            }

                            // ส่งข้อมูลไปยัง ConclusionFragment
                            Bundle bundle = new Bundle();
                            bundle.putInt("DAILY_TOTAL", dailyTotal);
                            bundle.putDouble("GOAL", goal);  // แก้ไขให้ใช้ putDouble แทน putInt
                            bundle.putString("STATUS_MESSAGE", statusMessage);
                            bundle.putString("SELECTED_DATE", selectedDate);

                            ConclusionFragment fragment = new ConclusionFragment();
                            fragment.setArguments(bundle);

                            // แสดง Fragment โดยไม่ต้องเปลี่ยนแปลง Activity
                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.frameLayout, fragment) // แทนที่ด้วย container ที่เหมาะสม
                                    .addToBackStack(null) // เพิ่มไปที่ back stack
                                    .commit();
                        } catch (NumberFormatException e) {
                            Toast.makeText(requireActivity(), getString(R.string.invalid_goal_input), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireActivity(), getString(R.string.enter_spending_goal), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireActivity(), getString(R.string.no_transactions_for_date), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireActivity(), getString(R.string.select_date_first), Toast.LENGTH_SHORT).show();
            }
        });



        editTextDate = view.findViewById(R.id.editTextDate);
        editTextDate.setText(selectedDate);
        editTextDate.setOnClickListener(view1 -> {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    getActivity(),
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    mDateSetListener,
                    year, month, day);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        });

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            // เก็บวันที่ที่เลือกในรูปแบบ dd/MM/yyyy
            selectedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);

            // อัปเดต editTextDate ด้วยวันที่ที่เลือก
            editTextDate.setText(selectedDate);

            // แสดง Toast (สามารถลบออกได้หากไม่ต้องการ)
            Toast.makeText(getActivity(), getString(R.string.selected_date_message, selectedDate), Toast.LENGTH_SHORT).show();


            // ลบเฉพาะ View ที่มีแท็ก "transaction" ใน historyList
            for (int i = historyList.getChildCount() - 1; i >= 0; i--) {
                View child = historyList.getChildAt(i);
                if ("transaction".equals(child.getTag())) {
                    historyList.removeView(child);
                }
            }

            // ดึงข้อมูลรายการจากฐานข้อมูลตามวันที่ที่เลือก
            List<Transaction> dailyTransactions = databaseHelper.getTransactionsByDate(selectedDate);

            // ตรวจสอบว่ามีรายการหรือไม่และแสดงผล
            if (dailyTransactions.isEmpty()) {
                noTransactionsText.setVisibility(View.VISIBLE);
            } else {
                noTransactionsText.setVisibility(View.GONE);
                for (Transaction transaction : dailyTransactions) {
                    addTransactionToLayout(transaction);
                }
            }

            // ดึง Daily Goal สำหรับวันที่เลือก
            double dailyGoal = databaseHelper.getDailyGoal(selectedDate);

            // แสดง Daily Goal ใน EditText
            goalInput.setText(String.format("%.2f", dailyGoal));  // แสดงผลในรูปแบบทศนิยม 2 ตำแหน่ง
        });


        mDateSetListener = (datePicker, year, month, day) -> {
            month += 1; // เดือนเริ่มจาก 0 จึงต้องบวกเพิ่ม 1
            selectedDate = String.format("%02d/%02d/%d", day, month, year); // เก็บวันที่ที่เลือก

            // อัปเดต editTextDate ด้วยวันที่ที่เลือก
            editTextDate.setText(selectedDate);
            // ดึง Daily Goal สำหรับวันที่เลือก
            double dailyGoal = databaseHelper.getDailyGoal(selectedDate);
            // แสดง Daily Goal ใน EditText
            goalInput.setText(String.format("%.2f", dailyGoal));


            // ตั้งค่า calendarView ให้แสดงวันที่ที่เลือก
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(year, month - 1, day);
            calendarView.setDate(selectedCalendar.getTimeInMillis(), true, true);

            // ลบเฉพาะ View ที่มีแท็ก "transaction" ใน historyList
            for (int i = historyList.getChildCount() - 1; i >= 0; i--) {
                View child = historyList.getChildAt(i);
                if ("transaction".equals(child.getTag())) {
                    historyList.removeView(child);
                }
            }

            // ดึงข้อมูลจากฐานข้อมูลตามวันที่ที่เลือก
            List<Transaction> dailyTransactions = databaseHelper.getTransactionsByDate(selectedDate);

            // ตรวจสอบว่ามีรายการหรือไม่และแสดงผล
            if (dailyTransactions.isEmpty()) {
                noTransactionsText.setVisibility(View.VISIBLE);
            } else {
                noTransactionsText.setVisibility(View.GONE);
                for (Transaction transaction : dailyTransactions) {
                    addTransactionToLayout(transaction);
                }
            }
        };

        return view;
    }

    public void getDate() {
        long date = calendarView.getDate();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
        calendar.setTimeInMillis(date);
        String selected_date = simpleDateFormat.format(calendar.getTime());
        Toast.makeText(getActivity(), selected_date, Toast.LENGTH_SHORT).show();
    }

    // Function to show dialog for adding transaction
    @SuppressLint("SetTextI18n")
    private void showAddTransactionDialog(int year, int month, int day) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.add_transaction)); // ใช้ getString() ดึงข้อความจาก strings.xml

        // Create Layout for Dialog
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        // Income Checkbox
        CheckBox incomeCheckbox = new CheckBox(getActivity());
        incomeCheckbox.setText(getString(R.string.income)); // ใช้ข้อความจาก strings.xml
        layout.addView(incomeCheckbox);

        // Outcome Checkbox
        CheckBox outcomeCheckbox = new CheckBox(getActivity());
        outcomeCheckbox.setText(getString(R.string.outcome)); // ใช้ข้อความจาก strings.xml
        layout.addView(outcomeCheckbox);

        // Spinner for categories
        final Spinner categorySpinner = new Spinner(getActivity());
        layout.addView(categorySpinner);

        // Update the category spinner based on the selected checkboxes
        incomeCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                outcomeCheckbox.setChecked(false); // Uncheck outcome if income is selected
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        getActivity(),
                        android.R.layout.simple_spinner_item,
                        getResources().getStringArray(R.array.income_categories) // ดึงค่าจาก strings.xml
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categorySpinner.setAdapter(adapter);
            }
        });

        outcomeCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                incomeCheckbox.setChecked(false); // Uncheck income if outcome is selected
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        getActivity(),
                        android.R.layout.simple_spinner_item,
                        getResources().getStringArray(R.array.outcome_categories) // ดึงค่าจาก strings.xml
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categorySpinner.setAdapter(adapter);
            }
        });

        // Add EditText for amount input
        final EditText amountInput = new EditText(getActivity());
        amountInput.setHint(getString(R.string.enter_amount)); // ใช้ข้อความจาก strings.xml
        layout.addView(amountInput);

        builder.setView(layout);

        // Dialog buttons
        builder.setPositiveButton(getString(R.string.add), (dialog, which) -> { // ใช้ getString()
            String billType = "";
            if (incomeCheckbox.isChecked()) {
                billType = getString(R.string.income); // ใช้ข้อความจาก strings.xml
            } else if (outcomeCheckbox.isChecked()) {
                billType = getString(R.string.outcome); // ใช้ข้อความจาก strings.xml
            }

            String amount = amountInput.getText().toString();
            String category = categorySpinner.getSelectedItem() != null ? categorySpinner.getSelectedItem().toString() : ""; // Get selected category

            if (!amount.isEmpty() && !billType.isEmpty()) {
                addTransaction(year, month, day, billType, amount, category); // Pass category to the method
            } else {
                Toast.makeText(getActivity(), getString(R.string.please_fill_in), Toast.LENGTH_SHORT).show(); // ใช้ข้อความจาก strings.xml
            }
        });

        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel()); // ใช้ getString()

        builder.show();
    }


    @SuppressLint("SetTextI18n")
    private void addTransaction(int year, int month, int day, String billType, String amount, String category) {
        // สร้าง LinearLayout สำหรับแสดงข้อมูลการทำรายการ
        LinearLayout transactionContainer = new LinearLayout(getActivity());
        transactionContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        transactionContainer.setOrientation(LinearLayout.VERTICAL);
        transactionContainer.setPadding(16, 16, 16, 16);

        // ตั้งค่า Tag เพื่อใช้ในการลบในอนาคต
        transactionContainer.setTag("transaction");

        // ตั้งค่าสีตามประเภทของบิล (Outcome หรือ Income)
        int borderColor = ("Expenses".equalsIgnoreCase(billType) || "รายจ่าย".equalsIgnoreCase(billType)) ? 0xFFFFB74D : 0xFF81C784;
        transactionContainer.setBackground(createBorderDrawable(borderColor));

        // สร้าง Calendar สำหรับวันที่ที่ผู้ใช้เลือก
        Calendar selectedCalendar = Calendar.getInstance();
        selectedCalendar.set(year, month - 1, day);

        // ดึงเวลาปัจจุบัน
        Calendar currentCalendar = Calendar.getInstance();
        selectedCalendar.set(Calendar.HOUR_OF_DAY, currentCalendar.get(Calendar.HOUR_OF_DAY));
        selectedCalendar.set(Calendar.MINUTE, currentCalendar.get(Calendar.MINUTE));
        selectedCalendar.set(Calendar.SECOND, currentCalendar.get(Calendar.SECOND));
        selectedCalendar.set(Calendar.MILLISECOND, currentCalendar.get(Calendar.MILLISECOND));

        // ฟอร์แมตวันที่และเวลา
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String formattedDateTime = dateTimeFormat.format(selectedCalendar.getTime());

        // ตรวจสอบให้แน่ใจว่า amount เป็นตัวเลขก่อนแปลงเป็น double
        double amountDouble;
        try {
            amountDouble = Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            Toast.makeText(getActivity(), getString(R.string.enter_numbers_only), Toast.LENGTH_SHORT).show();
            return;
        }

        // สร้าง TextView เพื่อแสดงข้อมูลการทำรายการ
        TextView transactionView = new TextView(getActivity());
        transactionView.setText(String.format("%s - %s: %s - %.2f %s", formattedDateTime, billType, category, amountDouble, getString(R.string.currency_unit)));
        transactionContainer.addView(transactionView);

        // เพิ่ม transactionContainer ลงใน history list
        historyList.addView(transactionContainer);

        // ซ่อนข้อความ "No Transactions" หากมีรายการ
        noTransactionsText.setVisibility(View.GONE);

        // บันทึกรายการลงในฐานข้อมูล
        databaseHelper.addTransaction(formattedDateTime, billType, category, amountDouble);
        Toast.makeText(getActivity(), getString(R.string.transaction_saved), Toast.LENGTH_SHORT).show();


        // สร้างปุ่มลบ
        Button deleteButton = new Button(getActivity());
        deleteButton.setText(getString(R.string.delete));

        transactionContainer.addView(deleteButton);

        // กำหนดการทำงานของปุ่มลบ
        deleteButton.setOnClickListener(v -> {

            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.confirm_delete)) // ใช้ getString() กับข้อความใน strings.xml
                    .setMessage(getString(R.string.are_you_sure_delete))
                    .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                        boolean isDeleted = databaseHelper.deleteTransaction(formattedDateTime, billType, category, amountDouble);
                        if (isDeleted) {
                            historyList.removeView(transactionContainer);
                            Toast.makeText(getActivity(), getString(R.string.transaction_deleted), Toast.LENGTH_SHORT).show();

                            // หากไม่มีรายการเหลือ ให้แสดงข้อความ "No Transactions"
                            if (historyList.getChildCount() == 0) {
                                noTransactionsText.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.unable_to_delete_transaction), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss())
                    .show();
        });


        // สร้างปุ่ม Edit
        Button editButton = new Button(getActivity());
        editButton.setText(getString(R.string.edit));
        transactionContainer.addView(editButton);

        // กำหนดการทำงานของปุ่ม Edit
        editButton.setOnClickListener(v -> {
            // เปิด Dialog เพื่อแก้ไขข้อมูลการทำรายการ
            openEditTransactionDialog(formattedDateTime, billType, category, amountDouble);
        });
    }


    private GradientDrawable createBorderDrawable(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(Color.WHITE);
        drawable.setStroke(4, color); // Set border width and color
        return drawable;
    }

    @SuppressLint("DefaultLocale")
    private void addTransactionToLayout(Transaction transaction) {
        LinearLayout transactionContainer = new LinearLayout(getActivity());
        transactionContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        transactionContainer.setOrientation(LinearLayout.VERTICAL);
        transactionContainer.setPadding(16, 16, 16, 16);

        // ตรวจสอบภาษาของเครื่อง
        String language = Locale.getDefault().getLanguage();

        // ตั้งค่า Tag เพื่อใช้ในการลบในอนาคต
        transactionContainer.setTag("transaction");

        // กำหนดสีขอบตามประเภท
        int borderColor;
        if ("Income".equalsIgnoreCase(transaction.getType()) || "รายรับ".equalsIgnoreCase(transaction.getType())) {
            borderColor = 0xFF81C784; // สีเขียวสำหรับ income
        } else if ("Expenses".equalsIgnoreCase(transaction.getType()) || "รายจ่าย".equalsIgnoreCase(transaction.getType())) {
            borderColor = 0xFFFFB74D; // สีเหลืองสำหรับ outcome
        } else {
            borderColor = 0; // ไม่มีสีสำหรับประเภทอื่น
        }


        // กำหนดสีขอบถ้ามีการกำหนดสี
        if (borderColor != 0) {
            transactionContainer.setBackground(createBorderDrawable(borderColor));
        }

        // แปลงข้อความประเภทและหมวดหมู่ตามภาษาเครื่อง
        String type = transaction.getType();
        String category = transaction.getCategory();

        if ("th".equals(language)) {
            // ถ้าเป็นภาษาไทย แปลงข้อมูลเป็นภาษาไทย
            type = convertToThai(type);
            category = convertToThai(category);
        } else if ("en".equals(language)) {
            // ถ้าเป็นภาษาอังกฤษ แปลงข้อมูลเป็นภาษาอังกฤษ (ถ้าจำเป็น)
            type = convertToEnglish(type);
            category = convertToEnglish(category);
        }

        // สร้าง TextView เพื่อแสดงรายละเอียดรายการ
        TextView transactionView = new TextView(getActivity());
        transactionView.setText(String.format("%s - %s: %s - %d %s",
                transaction.getDate(),
                type,
                category,
                (int) transaction.getAmount(),
                getString(R.string.currency_unit)
        ));

        transactionContainer.addView(transactionView);

        // สร้างปุ่มลบ
        Button deleteButton = new Button(getActivity());
        deleteButton.setText(R.string.delete);
        transactionContainer.addView(deleteButton);

        // กำหนดการทำงานของปุ่มลบ
        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.confirm_delete))
                    .setMessage(getString(R.string.are_you_sure_delete))
                    .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                        boolean isDeleted = databaseHelper.deleteTransaction(
                                transaction.getDate(),
                                transaction.getType(),
                                transaction.getCategory(),
                                transaction.getAmount()
                        );

                        if (isDeleted) {
                            historyList.removeView(transactionContainer);
                            Toast.makeText(getActivity(), getString(R.string.transaction_deleted), Toast.LENGTH_SHORT).show();

                            // หากไม่มีรายการเหลือ ให้แสดงข้อความ "No Transactions"
                            if (historyList.getChildCount() == 0) {
                                noTransactionsText.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.unable_to_delete_transaction), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // สร้างปุ่ม Edit
        Button editButton = new Button(getActivity());
        editButton.setText(getString(R.string.edit));
        transactionContainer.addView(editButton);

        // กำหนดการทำงานของปุ่ม Edit
        editButton.setOnClickListener(v -> {
            // เปิด Dialog เพื่อแก้ไขข้อมูลการทำรายการ
            openEditTransactionDialog(transaction.getDate(), transaction.getType(), transaction.getCategory(), transaction.getAmount());
        });

        // เพิ่มรายการไปยัง history list
        historyList.addView(transactionContainer);
    }

    private void openEditTransactionDialog(String date, String type, String category, double amount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.edit_transaction));  // ใช้ getString() แทนข้อความที่ฮาร์ดโค้ด

        // Create Layout for Dialog
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        // Income Checkbox
        CheckBox incomeCheckbox = new CheckBox(getActivity());
        incomeCheckbox.setText(getString(R.string.income));  // ใช้ getString() แทนข้อความที่ฮาร์ดโค้ด
        if ("Income".equalsIgnoreCase(type)) {
            incomeCheckbox.setChecked(true);
        }
        layout.addView(incomeCheckbox);

        // Outcome Checkbox
        CheckBox outcomeCheckbox = new CheckBox(getActivity());
        outcomeCheckbox.setText(getString(R.string.outcome));  // ใช้ getString() แทนข้อความที่ฮาร์ดโค้ด
        if ("Expenses".equalsIgnoreCase(type)) {
            outcomeCheckbox.setChecked(true);
        }
        layout.addView(outcomeCheckbox);

        // Spinner for categories
        final Spinner categorySpinner = new Spinner(getActivity());
        layout.addView(categorySpinner);

        // Update the category spinner based on the selected checkboxes
        if (incomeCheckbox.isChecked()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.income_categories));
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(adapter);
            categorySpinner.setSelection(getCategoryPosition(getResources().getStringArray(R.array.income_categories), category)); // Set selected category
        } else if (outcomeCheckbox.isChecked()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.outcome_categories));
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(adapter);
            categorySpinner.setSelection(getCategoryPosition(getResources().getStringArray(R.array.outcome_categories), category)); // Set selected category
        }

        incomeCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                outcomeCheckbox.setChecked(false); // Uncheck outcome if income is selected
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.income_categories));
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categorySpinner.setAdapter(adapter);
            }
        });

        outcomeCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                incomeCheckbox.setChecked(false); // Uncheck income if outcome is selected
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.outcome_categories));
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categorySpinner.setAdapter(adapter);
            }
        });

        // Add EditText for amount input
        final EditText amountInput = new EditText(getActivity());
        amountInput.setHint(getString(R.string.enter_amount));  // ใช้ getString() แทนข้อความที่ฮาร์ดโค้ด
        amountInput.setText(String.valueOf(amount)); // Set the current amount
        layout.addView(amountInput);

        builder.setView(layout);

        // Dialog buttons
        builder.setPositiveButton(getString(R.string.save), (dialog, which) -> {  // ใช้ getString() แทนข้อความที่ฮาร์ดโค้ด
            String billType = "";
            if (incomeCheckbox.isChecked()) {
                billType = "Income";
            } else if (outcomeCheckbox.isChecked()) {
                billType = "Expenses";
            }

            String amountStr = amountInput.getText().toString();
            String categoryEdit = categorySpinner.getSelectedItem() != null ? categorySpinner.getSelectedItem().toString() : "";

            if (!amountStr.isEmpty() && !billType.isEmpty()) {
                double newAmount = Double.parseDouble(amountStr);
                updateTransactionInDatabase(date, billType, categoryEdit, newAmount); // Update transaction
            } else {
                Toast.makeText(getActivity(), getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();  // ใช้ getString() แทนข้อความที่ฮาร์ดโค้ด
            }
        });

        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());  // ใช้ getString() แทนข้อความที่ฮาร์ดโค้ด

        builder.show();
    }



    // Helper function to get the position of a category in the array
    private int getCategoryPosition(String[] categories, String category) {
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(category)) {
                return i;
            }
        }
        return 0; // Default to the first category if not found
    }

    // Function to update the transaction in the database
    private void updateTransactionInDatabase(String date, String billType, String category, double amount) {
        boolean isUpdated = databaseHelper.updateTransaction(date, billType, category, amount);
        if (isUpdated) {
            Toast.makeText(getActivity(), getString(R.string.transaction_updated), Toast.LENGTH_SHORT).show();  // ใช้ getString() แทนข้อความที่ฮาร์ดโค้ด
            refreshTransactionList(); // Refresh list after updating
        } else {
            Toast.makeText(getActivity(), getString(R.string.failed_to_update_transaction), Toast.LENGTH_SHORT).show();  // ใช้ getString() แทนข้อความที่ฮาร์ดโค้ด
        }
    }


    private void refreshTransactionList() {
        // ลบเฉพาะ View ที่มีแท็ก "transaction" ใน historyList
        for (int i = historyList.getChildCount() - 1; i >= 0; i--) {
            View child = historyList.getChildAt(i);
            if ("transaction".equals(child.getTag())) {
                historyList.removeView(child);
            }
        }
        List<Transaction> allTransactions = databaseHelper.getTransactionsByDate(selectedDate); // สมมติว่าเรามีฟังก์ชันนี้ใน DatabaseHelper ที่ดึงข้อมูลทั้งหมด
        for (Transaction transaction : allTransactions) {
            addTransactionToLayout(transaction); // เพิ่มรายการที่อัปเดตกลับไปยัง UI
        }
    }

    private void filterBills(String query) {
        // ตรวจสอบว่ามีรายการใน historyList หรือไม่
        if (historyList.getChildCount() > 0) {
            for (int i = 0; i < historyList.getChildCount(); i++) {
                View child = historyList.getChildAt(i);

                // ตรวจสอบว่า View มีแท็ก "transaction" หรือไม่
                if ("transaction".equals(child.getTag())) {
                    // ค้นหา TextView ภายในแต่ละ transactionContainer
                    TextView transactionView = (TextView) ((LinearLayout) child).getChildAt(0);
                    String transactionText = transactionView.getText().toString().toLowerCase();

                    // แสดงหรือซ่อน View ตามคำที่ค้นหา
                    if (transactionText.contains(query.toLowerCase())) {
                        child.setVisibility(View.VISIBLE);
                    } else {
                        child.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    private String convertToThai(String text) {
        if ("Expenses".equalsIgnoreCase(text)) {
            return "รายจ่าย";
        } else if ("Income".equalsIgnoreCase(text)) {
            return "รายรับ";
        } else if ("Food & Beverages".equalsIgnoreCase(text)) {
            return "อาหารและเครื่องดื่ม";
        } else if ("Bills & Utilities".equalsIgnoreCase(text)) {
            return "ค่าสาธารณูปโภค";
        } else if ("Shopping".equalsIgnoreCase(text)) {
            return "ช้อปปิ้ง";
        } else if ("Household Items".equalsIgnoreCase(text)) {
            return "ของใช้ในบ้าน";
        } else if ("Family".equalsIgnoreCase(text)) {
            return "ครอบครัว";
        } else if ("Travel".equalsIgnoreCase(text)) {
            return "การเดินทาง";
        } else if ("Health & Fitness".equalsIgnoreCase(text)) {
            return "สุขภาพและฟิตเนส";
        } else if ("Education".equalsIgnoreCase(text)) {
            return "การศึกษา";
        } else if ("Entertainment".equalsIgnoreCase(text)) {
            return "ความบันเทิง";
        } else if ("Giving & Donations".equalsIgnoreCase(text)) {
            return "บริจาคและการให้";
        } else if ("Insurance".equalsIgnoreCase(text)) {
            return "ประกัน";
        } else if ("Other Expenses".equalsIgnoreCase(text)) {
            return "ค่าใช้จ่ายอื่น ๆ";
        } else if ("Salary".equalsIgnoreCase(text)) {
            return "เงินเดือน";
        } else if ("Other Income".equalsIgnoreCase(text)) {
            return "รายได้อื่น ๆ";
        } else if ("Transfer In".equalsIgnoreCase(text)) {
            return "โอนเข้า";
        } else if ("Interest".equalsIgnoreCase(text)) {
            return "ดอกเบี้ย";
        }
        return text; // ถ้าไม่ตรงกับประเภทใดๆ ก็คืนค่ากลับมาเป็นข้อความเดิม
    }

    private String convertToEnglish(String text) {
        if ("รายจ่าย".equalsIgnoreCase(text)) {
            return "Expenses";
        } else if ("รายรับ".equalsIgnoreCase(text)) {
            return "Income";
        } else if ("อาหารและเครื่องดื่ม".equalsIgnoreCase(text)) {
            return "Food & Beverages";
        } else if ("ค่าสาธารณูปโภค".equalsIgnoreCase(text)) {
            return "Bills & Utilities";
        } else if ("ช้อปปิ้ง".equalsIgnoreCase(text)) {
            return "Shopping";
        } else if ("ของใช้ในบ้าน".equalsIgnoreCase(text)) {
            return "Household Items";
        } else if ("ครอบครัว".equalsIgnoreCase(text)) {
            return "Family";
        } else if ("การเดินทาง".equalsIgnoreCase(text)) {
            return "Travel";
        } else if ("สุขภาพและฟิตเนส".equalsIgnoreCase(text)) {
            return "Health & Fitness";
        } else if ("การศึกษา".equalsIgnoreCase(text)) {
            return "Education";
        } else if ("ความบันเทิง".equalsIgnoreCase(text)) {
            return "Entertainment";
        } else if ("บริจาคและการให้".equalsIgnoreCase(text)) {
            return "Giving & Donations";
        } else if ("ประกัน".equalsIgnoreCase(text)) {
            return "Insurance";
        } else if ("ค่าใช้จ่ายอื่น ๆ".equalsIgnoreCase(text)) {
            return "Other Expenses";
        } else if ("เงินเดือน".equalsIgnoreCase(text)) {
            return "Salary";
        } else if ("รายได้อื่น ๆ".equalsIgnoreCase(text)) {
            return "Other Income";
        } else if ("โอนเข้า".equalsIgnoreCase(text)) {
            return "Transfer In";
        } else if ("ดอกเบี้ย".equalsIgnoreCase(text)) {
            return "Interest";
        }
        return text; // ถ้าไม่ตรงกับประเภทใดๆ ก็คืนค่ากลับมาเป็นข้อความเดิม
    }

}
