package com.sixeco.nettydemo.command;

public enum Command implements ICommand {
    GROUP_SHARED_RESOURCES(1),
    JOIN_GROUP(2),
    ;

    private final int value;

    private Command(int value) {
        this.value = value;
    }


    @Override
    public int getNumber() {
        return value;
    }

    @Override
    public String enumName() {
        return name();
    }

    @Override
    public String toString() {
        return "Command{" +
                "value=" + value +
                '}';
    }

    public static Command forNumber(int value) {
        for (Command command : Command.values()) {
            if (command.getNumber() == value) {
                return command;
            }
        }
        return null;
    }

}
