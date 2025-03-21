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
package pinorobotics.jros2rviztools.tests.integration;

import id.jros2client.JRos2ClientFactory;
import id.xfunction.logging.XLogger;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pinorobotics.jros2rviztools.JRos2RvizToolsFactory;
import pinorobotics.jrosrviztools.entities.Color;
import pinorobotics.jrosrviztools.entities.MarkerType;
import pinorobotics.jrosrviztools.entities.Point;
import pinorobotics.jrosrviztools.entities.Pose;
import pinorobotics.jrosrviztools.entities.Scales;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
public class JRos2RvizToolsIntegrationTests {

    @BeforeAll
    public static void setupAll() {
        XLogger.load("jros2rviztools-test.properties");
    }

    @Test
    public void test_example_from_documentation() throws Exception {
        var clientFactory = new JRos2ClientFactory();
        var rvizToolsFactory = new JRos2RvizToolsFactory();
        try (var commands = new Ros2Commands();
                var client = clientFactory.createClient();
                var rvizTools =
                        rvizToolsFactory.createJRosRvizTools(
                                client, "map", "/visualization_marker_array")) {
            var rviz = commands.runRviz(Paths.get("").toAbsolutePath().resolve("test.rviz"));
            rvizTools.publishTextAsync(
                    Color.RED, Scales.XLARGE, new Pose(new Point(0, 0, 1)), "Hello from Java");
            rvizTools.publishMarkersAsync(
                    Color.RED, Scales.XLARGE, MarkerType.CUBE, new Point(1, 0, 2));
            System.out.println("Press Enter to stop...");
            System.in.read();
            rviz.await();
        }
    }
}
