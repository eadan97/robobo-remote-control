/*******************************************************************************
 *
 *   Copyright 2017 Mytech Ingenieria Aplicada <http://www.mytechia.com>
 *   Copyright 2017 Gervasio Varela <gervasio.varela@mytechia.com>
 *   Copyright 2017 Julio Gomez <julio.gomez@mytechia.com>
 *
 *   This file is part of Robobo Ros Module.
 *
 *   Robobo Ros Module is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Robobo Ros Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Robobo Ros Module.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/
package com.mytechia.robobo.framework.remotecontrol.ros;

import com.mytechia.robobo.framework.IModule;

import org.ros.node.NodeMain;

/**
 * Public interface of the Robobo ROS node that implements a proxy in ROS for the Robobo Remote
 * Control Protocol
 *
 * The module allows the execution of arbritray ROS-java nodes.
 *
 */

public interface IRosRemoteControlModule extends IModule {

    /** Executes a new ROS-java 'inside' the robot.
     *
     * @param node the new ROS-java node
     */
    void startRoboboRosNode(NodeMain node);

    /** Returns the name of the Robobo robot for multi-robot configurations
     *
     * @return the name of the Robobo robot
     */
    String getRoboboName();

}
