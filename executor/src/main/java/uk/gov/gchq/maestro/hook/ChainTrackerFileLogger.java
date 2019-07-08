package uk.gov.gchq.maestro.hook;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.maestro.util.Request;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A {@code ChainTrackingHook} is a {@link Hook} that sends logs of the
 * time and name of an executed operation to a {@link Logger}.
 */
@JsonPropertyOrder(alphabetic = true)
public class ChainTrackerFileLogger extends ChainTrackingHook implements Hook {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChainTrackerFileLogger.class);

    public ChainTrackerFileLogger() {
    }

    /**
     * Instantiates an {@link ExecutionTrackingInfo} Object containing
     * various fields extracted from a request.
     *
     * @param request Request containing the Operation and Context
     */
    @Override
    public void preExecute(final Request request) {
        ExecutionTrackingInfo preInfo = new ExecutionTrackingInfo(request);
        track(preInfo);
    }

    @Override
    public <T> T postExecute(final T result, final Request request) {
        ExecutionTrackingInfo postInfo = new ExecutionTrackingInfo(result, request);
        track(postInfo);
        return result;
    }

    @Override
    public <T> T onFailure(final T result, final Request request, final Exception e) {
        LOGGER.warn("[{}] Failed to Execute: {}  - {}", new java.util.Date(), request.getOperation().getClass().getSimpleName(), e.getMessage());
        return null;
    }

    public void track(ExecutionTrackingInfo info) {
        final String FILE_PATH="log.txt";
        File file = new File(FILE_PATH);

        if (info.getTimeBegin() != null){ //preInfo
            try (BufferedWriter writer = new BufferedWriter( new FileWriter(file, false))) {

                String line = info.toString();
                writer.write(line, 0, line.length());

            } catch (IOException e) {
                System.out.println("[!]" + e);
                e.printStackTrace();
            }
        }
        else if (info.getTimeEnd() != null){ //postInfo
            try (BufferedReader bufferedReader = new BufferedReader (new FileReader(file));
                 BufferedWriter writer = new BufferedWriter (new FileWriter(file, true))) {

                if (bufferedReader.readLine() == null){ // #TODO better logic needed
                    LOGGER.warn("[{}] : Logging file named '{} is empty - postInfo will be logged before preInfo", new java.util.Date(),FILE_PATH);
                    String line = info.toString();
                    writer.write(line, 0, line.length());

                } else {
                    String line;
                    StringBuffer newLine;
                    while ((line = bufferedReader.readLine()) != null) {
                        if (line.contains(info.getJobId())){
                            int index = line.indexOf(info.getJobId());
                            String insertString = "timeEnd= " + info.getTimeEnd() + ", ";
                            newLine = new StringBuffer(line);
                            newLine.insert(index + -7, insertString);
                            System.out.println("DEBUG: " + newLine.toString());
                        } else {
                            newLine = new StringBuffer(info.toString());
                        }
                        System.out.println(line);
                        writer.newLine();
                        writer.write(newLine.toString(), 0, line.length());
                    }
                }
            } catch (IOException e) {
                System.out.println("[!]" + e);
                e.printStackTrace();
            }
        }
        else {
            LOGGER.warn("Could not detect type of info packet.");

            try (BufferedWriter writer = new BufferedWriter( new FileWriter(file, true))) {

                String line = info.toString();
                writer.newLine();
                writer.write(line, 0, line.length());

            } catch (IOException e) {
                System.out.println("[!]" + e);
                e.printStackTrace();
            }
        }
    }
}
