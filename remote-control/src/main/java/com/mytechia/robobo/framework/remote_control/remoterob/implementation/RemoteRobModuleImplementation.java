/*******************************************************************************
 * Copyright 2016 Mytech Ingenieria Aplicada <http://www.mytechia.com>
 * Copyright 2016 Luis Llamas <luis.llamas@mytechia.com>
 * <p>
 * This file is part of Robobo Remote Control Module.
 * <p>
 * Robobo Remote Control Module is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Robobo Remote Control Module is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with Robobo Remote Control Module.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package com.mytechia.robobo.framework.remote_control.remoterob.implementation;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

import com.mytechia.commons.framework.exception.InternalErrorException;

import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.remote_control.remotemodule.Command;
import com.mytechia.robobo.framework.remote_control.remotemodule.ICommandExecutor;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;
import com.mytechia.robobo.framework.remote_control.remotemodule.Status;
import com.mytechia.robobo.framework.remote_control.remotemodule.websocket.Connection;
import com.mytechia.robobo.framework.remote_control.remoterob.IRemoteRobModule;
import com.mytechia.robobo.rob.BatteryStatus;
import com.mytechia.robobo.rob.FallStatus;
import com.mytechia.robobo.rob.GapStatus;
import com.mytechia.robobo.rob.IRSensorStatus;
import com.mytechia.robobo.rob.IRob;

import com.mytechia.robobo.rob.IRobInterfaceModule;
import com.mytechia.robobo.rob.IRobStatusListener;
import com.mytechia.robobo.rob.MotorStatus;
import com.mytechia.robobo.rob.MoveMTMode;
import com.mytechia.robobo.rob.WallConnectionStatus;
import com.mytechia.robobo.rob.movement.IRobMovementModule;
import com.mytechia.robobo.util.Color;

import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

public class RemoteRobModuleImplementation implements IRemoteRobModule {

    private IRemoteControlModule rcmodule;
    private IRob irob;
    private IRobMovementModule movementModule;
    private String TAG = "RemoteRob";
    private Context context;

    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {
        context = manager.getApplicationContext();
        rcmodule = manager.getModuleInstance(IRemoteControlModule.class);
        movementModule = manager.getModuleInstance(IRobMovementModule.class);
        irob = manager.getModuleInstance(IRobInterfaceModule.class).getRobInterface();

        irob.setOperationMode((byte)1);
        irob.setRobStatusPeriod(100);
        irob.addRobStatusListener(new IRobStatusListener() {
            @Override
            public void statusMotorsMT(MotorStatus left, MotorStatus right) {
                //Log.d(TAG,"Left: "+left.getVariationAngle()+" Vel: "+ left.getAngularVelocity()+" Vol: "+ left.getVoltage()
                //        +" Right: "+ right.getVariationAngle()+" Vel: "+ right.getAngularVelocity()+" Vol: "+ right.getVoltage());

            }

            @Override
            public void statusMotorPan(MotorStatus status) {

            }

            @Override
            public void statusMotorTilt(MotorStatus status) {

            }

            @Override
            public void statusGaps(Collection<GapStatus> gaps) {
                Status s  = new Status("GAPSTATUS");
                for (GapStatus status:gaps){
                    s.putContents(status.getId().toString(),String.valueOf(status.isGap()));
                }
                rcmodule.postStatus(s);
            }

            @Override
            public void statusFalls(Collection<FallStatus> fall) {
                Status s  = new Status("FALLSTATUS");
                for (FallStatus status:fall){
                    s.putContents(status.getId().toString(),String.valueOf(status.isFall()));
                }
                rcmodule.postStatus(s);
            }

            @Override
            public void statusIRSensorStatus(Collection<IRSensorStatus> irSensorStatus) {
                Status s  = new Status("IRSTATUS");
                for (IRSensorStatus status : irSensorStatus){
                    s.putContents(status.getId().toString(),String.valueOf(status.getDistance()));
                }
                rcmodule.postStatus(s);
            }

            @Override
            public void statusBattery(BatteryStatus battery) {
                Status s  = new Status("BATTLEV");

                    s.putContents("level",String.valueOf(battery.getBattery()));

                rcmodule.postStatus(s);

//                s  = new Status("OBOBATTLEV");
//
//                s.putContents("level",String.valueOf(getBatteryLevel()));
//
//                rcmodule.postStatus(s);



            }

            @Override
            public void statusWallConnectionStatus(WallConnectionStatus wallConnectionStatus) {

            }

            @Override
            public void robCommunicationError(InternalErrorException ex) {

            }
        });

        rcmodule.registerCommand("MOVEBYDEGREES", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                HashMap<String,String> par = c.getParameters();
                String wheel = par.get("wheel");
                int degrees = Math.abs(Integer.parseInt(par.get("degrees")));
                int speed = Integer.parseInt(par.get("speed"));

                if (wheel.equals("right")){
                    if(speed>0){
                        //FF
                        try {

                            irob.moveMT(MoveMTMode.FORWARD_FORWARD,speed,degrees,0,0);
                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    }else {
                        //FR
                        try {
                            irob.moveMT(MoveMTMode.REVERSE_REVERSE,  (speed*-1), degrees, 0, 0);
                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    }
                }else if (wheel.equals("left")){
                    if(speed>0){
                        //FF
                        try {
                            irob.moveMT(MoveMTMode.FORWARD_FORWARD,0,0,speed,degrees);

                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    }else {
                        //FR
                        try {
                            irob.moveMT(MoveMTMode.REVERSE_REVERSE,0,0, (speed*-1), degrees);
                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    }

                }else if (wheel.equals("both")){
                    if (speed>0){
                        try {
                            //movementModule.moveForwardsAngle(speed,degrees);
                            irob.moveMT(MoveMTMode.FORWARD_FORWARD, speed, degrees,speed,degrees);

                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    }else {
                        try {
                            //movementModule.moveBackwardsAngle((speed*(-1)),degrees);
                            irob.moveMT(MoveMTMode.REVERSE_REVERSE, (speed*(-1)), degrees,(speed*(-1)),degrees);

                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        });

        rcmodule.registerCommand("MOVEBYTIME", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                HashMap<String,String> par = c.getParameters();
                String wheel = par.get("wheel");
                int time = Math.round(Float.parseFloat(par.get("time"))*1000);
                int speed = Integer.parseInt(par.get("speed"));

                if (wheel.equals("right")){
                    if (speed>0){
                        try {
                            //movementModule.turnRightBackwardsTime(speed,time);
                            //LR
                            irob.moveMT(MoveMTMode.FORWARD_FORWARD,speed,0,time);

                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    }else {
                        try {
                            //movementModule.turnRightTime((speed*(-1)),time);
                            irob.moveMT(MoveMTMode.REVERSE_REVERSE,(speed*(-1)),0,time);
                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    }

                }else if (wheel.equals("left")){
                    if (speed>0){
                        try {
                            //movementModule.turnLeftBackwardsTime(speed,time);
                            irob.moveMT(MoveMTMode.FORWARD_FORWARD,0,speed,time);


                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    }else {
                        try {
                            irob.moveMT(MoveMTMode.REVERSE_REVERSE,0,(speed*(-1)),time);
                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    }

                }else if (wheel.equals("both")){
                    if (speed>0){
                        try {
                            irob.moveMT(MoveMTMode.FORWARD_FORWARD,speed,speed,time);

                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    }else {
                        try {
                            irob.moveMT(MoveMTMode.REVERSE_REVERSE,(speed*(-1)),(speed*(-1)),time);
                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        });

        rcmodule.registerCommand("TURNINPLACE", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                HashMap<String,String> par = c.getParameters();
                int degrees = Integer.parseInt(par.get("degrees"));
                if (degrees>0){
                    try {
                        irob.moveMT(MoveMTMode.FORWARD_REVERSE,50,degrees,50,degrees);

                    } catch (InternalErrorException e) {
                        e.printStackTrace();
                    }
                }else{
                    try {
                        irob.moveMT(MoveMTMode.REVERSE_FORWARD,50,degrees*(-1),50,degrees*(-1));
                    } catch (InternalErrorException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        rcmodule.registerCommand("MOVETWOWHEELS", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                HashMap<String,String> par = c.getParameters();
                int time = Math.round(Float.parseFloat(par.get("time"))*1000);
                int lspeed = Integer.parseInt(par.get("lspeed"));
                int rspeed = Integer.parseInt(par.get("rspeed"));
                Log.d(TAG, "MOVETWOWHEELS Left: "+lspeed+" Right: "+rspeed);

                if (lspeed>0){
                    if(rspeed>0){
                        //FF - BIEN
                        try {
                            irob.moveMT(MoveMTMode.FORWARD_FORWARD,rspeed,lspeed,time);
                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    }else if(rspeed<0){
                        //FR
                        try {
                            irob.moveMT(MoveMTMode.REVERSE_FORWARD,(rspeed*(-1)),lspeed,time);
                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    }else {
                        //F0
                        try {
                            irob.moveMT(MoveMTMode.FORWARD_FORWARD,0,lspeed,time);
                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    }
                }else if (lspeed<0){
                    if (rspeed > 0) {
                        //RF - MAL INVERTIDO
                        try {
                            irob.moveMT(MoveMTMode.FORWARD_REVERSE, rspeed, (lspeed * (-1)),  time);
                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    } else if (rspeed < 0 ){
                        //RR
                        try {
                            irob.moveMT(MoveMTMode.REVERSE_REVERSE,(rspeed * (-1)), (lspeed * (-1)),  time);
                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    }else{
                        //RR
                        try {
                            irob.moveMT(MoveMTMode.REVERSE_REVERSE, 0, (lspeed * (-1)), time);
                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    }
                }else {
                    if (rspeed > 0) {
                        //RF - MAL INVERTIDO
                        try {
                            irob.moveMT(MoveMTMode.FORWARD_FORWARD,rspeed, 0,  time);
                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    } else if (rspeed < 0 ){
                        //RR
                        try {
                            irob.moveMT(MoveMTMode.REVERSE_REVERSE, (rspeed * (-1)), 0,  time);
                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    }

                }

            }
        });

        rcmodule.registerCommand("MOTORSON", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                HashMap<String,String> par = c.getParameters();
                String rmotor = par.get("rmotor");
                String lmotor = par.get("lmotor");
                int speed = Integer.parseInt(par.get("speed"));

                if (Objects.equals(lmotor, "forward")){
                    if(Objects.equals(rmotor, "forward")){
                        //FF
                        try {
                            irob.moveMT(MoveMTMode.FORWARD_FORWARD,speed,speed,Integer.MAX_VALUE);
                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    }else if(Objects.equals(rmotor, "backward")){
                        //FR
                        try {
                            irob.moveMT(MoveMTMode.REVERSE_FORWARD,speed,speed,Integer.MAX_VALUE);
                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    }else {
                        //F0
                        try {
                            irob.moveMT(MoveMTMode.FORWARD_FORWARD,speed,0,Integer.MAX_VALUE);

                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    }
                }else if (Objects.equals(lmotor, "backward")){
                    if(Objects.equals(rmotor, "forward")){
                        //RF
                        try {
                            irob.moveMT(MoveMTMode.FORWARD_REVERSE,speed,speed,Integer.MAX_VALUE);
                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    }else if(Objects.equals(rmotor, "backward")){
                        //RR
                        try {
                            irob.moveMT(MoveMTMode.REVERSE_REVERSE,speed,speed,Integer.MAX_VALUE);
                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    }else {
                        //R0
                        try {
                            irob.moveMT(MoveMTMode.REVERSE_REVERSE,speed,0,Integer.MAX_VALUE);
                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    }
                }else {
                    if(Objects.equals(rmotor, "forward")){
                        //0F
                        try {
                            irob.moveMT(MoveMTMode.FORWARD_FORWARD,0,speed,Integer.MAX_VALUE);
                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    }else if(Objects.equals(rmotor, "backward")){
                        //0R
                        try {
                            irob.moveMT(MoveMTMode.REVERSE_REVERSE,0,speed,Integer.MAX_VALUE);
                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    }else {
                        //00
                        try {
                            irob.moveMT(MoveMTMode.REVERSE_REVERSE,0,0,Integer.MAX_VALUE);
                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        });

        rcmodule.registerCommand("MOVEPAN", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                HashMap<String,String> par = c.getParameters();
                int pos = Integer.parseInt(par.get("pos"));
                int speed = Integer.parseInt(par.get("speed"));

                try {
                    irob.movePan((short)speed, pos);
                } catch (InternalErrorException e) {
                    e.printStackTrace();
                }

            }
        });

        rcmodule.registerCommand("MOVETILT", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                HashMap<String,String> par = c.getParameters();
                int pos = Integer.parseInt(par.get("pos"));
                int speed = Integer.parseInt(par.get("speed"));

                try {
                    irob.moveTilt((short)speed, pos);
                } catch (InternalErrorException e) {
                    e.printStackTrace();
                }
            }
        });

        rcmodule.registerCommand("LEDCOLOR", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                HashMap<String,String> par = c.getParameters();
                String led =par.get("led");
                int ledint = 0;
                Color color = new Color(0,0,0);
                boolean all=false;
                if (led.equals("all")){
                    all = true;
                }else {
                    ledint = Integer.parseInt(led);
                }

                String colorST = par.get("color");

                switch (colorST){
                    case "white":
                           color = new Color(255,255,255);
                        break;
                    case "red":
                        color = new Color(255,0,0);
                        break;
                    case "blue":
                        color = new Color(0,0,255);
                        break;
                    case "cyan":
                        color = new Color(0,255,255);
                        break;
                    case "magenta":
                        color = new Color(255,0,255);
                        break;
                    case "yellow":
                        color = new Color(255,255,0);
                        break;
                    case "green":
                        color = new Color(0,255,0);
                        break;
                    case "orange":
                        color = new Color(255,165,0);
                        break;
                    case "on":
                        color = new Color(255,255,255);
                        break;
                    case "off":
                        color = new Color(0,0,0);
                        break;
                }
                if (all){
                    for (int i = 0; i<10; i++) {
                        try {
                            irob.setLEDColor(i,color);
                        } catch (InternalErrorException e) {
                            e.printStackTrace();
                        }
                    }
                }else {
                    try {
                        irob.setLEDColor(ledint,color);
                    } catch (InternalErrorException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void shutdown() throws InternalErrorException {

    }

    @Override
    public String getModuleInfo() {
        return "Remote Rob Module";
    }

    @Override
    public String getModuleVersion() {
        return "v0.1";
    }
    public float getBatteryLevel() {

        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float)level / (float)scale) * 100.0f;
    }
}
