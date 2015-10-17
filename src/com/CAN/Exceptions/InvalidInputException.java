package com.CAN.Exceptions;

public class InvalidInputException extends Exception {

	private static final long serialVersionUID = 1L;

	@Override
	public String getMessage() {
		return "invalid input passed.";
	}
	
	@Override
	public void printStackTrace() {
		super.printStackTrace();
	}
	
}
