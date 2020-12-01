package cn.ikaze.healthgo.step;

import android.util.Log;

import java.math.BigDecimal;


public class StepDetector {
    private final int DELAY = 100;
    private final int MAX_DELAY = 600;
    private final float GRAVITY = 9.810001516857282f;
    private float MAX_VEL = 14f;
    private float MIN_VEL = 7f;
    private float init_vel = 1.1f;
    //Threshold
    private float VEL_THRESHOLD = init_vel;
    private float[] four_vels = {init_vel, init_vel, init_vel, init_vel};
    private int pos = 0;
    private StepListener stepListener;
    private long lastStepTime = 0;
    private float lastVel = 0;

    //Initial WAIT_MODEL, normal step count after 5 consecutive steps,
    // No step is not recorded for 5 consecutive steps
    private final long WAIT_STEPS = 4;
    private final int WAIT_MODEL = 0;
    // During ACTIVITY operation, real-time synchronization steps, without verification
    private final int ACTIVITY_MODEL = 1;
    //Run mode, has been tested by WAIT_MODEL
    private final int RUN_MODEL = 2;
    private int model;

    private boolean isActivity = false;

    private long tempSteps = 0;

    private int initCount;

    //crest
    private float crest=0;
    // trough
    private float trough=0;

    private final int up=0;
    private final int down=1;
    private final int init=2;
    private  int nowStatus=init;
    private int lastStatus=init;

    public StepDetector() {
    }

    public StepDetector(StepListener stepListener) {
        model = WAIT_MODEL;
        initCount = 0;
        this.stepListener = stepListener;
    }

    public int getModel() {
        return model;
    }

    public void setModel(int model) {
        this.model = model;
    }

    public void updateStep(float x, float y, float z) {

        long curTime = System.currentTimeMillis();
        if (lastStepTime == 0) {
            lastStepTime = curTime;
        }
        if (curTime - lastStepTime < DELAY)
            return;
        lastStepTime = curTime;

        BigDecimal b = new BigDecimal(Math.sqrt(x * x + y * y + z * z));
        float vel = b.setScale(2, BigDecimal.ROUND_DOWN).floatValue();

        Log.d("eee", "v: " + vel);

        if (lastVel == 0)
            lastVel = vel;

        if (vel < MIN_VEL || vel > MAX_VEL) {
            Log.d("step", "min max");
            initStepDetector();
            return;
        }

        VEL_THRESHOLD = getVEL_THRESHOLD();
        Log.d("eee", "threshold " + VEL_THRESHOLD);


        if(vel>lastVel)
        {
            if (lastStatus==up || crest==0)
                crest=vel;
            if (trough==0)
                trough=lastVel;
            nowStatus=up;
        }
        else if (vel<=lastVel)
        {
            if (lastStatus==down || trough==0)
                trough=vel;
            if (crest==0)
                crest=lastVel;
            nowStatus=down;
        }
        Log.d("eee", "vel: "+vel + ",," + lastVel );
        Log.d("eee","status: "+nowStatus+",,"+lastStatus);
        Log.d("eee","wave: "+crest+",,"+trough);


        if (nowStatus!=lastStatus&& (nowStatus!=init && lastStatus!=init))
        {
            Log.d("eee","cha: "+(crest-trough));
            if (crest - trough >= VEL_THRESHOLD) {
                realSteps(1);
                updateVEL_THRESHOLD(crest - trough);
            } else {
                Log.d("eee","< THRESHOLD");
                initStepDetector();
            }
            crest=trough=0;
        }

        lastVel = vel;
        lastStatus=nowStatus;

    }

    public void realSteps(long num) {
        Log.d("step", "model " + model);
        initCount = 0;
        if (model == ACTIVITY_MODEL || model == RUN_MODEL)
            stepListener.step(num);
        else {
            Log.d("step", "temp_step " + tempSteps);

            if (tempSteps >= WAIT_STEPS) {
                model = RUN_MODEL;
                stepListener.step(num + tempSteps);
                tempSteps = 0;
            } else tempSteps++;
        }

    }

    public void updateModel(boolean f) {
        if (f) {
            setModel(ACTIVITY_MODEL);
//            init_vel = 1.1f;
        } else {
//            init_vel = 1.1f;
            if (model == ACTIVITY_MODEL) {
                setModel(WAIT_MODEL);
            }
        }
    }

    public void initThreshold() {
        pos=0;
        for (int i = 0; i < 4; i++)
            four_vels[i] = init_vel;

    }

    public void initStepDetector() {

        Log.d("eee","initCount="+initCount);

        if (initCount < 2) {
            initCount++;
            return;
        }
        Log.d("eee","init step()");
        nowStatus=init;
        lastStatus=init;
        crest=0;
        trough=0;
        initThreshold();
        if (model == RUN_MODEL)
            model = WAIT_MODEL;
        tempSteps = 0;
        initCount = 0;

    }

    public void updateVEL_THRESHOLD(float vel) {

        four_vels[pos++] = vel;
        if (pos == 4)
            pos = 0;
        Log.d("eee", "dour: " + four_vels[0] + " " + four_vels[1] + " " + four_vels[2] + " " + four_vels[3]);
    }

    public float getVEL_THRESHOLD() {
        float sum = 0;
        for (int i = 0; i < 4; i++) {
            sum += four_vels[i];
        }
        sum = sum / 4;
        return sum;
    }


}
