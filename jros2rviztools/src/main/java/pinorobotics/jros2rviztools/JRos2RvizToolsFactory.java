/*
 * Copyright 2020 jrosrviztools project
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

import id.jrosclient.JRosClient;
import id.jrosclient.RosVersion;
import id.xfunction.Preconditions;
import pinorobotics.jrosrviztools.JRosRvizTools;

/**
 * Factory methods for {@link JRos2RvizTools}
 *
 * @author lambdaprime intid@protonmail.com
 */
public class JRos2RvizToolsFactory {

    /** Create RViz tools */
    public JRosRvizTools createJRosRvizTools(JRosClient client, String baseFrame, String topic) {
        Preconditions.isTrue(
                client.getSupportedRosVersion().contains(RosVersion.ROS2), "Requires ROS2 client");
        return new JRos2RvizTools(client, baseFrame, topic);
    }
}
