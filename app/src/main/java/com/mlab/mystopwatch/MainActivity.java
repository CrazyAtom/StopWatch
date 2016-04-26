package com.mlab.mystopwatch;

import java.util.ArrayList;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    TimeThread thread;  // 타임 스레드
    int lapCount;       // 랩 카운트
    int count;          // 핸들러가 호출한 횟수 카운트
    int sec, min, hour; // 초, 분, 시간 표시 위해서

    enum STOPWATCH_STATE { READY, RUNNING, PAUSE, STOP, RESET };
    STOPWATCH_STATE state = STOPWATCH_STATE.READY;  // stopwatch 상태
    ListView listView;  // 랩타임 출력
    ArrayAdapter<String> adapter;   // ListView에 출력할 adapter 객체
    ArrayList<String> lapList;      // LapTime을 기록할 배열

    Button pauseBtn, stopBtn;   // 버튼

    int pos_h2, pos_h1, pos_min2, pos_min1, pos_sec2, pos_sec1, pos_m2, pos_m1;   // 각각의 자리수를 나타내는 변수
    int[] img = new int[10];    // 숫자 이미지 리소스 id 값을 담을 정수형 배열객체 생성
    ImageView imgH2, imgH1, imgMi2, imgMi1, imgS2, imgS1, imgM2, imgM1; // 숫자의 imageView 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView)findViewById(R.id.listView);
        pauseBtn = (Button)findViewById(R.id.pause);
        stopBtn = (Button)findViewById(R.id.stop);

        // ListView에 LapTime 출력하기 위해
        lapList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lapList);

        // ListView 객체에 adapter 객체 연결하기
        listView.setAdapter(adapter);

        // 경계선 설정
        listView.setDivider(new ColorDrawable(Color.DKGRAY));   // 컬러
        listView.setDividerHeight(1);   // 굵기

        // 이미지 리소스를 배열에 저장
        for(int i = 0; i < 10; i++) {
            img[i] = R.drawable.f00 + i;
        }

        // 각 자리수를 구성하는 imageView 객체의 참조값
        imgH2 = (ImageView)findViewById(R.id.h2);
        imgH1 = (ImageView)findViewById(R.id.h1);
        imgMi2 = (ImageView)findViewById(R.id.min2);
        imgMi1 = (ImageView)findViewById(R.id.min1);
        imgS2 = (ImageView)findViewById(R.id.sec2);
        imgS1 = (ImageView)findViewById(R.id.sec1);
        imgM2 = (ImageView)findViewById(R.id.m2);
        imgM1 = (ImageView)findViewById(R.id.m1);

        // thread 생성
        thread = new TimeThread(handler);
    }

    // 버튼 콜백 메소드
    public void push(View v) {
        switch (v.getId()) {
            case R.id.start :                           // 시작 버튼 클릭시
                resetTime();                            // 시간 초기화
                try {
                    thread.start();                     // thread 시작
                } catch (Exception e) {                 // 예외 발생시
                    thread.stopForever();               // thread 정지
                    thread = new TimeThread(handler);   // thread 다시 생성
                    thread.start();                     // thread 시작
                }
                state = STOPWATCH_STATE.RUNNING;
                break;
            case R.id.pause :                           // 멈춤 버튼 클릭시
                if(state == STOPWATCH_STATE.RUNNING) {
                    thread.pauseNResume(true);          // thread 일시 정지
                    pauseBtn.setText("재시작");
                    state = STOPWATCH_STATE.PAUSE;
                } else if(state == STOPWATCH_STATE.PAUSE) {
                    thread.pauseNResume(false);         // thread 재시작
                    pauseBtn.setText("일시정지");
                    state = STOPWATCH_STATE.RUNNING;
                }
                break;
            case R.id.stop :                            // 정지 버튼 클릭시
                if(state == STOPWATCH_STATE.RUNNING) {  // 동작중일때만 정지 가능하다
                    thread.stopForever();               // thread 정지
                    stopBtn.setText("리셋");
                    state = STOPWATCH_STATE.STOP;
                } else if(state == STOPWATCH_STATE.STOP) {  // 정지 상태에서는 초기화 시켜 준다
                    resetTime();
                    stopBtn.setText("정지");
                    state = STOPWATCH_STATE.READY;
                }
                break;
            case R.id.check :                           // LapTime 체크 버튼 클릭시
                if (state == STOPWATCH_STATE.RUNNING) {
                    lapCount++;

                    String lap = lapCount + " 번 lap : " + hour + "h " + min + "m " + sec + " s " + count + "ms";
                    lapList.add(lap);
                    adapter.notifyDataSetChanged();
                }
                break;
        }
    }

    // 시간 초기화 메소드
    public void resetTime() {

        // 시간 관련 초기화
        count = 0; sec = 0; min = 0; hour = 0;
        pos_h2 = 0; pos_h1 = 0; pos_min2 = 0; pos_min1 = 0; pos_sec2 = 0; pos_sec1 = 0; pos_m2 = 0; pos_m1 = 0;

        // 각각의 자리수를 나타내는 이미지 뷰의 리소스 교체
        setTimeImage();

        // 그 외 초기화
        lapCount = 0;       // 랩카운터 초기화
        lapList.clear();    // 모델 데이터 초기화
        adapter.notifyDataSetChanged(); // 모델의 내용이 변경 되었다고 adapter에 알려 준다
    };

    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            
            count++;    // 카운트 증가 
            if(count == 100) {  // 초 증가
                sec++;
                count = 0;
            }
            if(sec == 60) {     // 분 증가
                min++;
                sec = 0;
            }
            if(min == 60) {     // 시 증가
                hour++;
                min = 0;
            }
            
            // 8자리 출력을 위해
            pos_m1 = count % 10;
            pos_m2 = count / 10;
            pos_sec1 = sec % 10;
            pos_sec2 = sec / 10;
            pos_min1 = min % 10;
            pos_min2 = min / 10;
            pos_h1 = hour % 10;
            pos_h2 = hour / 10;

            // 각각의 자리수를 나타내는 이미지 뷰의 리소스 교체
            setTimeImage();
        }
    };

    public void setTimeImage() {
        imgM1.setImageResource(img[pos_m1]);
        imgM2.setImageResource(img[pos_m2]);
        imgS1.setImageResource(img[pos_sec1]);
        imgS2.setImageResource(img[pos_sec2]);
        imgMi1.setImageResource(img[pos_min1]);
        imgMi2.setImageResource(img[pos_min2]);
        imgH1.setImageResource(img[pos_h1]);
        imgH2.setImageResource(img[pos_h2]);
    };
}

