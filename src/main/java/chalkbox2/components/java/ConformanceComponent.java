package chalkbox2.components.java;


import chalkbox.api.collections.Bundle;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import chalkbox.api.common.java.Compiler;
import chalkbox.api.files.FileLoader;
import chalkbox.java.conformance.SourceLoader;
import chalkbox.java.conformance.comparator.ClassComparator;
import chalkbox.java.conformance.comparator.CodeComparator;
import chalkbox2.api.ComponentImpl;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConformanceComponent extends ComponentImpl {
    //@ConfigItem(description = "The location of files to use for conformance checking")
    public String conformance;

    //@ConfigItem(description = "The expected file structure for the assignment")
    public String structure;

    //@ConfigItem
    public String classPath;

    private Map<String, Class> expectedClasses;
    private List<String> expectedFiles;

    public ConformanceComponent() {
        // follow a builder/functional style I guess.
        // load the solution files into yourself
        // load the structure of the assignment
        logger().error("Help im a security hole");
    }

    public void init() throws Exception {
        //loadStructure();
        //loadExpected();
    }

    public void run(String... args) {
        // runs a single assignment given the source directory of the submission

    }

    /**
     * Loads the expected class files into the conformance checker
     */
    public void loadExpected() throws IOException {
        Bundle expected = new Bundle(new File(conformance));
        StringWriter output = new StringWriter();

        /* Load output directories for the solution and the tests */
        Bundle out;
        try {
            out = new Bundle();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        /* Compile the sample solution */
        Compiler.compile(Compiler.getSourceFiles(expected), classPath,
                out.getUnmaskedPath(), output);

        SourceLoader expectedLoader = new SourceLoader(out.getUnmaskedPath());
        try {
            expectedClasses = expectedLoader.getClassMap();
        } catch (ClassNotFoundException cnf) {
            throw new RuntimeException("Failed to load expected class");
        }
    }

    public void loadStructure() {
        expectedFiles = FileLoader.loadFiles(structure);
    }

    public Collection files(Collection submission) {
        List<String> missing = new ArrayList<>();
        List<String> extra = new ArrayList<>();
        List<String> actual = FileLoader.loadFiles(submission.getSource().getUnmaskedPath());

        for (String expected : expectedFiles) {
            if (!actual.contains(expected)) {
                missing.add(expected);
            }
        }

        for (String path : actual) {
            if (!expectedFiles.contains(path)) {
                extra.add(path);
            }
        }

        submission.getResults().set("structure.missing", missing);
        submission.getResults().set("structure.extra", extra);

        return submission;
    }


    public Collection compare(Collection submission) throws IOException {
        Data data = submission.getResults();

        if (!data.is("compilation.compiles")) {
            return submission;
        }

        SourceLoader submissionLoader = new SourceLoader(submission.getWorking()
                .getUnmaskedPath("bin"));
        Map<String, Class> submissionMap;
        try {
            submissionMap = submissionLoader.getClassMap();
        } catch (ClassNotFoundException|NoClassDefFoundError cnf) {
            data.set("conformance.error", "Unable to find a class - consult a tutor");
            cnf.printStackTrace();
            return submission;
        }

        for (String className : expectedClasses.keySet()) {
            // Skip anon generated classes
            if (className.contains("$")) {
                continue;
            }

            String jsonKey = "conformance." + className.replace(".", "\\.") + ".";
            Class expectedClass = expectedClasses.get(className);
            Class actualClass = submissionMap.get(className);

            if (expectedClass == null || actualClass == null) {
                data.set(jsonKey + "differs", true);
                data.set(jsonKey + "output", "Unable to load class");
                continue;
            }

            CodeComparator<Class> comparator = new ClassComparator(expectedClass,
                    actualClass);
            data.set(jsonKey + "differs", comparator.hasDifference());
            data.set(jsonKey + "output", comparator.toString());
        }

        return submission;
    }
}
