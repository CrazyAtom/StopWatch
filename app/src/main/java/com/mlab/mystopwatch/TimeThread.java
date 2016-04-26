package com.mlab.mystopwatch;

import android.os.Handler;

/**
 * Created by 강현부 on 2016-03-24.
 */
public class TimeThread extends Thread {

    Handler handler;
    boolean isRun = true;
    boolean isWait = false;

    public TimeThread(Handler handler) {
        this.handler = handler;
    }

    // thread 일시 정지 or 재시작
    public void pauseNResume(boolean isWait) {
        synchronized (this) {
            this.isWait = isWait;
            notify();
        }
    }

    // thread 완전 정지
    public void stopForever() {
        synchronized (this) {
            isRun = false;
            notify();
        }
    }

    public void run() {
        while (isRun) {
            try {
                // 매주기 10/1000 초식 쉰다.
                Thread.sleep(10);
            } catch (Exception e) {
                //
            }
            // stop 일때 isWait을 true로 바꾼다
            if (isWait) {
                try {
                    synchronized (this) { // thread가 실행중에 값이 바뀌면 충돌이 발생 할 우려가 있으므로 동기화 블럭에서 해주는 것들이 있다.
                        wait();
                    }
                } catch (Exception e) {
                    //
                }
            }

            // 핸들러에게 메세지 전송
            handler.sendEmptyMessage(0);
        }
    }
}

