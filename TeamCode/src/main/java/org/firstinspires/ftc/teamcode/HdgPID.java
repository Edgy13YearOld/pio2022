package org.firstinspires.ftc.teamcode;

//This PID loop is only to be used with RADIANS
//This PID loop accounts for the fact that moving past π will go to -π
//This PID loop may not work as intended if refresh rate is very low (More than π radians/tick)
public class HdgPID extends PIDLoop{
    private int state = 0;
    private double prevD = 1;

    public HdgPID(){
        this(0, 0, 0, 0, -1, 1);
    }
    public HdgPID(double kp, double ki, double kd){
        this(kp, ki, kd, 0, -1, 1);
    }
    public HdgPID(double kp, double ki, double kd, double goal){
        this(kp, ki, kd, goal, -1, 1);
    }
    public HdgPID(double kp, double ki, double kd, double goal, double outMin, double outMax){

        super(kp, ki, kd, goal, outMin, outMax);
    }

    public int getState(){
        return state;
    }

    public double update(double input, double time){
        pTerm = calculateP(input);
        iTerm = calculateI(input, time);
        dTerm = calculateD(input, time);
        return output();
    }

    public double error(double input){
        if(Math.abs(goal - input) < Math.min(Math.abs(goal + 2 * Math.PI - input), Math.abs(goal - 2 * Math.PI - input))){
            return goal - input;
        }else if(Math.abs(goal + 2 * Math.PI -input) < Math.abs(goal - 2 * Math.PI - input)){
            return goal + 2 * Math.PI - input;
        }else{
            return goal - 2 * Math.PI - input;
        }
    }

    //Method to move the goal by a certain amount
    //Uses include controlling heading goal with a joystick on a gamepad
    //Sluggish and inconsistent
    public void moveGoal(double goalChange, double time, double input){
        goal = Math.max(input-Math.PI,Math.min(input+Math.PI,goal + goalChange * (time - gPreTime)));
        if(goal < -Math.PI){
            goal += 2 * Math.PI;
        }else if(goal > Math.PI){
            goal -= 2 * Math.PI;
        }
        gPreTime = time;
        this.reset();
    }

    public double manage(double sensorInput, double userInput, double time){
        if(Math.abs(userInput) >= 0.1) state = 2;
        switch(state) {
            case 0:
                return this.update(sensorInput, time);
            case 1:
                dTerm = this.calculateD(sensorInput, time);
                if(dTerm/prevD < 0){
                    this.goal = sensorInput;
                    state--;
                    return 0;
                }
                prevD = dTerm;
                return dTerm * kd;
            case 2:
                this.goal = sensorInput;
                state--;
                dTerm = this.calculateD(sensorInput, time);
                prevD = dTerm;
                return -userInput;
            default:
                throw(new IllegalStateException("Impossible heading management state"));
        }

    }
}
