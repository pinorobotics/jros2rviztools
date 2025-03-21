/*
 * Copyright 2021 jrosrviztools project
 * 
 * Website: https://github.com/pinorobotics/jros2rviztools
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pinorobotics.jros2rviztools;

import id.jros2messages.std_msgs.HeaderMessage;
import id.jros2messages.visualization_msgs.MarkerArrayMessage;
import id.jros2messages.visualization_msgs.MarkerMessage;
import id.jros2messages.visualization_msgs.MarkerMessage.Action;
import id.jros2messages.visualization_msgs.MarkerMessage.Type;
import id.jrosclient.JRosClient;
import id.jrosclient.TopicSubmissionPublisher;
import id.jrosmessages.geometry_msgs.PointMessage;
import id.jrosmessages.geometry_msgs.PolygonMessage;
import id.jrosmessages.geometry_msgs.PoseMessage;
import id.jrosmessages.geometry_msgs.QuaternionMessage;
import id.jrosmessages.primitives.Duration;
import id.jrosmessages.primitives.Time;
import id.jrosmessages.std_msgs.StringMessage;
import id.xfunction.Preconditions;
import id.xfunction.lang.XThread;
import id.xfunction.logging.XLogger;
import id.xfunction.util.IdempotentService;
import pinorobotics.jrosrviztools.JRosRvizTools;
import pinorobotics.jrosrviztools.entities.Color;
import pinorobotics.jrosrviztools.entities.MarkerType;
import pinorobotics.jrosrviztools.entities.Point;
import pinorobotics.jrosrviztools.entities.Pose;
import pinorobotics.jrosrviztools.entities.Vector3;
import pinorobotics.jrosrviztools.exceptions.JRosRvizToolsException;

/**
 * ROS2 implementation of {@link JRosRvizTools} to work with RViz
 *
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class JRos2RvizTools extends IdempotentService implements JRosRvizTools {

    private static final XLogger LOGGER = XLogger.getLogger(JRos2RvizTools.class);
    private static final QuaternionMessage ORIENTATION = new QuaternionMessage().withW(1.0);
    private TopicSubmissionPublisher<MarkerArrayMessage> markerPublisher;
    private JRosClient client;
    private String baseFrame;
    private volatile int nsCounter;
    private JRos2RvizEntitiesTransformer transformer = new JRos2RvizEntitiesTransformer();

    JRos2RvizTools(JRosClient client, String baseFrame, String topic) {
        this.client = client;
        this.baseFrame = baseFrame;
        markerPublisher = new TopicSubmissionPublisher<>(MarkerArrayMessage.class, topic);
    }

    /** Send text message to RViz which will be displayed at the given position. */
    @Override
    public void publishTextAsync(Color color, Vector3 scale, Pose pose, String text)
            throws JRosRvizToolsException {
        LOGGER.entering("publishText");
        start();
        publish(
                new MarkerMessage()
                        .withHeader(createHeader())
                        .withNs(new StringMessage(nextNameSpace()))
                        .withType(Type.TEXT_VIEW_FACING)
                        .withAction(Action.ADD)
                        .withText(new StringMessage().withData(text))
                        .withPose(transformer.toPoseMessage(pose).withQuaternion(ORIENTATION))
                        .withColor(transformer.toColorRGBMessage(color))
                        .withScale(transformer.toVector3Message(scale))
                        .withLifetime(Duration.UNLIMITED));
        LOGGER.exiting("publishText");
    }

    /**
     * Publish new marker to RViz
     *
     * @param points Points with coordinates which describe marker position in space
     */
    @Override
    public void publishMarkersAsync(
            Color color, Vector3 scale, MarkerType markerType, Point... points)
            throws JRosRvizToolsException {
        LOGGER.entering("publishMarker");
        start();
        var markers = new MarkerMessage[points.length];
        for (int i = 0; i < markers.length; i++) {
            markers[i] =
                    new MarkerMessage()
                            .withHeader(createHeader())
                            .withNs(new StringMessage(nextNameSpace()))
                            .withType(transformer.toMarkerType(markerType))
                            .withAction(Action.ADD)
                            .withPose(
                                    new PoseMessage()
                                            .withPosition(transformer.toPointMessage(points[i]))
                                            .withQuaternion(new QuaternionMessage().withW(1.0)))
                            .withScale(transformer.toVector3Message(scale))
                            .withColor(transformer.toColorRGBMessage(color))
                            .withLifetime(Duration.UNLIMITED);
        }
        publish(markers);
        LOGGER.exiting("publishMarker");
    }

    /**
     * Publish multiple planes to RViz
     *
     * <p>To draw a plane in RViz we could use {@link PolygonMessage} but RViz plugin renders only
     * one polygon on the scene. It means that it will not let us draw multiple planes/polygons.
     *
     * <p>To draw multiple planes, this method relies on the solution present in <a
     * href="https://github.com/PickNikRobotics/rviz_visual_tools/blob/147b1cd79bd4eddddf6ae8751e71756ace2ee581/src/rviz_visual_tools.cpp#L1008">RvizVisualTools::publishXYPlane</a>:
     * which is based on drawing plane with triangles.
     *
     * @param points coordinates of plane corners in space
     */
    public void publishPlaneAsync(Color color, Vector3 scale, Point... points)
            throws JRosRvizToolsException {
        LOGGER.entering("publishPlaneAsync");
        Preconditions.isTrue(
                points.length % 4 == 0, "Number of points should be div by 4 (4 points per plane)");
        start();
        var markers = new MarkerMessage[points.length / 4];
        for (int i = 0; i < markers.length; i++) {
            markers[i] =
                    new MarkerMessage()
                            .withHeader(createHeader())
                            .withNs(new StringMessage(nextNameSpace()))
                            .withType(MarkerMessage.Type.TRIANGLE_LIST)
                            .withAction(Action.ADD)
                            .withPoints(
                                    new PointMessage[] {
                                        transformer.toPointMessage(points[i * 4 + 0]),
                                        transformer.toPointMessage(points[i * 4 + 1]),
                                        transformer.toPointMessage(points[i * 4 + 2]),
                                        transformer.toPointMessage(points[i * 4 + 2]),
                                        transformer.toPointMessage(points[i * 4 + 3]),
                                        transformer.toPointMessage(points[i * 4 + 0]),
                                    })
                            .withScale(transformer.toVector3Message(scale))
                            .withColor(transformer.toColorRGBMessage(color))
                            .withLifetime(Duration.UNLIMITED);
        }
        publish(markers);
        LOGGER.exiting("publishPlaneAsync");
    }

    @Override
    protected void onClose() {
        LOGGER.entering("close");
        markerPublisher.close();
        LOGGER.exiting("close");
    }

    @Override
    protected void onStart() {
        client.publish(markerPublisher);
    }

    private String nextNameSpace() {
        return "@" + hashCode() + "." + nsCounter++;
    }

    private void publish(MarkerMessage... markers) {
        var message = new MarkerArrayMessage().withMarkers(markers);
        while (markerPublisher.getNumberOfSubscribers() == 0) {
            LOGGER.fine("No subscribers");
            XThread.sleep(100);
        }
        LOGGER.fine("Nuber of markers to publish: {0}", markers.length);
        markerPublisher.submit(message);
    }

    private HeaderMessage createHeader() {
        return new HeaderMessage().withFrameId(baseFrame).withStamp(Time.now());
    }
}
