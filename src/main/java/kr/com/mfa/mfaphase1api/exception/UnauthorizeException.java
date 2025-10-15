package kr.com.mfa.mfaphase1api.exception;

public class UnauthorizeException extends RuntimeException {
  public UnauthorizeException(String message) {
    super(message);
  }
}
