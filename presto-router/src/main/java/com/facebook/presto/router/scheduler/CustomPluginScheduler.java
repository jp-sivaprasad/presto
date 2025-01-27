/*
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
package com.facebook.presto.router.scheduler;

import com.facebook.airlift.log.Logger;
import com.facebook.presto.spi.router.ClusterInfo;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CustomPluginScheduler
        implements Scheduler
{
    private SchedulerManager schedulerManager;

    private static final Logger log = Logger.get(CustomPluginScheduler.class);

    public CustomPluginScheduler(SchedulerManager schedulerManager)
    {
        this.schedulerManager = schedulerManager;
        schedulerManager.setRequired();
    }

    @Override
    public Optional<URI> getDestination(String user, String query)
    {
        try {
            return schedulerManager.getScheduler().getDestination(user, query);
        }
        catch (Exception e) {
            log.warn(e, "Error getting destination");
            return Optional.empty();
        }
    }

    @Override
    public void setCandidates(List<URI> candidates)
    {
        schedulerManager.getScheduler().setCandidates(candidates);
    }

    @Override
    public void setClusterInfos(Map<URI, ClusterInfo> clusterInfo)
    {
        schedulerManager.getScheduler().setClusterInfos(clusterInfo);
    }
}
