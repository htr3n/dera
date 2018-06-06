package dera.frontend.command;

public interface Command {
    void execute() throws Throwable;
    void undo();
}
