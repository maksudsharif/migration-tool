package com.maksudsharif.migration.exceptions;

public class ArkCaseCallFailedException extends Exception
{
    public ArkCaseCallFailedException()
    {
    }

    public ArkCaseCallFailedException(String message)
    {
        super(message);
    }

    public ArkCaseCallFailedException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
