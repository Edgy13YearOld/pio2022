/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;


/**
 * This file tests the robot's sensor
 *
 * This particular OpMode shows sensor values and robot performance in tick per second (TPS)
 */

@TeleOp(name="Sensor Testing", group="Proto Comp Robot")
@Disabled
public class SensorTesting extends LinearOpMode {

    // Declare OpMode members.
    private ElapsedTime runtime = new ElapsedTime();
    private DcMotor leftBackDrive, leftFrontDrive, rightBackDrive, rightFrontDrive, intakeMotor;

    //For performance measuring
    private double prevElapsedTime = 0;

    @Override
    public void runOpMode() {
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        // Initialize the hardware variables. Note that the strings used here as parameters
        // to 'get' must correspond to the names assigned during the robot configuration
        // step (using the FTC Robot Controller app on the phone).
        leftBackDrive = hardwareMap.get(DcMotor.class, "left_back_drive");
        leftFrontDrive = hardwareMap.get(DcMotor.class, "left_front_drive");
        rightBackDrive = hardwareMap.get(DcMotor.class, "right_back_drive");
        rightFrontDrive = hardwareMap.get(DcMotor.class, "right_front_drive");
        intakeMotor = hardwareMap.get(DcMotor.class, "intake_motor");

        //Sensor Managers
        ContinuousSensor2m backDistance = new ContinuousSensor2m(hardwareMap.get(DistanceSensor.class, "back_distance"), 30);

        // Most robots need the motor on one side to be reversed to drive forward
        // Reverse the motor that runs backwards when connected directly to the battery
        leftBackDrive.setDirection(DcMotor.Direction.REVERSE);
        leftFrontDrive.setDirection(DcMotorSimple.Direction.REVERSE);
        rightBackDrive.setDirection(DcMotor.Direction.REVERSE);  //Motor wires are backwards, put direction to FORWARD when fixed
        rightFrontDrive.setDirection(DcMotorSimple.Direction.FORWARD);



        // Wait for the game to start (driver presses PLAY)
        waitForStart();
        runtime.reset();
        prevElapsedTime = 0;
        backDistance.start();

        //For noise measuring
        double max = Double.MIN_VALUE, min = Double.MAX_VALUE;

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {


            // This mode uses left stick to translate, and right stick to rotate.
            // - This uses basic math to combine motions and is easier to drive straight.
            double drive = -gamepad1.left_stick_y; //Move left stick up/down to move forward/backward
            double strafe = gamepad1.left_stick_x; //Move left stick right/left to move right/left
            double turn = -gamepad1.right_stick_x; //Move right stick right/left to turn right/left

            double leftFrontPower = CompRobot.stallPower(Range.clip(drive - turn + strafe, -1.0, 1.0), 0.1);
            double leftBackPower = CompRobot.stallPower(Range.clip(drive - turn - strafe, -1.0, 1.0),0.1);
            double rightFrontPower = CompRobot.stallPower(Range.clip(drive + turn - strafe, -1.0, 1.0),0.1);
            double rightBackPower = CompRobot.stallPower(Range.clip(drive + turn + strafe, -1.0, 1.0),0.1);

            // Send calculated power to wheels
            leftBackDrive.setPower(leftBackPower);
            leftFrontDrive.setPower(leftFrontPower);
            rightBackDrive.setPower(rightBackPower);
            rightFrontDrive.setPower(rightFrontPower);

            //Intake motor control
            if(gamepad1.left_bumper)  intakeMotor.setPower(-1);
            else if(gamepad1.right_bumper) intakeMotor.setPower(1);
            else intakeMotor.setPower(0);

            //Get Distances
            double backRange = backDistance.getDistance();
            max = Math.max(max, backRange);
            min = Math.min(min, backRange);

            // Show the elapsed game time, performance, and wheel power.
            telemetry.addData("Status", "\n\tRun Time: " + runtime.toString() + "\n\tTPS: %.2f", 1/(getRuntime()-prevElapsedTime));
            telemetry.addData("Motors", "\n\tLF(%.2f)\tRF(%.2f)\n\tLB(%.2f)\tRB(%.2f)",
                    leftFrontPower, rightFrontPower, leftBackPower, rightBackPower);
            telemetry.addData("Distances", "\n\tBack: %.2f\n\t\tMinimum: %.2f\n\t\tMaximum: %.2f", backRange, min, max);
            telemetry.update();
            prevElapsedTime = getRuntime();
        }
    }
}
