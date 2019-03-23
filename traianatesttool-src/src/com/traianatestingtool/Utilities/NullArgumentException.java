/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.traianatestingtool.Utilities;

/**
 *
 * @author mukunthant
 */
public class NullArgumentException extends IllegalArgumentException {
	
	private static final long serialVersionUID = -3710023191000922886L;
	
	private static final String MESSAGE = "%s cannot be null";
	
	public NullArgumentException(String argument) {
		super(String.format(MESSAGE, argument));
	}

}