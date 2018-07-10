package com.example.pjh.calc;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class CalcActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvResult;  // 계산 결과 textview
    private String result;

    private ArrayList<Double> listNum;  // 피연산자 list
    private ArrayList<String> listOperator; // 연산자 list

    private boolean isFinishOperation;  // '='(equal) 키를 눌러 연산 직후인지를 알려주는 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc);

        optimizeButtonSize();   // 버튼 사이즈 최적화

        result = "0";

        listNum = new ArrayList<>();
        listOperator = new ArrayList<>();

        isFinishOperation = false;

        tvResult = findViewById(R.id.tv_result);
        tvResult.setText(result);
        // 오른쪽 or 왼쪽으로 swiping 했을 경우 backspace(한 칸 지우기) 작동
        tvResult.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeRight() {
                backspace();
            }

            @Override
            public void onSwipeLeft() {
                backspace();
            }
        });
    }

    @Override
    public void onClick(View view) {
        TextView btn = (TextView) view;
        String btnName = btn.getText().toString();

        switch (btnName) {

            case "AC" :
                initialize();   // 초기화
                break;

            case "±":
                // 계산이 끝난 경우 초기화
                if (isFinishOperation) {
                    initialize();
                    isFinishOperation = false;
                    break;
                }

                // result 에 올라와 있는 숫자 × (-1)
                try {
                    result = String.valueOf(Integer.parseInt(result) * (-1));
                } catch (NumberFormatException e) {
                    result = String.valueOf(Double.parseDouble(result) * (-1));
                }
                tvResult.setText(result);
                break;

            case "%":
                double value = Double.parseDouble(calculate());
                double percent = Double.parseDouble(result);

                result = String.valueOf(percent / 100 * value);

                // 결과가 무조건 double 형 이므로 소수부가 0일 경우, 소수부를 제거
                result = isFractionalPartZero(result)
                        ? String.valueOf((int) Double.parseDouble(result))
                        : result;
                tvResult.setText(result);
                break;

            case "÷":
                addOperator(btnName);
                break;

            case "×":
                addOperator(btnName);
                break;

            case "－":
                addOperator(btnName);
                break;

            case "+":
                addOperator(btnName);
                break;

            case "=":
                if (isFinishOperation)
                    break;

                listNum.add(Double.parseDouble(result));

                result = calculate();

                // 결과가 무조건 double 형 이므로 소수부가 0일 경우, 소수부를 제거
                result = isFractionalPartZero(result)
                        ? String.valueOf((int) Double.parseDouble(result))
                        : result;
                tvResult.setText(result);

                isFinishOperation = true;
                break;

            case ".":
                if (isFinishOperation) {    // 연산이 끝난 직후인 경우는 초기화 후 진행
                    initialize();
                }

                if (isInteger(result)) {    // result 가 정수일 경우만 소수점을 붙임
                    result = result + btnName;
                    tvResult.setText(result);
                }
                break;

            // 숫자 키(0~9)
            default:
                if (isFinishOperation) {    // 연산이 끝난 직후인 경우는 초기화 후 진행
                    initialize();
                }

                if (result.length() >= 10) {    // 입력 가능한 자리수 제한
                    Toast.makeText(this, getString(R.string.toast_character_limit), Toast.LENGTH_SHORT).show();
                    break;
                }
                result = (result.equals("0"))
                        ? btnName               // result 가 0인 경우
                        : result + btnName;     // result 가 0이 아닌 경우

                tvResult.setText(result);
                break;
        }
    }

    /**
     * initialize
     */
    private void initialize() {
        result = "0";
        listNum.clear();
        listOperator.clear();
        isFinishOperation = false;
        tvResult.setText(result);
    }

    /**
     * 연산자 버튼을 눌렀을 때, 연산자 리스트에 추가
     */
    private void addOperator(String operator) {
        if (isFinishOperation) {    // '=' 버튼으로 계산이 끝난 뒤, 다시 연산자 버튼을 눌렀을 때
            isFinishOperation = false;
            listOperator.add(operator);
            result = "0";
            return;
        }

        if (listOperator.size() <= listNum.size()) {    // 일반적인 경우
            listNum.add(Double.valueOf(result));
            listOperator.add(operator);

            if (listOperator.size() >= 1) { // 1 + 2 + 3 + 4 + .... 이런식으로 '=' 키를 누르지 않고 여러 번 연산을 반복하는 경우.
                result = calculate();
                tvResult.setText(isFractionalPartZero(result)
                        ? String.valueOf((int) Double.parseDouble(result))
                        : result);
            }
            result = "0";
        }
    }


    /**
     * list 에 있는 모든 피연산자들을 모두 연산하는 함수
     * @return  결과값(string)
     */
    private String calculate() {
        double result = listNum.get(0);

        for (int i = 0; i < listNum.size() - 1; i++) {
            double tmp = listNum.get(i+1);

            if (listOperator.get(i).equals("÷")) {
                result = result / tmp;
            } else if (listOperator.get(i).equals("×")) {
                result = result * tmp;
            } else if (listOperator.get(i).equals("－")) {
                result = result - tmp;
            } else if (listOperator.get(i).equals("+")) {
                result = result + tmp;
            }
        }
        return String.valueOf(result);
    }

    /**
     * 문자열 s가 정수인지, 정수가 아닌 실수인지 확인
     */
    private boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 소수부가 0인지 확인
     */
    private boolean isFractionalPartZero(String s) {
        int index = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '.') {
                index = i;
            }
        }

        if (index == 0)
            return true;

        String decimalFraction = s.substring(index + 1);
        Double d = Double.parseDouble(decimalFraction);

        return (d == 0);
    }

    /**
     * 버튼 사이즈 optimize
     */
    private void optimizeButtonSize() {
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        int displayWidth = dm.widthPixels;

        findViewById(R.id.ll1).getLayoutParams().height = displayWidth / 4;
        findViewById(R.id.ll2).getLayoutParams().height = displayWidth / 4;
        findViewById(R.id.ll3).getLayoutParams().height = displayWidth / 4;
        findViewById(R.id.ll4).getLayoutParams().height = displayWidth / 4;
        findViewById(R.id.ll5).getLayoutParams().height = displayWidth / 4;
    }

    /**
     * 숫자 한 칸 지우기
     */
    private void backspace() {
        if (result.equals("0")) {
            return;
        } else if (result.length() == 1) {
            result = "0";
        } else {
            result = result.substring(0, result.length() - 1);
        }
        tvResult.setText(result);
    }

    private void debug() {
        String log = "listNum: ";
        for (int i = 0; i < listNum.size(); i++) {
            log += listNum.get(i) + ", ";
        }

        log += "\noperator: ";
        for (int i = 0; i < listOperator.size(); i++) {
            log += listOperator.get(i) + ", ";
        }
        Log.e("test", log);
    }
}
