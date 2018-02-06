package com.ibm.watson.WatsonVRTraining.util;

public class TrainingAppException extends Exception {

	public TrainingAppException() {
	}

	public TrainingAppException(String message) {
		super(message);
	}

	public TrainingAppException(Throwable cause) {
		super(cause);
	}

	public TrainingAppException(String message, Throwable cause) {
		super(message, cause);
	}

	public TrainingAppException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
