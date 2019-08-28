package com.maksudsharif.migration.exceptions;

public class ArkCaseAuthenticationFailedException extends ArkCaseCallFailedException
{
    public ArkCaseAuthenticationFailedException()
    {
    }

    public ArkCaseAuthenticationFailedException(String message)
    {
        super(message);
    }

    public ArkCaseAuthenticationFailedException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
