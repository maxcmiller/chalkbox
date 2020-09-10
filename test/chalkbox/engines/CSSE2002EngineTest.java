package chalkbox.engines;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CSSE2002EngineTest {
    private static final String BASE_FOLDER = "./test/resources/csse2002/";

    private static final String CONFIG_PATH = BASE_FOLDER + "config.yml";
    private static final String RESULTS_PATH = BASE_FOLDER + "results/results.json";
    private static final String EXPECTED_PATH = BASE_FOLDER + "results/expected.json";

    @Test
    public void testEngine() throws IOException {
        Engine engine = null;
        try {
            engine = EngineLoader.load(CONFIG_PATH);
        } catch (ConfigFormatException e) {
            fail("Configuration file should not be invalid");
        }
        engine.run();

        File results = Paths.get(RESULTS_PATH).toFile();
        File expected = Paths.get(EXPECTED_PATH).toFile();

        assertEquals("Output JSON files are different",
                FileUtils.readFileToString(expected, "utf-8"),
                FileUtils.readFileToString(results, "utf-8"));
    }
}