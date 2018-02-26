package activesupport.jenkins;

import org.jetbrains.annotations.NotNull;

public enum JenkinsParameterKeys {
    JOB("INCLUDE_TYPES"),
    NODE("Run on Nodes"),
    REPORT("REPORT_NAME"),
    COMMAND("COMMAND");

    String name;

    JenkinsParameterKeys(@NotNull String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
