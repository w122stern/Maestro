/*
 * Copyright 2019 Crown Copyright
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
package uk.gov.gchq.maestro.hook;

import uk.gov.gchq.maestro.util.Request;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ExecutionTrackingInfo {

    private String operationType;
    private String jobId;
    private String executorId;
    private String userId;
    private Date timeBegin;
    private Date timeEnd;
//    private O monitoringEndpoint #TODO

    public ExecutionTrackingInfo(Request request){
        this.operationType = request.getOperation().getClass().getSimpleName();
        this.jobId = request.getContext().getJobId();
        this.executorId = request.getConfig().getId();
        this.userId = request.getContext().getUser().getUserId();
        this.timeBegin = new Date();
    }

    public void setTimeEnd(final Date timeEnd) {
        this.timeEnd = timeEnd;
    }

    @Override
    public String toString() {
        String strTimeBegin = ( this.timeBegin != null) ? new SimpleDateFormat("MM/dd HH:mm:ss").format(timeBegin) : "";
        String strTimeEnd = (this.timeEnd != null) ? new SimpleDateFormat("MM/dd HH:mm:ss").format(timeEnd) : "";
        return "ExecutionTrackingInfo{" +
                "operationType='" + operationType +'\'' +
                ", jobId='" + jobId + '\'' +
                ", executorId='" + executorId + '\'' +
                ", userId='" + userId + '\'' +
                ", timeBegin=" + strTimeBegin +
                ", timeEnd=" + strTimeEnd +
                '}';
    }
}