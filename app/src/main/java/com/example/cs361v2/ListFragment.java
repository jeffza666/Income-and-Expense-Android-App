package com.example.cs361v2;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.example.cs361v2.DatabaseHelper;
import com.example.cs361v2.R;
import com.example.cs361v2.Transaction;
import com.example.cs361v2.TransactionAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListFragment extends Fragment {

    private RadioGroup radioGroup;
    private RadioButton radioMonth, radioYear;
    private Spinner spinnerMonth, spinnerYear;
    private Button btnShowData;
    private TextView txtTotal;
    private ListView listViewTransactions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        radioGroup = view.findViewById(R.id.radioGroup);
        radioMonth = view.findViewById(R.id.radioMonth);
        radioYear = view.findViewById(R.id.radioYear);
        spinnerMonth = view.findViewById(R.id.spinnerMonth);
        spinnerYear = view.findViewById(R.id.spinnerYear);
        btnShowData = view.findViewById(R.id.btnShowData);
        txtTotal = view.findViewById(R.id.txtTotal);
        listViewTransactions = view.findViewById(R.id.listViewTransactions);

        // ตั้งค่า Spinner เดือน
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.months_array,
                android.R.layout.simple_spinner_item
        );
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        // ตั้งค่า Spinner ปี
        ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.years_array,
                android.R.layout.simple_spinner_item
        );
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        // Set listeners to show/hide the Spinners based on the selected RadioButton
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioMonth) {
                spinnerMonth.setVisibility(View.VISIBLE);
                spinnerYear.setVisibility(View.VISIBLE); // แสดงทั้งเดือนและปี

                // ใช้ String Resources
                String totalIncomeMonth = getString(R.string.total_income_month, 0.00);
                String totalExpenseMonth = getString(R.string.total_expense_month, 0.00);
                txtTotal.setText(totalIncomeMonth + "\n" + totalExpenseMonth);

                listViewTransactions.setAdapter(null);
            } else if (checkedId == R.id.radioYear) {
                spinnerMonth.setVisibility(View.GONE);
                spinnerYear.setVisibility(View.VISIBLE);

                // ใช้ String Resources
                String totalIncomeYear = getString(R.string.total_income_year, 0.00);
                String totalExpenseYear = getString(R.string.total_expense_year, 0.00);
                txtTotal.setText(totalIncomeYear + "\n" + totalExpenseYear);

                listViewTransactions.setAdapter(null);
            }
        });


        // Handle the "Show Data" button click event
        btnShowData.setOnClickListener(v -> {
            String selectedDate = "";

            // ตรวจสอบว่าเลือกเดือนหรือปี
            if (radioMonth.isChecked()) {
                // เมื่อเลือกเดือน, ให้เลือกปีด้วย
                int month = spinnerMonth.getSelectedItemPosition() + 1; // เดือนต้องเพิ่ม 1
                int year = Integer.parseInt(spinnerYear.getSelectedItem().toString()); // รับปีจาก Spinner
                selectedDate = String.format("%04d-%02d", year, month); // รูปแบบ: YYYY-MM
            } else if (radioYear.isChecked()) {
                // เมื่อเลือกปี, ให้ใช้ปีที่เลือก
                selectedDate = spinnerYear.getSelectedItem().toString(); // รูปแบบ: YYYY
            }

            // ทำการค้นหาข้อมูลจากฐานข้อมูลตามวันที่ที่เลือก
            queryData(selectedDate);
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


        return view;
    }

    private void queryData(String date) {
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        List<Transaction> transactions;
        boolean isYearly = false;

        // ตรวจสอบว่า date เป็นปีหรือเดือน
        if (date.length() == 4) {
            // หาก date เป็นปี (เช่น "2024")
            transactions = dbHelper.getTransactionsByYear(date);
            isYearly = true;
        } else {
            // หาก date เป็นเดือน (เช่น "2024-11")
            transactions = dbHelper.getTransactionsByMonth(date);
        }

        dbHelper.logAllTransactions();

        if (transactions != null && !transactions.isEmpty()) {
            if (isYearly) {
                // กรณีผู้ใช้เลือกดูข้อมูลแบบปี
                Map<String, double[]> monthlyTotals = new HashMap<>();
                double totalIncome = 0.0;
                double totalExpense = 0.0;

                for (Transaction transaction : transactions) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        Date transactionDate = sdf.parse(transaction.getDate());
                        String monthKey = new SimpleDateFormat("MM-yyyy").format(transactionDate); // ใช้เดือน-ปี

                        double amount = transaction.getAmount();
                        if (transaction.getType().equalsIgnoreCase("Income") || transaction.getType().equalsIgnoreCase("รายรับ")) {
                            totalIncome += amount; // รายรับ
                        }
                        else if (transaction.getType().equalsIgnoreCase("Expenses") || transaction.getType().equalsIgnoreCase("รายจ่าย")) {
                            totalExpense += amount;
                        }

                        double[] amounts = monthlyTotals.getOrDefault(monthKey, new double[]{0.0, 0.0});
                        if (transaction.getType().equalsIgnoreCase("Income") || transaction.getType().equalsIgnoreCase("รายรับ")) {
                            amounts[0] += amount; // รายรับ
                        }
                        else if (transaction.getType().equalsIgnoreCase("Expenses") || transaction.getType().equalsIgnoreCase("รายจ่าย")) {
                            amounts[1] += amount; // รายจ่าย
                        }
                        monthlyTotals.put(monthKey, amounts);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                txtTotal.setText(
                        getString(R.string.total_income_year, totalIncome) + "\n" +
                                getString(R.string.total_expense_year, totalExpense)
                );

                // สร้างรายการที่จะให้แสดงใน ListView (แสดงตามเดือน)
                List<String> displayList = new ArrayList<>();
                for (Map.Entry<String, double[]> entry : monthlyTotals.entrySet()) {
                    String monthStr = entry.getKey(); // เดือน-ปี (รูปแบบ: 11-2024)
                    double[] amounts = entry.getValue();

                    String incomeText = getString(R.string.income_label, amounts[0]);
                    String expenseText = getString(R.string.expense_label, amounts[1]);

                    displayList.add(monthStr + "\n" + incomeText + "\n" + expenseText);
                }


                // หากไม่มีข้อมูลแสดงข้อความแจ้งเตือนที่ TextView
                if (displayList.isEmpty()) {
                    txtTotal.setText(getString(R.string.no_data_found));
                } else {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, displayList);
                    listViewTransactions.setAdapter(adapter);
                }

            } else {
                // กรณีผู้ใช้เลือกดูข้อมูลแบบเดือน
                Map<String, double[]> dailyTotals = new HashMap<>();
                double totalIncome = 0.0;
                double totalExpense = 0.0;

                for (Transaction transaction : transactions) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        Date transactionDate = sdf.parse(transaction.getDate());
                        String dayKey = new SimpleDateFormat("dd-MM-yyyy").format(transactionDate); // ใช้วัน-เดือน-ปี

                        double amount = transaction.getAmount();
                        if (transaction.getType().equalsIgnoreCase("Income") || transaction.getType().equalsIgnoreCase("รายรับ")) {
                            totalIncome += amount; // รายรับ
                        }
                        else if (transaction.getType().equalsIgnoreCase("Expenses") || transaction.getType().equalsIgnoreCase("รายจ่าย")) {
                            totalExpense += amount; // รายจ่าย
                        }


                        double[] amounts = dailyTotals.getOrDefault(dayKey, new double[]{0.0, 0.0});
                        if (transaction.getType().equalsIgnoreCase("Income") || transaction.getType().equalsIgnoreCase("รายรับ")) {
                            amounts[0] += amount; // รายรับ
                        }
                        else if (transaction.getType().equalsIgnoreCase("Expenses") || transaction.getType().equalsIgnoreCase("รายจ่าย")) {
                            amounts[1] += amount; // รายจ่าย
                        }

                        dailyTotals.put(dayKey, amounts);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                // แสดงผลรวมรายรับรายจ่ายทั้งเดือน
                txtTotal.setText(
                        getString(R.string.total_income_month, totalIncome) + "\n" +
                                getString(R.string.total_expense_month, totalExpense)
                );


                // สร้างรายการที่จะให้แสดงใน ListView (แสดงตามวัน)
                List<String> displayList = new ArrayList<>();
                for (Map.Entry<String, double[]> entry : dailyTotals.entrySet()) {
                    String dateStr = entry.getKey();  // วันที่ (รูปแบบ: 08-11-2024)
                    double[] amounts = entry.getValue();

                    String incomeText = getString(R.string.income_label, amounts[0]);
                    String expenseText = getString(R.string.expense_label, amounts[1]);

                    displayList.add(dateStr + "\n" + incomeText + "\n" + expenseText);
                }


                // หากไม่มีข้อมูลแสดงข้อความแจ้งเตือนที่ TextView
                if (displayList.isEmpty()) {
                    txtTotal.setText(getString(R.string.no_data_found));
                } else {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, displayList);
                    listViewTransactions.setAdapter(adapter);
                }
            }
        } else {
            // หากไม่มีข้อมูลเลยให้แสดงข้อความใน TextView
            txtTotal.setText(getString(R.string.no_data_available));
            listViewTransactions.setAdapter(null);
        }
    }

}
