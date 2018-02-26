package activesupport.jenkins;

import org.jetbrains.annotations.NotNull;

public enum JenkinsParameterKey {
    JOB("INCLUDE_TYPES"),
    NODE("Run on Nodes"),
    REPORT("REPORT_NAME"),
    COMMAND("COMMAND");

    String name;

    JenkinsParameterKey(@NotNull String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
