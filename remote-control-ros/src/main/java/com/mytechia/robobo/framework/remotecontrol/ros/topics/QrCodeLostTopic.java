package com.mytechia.robobo.framework.remotecontrol.ros.topics;

import com.mytechia.robobo.framework.remote_control.remotemodule.Status;
import com.mytechia.robobo.framework.remotecontrol.ros.util.NodeNameUtility;

import org.ros.node.topic.Publisher;

import robobo_msgs.QrCodeChange;


/**
 * Topic (shared) for lost qr codes by the vision module.
 * <p>
 * The topic is robot/qr/change
 */
public class QrCodeLostTopic extends AStatusTopic {

    private static final String TOPIC = "qr/change";
    public static final String STATUS = "QRCODELOST";


    private Publisher<QrCodeChange> topic;


    public QrCodeLostTopic(StatusNode node) {
        super(node, TOPIC, STATUS, null);
    }


    public void start() {
        this.topic = this.node.getConnectedNode().newPublisher(NodeNameUtility.createNodeAction(this.node.getRoboboName(), this.getTopicName()), QrCodeChange._TYPE);
    }


    @Override
    public void publishStatus(Status status) {

        if (status.getName().equals(this.getSupportedStatus())) {
            QrCodeChange msg = this.topic.newMessage();

            String id = status.getValue().get("id");
//                    coordx = status.getValue().get("coordx"),
//                    coordy = status.getValue().get("coordy"),
//                    distance = status.getValue().get("distance");

            if (id != null
            ) {

                msg.setId(id);
                this.topic.publish(msg);

            }

        }

    }
}