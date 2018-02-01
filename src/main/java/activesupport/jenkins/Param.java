package activesupport.jenkins;

import org.jetbrains.annotations.NotNull;

public enum Param {
    JOB("INCLUDE_TYPES"),
    NODE("Run on Nodes"),
    REPORT("REPORT_NAME");

    String name;

    Param(@NotNull String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
