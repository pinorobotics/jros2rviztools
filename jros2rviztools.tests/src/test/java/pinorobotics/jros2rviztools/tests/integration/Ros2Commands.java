/*
 * Copyright 2022 jrosrviztools project
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

import id.xfunction.lang.XExec;
import id.xfunction.lang.XProcess;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Ros2Commands implements AutoCloseable {

    private List<XProcess> procs = new ArrayList<>();

    public XProcess runRviz(Path configFile) {
        var proc = new XExec("rviz2 -d " + configFile).start();
        procs.add(proc);
        return proc;
    }

    @Override
    public void close() {
        procs.forEach(XProcess::destroyAllForcibly);
    }
}
