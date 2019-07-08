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
    private Date timeBegin;
    private Date timeEnd;
    private String jobId;
    private String parentJobId;
    private String executorId;
    private String userId;
//    private O monitoringEndpoint #TODO

    public String getParentJobId() {
        return parentJobId;
    }

    public ExecutionTrackingInfo(Request request){
        this.operationType = request.getOperation().getClass().getSimpleName();
        this.jobId = request.getContext().getJobId();
        this.parentJobId = request.getContext().getParentJobId();
        this.executorId = request.getConfig().getId();
        this.userId = request.getContext().getUser().getUserId();
        this.timeBegin = new Date();
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(final String jobId) {
        this.jobId = jobId;
    }

    public <T> ExecutionTrackingInfo(T result, Request request){
        this.operationType = request.getOperation().getClass().getSimpleName();
        this.jobId = request.getContext().getJobId();
        this.parentJobId = request.getContext().getParentJobId();
        this.executorId = request.getConfig().getId();
        this.userId = request.getContext().getUser().getUserId();
        this.timeEnd = new Date();
    }

    public Date getTimeEnd() {
        return timeEnd;
    }

    public Date getTimeBegin() {
        return timeBegin;
    }


    @Override
    public String toString() {
        String strTimeBegin = ( this.timeBegin != null) ? ", timeBegin=" + new SimpleDateFormat("MM/dd HH:mm:ss").format(timeBegin) : "";
        String strTimeEnd = (this.timeEnd != null) ? ", timeEnd=" + new SimpleDateFormat("MM/dd HH:mm:ss").format(timeEnd) : "";
        return "ExecutionTrackingInfo{" +
                "operationType='" + operationType +'\'' +
                strTimeBegin +
                strTimeEnd +
                ", jobId='" + jobId + '\'' +
                ", parentJobId='" + parentJobId + '\'' +
                ", executorId='" + executorId + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }

    public String getOperationType() {
        return operationType;
    }
}