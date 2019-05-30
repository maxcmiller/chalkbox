package chalkbox.python;

import chalkbox.api.annotations.ConfigItem;
import chalkbox.api.annotations.Pipe;
import chalkbox.api.annotations.Processor;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import chalkbox.api.common.Execution;
import chalkbox.api.common.ProcessExecution;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Processor
public class CSSE1001Test {
    @ConfigItem(key = "python", required = false,
            description = "Command to execute python from terminal")
    public String PYTHON = "python3";

    @ConfigItem(key = "runner", description = "Name of the test runner")
    public String runner;

    @ConfigItem(key = "included", description = "Path to supplied assignment files")
    public String included;

    @Pipe(stream = "submissions")
    public Collection run(Collection collection) {
        Data feedback = collection.getResults();
        ProcessExecution process;
        Map<String, String> environment = new HashMap<>();
        environment.put("PYTHONPATH", included);
        File working = new File(collection.getWorking().getUnmaskedPath());

        try {
            collection.getWorking().copyFolder(new File(included));
        } catch (IOException e) {
            feedback.set("test.error", "Unable to copy supplied directory");
            return collection;
        }

        try {
            process = Execution.runProcess(working, environment, 10000,
                    PYTHON, "-m", runner, "--json");
        } catch (IOException e) {
            feedback.set("test.error", "IOException occurred");
            return collection;
        } catch (TimeoutException e) {
            feedback.set("test.error", "Timed out executing tests");
            return collection;
        }

        String output = process.getOutput();
        feedback.set("test", new Data(output));

        System.err.println(process.getError());

        return collection;
    }
}