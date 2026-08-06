package eleeter.elfontlib.emoji.build;

public class EmojiBuildException extends RuntimeException
{
    public EmojiBuildException(String message)
    {
        super(message);
    }

    public EmojiBuildException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
