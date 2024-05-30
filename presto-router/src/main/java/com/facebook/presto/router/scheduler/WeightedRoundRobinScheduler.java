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

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Collections.nCopies;

// As the weighted round-robin scheduler, similar to the normal round-robin method,
// keeps the selected index as a state for scheduling, the candidates and weights
// shall not be modified after they are assigned.
// This design indicates that the weighted round-robin scheduler can only be used
// when the candidates and weights are always consistent. Otherwise, users need to
// create a new scheduler object to pick up the change.
public class WeightedRoundRobinScheduler
        implements Scheduler
{
    private List<URI> candidates;
    private HashMap<URI, Integer> weights;
    private List<URI> serverList;

    private final Map<String, Integer> candidateIndexByGroup = new HashMap<>();
    private String candidateGroupName;

    @Override
    public Optional<URI> getDestination(String user)
    {
        int serverIndex = 0;

        checkArgument(candidates.size() == weights.size());

        if (serverList == null || weights.values().stream().mapToInt(Integer::intValue).sum() != serverList.size()) {
            this.generateServerList();
        }

        synchronized (candidateIndexByGroup) {
            if (!candidateIndexByGroup.containsKey(candidateGroupName)) {
                candidateIndexByGroup.put(candidateGroupName, 0);
            }
            else {
                serverIndex = candidateIndexByGroup.get(candidateGroupName) + 1;
            }
            if (serverIndex >= serverList.size()) {
                serverIndex = 0;
            }
            candidateIndexByGroup.put(candidateGroupName, serverIndex);
            return Optional.of(serverList.get(serverIndex));
        }
    }

    public void setCandidates(List<URI> candidates)
    {
        // Only keeps the first given `candidates` due to maintaining the
        // selected index for weighted round-robin.
        if (this.candidates == null) {
            this.candidates = candidates;
        }
    }

    public List<URI> getCandidates()
    {
        return candidates;
    }

    public void setWeights(HashMap<URI, Integer> weights)
    {
        // Only keeps the first given `weights` due to maintaining the
        // selected index for weighted round-robin.
        this.weights = weights;
    }

    public HashMap<URI, Integer> getWeights()
    {
        return weights;
    }

    private void generateServerList()
    {
        checkArgument(!candidates.isEmpty(), "Server candidates should not be empty");

        serverList = weights.keySet().stream()
                .map(uri -> nCopies(weights.get(uri), uri))
                .flatMap(Collection::stream)
                .collect(toImmutableList());
    }

    @Override
    public void setCandidateGroupName(String candidateGroupName)
    {
        this.candidateGroupName = candidateGroupName;
    }
}
